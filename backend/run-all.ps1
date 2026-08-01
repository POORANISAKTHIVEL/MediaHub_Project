# MediaHub - Start All 10 Services (Eureka + 8 Microservices + Gateway)
# Run this script from the MediaHub-Integrated folder:  .\run-all.ps1

# -- Resolve a valid JAVA_HOME (Java 17+) --------------------------
function Resolve-JavaHome {
    # 1) Honor an already-valid JAVA_HOME
    if ($env:JAVA_HOME -and (Test-Path (Join-Path $env:JAVA_HOME 'bin\java.exe'))) {
        return $env:JAVA_HOME
    }
    # 2) Try common install locations (edit/extend as needed)
    $candidates = @(
        'C:\Program Files\Java\jdk-21',
        'C:\Program Files\Java\jdk-17',
        'C:\Program Files\Eclipse Adoptium\jdk-17*',
        'C:\Program Files\Microsoft\jdk-17*',
        'C:\Program Files\Android\Android Studio\jbr'
    )
    foreach ($c in $candidates) {
        $resolved = Get-Item $c -ErrorAction SilentlyContinue | Select-Object -First 1
        if ($resolved -and (Test-Path (Join-Path $resolved.FullName 'bin\java.exe'))) {
            return $resolved.FullName
        }
    }
    # 3) Fall back to java.exe on PATH
    $java = Get-Command java.exe -ErrorAction SilentlyContinue
    if ($java) {
        return (Split-Path (Split-Path $java.Source -Parent) -Parent)
    }
    return $null
}

$env:JAVA_HOME = Resolve-JavaHome
if (-not $env:JAVA_HOME) {
    Write-Host "  [ERROR] No JDK found. Install Java 17+ or set JAVA_HOME, then re-run." -ForegroundColor Red
    exit 1
}
Write-Host "  Using JAVA_HOME = $env:JAVA_HOME" -ForegroundColor DarkGray
$env:PATH = (Join-Path $env:JAVA_HOME 'bin') + ';' + $env:PATH

function Test-PortInUse([int]$port) {
    return [bool](Get-NetTCPConnection -LocalPort $port -State Listen -ErrorAction SilentlyContinue)
}

function Start-Module([string]$modulePath, [string]$label, [string]$port) {
    $abs  = Join-Path (Get-Location) $modulePath
    $mvnw = Join-Path $abs 'mvnw.cmd'
    if ($port -and (Test-PortInUse ([int]$port))) {
        Write-Host "  [SKIP] [$label] port $port already in use - assuming it's already running." -ForegroundColor DarkYellow
        return
    }
    if (Test-Path $mvnw) {
        Write-Host "  Starting [$label] on port $port ..." -ForegroundColor Cyan
        Start-Process -FilePath $mvnw -ArgumentList 'spring-boot:run' -WorkingDirectory $abs -WindowStyle Normal
    } else {
        Write-Host "  [WARN] mvnw.cmd not found in $modulePath" -ForegroundColor Yellow
    }
}

function Wait-ForEureka([int]$timeoutSeconds = 240, [int]$intervalSeconds = 5) {
    $url = 'http://localhost:8761/actuator/health'
    $start = Get-Date
    $deadline = $start.AddSeconds($timeoutSeconds)

    while ((Get-Date) -lt $deadline) {
        try {
            $response = Invoke-WebRequest -Uri $url -UseBasicParsing -TimeoutSec 10
            $body = [System.Text.Encoding]::UTF8.GetString($response.RawContentStream.ToArray())
            if ($response.StatusCode -eq 200 -and $body -match '"status"\s*:\s*"UP"') {
                Write-Host "  Eureka is UP." -ForegroundColor Green
                return $true
            }
        } catch {
            $elapsed = [int]((Get-Date) - $start).TotalSeconds
            Write-Host "  Waiting for Eureka to start... (${elapsed}s / ${timeoutSeconds}s - first run downloads Maven deps, be patient)" -ForegroundColor Yellow
        }

        Start-Sleep -Seconds $intervalSeconds
    }

    Write-Host "  Eureka did not become healthy within $timeoutSeconds seconds." -ForegroundColor Red
    return $false
}

Write-Host ""
Write-Host "============================================" -ForegroundColor Green
Write-Host "   MediaHub - Starting All 10 Services" -ForegroundColor Green
Write-Host "============================================" -ForegroundColor Green
Write-Host ""

# -- STEP 0: Eureka Server (MUST start first) ----------------------
Write-Host "[Step 0] Starting Eureka Discovery Server..." -ForegroundColor Magenta
Start-Module 'eureka-server'                 'Eureka Server'      '8761'
if (-not (Wait-ForEureka 120 5)) {
    Write-Host "  WARNING: Eureka server did not become healthy before we started the other services." -ForegroundColor Red
    Write-Host "  Check the Eureka window for startup errors and retry once the server is up." -ForegroundColor Red
}

# -- STEP 1: Core Foundation Services ------------------------------
Write-Host "[Step 1] Starting core foundation services..." -ForegroundColor Yellow
Start-Module 'mediahub-combined\combined'    'IAM + Audit Log'    '8091'
Start-Sleep -Seconds 5

# -- STEP 2: Data Services -----------------------------------------
Write-Host "[Step 2] Starting data services..." -ForegroundColor Yellow
Start-Module 'contentcatalog_git_individual' 'Content Catalog'    '8093'
Start-Module 'mediahub\subscriptionPlan'     'Subscription Plan'  '8086'
Start-Sleep -Seconds 3

# -- STEP 3: Business Logic Services -------------------------------
Write-Host "[Step 3] Starting business logic services..." -ForegroundColor Yellow
Start-Module 'licensing'                     'Licensing'          '8083'
Start-Module 'editorial'                     'Editorial'          '9097'
Start-Module 'royalty'                       'Royalty'            '8045'
Start-Module 'notification'                  'Notification'       '8085'
Start-Sleep -Seconds 3

# -- STEP 4: Analytics Service -------------------------------------
Write-Host "[Step 4] Starting analytics service..." -ForegroundColor Yellow
Start-Module 'analytics'                     'Analytics'          '8098'
Start-Sleep -Seconds 3

# -- STEP 5: API Gateway (start last) ------------------------------
Write-Host "[Step 5] Starting API Gateway (last)..." -ForegroundColor Yellow
Start-Module 'gateway'                       'API Gateway'        '8094'

Write-Host ""
Write-Host "============================================" -ForegroundColor Green
Write-Host "   All MediaHub Services Are Starting...   " -ForegroundColor Green
Write-Host "============================================" -ForegroundColor Green
Write-Host ""
Write-Host "Service Discovery:" -ForegroundColor White
Write-Host "  Eureka Dashboard : http://localhost:8761" -ForegroundColor Magenta
Write-Host "  (Open this to see all registered services)" -ForegroundColor DarkGray
Write-Host ""
Write-Host "Service URLs (Direct Access):" -ForegroundColor White
Write-Host "  IAM + Audit Log  : http://localhost:8091  (mediahub-iam)" -ForegroundColor Gray
Write-Host "  Content Catalog  : http://localhost:8093  (mediahub-content)" -ForegroundColor Gray
Write-Host "  Subscription Plan: http://localhost:8086  (mediahub-subscription)" -ForegroundColor Gray
Write-Host "  Licensing        : http://localhost:8083  (mediahub-licensing)" -ForegroundColor Gray
Write-Host "  Editorial        : http://localhost:9097  (mediahub-editorial)" -ForegroundColor Gray
Write-Host "  Royalty          : http://localhost:8045  (mediahub-royalty)" -ForegroundColor Gray
Write-Host "  Notification     : http://localhost:8085  (mediahub-notification)" -ForegroundColor Gray
Write-Host "  Analytics        : http://localhost:8098  (mediahub-analytics)" -ForegroundColor Gray
Write-Host "  API Gateway      : http://localhost:8094  (mediahub-gateway)" -ForegroundColor Green
Write-Host ""
Write-Host "Gateway Routes (Single Entry Point - port 8094):" -ForegroundColor White
Write-Host "  /iam/**           --> IAM + Audit Log  (8091)" -ForegroundColor Gray
Write-Host "  /content/**       --> Content Catalog  (8093)" -ForegroundColor Gray
Write-Host "  /subscription/**  --> Subscription     (8086)" -ForegroundColor Gray
Write-Host "  /licensing/**     --> Licensing        (8083)" -ForegroundColor Gray
Write-Host "  /editorial/**     --> Editorial        (9097)" -ForegroundColor Gray
Write-Host "  /royalty/**       --> Royalty          (8045)" -ForegroundColor Gray
Write-Host "  /notification/**  --> Notification     (8085)" -ForegroundColor Gray
Write-Host "  /analytics/**     --> Analytics        (8098)" -ForegroundColor Gray
Write-Host ""
Write-Host "Health Checks:" -ForegroundColor White
Write-Host "  Eureka : http://localhost:8761/actuator/health" -ForegroundColor Gray
Write-Host "  Gateway: http://localhost:8094/actuator/health" -ForegroundColor Gray
Write-Host ""
Write-Host "Wait 60-90 seconds for all services to register in Eureka." -ForegroundColor Yellow
Write-Host "============================================" -ForegroundColor Green
