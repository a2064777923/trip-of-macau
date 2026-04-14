Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

function Wait-ContainerReady {
    param(
        [Parameter(Mandatory = $true)][string]$Name,
        [int]$TimeoutSeconds = 120
    )

    $deadline = (Get-Date).AddSeconds($TimeoutSeconds)
    while ((Get-Date) -lt $deadline) {
        $status = docker inspect --format "{{if .State.Health}}{{.State.Health.Status}}{{else}}{{.State.Status}}{{end}}" $Name 2>$null
        if ($LASTEXITCODE -eq 0) {
            $normalized = ($status | Out-String).Trim()
            if ($normalized -in @("healthy", "running")) {
                Write-Host "[ok] $Name -> $normalized"
                return
            }
        }
        Start-Sleep -Seconds 2
    }

    throw "Container $Name did not become ready within $TimeoutSeconds seconds."
}

function Wait-JsonEndpoint {
    param(
        [Parameter(Mandatory = $true)][string]$Url,
        [int]$TimeoutSeconds = 120
    )

    $deadline = (Get-Date).AddSeconds($TimeoutSeconds)
    while ((Get-Date) -lt $deadline) {
        try {
            return Invoke-RestMethod -Uri $Url -Method Get -TimeoutSec 10
        } catch {
            Start-Sleep -Seconds 2
        }
    }

    throw "Endpoint did not become reachable: $Url"
}

function Assert-Equals {
    param(
        [Parameter(Mandatory = $true)]$Actual,
        [Parameter(Mandatory = $true)]$Expected,
        [Parameter(Mandatory = $true)][string]$Label
    )

    if ($Actual -ne $Expected) {
        throw "$Label expected '$Expected' but received '$Actual'."
    }
}

function Test-MySqlReady {
    cmd /c "mysql -uroot -pAbc123456 -D aoxiaoyou -e ""SELECT 1;"" >nul 2>nul"
    return $LASTEXITCODE -eq 0
}

Write-Host "[phase-01] ensuring local datastores are running..."
$mysqlPortInUse = Get-NetTCPConnection -LocalPort 3306 -ErrorAction SilentlyContinue | Select-Object -First 1
if ($null -ne $mysqlPortInUse) {
    Write-Host "[phase-01] detected an existing listener on 3306; reusing local MySQL."
    if (-not (Test-MySqlReady)) {
        throw "A process is listening on 3306, but MySQL root connectivity to database 'aoxiaoyou' failed."
    }
} else {
    docker compose -f docker-compose.local.yml up -d mysql | Out-Host
    if ($LASTEXITCODE -ne 0) {
        throw "docker compose failed while starting mysql."
    }
    Wait-ContainerReady -Name "trip-of-macau-mysql"
    if (-not (Test-MySqlReady)) {
        throw "MySQL did not become queryable after startup."
    }
}

docker compose -f docker-compose.local.yml up -d mongodb | Out-Host
if ($LASTEXITCODE -ne 0) {
    throw "docker compose failed while starting mongodb."
}

Wait-ContainerReady -Name "trip-of-macau-mongo"

Write-Host "[phase-01] probing public and admin health endpoints..."
$publicHealth = Wait-JsonEndpoint -Url "http://127.0.0.1:8080/api/v1/health"
$publicActuator = Wait-JsonEndpoint -Url "http://127.0.0.1:8080/actuator/health"
$adminHealth = Wait-JsonEndpoint -Url "http://127.0.0.1:8081/api/v1/health"

Assert-Equals -Actual $publicHealth.data.service -Expected "trip-of-macau-server" -Label "public health service"
Assert-Equals -Actual $publicHealth.data.status -Expected "UP" -Label "public health status"
Assert-Equals -Actual $publicActuator.status -Expected "UP" -Label "public actuator status"
Assert-Equals -Actual $adminHealth.data.service -Expected "aoxiaoyou-admin-backend" -Label "admin health service"
Assert-Equals -Actual $adminHealth.data.status -Expected "UP" -Label "admin health status"

Write-Host "[phase-01] smoke checks passed."
