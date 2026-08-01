# MediaHub - Stop All Services
# Run this script from the MediaHub-Integrated folder:  .\stop-all.ps1
# Note: For best results, run as Administrator (right-click PowerShell > Run as Administrator)

# Check if running as Administrator
$isAdmin = ([Security.Principal.WindowsPrincipal] [Security.Principal.WindowsIdentity]::GetCurrent()).IsInRole([Security.Principal.WindowsBuiltInRole] "Administrator")

Write-Host ""
Write-Host "============================================" -ForegroundColor Green
Write-Host "   MediaHub - Stopping All Services" -ForegroundColor Green
Write-Host "============================================" -ForegroundColor Green
Write-Host ""

if (-not $isAdmin) {
    Write-Host "  [WARN] Not running as Administrator." -ForegroundColor Yellow
    Write-Host "  [WARN] Permission to kill all processes may be limited." -ForegroundColor Yellow
    Write-Host "  [HINT] For full stop capability, run: powershell -Command 'Start-Process powershell -ArgumentList '-ExecutionPolicy Bypass -File \"%cd%\stop-all.ps1\"' -Verb RunAs'" -ForegroundColor Gray
    Write-Host ""
}

$ports = @(8761, 8091, 8093, 8086, 8083, 9097, 8045, 8085, 8098, 8094)
$stopped = @()

# 1. Stop all java.exe processes running Spring Boot (most reliable method)
Write-Host "  [1] Stopping Spring Boot Java processes..." -ForegroundColor Cyan
try {
    $javaProcs = Get-Process -Name java -ErrorAction SilentlyContinue
    if ($javaProcs) {
        foreach ($proc in $javaProcs) {
            Write-Host "    Stopping java.exe PID $($proc.Id)..." -ForegroundColor Yellow
            Stop-Process -Id $proc.Id -Force -ErrorAction SilentlyContinue
            $stopped += $proc.Id
        }
    } else {
        Write-Host "    No java processes found." -ForegroundColor Gray
    }
} catch {
    Write-Host "    Error stopping java processes: $_" -ForegroundColor Red
}

# 2. Stop processes listening on MediaHub ports (fallback)
Write-Host ""
Write-Host "  [2] Stopping processes on MediaHub ports..." -ForegroundColor Cyan
foreach ($port in $ports) {
    try {
        $netConns = Get-NetTCPConnection -LocalPort $port -State Listen -ErrorAction SilentlyContinue
        if ($netConns) {
            foreach ($conn in $netConns) {
                $pid = $conn.OwningProcess
                if ($pid -notin $stopped -and $pid -gt 4) {
                    $proc = Get-Process -Id $pid -ErrorAction SilentlyContinue
                    if ($proc) {
                        Write-Host "    Stopping $($proc.ProcessName) (PID $pid) on port $port..." -ForegroundColor Yellow
                        Stop-Process -Id $pid -Force -ErrorAction SilentlyContinue
                        $stopped += $pid
                    }
                }
            }
        }
    } catch {
        # Silently skip port check on permission errors
    }
}

if ($stopped.Count -eq 0) {
    Write-Host ""
    Write-Host "  [RESULT] No services were running to stop." -ForegroundColor DarkYellow
} else {
    Write-Host ""
    Write-Host "  [RESULT] Stopped $($stopped.Count) process(es)." -ForegroundColor Green
    Write-Host "  Waiting 2s for cleanup..." -ForegroundColor Gray
    Start-Sleep -Seconds 2
}

Write-Host ""
Write-Host "============================================" -ForegroundColor Green
Write-Host "   MediaHub Services Stopped" -ForegroundColor Green
Write-Host "============================================" -ForegroundColor Green
Write-Host ""
