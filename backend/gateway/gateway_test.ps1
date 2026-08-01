$body = '{"name":"Premium","price":14.99,"billingCycle":"Monthly","contentAccessLevel":"4K HDR, full catalogue","maxDevices":5,"downloadAllowed":1}'
Write-Host '--- DIRECT ---'
try {
    $direct = Invoke-WebRequest -Uri 'http://localhost:8086/mediaHub/subscriptionPlan/plans/createPlan' -Method POST -Headers @{ 'Content-Type' = 'application/json' } -Body $body -UseBasicParsing
    Write-Host $direct.StatusCode
    Write-Host $direct.Content
} catch {
    Write-Host 'DIRECT ERROR:'
    Write-Host $_.Exception.Message
    if ($_.Exception.Response -ne $null) {
        $resp = $_.Exception.Response
        try { Write-Host (New-Object System.IO.StreamReader($resp.GetResponseStream())).ReadToEnd() } catch {}
    }
}
Write-Host '--- GATEWAY ---'
try {
    $gateway = Invoke-WebRequest -Uri 'http://localhost:8094/subscription/mediaHub/subscriptionPlan/plans/createPlan' -Method POST -Headers @{ 'Content-Type' = 'application/json' } -Body $body -UseBasicParsing
    Write-Host $gateway.StatusCode
    Write-Host $gateway.Content
} catch {
    Write-Host 'GATEWAY ERROR:'
    Write-Host $_.Exception.Message
    if ($_.Exception.Response -ne $null) {
        $resp = $_.Exception.Response
        try { Write-Host (New-Object System.IO.StreamReader($resp.GetResponseStream())).ReadToEnd() } catch {}
    }
}
