param(
    [string]$MySqlHost = '127.0.0.1',
    [int]$MySqlPort = 3306,
    [string]$MySqlDatabase = 'aoxiaoyou',
    [string]$MySqlUser = 'root',
    [string]$MySqlPassword = 'Abc123456'
)

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'

$seedFile = Resolve-Path (Join-Path $PSScriptRoot 'mysql/init/06-live-backend-mock-migration.sql')
$seedPath = $seedFile.Path.Replace('\', '/')
$previousPassword = $env:MYSQL_PWD

try {
    $env:MYSQL_PWD = $MySqlPassword
    & mysql `
        --default-character-set=utf8mb4 `
        --protocol=TCP `
        --host=$MySqlHost `
        --port=$MySqlPort `
        --user=$MySqlUser `
        --database=$MySqlDatabase `
        --execute="SOURCE $seedPath"

    if ($LASTEXITCODE -ne 0) {
        throw "Phase 6 seed apply failed: $seedPath"
    }

    Write-Host '[phase-06] Phase 6 mock migration seed applied successfully.'
} finally {
    if ($null -eq $previousPassword) {
        Remove-Item Env:MYSQL_PWD -ErrorAction SilentlyContinue
    } else {
        $env:MYSQL_PWD = $previousPassword
    }
}
