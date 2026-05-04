param(
  [string]$SqlPath = '',
  [string]$MysqlHost = '',
  [int]$MysqlPort = 0,
  [string]$Database = '',
  [string]$Username = '',
  [string]$Password = '',
  [switch]$VerifyOnly,
  [switch]$AllowNonLocal
)

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
$OutputEncoding = [System.Text.Encoding]::UTF8

$ProjectRoot = (Resolve-Path -LiteralPath (Join-Path $PSScriptRoot '..\..')).Path
if ([string]::IsNullOrWhiteSpace($SqlPath)) {
  $SqlPath = Join-Path $ProjectRoot 'scripts/local/mysql/init/52-phase-43-traveler-progress-reward-ops.sql'
}
$SqlPath = (Resolve-Path -LiteralPath $SqlPath).Path

function Get-EnvOrDefault {
  param(
    [Parameter(Mandatory = $true)][string]$Name,
    [Parameter(Mandatory = $true)][string]$DefaultValue
  )
  $value = [Environment]::GetEnvironmentVariable($Name)
  if ([string]::IsNullOrWhiteSpace($value)) { return $DefaultValue }
  return $value.Trim()
}

if ([string]::IsNullOrWhiteSpace($MysqlHost)) { $MysqlHost = Get-EnvOrDefault -Name 'LOCAL_MYSQL_HOST' -DefaultValue '127.0.0.1' }
if ($MysqlPort -le 0) { $MysqlPort = [int](Get-EnvOrDefault -Name 'LOCAL_MYSQL_PORT' -DefaultValue '3306') }
if ([string]::IsNullOrWhiteSpace($Database)) { $Database = Get-EnvOrDefault -Name 'LOCAL_MYSQL_DATABASE' -DefaultValue 'aoxiaoyou' }
if ([string]::IsNullOrWhiteSpace($Username)) { $Username = Get-EnvOrDefault -Name 'LOCAL_MYSQL_USERNAME' -DefaultValue 'root' }
if ([string]::IsNullOrWhiteSpace($Password)) { $Password = Get-EnvOrDefault -Name 'LOCAL_MYSQL_PASSWORD' -DefaultValue 'Abc123456' }

function Assert-LocalTarget {
  param([string]$HostName)
  if ($AllowNonLocal) { return }
  $localHosts = @('127.0.0.1', 'localhost', '::1')
  if ($localHosts -contains $HostName.Trim().ToLowerInvariant()) { return }
  throw "Refusing to apply Phase 43 migration to non-local MySQL host '$HostName'. Use -AllowNonLocal explicitly if this is intentional."
}

function Redact-SecretText {
  param([AllowNull()][string]$Text)
  if ([string]::IsNullOrEmpty($Text)) { return '' }
  $value = $Text
  $value = $value -replace '(?i)sk-[A-Za-z0-9_\-]{8,}', '[redacted-api-key]'
  $value = $value -replace '(?i)(MYSQL_ROOT_PASSWORD|password|SECRET|SECRET_ID|SECRET_KEY|COS_SECRET)', '[redacted-field]'
  return $value
}

function Write-SafeHost {
  param([string]$Message)
  Write-Host (Redact-SecretText -Text $Message)
}

Assert-LocalTarget -HostName $MysqlHost

$sql = Get-Content -Encoding UTF8 -Raw -LiteralPath $SqlPath
if ($sql -notmatch '(?im)^\s*SET\s+NAMES\s+utf8mb4\s*;') {
  throw "SQL file must declare SET NAMES utf8mb4; before Phase 43 migration is applied: $SqlPath"
}
$requiredSqlMarkers = @(
  'CREATE TABLE IF NOT EXISTS',
  'ON DUPLICATE KEY UPDATE',
  'phase43_smoke_game_reward',
  'phase43_smoke_resend_rule',
  'user_game_reward_grants',
  'uk_user_game_reward_grant_idempotency',
  'uk_reward_redemptions_idempotency'
)
foreach ($marker in $requiredSqlMarkers) {
  if ($sql -notlike "*$marker*") {
    throw "SQL file is missing required Phase 43 marker: $marker"
  }
}

function New-TempSqlFile {
  param([string]$Content)
  $tempPath = Join-Path ([System.IO.Path]::GetTempPath()) ("phase43-mysql-" + [Guid]::NewGuid().ToString('N') + ".sql")
  Set-Content -LiteralPath $tempPath -Encoding UTF8 -Value $Content
  return $tempPath
}

function Get-MysqlExe {
  $cmd = Get-Command mysql.exe -ErrorAction SilentlyContinue
  if ($cmd) { return $cmd.Source }
  $cmd = Get-Command mysql -ErrorAction SilentlyContinue
  if ($cmd) { return $cmd.Source }
  return ''
}

function Invoke-MysqlText {
  param(
    [Parameter(Mandatory = $true)][string]$SqlText,
    [switch]$Silent
  )
  $tempSql = New-TempSqlFile -Content $SqlText
  $mysqlExe = Get-MysqlExe
  $previousPwd = [Environment]::GetEnvironmentVariable('MYSQL_PWD')
  try {
    if (-not [string]::IsNullOrWhiteSpace($mysqlExe)) {
      $env:MYSQL_PWD = $Password
      $args = @(
        '--default-character-set=utf8mb4',
        '--protocol=TCP',
        "--host=$MysqlHost",
        "--port=$MysqlPort",
        "--user=$Username",
        "--database=$Database",
        '--batch',
        '--raw',
        "--execute=SOURCE $($tempSql.Replace('\', '/'))"
      )
      $output = & $mysqlExe @args 2>&1
      $exitCode = $LASTEXITCODE
    } else {
      if ($MysqlHost -notin @('127.0.0.1', 'localhost')) {
        throw 'mysql.exe is unavailable and Docker fallback only supports the local compose MySQL service.'
      }
      $dockerCmd = Get-Command docker -ErrorAction SilentlyContinue
      if (-not $dockerCmd) {
        throw 'Neither mysql.exe nor docker is available for applying Phase 43 migration.'
      }
      $relativeSql = $tempSql
      $output = Get-Content -Encoding UTF8 -Raw -LiteralPath $relativeSql | docker compose -f (Join-Path $ProjectRoot 'docker-compose.local.yml') exec -T mysql mysql `
        --default-character-set=utf8mb4 `
        "-u$Username" `
        "-p$Password" `
        $Database 2>&1
      $exitCode = $LASTEXITCODE
    }
    if ($exitCode -ne 0) {
      throw "MySQL command failed with exit code $exitCode. Output: $(Redact-SecretText -Text (($output | Out-String).Trim()))"
    }
    if (-not $Silent) {
      $text = (($output | Out-String).Trim())
      if (-not [string]::IsNullOrWhiteSpace($text)) {
        Write-SafeHost $text
      }
    }
    return (($output | Out-String).Trim())
  } finally {
    if ($null -eq $previousPwd) {
      Remove-Item Env:MYSQL_PWD -ErrorAction SilentlyContinue
    } else {
      $env:MYSQL_PWD = $previousPwd
    }
    Remove-Item -LiteralPath $tempSql -Force -ErrorAction SilentlyContinue
  }
}

function Invoke-MysqlScalar {
  param([Parameter(Mandatory = $true)][string]$SqlText)
  $raw = Invoke-MysqlText -SqlText $SqlText -Silent
  $lines = @($raw -split "`r?`n" | Where-Object { -not [string]::IsNullOrWhiteSpace($_) })
  if ($lines.Count -eq 0) { return '' }
  return $lines[-1].Trim()
}

function Test-Exists {
  param(
    [Parameter(Mandatory = $true)][string]$Label,
    [Parameter(Mandatory = $true)][string]$SqlText
  )
  $value = Invoke-MysqlScalar -SqlText $SqlText
  $ok = $false
  try { $ok = ([int]$value) -gt 0 } catch { $ok = $false }
  [pscustomobject]@{
    Label = $Label
    Passed = $ok
    Value = $value
  }
}

function Test-Phase43Schema {
  $checks = @(
    (Test-Exists -Label 'table user_game_reward_grants' -SqlText "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = 'user_game_reward_grants';"),
    (Test-Exists -Label 'column user_game_reward_grants.idempotency_key' -SqlText "SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'user_game_reward_grants' AND column_name = 'idempotency_key';"),
    (Test-Exists -Label 'index uk_user_game_reward_grant_idempotency' -SqlText "SELECT COUNT(*) FROM information_schema.statistics WHERE table_schema = DATABASE() AND table_name = 'user_game_reward_grants' AND index_name = 'uk_user_game_reward_grant_idempotency';"),
    (Test-Exists -Label 'column reward_redemptions.source_event_id' -SqlText "SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'reward_redemptions' AND column_name = 'source_event_id';"),
    (Test-Exists -Label 'column reward_redemptions.source_rule_id' -SqlText "SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'reward_redemptions' AND column_name = 'source_rule_id';"),
    (Test-Exists -Label 'column reward_redemptions.idempotency_key' -SqlText "SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'reward_redemptions' AND column_name = 'idempotency_key';"),
    (Test-Exists -Label 'index uk_reward_redemptions_idempotency' -SqlText "SELECT COUNT(*) FROM information_schema.statistics WHERE table_schema = DATABASE() AND table_name = 'reward_redemptions' AND index_name = 'uk_reward_redemptions_idempotency';"),
    (Test-Exists -Label 'fixture game_rewards.phase43_smoke_game_reward' -SqlText "SELECT COUNT(*) FROM game_rewards WHERE code = 'phase43_smoke_game_reward';"),
    (Test-Exists -Label 'fixture reward_rules.phase43_smoke_resend_rule' -SqlText "SELECT COUNT(*) FROM reward_rules WHERE code = 'phase43_smoke_resend_rule';")
  )
  $failed = @($checks | Where-Object { -not $_.Passed })
  foreach ($check in $checks) {
    $status = if ($check.Passed) { 'PASS' } else { 'FAIL' }
    Write-SafeHost ("[{0}] {1} value={2}" -f $status, $check.Label, $check.Value)
  }
  if ($failed.Count -gt 0) {
    throw "Phase 43 schema verification failed: $($failed.Label -join ', ')"
  }
}

Write-SafeHost "Phase 43 migration target: host=$MysqlHost port=$MysqlPort database=$Database user=$Username verifyOnly=$([bool]$VerifyOnly)"

if (-not $VerifyOnly) {
  Write-SafeHost "Applying Phase 43 migration from UTF-8 SQL file: $SqlPath"
  Invoke-MysqlText -SqlText $sql -Silent | Out-Null
}

Test-Phase43Schema
Write-SafeHost 'Phase 43 migration verification completed.'
