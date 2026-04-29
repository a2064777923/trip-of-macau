param(
  [string]$AdminBaseUrl = 'http://127.0.0.1:8081',
  [string]$PublicBaseUrl = 'http://127.0.0.1:8080',
  [string]$Username = 'admin',
  [string]$Password = 'admin123',
  [switch]$SkipAdminTests,
  [switch]$SkipAdminBuild,
  [switch]$SkipServerTests,
  [switch]$SkipClientBuild
)

$ErrorActionPreference = 'Stop'

function Get-RepoRoot {
  return (Resolve-Path (Join-Path $PSScriptRoot '..\..')).Path
}

function Test-HttpReady {
  param(
    [Parameter(Mandatory = $true)][string]$Name,
    [Parameter(Mandatory = $true)][string]$Url
  )

  try {
    $response = Invoke-WebRequest -UseBasicParsing -Uri $Url -TimeoutSec 10
  } catch {
    throw "$Name is not ready at $Url : $($_.Exception.Message)"
  }

  if ($response.StatusCode -lt 200 -or $response.StatusCode -ge 400) {
    throw "$Name returned unexpected status $($response.StatusCode) at $Url"
  }

  return $response.StatusCode
}

function Invoke-PhaseScript {
  param(
    [Parameter(Mandatory = $true)][string]$StageName,
    [Parameter(Mandatory = $true)][string]$RuleMarker,
    [Parameter(Mandatory = $true)][string]$ScriptPath,
    [string[]]$Arguments = @()
  )

  if (-not (Test-Path $ScriptPath)) {
    throw "Missing stage script: $ScriptPath"
  }

  Write-Host ''
  Write-Host ("==> {0} {1}" -f $RuleMarker, $StageName) -ForegroundColor Cyan

  $startedAt = Get-Date
  $scriptOutput = & powershell.exe -NoProfile -ExecutionPolicy Bypass -File $ScriptPath @Arguments 2>&1
  $exitCode = $LASTEXITCODE
  if ($null -ne $scriptOutput) {
    foreach ($entry in @($scriptOutput)) {
      if ($entry -is [System.Management.Automation.ErrorRecord]) {
        Write-Host ($entry.ToString()) -ForegroundColor Red
      } else {
        Write-Host ([string]$entry)
      }
    }
  }
  if ($exitCode -ne 0) {
    throw "$StageName failed with exit code $exitCode"
  }

  $finishedAt = Get-Date
  return [pscustomobject]@{
    stage = $StageName
    ruleMarker = $RuleMarker
    script = $ScriptPath
    startedAt = $startedAt.ToString('s')
    finishedAt = $finishedAt.ToString('s')
    durationSeconds = [math]::Round((New-TimeSpan -Start $startedAt -End $finishedAt).TotalSeconds, 2)
    status = 'passed'
  }
}

$repoRoot = Get-RepoRoot
$seedScript = Join-Path $repoRoot 'scripts/local/seed-lisboeta-indoor.ps1'
$phase15Script = Join-Path $repoRoot 'scripts/local/smoke-phase-15-indoor-authoring.ps1'
$phase16Script = Join-Path $repoRoot 'scripts/local/smoke-phase-16-indoor-rule-governance.ps1'
$phase17Script = Join-Path $repoRoot 'scripts/local/smoke-phase-17-indoor-runtime.ps1'

$adminStatus = Test-HttpReady -Name 'Admin backend' -Url "$AdminBaseUrl/swagger-ui.html"
$publicStatus = Test-HttpReady -Name 'Public backend' -Url "$PublicBaseUrl/api/v1/health"

$stages = @()

$stages += Invoke-PhaseScript `
  -StageName 'Normalize Lisboa indoor closure fixture' `
  -RuleMarker '[RULE-01][RULE-02][RULE-03][RULE-04][RULE-05]' `
  -ScriptPath $seedScript `
  -Arguments @(
    '-AdminBaseUrl', $AdminBaseUrl,
    '-PublicBaseUrl', $PublicBaseUrl,
    '-Username', $Username,
    '-Password', $Password
  )

$phase15Args = @(
  '-AdminBaseUrl', $AdminBaseUrl,
  '-PublicBaseUrl', $PublicBaseUrl,
  '-Username', $Username,
  '-Password', $Password
)
if ($SkipAdminTests) {
  $phase15Args += '-SkipTests'
}
if ($SkipAdminBuild) {
  $phase15Args += '-SkipBuild'
}
$stages += Invoke-PhaseScript `
  -StageName 'Run structured indoor authoring smoke' `
  -RuleMarker '[RULE-01][RULE-02]' `
  -ScriptPath $phase15Script `
  -Arguments $phase15Args

$phase16Args = @(
  '-AdminBaseUrl', $AdminBaseUrl,
  '-Username', $Username,
  '-Password', $Password
)
if ($SkipAdminBuild) {
  $phase16Args += '-SkipBuild'
}
$stages += Invoke-PhaseScript `
  -StageName 'Run indoor governance smoke' `
  -RuleMarker '[RULE-04][RULE-05]' `
  -ScriptPath $phase16Script `
  -Arguments $phase16Args

$phase17Args = @(
  '-AdminBaseUrl', $AdminBaseUrl,
  '-PublicBaseUrl', $PublicBaseUrl,
  '-Username', $Username,
  '-Password', $Password
)
if ($SkipServerTests) {
  $phase17Args += '-SkipServerTests'
}
if ($SkipClientBuild) {
  $phase17Args += '-SkipBuild'
}
$stages += Invoke-PhaseScript `
  -StageName 'Run public indoor runtime smoke' `
  -RuleMarker '[RULE-03]' `
  -ScriptPath $phase17Script `
  -Arguments $phase17Args

Write-Host ''
Write-Host 'Phase 21 indoor closure smoke passed.' -ForegroundColor Green

[ordered]@{
  adminReady = @{
    url = "$AdminBaseUrl/swagger-ui.html"
    statusCode = $adminStatus
  }
  publicReady = @{
    url = "$PublicBaseUrl/api/v1/health"
    statusCode = $publicStatus
  }
  stages = $stages
} | ConvertTo-Json -Depth 8
