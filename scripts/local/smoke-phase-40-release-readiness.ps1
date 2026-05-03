param(
  [switch]$Quick,
  [switch]$IncludeBuilds,
  [switch]$IncludeLive,
  [switch]$IncludeAdminQaActions,
  [string]$ReportPath = ''
)

$ErrorActionPreference = 'Stop'
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
$OutputEncoding = [System.Text.Encoding]::UTF8

$ProjectRoot = (Resolve-Path -LiteralPath (Join-Path $PSScriptRoot '..\..')).Path
$PhaseDir = '.planning/phases/40-acceptance-cost-visibility-and-release-readiness'
if ([string]::IsNullOrWhiteSpace($ReportPath)) {
  $ReportPath = Join-Path $ProjectRoot "$PhaseDir/40-SMOKE-REPORT.md"
}

$Results = New-Object System.Collections.Generic.List[object]

function Get-EnvFlag {
  param([Parameter(Mandatory = $true)][string]$Name)
  $value = [Environment]::GetEnvironmentVariable($Name)
  if ([string]::IsNullOrWhiteSpace($value)) { return $false }
  return @('1', 'true', 'yes', 'on') -contains $value.Trim().ToLowerInvariant()
}

function Get-EnvValue {
  param([Parameter(Mandatory = $true)][string]$Name)
  $value = [Environment]::GetEnvironmentVariable($Name)
  if ([string]::IsNullOrWhiteSpace($value)) { return $null }
  return $value.Trim()
}

function ConvertTo-RelativePath {
  param([string]$Path)
  if ([string]::IsNullOrWhiteSpace($Path)) { return '' }
  $resolved = $Path
  try { $resolved = (Resolve-Path -LiteralPath $Path -ErrorAction SilentlyContinue).Path } catch { $resolved = $Path }
  if ($resolved.StartsWith($ProjectRoot, [System.StringComparison]::OrdinalIgnoreCase)) {
    return $resolved.Substring($ProjectRoot.Length).TrimStart('\', '/').Replace('\', '/')
  }
  return $Path.Replace('\', '/')
}

function Escape-MarkdownCell {
  param([string]$Value)
  if ($null -eq $Value) { return '' }
  return ($Value -replace '\|', '/' -replace "`r?`n", ' ').Trim()
}

function Assert-NoSecretText {
  param(
    [AllowNull()][string]$Text,
    [Parameter(Mandatory = $true)][string]$Context
  )
  if ([string]::IsNullOrEmpty($Text)) { return }
  $patterns = @(
    'sk-[A-Za-z0-9_\-]{8,}',
    'Bearer\s+',
    'COS_SECRET',
    'SECRET_ID',
    'SECRET_KEY',
    'providerApiKey',
    'apiKey',
    'authorization',
    'promptText',
    'scriptText',
    'localPath'
  )
  foreach ($pattern in $patterns) {
    if ($Text -match "(?i)$pattern") {
      throw "$Context contains a banned secret-bearing pattern: $pattern"
    }
  }
}

function Redact-SecretText {
  param([AllowNull()][string]$Text)
  if ([string]::IsNullOrEmpty($Text)) { return '' }
  $value = $Text
  $value = $value -replace '(?i)sk-[A-Za-z0-9_\-]{8,}', '[redacted-api-key]'
  $value = $value -replace '(?i)Bearer\s+[A-Za-z0-9._~+/=-]+', 'Bearer [redacted-token]'
  $value = $value -replace '(?i)(COS_SECRET|SECRET_ID|SECRET_KEY|providerApiKey|apiKey|authorization|promptText|scriptText|localPath)', '[redacted-field]'
  return $value
}

function Add-Result {
  param(
    [Parameter(Mandatory = $true)][string]$Area,
    [Parameter(Mandatory = $true)][string]$Check,
    [Parameter(Mandatory = $true)][ValidateSet('PASS', 'SKIP', 'FAIL', 'BLOCKED')][string]$Status,
    [Parameter(Mandatory = $true)][string]$Evidence,
    [string]$Command = ''
  )
  Assert-NoSecretText -Text $Evidence -Context "$Area/$Check evidence"
  Assert-NoSecretText -Text $Command -Context "$Area/$Check command"
  $Results.Add([pscustomobject]@{
    Area = $Area
    Check = $Check
    Status = $Status
    Evidence = $Evidence
    Command = $Command
  })
}

function Invoke-CheckedCommand {
  param(
    [Parameter(Mandatory = $true)][string]$Area,
    [Parameter(Mandatory = $true)][string]$Check,
    [Parameter(Mandatory = $true)][string]$Command,
    [string]$WorkingDirectory = $ProjectRoot
  )

  $startInfo = New-Object System.Diagnostics.ProcessStartInfo
  $startInfo.FileName = 'powershell.exe'
  $startInfo.Arguments = "-NoProfile -ExecutionPolicy Bypass -Command $Command"
  $startInfo.WorkingDirectory = $WorkingDirectory
  $startInfo.RedirectStandardOutput = $true
  $startInfo.RedirectStandardError = $true
  $startInfo.UseShellExecute = $false
  $startInfo.StandardOutputEncoding = [System.Text.Encoding]::UTF8
  $startInfo.StandardErrorEncoding = [System.Text.Encoding]::UTF8

  $process = New-Object System.Diagnostics.Process
  $process.StartInfo = $startInfo
  [void]$process.Start()
  $stdout = $process.StandardOutput.ReadToEnd()
  $stderr = $process.StandardError.ReadToEnd()
  $process.WaitForExit()

  $combined = "$stdout`n$stderr"

  if ($process.ExitCode -eq 0) {
    Add-Result -Area $Area -Check $Check -Status 'PASS' -Evidence 'Command exited 0.' -Command $Command
  } else {
    $line = (($combined -split "`r?`n") | Where-Object { -not [string]::IsNullOrWhiteSpace($_) } | Select-Object -Last 1)
    if ([string]::IsNullOrWhiteSpace($line)) { $line = "exit code $($process.ExitCode)" }
    $safeLine = Redact-SecretText -Text $line
    if ($safeLine.Length -gt 180) { $safeLine = $safeLine.Substring(0, 180) + '...' }
    Add-Result -Area $Area -Check $Check -Status 'BLOCKED' -Evidence "Command exited $($process.ExitCode): $safeLine" -Command $Command
  }
}

function Invoke-JsonRequest {
  param(
    [Parameter(Mandatory = $true)][string]$Method,
    [Parameter(Mandatory = $true)][string]$Url,
    [string]$Token,
    $Body
  )
  $headers = @{ Accept = 'application/json' }
  if (-not [string]::IsNullOrWhiteSpace($Token)) { $headers.Authorization = "Bearer $Token" }
  $parameters = @{
    Method = $Method
    Uri = $Url
    Headers = $headers
    ErrorAction = 'Stop'
    TimeoutSec = 30
  }
  if ($null -ne $Body) {
    $parameters.Body = ($Body | ConvertTo-Json -Depth 20 -Compress)
    $parameters.ContentType = 'application/json; charset=utf-8'
  }
  return Invoke-RestMethod @parameters
}

function Ensure-Success {
  param($Response, [string]$Context)
  if ($null -eq $Response) { throw "$Context returned empty response" }
  if ($Response.PSObject.Properties.Name -contains 'code') {
    if ($Response.code -ne 0 -and $Response.code -ne 200) {
      throw "$Context failed with code=$($Response.code)"
    }
    return $Response.data
  }
  return $Response
}

function Normalize-BearerToken {
  param([string]$Token)
  if ([string]::IsNullOrWhiteSpace($Token)) { return $null }
  $trimmed = $Token.Trim()
  if ($trimmed.StartsWith('Bearer ', [System.StringComparison]::OrdinalIgnoreCase)) {
    return $trimmed.Substring(7).Trim()
  }
  return $trimmed
}

function Get-AdminToken {
  param([Parameter(Mandatory = $true)][string]$BaseUrl)
  $token = Normalize-BearerToken -Token (Get-EnvValue -Name 'PHASE40_ADMIN_BEARER_TOKEN')
  if ($token) { return $token }
  $username = Get-EnvValue -Name 'PHASE40_ADMIN_USERNAME'
  $password = Get-EnvValue -Name 'PHASE40_ADMIN_PASSWORD'
  if (-not $username) { $username = 'admin' }
  if (-not $password) { $password = 'admin123' }
  $login = Ensure-Success -Context 'admin login' -Response (
    Invoke-JsonRequest -Method POST -Url "$BaseUrl/api/admin/v1/auth/login" -Body @{
      username = $username
      password = $password
    }
  )
  if ([string]::IsNullOrWhiteSpace([string]$login.token)) { throw 'admin login did not return token' }
  return [string]$login.token
}

function Test-PlanningArtifacts {
  $required = @(
    "$PhaseDir/40-CONTEXT.md",
    "$PhaseDir/40-RESEARCH.md",
    '.planning/phases/36-material-production-pipeline-and-asset-promotion/36-VERIFICATION.md',
    '.planning/phases/38-public-runtime-asset-consumption/38-VERIFICATION.md',
    '.planning/phases/39-mini-program-story-mode-experience/39-VERIFICATION.md'
  )
  $missing = @()
  foreach ($relative in $required) {
    if (-not (Test-Path -LiteralPath (Join-Path $ProjectRoot $relative))) { $missing += $relative }
  }
  if ($missing.Count -eq 0) {
    Add-Result -Area 'planning' -Check 'required artifacts' -Status 'PASS' -Evidence 'Required planning and verification files exist.'
  } else {
    Add-Result -Area 'planning' -Check 'required artifacts' -Status 'FAIL' -Evidence ('Missing: ' + ($missing -join ', '))
  }
}

function Test-AiObservabilityApi {
  $skipAdmin = Get-EnvFlag -Name 'PHASE40_SKIP_ADMIN_AUTH'
  if ($skipAdmin) {
    Add-Result -Area 'admin ai' -Check 'observability API' -Status 'SKIP' -Evidence 'PHASE40_SKIP_ADMIN_AUTH=true.'
    return
  }
  $baseUrl = Get-EnvValue -Name 'PHASE40_ADMIN_BASE_URL'
  if (-not $baseUrl) { $baseUrl = 'http://127.0.0.1:8081' }
  $baseUrl = $baseUrl.TrimEnd('/')
  try {
    $token = Get-AdminToken -BaseUrl $baseUrl
    $overview = Ensure-Success -Context 'ai overview' -Response (
      Invoke-JsonRequest -Method GET -Url "$baseUrl/api/admin/v1/ai/overview" -Token $token
    )
    $jobs = Ensure-Success -Context 'ai generation jobs' -Response (
      Invoke-JsonRequest -Method GET -Url "$baseUrl/api/admin/v1/ai/generation-jobs?pageNum=1&pageSize=5" -Token $token
    )
    $logs = Ensure-Success -Context 'ai logs' -Response (
      Invoke-JsonRequest -Method GET -Url "$baseUrl/api/admin/v1/ai/logs?pageNum=1&pageSize=5" -Token $token
    )
    $summary = @{
      overviewPresent = $null -ne $overview
      jobRows = @($jobs.list).Count
      logRows = @($logs.list).Count
    } | ConvertTo-Json -Depth 5 -Compress
    Assert-NoSecretText -Text $summary -Context 'admin ai observability summary'
    Add-Result -Area 'admin ai' -Check 'observability API' -Status 'PASS' -Evidence "Overview available; jobs=$(@($jobs.list).Count); logs=$(@($logs.list).Count)."
  } catch {
    $message = $_.Exception.Message
    Assert-NoSecretText -Text $message -Context 'admin ai blocked message'
    Add-Result -Area 'admin ai' -Check 'observability API' -Status 'BLOCKED' -Evidence "Admin AI check unavailable: $message"
  }
}

function Write-Report {
  $reportFullPath = $ReportPath
  if (-not [System.IO.Path]::IsPathRooted($reportFullPath)) {
    $reportFullPath = Join-Path $ProjectRoot $reportFullPath
  }
  $dir = Split-Path -Parent $reportFullPath
  if (-not (Test-Path -LiteralPath $dir)) { New-Item -ItemType Directory -Path $dir | Out-Null }

  $mode = @(
    "Quick=$([bool]$Quick)",
    "IncludeBuilds=$([bool]$IncludeBuilds)",
    "IncludeLive=$([bool]$IncludeLive)",
    "IncludeAdminQaActions=$([bool]$IncludeAdminQaActions)"
  ) -join '; '
  $lines = New-Object System.Collections.Generic.List[string]
  $lines.Add('# Phase 40 Smoke Report')
  $lines.Add('')
  $lines.Add("Generated: $(Get-Date -Format s)")
  $lines.Add("Mode: $mode")
  $lines.Add('')
  $lines.Add('| Area | Check | Status | Evidence | Command |')
  $lines.Add('| --- | --- | --- | --- | --- |')
  foreach ($result in $Results) {
    $lines.Add("| $(Escape-MarkdownCell $result.Area) | $(Escape-MarkdownCell $result.Check) | $(Escape-MarkdownCell $result.Status) | $(Escape-MarkdownCell $result.Evidence) | $(Escape-MarkdownCell $result.Command) |")
  }
  $lines.Add('')
  $hasFail = @($Results | Where-Object { $_.Status -eq 'FAIL' }).Count -gt 0
  $hasBlocked = @($Results | Where-Object { $_.Status -eq 'BLOCKED' }).Count -gt 0
  if ($hasFail) {
    $lines.Add('Final outcome: FAIL')
  } elseif ($hasBlocked) {
    $lines.Add('Final outcome: BLOCKED')
  } else {
    $lines.Add('Final outcome: PASS_WITH_SKIPS_ALLOWED')
  }
  $content = ($lines -join "`n") + "`n"
  Assert-NoSecretText -Text $content -Context 'Phase 40 smoke report'
  Set-Content -LiteralPath $reportFullPath -Value $content -Encoding UTF8
}

$IncludeBuilds = $IncludeBuilds -or (Get-EnvFlag -Name 'PHASE40_INCLUDE_BUILDS')
$IncludeLive = $IncludeLive -or (Get-EnvFlag -Name 'PHASE40_INCLUDE_LIVE')
$IncludeAdminQaActions = $IncludeAdminQaActions -or (Get-EnvFlag -Name 'PHASE40_INCLUDE_ADMIN_QA_ACTIONS')
$FailOnSkippedLive = Get-EnvFlag -Name 'PHASE40_FAIL_ON_SKIPPED_LIVE'

Test-PlanningArtifacts

Invoke-CheckedCommand -Area 'phase 36' -Check 'material production validate-only' -Command '& .\scripts\local\smoke-phase-36-material-production.ps1 -ValidateOnly'

if ($IncludeLive) {
  Invoke-CheckedCommand -Area 'phase 36' -Check 'live material and COS' -Command '& .\scripts\local\smoke-phase-36-material-production.ps1 -IncludeVideo -RunProduction'
} else {
  Add-Result -Area 'phase 36' -Check 'live material and COS' -Status 'SKIP' -Evidence 'set PHASE40_INCLUDE_LIVE=true to enable provider/COS live checks.'
}

Invoke-CheckedCommand -Area 'phase 37' -Check 'material QA read-only' -Command '& .\scripts\local\smoke-phase-37-material-qa.ps1 -SkipActions'

if ($IncludeAdminQaActions) {
  Invoke-CheckedCommand -Area 'phase 37' -Check 'material QA actions' -Command '& .\scripts\local\smoke-phase-37-material-qa.ps1'
} else {
  Add-Result -Area 'phase 37' -Check 'material QA actions' -Status 'SKIP' -Evidence 'set PHASE40_INCLUDE_ADMIN_QA_ACTIONS=true to enable QA mutation checks.'
}

Invoke-CheckedCommand -Area 'phase 38' -Check 'public runtime assets' -Command '& .\scripts\local\smoke-phase-38-public-runtime-assets.ps1'
Invoke-CheckedCommand -Area 'phase 39' -Check 'story mode smoke' -Command '& .\scripts\local\smoke-phase-39-mini-program-story-mode.ps1'
Invoke-CheckedCommand -Area 'client' -Check 'phase 39 npm smoke alias' -Command 'npm run smoke:phase39:story-mode' -WorkingDirectory (Join-Path $ProjectRoot 'packages/client')
Test-AiObservabilityApi

if ($IncludeBuilds -or -not $Quick) {
  Invoke-CheckedCommand -Area 'client' -Check 'mini-program build' -Command 'npm run build:weapp' -WorkingDirectory (Join-Path $ProjectRoot 'packages/client')
} else {
  Add-Result -Area 'client' -Check 'mini-program build' -Status 'SKIP' -Evidence 'set PHASE40_INCLUDE_BUILDS=true or omit -Quick to run npm run build:weapp.'
}

if ($IncludeBuilds) {
  Invoke-CheckedCommand -Area 'admin ui' -Check 'build' -Command 'npm run build' -WorkingDirectory (Join-Path $ProjectRoot 'packages/admin/aoxiaoyou-admin-ui')
  Invoke-CheckedCommand -Area 'public backend' -Check 'compile' -Command 'mvn -q -DskipTests compile -f packages/server/pom.xml'
  Invoke-CheckedCommand -Area 'admin backend' -Check 'compile' -Command 'mvn -q -DskipTests compile -f packages/admin/aoxiaoyou-admin-backend/pom.xml'
} else {
  Add-Result -Area 'admin ui' -Check 'build' -Status 'SKIP' -Evidence 'set PHASE40_INCLUDE_BUILDS=true to run npm run build.'
  Add-Result -Area 'public backend' -Check 'compile' -Status 'SKIP' -Evidence 'set PHASE40_INCLUDE_BUILDS=true to run Maven compile.'
  Add-Result -Area 'admin backend' -Check 'compile' -Status 'SKIP' -Evidence 'set PHASE40_INCLUDE_BUILDS=true to run Maven compile.'
}

Write-Report

$failed = @($Results | Where-Object { $_.Status -eq 'FAIL' })
$blocked = @($Results | Where-Object { $_.Status -eq 'BLOCKED' })
if ($failed.Count -gt 0 -or $blocked.Count -gt 0) {
  Write-Host 'Phase 40 release-readiness smoke completed with blocking results. See the report for safe details.'
  exit 1
}

if ($FailOnSkippedLive) {
  $liveSkip = @($Results | Where-Object { $_.Status -eq 'SKIP' -and $_.Check -match 'live|build|actions' })
  if ($liveSkip.Count -gt 0) {
    Write-Host 'Phase 40 release-readiness smoke completed with skipped live checks and PHASE40_FAIL_ON_SKIPPED_LIVE=true.'
    exit 1
  }
}

Write-Host 'Phase 40 release-readiness smoke completed.'
