param(
  [string]$AdminBaseUrl = 'http://127.0.0.1:8081',
  [string]$PublicBaseUrl = 'http://127.0.0.1:8080',
  [string]$Username = 'admin',
  [string]$Password = 'admin123',
  [switch]$SkipServerTests,
  [switch]$SkipBuild
)

$ErrorActionPreference = 'Stop'

function Write-Utf8File {
  param(
    [Parameter(Mandatory = $true)][string]$Path,
    [Parameter(Mandatory = $true)]$Content
  )

  $encoding = New-Object System.Text.UTF8Encoding($false)
  if ($Content -isnot [string]) {
    $Content = $Content | ConvertTo-Json -Depth 60
  }
  [System.IO.File]::WriteAllText($Path, [string]$Content, $encoding)
}

function U {
  param([Parameter(Mandatory = $true)][string]$Text)

  return [regex]::Unescape($Text)
}

function Invoke-CurlJson {
  param(
    [Parameter(Mandatory = $true)][string]$Method,
    [Parameter(Mandatory = $true)][string]$Url,
    [string]$Token,
    [string]$JsonPath
  )

  $arguments = @('-sS', '-X', $Method, $Url)
  if ($Token) {
    $arguments += @('-H', "Authorization: Bearer $Token")
  }
  if ($JsonPath) {
    $arguments += @('-H', 'Content-Type: application/json; charset=utf-8', '--data-binary', "@$JsonPath")
  }

  $raw = & curl.exe @arguments
  if (-not $raw) {
    throw "Empty response from $Url"
  }
  return $raw | ConvertFrom-Json
}

function Invoke-JsonApi {
  param(
    [Parameter(Mandatory = $true)][string]$Method,
    [Parameter(Mandatory = $true)][string]$Url,
    [string]$Token,
    $Body
  )

  $jsonPath = $null
  try {
    if ($null -ne $Body) {
      $jsonPath = Join-Path $env:TEMP ("phase17-runtime-{0}.json" -f ([guid]::NewGuid().ToString('N')))
      Write-Utf8File -Path $jsonPath -Content $Body
    }
    return Invoke-CurlJson -Method $Method -Url $Url -Token $Token -JsonPath $jsonPath
  } finally {
    if ($jsonPath -and (Test-Path $jsonPath)) {
      Remove-Item $jsonPath -Force
    }
  }
}

function Ensure-ApiSuccess {
  param(
    [Parameter(Mandatory = $true)]$Response,
    [Parameter(Mandatory = $true)][string]$Context
  )

  if ($Response.code -ne 0) {
    throw "$Context failed: $($Response.message)"
  }
  return $Response.data
}

function Assert-True {
  param(
    [Parameter(Mandatory = $true)][bool]$Condition,
    [Parameter(Mandatory = $true)][string]$Message
  )

  if (-not $Condition) {
    throw $Message
  }
}

function Invoke-CheckedCommand {
  param(
    [Parameter(Mandatory = $true)][string]$Workdir,
    [Parameter(Mandatory = $true)][string[]]$Command
  )

  Write-Host ("==> {0}`n    {1}" -f $Workdir, ($Command -join ' '))
  Push-Location $Workdir
  try {
    & $Command[0] @($Command[1..($Command.Length - 1)])
    if ($LASTEXITCODE -ne 0) {
      throw "Command failed with exit code ${LASTEXITCODE}: $($Command -join ' ')"
    }
  } finally {
    Pop-Location
  }
}

function Get-RepoRoot {
  return (Resolve-Path (Join-Path $PSScriptRoot '..\..')).Path
}

function Ensure-CanonicalWitnessSet {
  $repoRoot = Get-RepoRoot
  $phase15Script = Join-Path $repoRoot 'scripts/local/smoke-phase-15-indoor-authoring.ps1'
  if (-not (Test-Path $phase15Script)) {
    throw "Cannot find Phase 15 witness seed script at $phase15Script"
  }

  Write-Host 'Refreshing canonical Lisboa 1F witness set before Phase 17 runtime smoke...'
  & powershell.exe -NoProfile -ExecutionPolicy Bypass -File $phase15Script `
    -AdminBaseUrl $AdminBaseUrl `
    -PublicBaseUrl $PublicBaseUrl `
    -Username $Username `
    -Password $Password `
    -Scenario seed `
    -SkipTests `
    -SkipBuild

  if ($LASTEXITCODE -ne 0) {
    throw 'Phase 15 witness seed failed before Phase 17 runtime smoke.'
  }
}

function Get-AdminToken {
  $response = Invoke-JsonApi -Method POST -Url "$AdminBaseUrl/api/admin/v1/auth/login" -Body @{
    username = $Username
    password = $Password
  }
  $data = Ensure-ApiSuccess -Response $response -Context 'Admin login'
  Assert-True -Condition (-not [string]::IsNullOrWhiteSpace($data.token)) -Message 'Admin login did not return a token.'
  return $data.token
}

function Get-LisboetaFloorFixture {
  param([Parameter(Mandatory = $true)][string]$Token)

  $buildingPage = Ensure-ApiSuccess -Response (
    Invoke-CurlJson -Method GET -Url "$AdminBaseUrl/api/admin/v1/map/indoor/buildings?pageNum=1&pageSize=200&keyword=lisboeta_macau" -Token $Token
  ) -Context 'Fetch indoor building list'

  $building = @($buildingPage.list | Where-Object { $_.buildingCode -eq 'lisboeta_macau' } | Select-Object -First 1)[0]
  Assert-True -Condition ($null -ne $building) -Message 'Lisboeta indoor building fixture is missing.'

  $detail = Ensure-ApiSuccess -Response (
    Invoke-CurlJson -Method GET -Url "$AdminBaseUrl/api/admin/v1/map/indoor/buildings/$($building.id)" -Token $Token
  ) -Context 'Fetch Lisboeta building detail'

  $floor = @($detail.floors | Where-Object { $_.floorCode -eq '1F' } | Select-Object -First 1)[0]
  Assert-True -Condition ($null -ne $floor) -Message 'Lisboeta 1F floor fixture is missing.'

  return @{
    Building = $detail
    Floor = $floor
  }
}

function Find-RuntimeNode {
  param(
    [Parameter(Mandatory = $true)]$Runtime,
    [Parameter(Mandatory = $true)][string]$MarkerCode
  )

  return @($Runtime.nodes | Where-Object { $_.markerCode -eq $MarkerCode } | Select-Object -First 1)[0]
}

function Find-Behavior {
  param(
    [Parameter(Mandatory = $true)]$Node,
    [Parameter(Mandatory = $true)][string]$BehaviorCode
  )

  return @($Node.behaviors | Where-Object { $_.behaviorCode -eq $BehaviorCode } | Select-Object -First 1)[0]
}

$publicHealth = Ensure-ApiSuccess -Response (Invoke-CurlJson -Method GET -Url "$PublicBaseUrl/api/v1/health") -Context 'Public health'
$adminToken = Get-AdminToken
Ensure-CanonicalWitnessSet
$fixture = Get-LisboetaFloorFixture -Token $adminToken

$building = $fixture.Building
$floor = $fixture.Floor

Write-Host ("Using Lisboeta building {0} floor {1} (id={2})" -f $building.buildingCode, $floor.floorCode, $floor.id)

$runtime = Ensure-ApiSuccess -Response (
  Invoke-CurlJson -Method GET -Url "$PublicBaseUrl/api/v1/indoor/floors/$($floor.id)/runtime?locale=zh-Hant"
) -Context 'Fetch public indoor runtime'

Assert-True -Condition (-not [string]::IsNullOrWhiteSpace($runtime.runtimeVersion)) -Message 'runtimeVersion is missing from the public runtime payload.'
Assert-True -Condition (($runtime.nodes | Measure-Object).Count -gt 0) -Message 'Runtime payload returned no nodes.'

$nightNode = Find-RuntimeNode -Runtime $runtime -MarkerCode '1f-phase15-night-market-overlay'
$royalNode = Find-RuntimeNode -Runtime $runtime -MarkerCode '1f-phase15-royal-palace-dwell'
$zipcityNode = Find-RuntimeNode -Runtime $runtime -MarkerCode '1f-phase15-zipcity-path'

Assert-True -Condition ($null -ne $nightNode) -Message 'night-market runtime node is missing.'
Assert-True -Condition ($null -ne $royalNode) -Message 'royal-palace runtime node is missing.'
Assert-True -Condition ($null -ne $zipcityNode) -Message 'zipcity runtime node is missing.'

$nightBehavior = Find-Behavior -Node $nightNode -BehaviorCode 'night-market-schedule-overlay'
$royalBehavior = Find-Behavior -Node $royalNode -BehaviorCode 'royal-palace-dwell-reveal'
$zipcityBehavior = Find-Behavior -Node $zipcityNode -BehaviorCode 'zipcity-guiding-path'

Assert-True -Condition ($null -ne $nightBehavior) -Message 'night-market-schedule-overlay is missing from runtime.'
Assert-True -Condition ($null -ne $royalBehavior) -Message 'royal-palace-dwell-reveal is missing from runtime.'
Assert-True -Condition ($null -ne $zipcityBehavior) -Message 'zipcity-guiding-path is missing from runtime.'

Assert-True -Condition ($nightNode.name -eq (U('\u591c\u5e02\u6d41\u5149\u6d6e\u5e55'))) -Message 'night-market zh-Hant label mismatch.'
Assert-True -Condition ($royalNode.name -eq (U('\u6d77\u9bae\u822b\u56de\u8072\u85cf\u7ae0'))) -Message 'royal-palace zh-Hant label mismatch.'
Assert-True -Condition ($zipcityNode.name -eq (U('\u98db\u7d22\u5f15\u8def\u5149\u8de1'))) -Message 'zipcity zh-Hant label mismatch.'
Assert-True -Condition ($nightBehavior.name -eq (U('\u591c\u5e02\u958b\u5834\u5149\u5e55'))) -Message 'night-market behavior zh-Hant label mismatch.'
Assert-True -Condition ($royalBehavior.name -eq (U('\u5bb4\u5e2d\u56de\u8072\u85cf\u7ae0'))) -Message 'royal-palace behavior zh-Hant label mismatch.'
Assert-True -Condition ($zipcityBehavior.name -eq (U('\u98db\u7d22\u5f15\u8def\u5149\u8de1'))) -Message 'zipcity behavior zh-Hant label mismatch.'

Assert-True -Condition ($nightBehavior.supported -eq $true) -Message 'night-market-schedule-overlay should be supported.'
Assert-True -Condition ($zipcityBehavior.supported -eq $true) -Message 'zipcity-guiding-path should be supported.'
Assert-True -Condition ($royalBehavior.requiresAuth -eq $true) -Message 'royal-palace-dwell-reveal should require auth.'
Assert-True -Condition (($zipcityBehavior.effectRules | Where-Object { $_.category -eq 'path_motion' } | Measure-Object).Count -ge 1) -Message 'zipcity-guiding-path should include a path_motion effect.'

$zipcityResponse = Ensure-ApiSuccess -Response (
  Invoke-JsonApi -Method POST -Url "$PublicBaseUrl/api/v1/indoor/runtime/interactions" -Body @{
    floorId = [long]$runtime.floorId
    nodeId = [long]$zipcityNode.nodeId
    behaviorId = [long]$zipcityBehavior.behaviorId
    triggerId = 'trigger-zipcity-tap'
    eventType = 'tap'
    relativeX = 0.104
    relativeY = 0.633
    eventTimestamp = '2026-04-17T10:00:00'
    clientSessionId = 'phase17-smoke-zipcity'
  }
) -Context 'Submit zipcity tap interaction'

Assert-True -Condition ($zipcityResponse.interactionAccepted -eq $true) -Message 'zipcity tap interaction should be accepted.'
Assert-True -Condition ($zipcityResponse.matchedTriggerId -eq 'trigger-zipcity-tap') -Message 'zipcity tap matchedTriggerId mismatch.'
Assert-True -Condition (($zipcityResponse.effects | Where-Object { $_.category -eq 'path_motion' } | Measure-Object).Count -ge 1) -Message 'zipcity interaction should return a path_motion effect.'
Assert-True -Condition ($null -ne $zipcityResponse.interactionLogId) -Message 'zipcity interaction should produce an interactionLogId.'

$royalAnonymousResponse = Ensure-ApiSuccess -Response (
  Invoke-JsonApi -Method POST -Url "$PublicBaseUrl/api/v1/indoor/runtime/interactions" -Body @{
    floorId = [long]$runtime.floorId
    nodeId = [long]$royalNode.nodeId
    behaviorId = [long]$royalBehavior.behaviorId
    triggerId = 'trigger-dwell-echo'
    eventType = 'dwell'
    dwellMs = 10000
    clientSessionId = 'phase17-smoke-anonymous'
  }
) -Context 'Submit anonymous royal dwell interaction'

Assert-True -Condition ($royalAnonymousResponse.interactionAccepted -eq $false) -Message 'royal anonymous dwell should be blocked.'
Assert-True -Condition ($royalAnonymousResponse.requiresAuth -eq $true) -Message 'royal anonymous dwell should require auth.'
Assert-True -Condition ($royalAnonymousResponse.blockedReason -eq 'auth_required') -Message 'royal anonymous dwell should return auth_required.'
Assert-True -Condition ($royalAnonymousResponse.matchedTriggerId -eq 'trigger-dwell-echo') -Message 'royal anonymous dwell matchedTriggerId mismatch.'
Assert-True -Condition ($null -ne $royalAnonymousResponse.interactionLogId) -Message 'royal anonymous dwell should produce an interactionLogId.'

$runtimeLogsCheck = Ensure-ApiSuccess -Response (
  Invoke-CurlJson -Method GET -Url "$PublicBaseUrl/api/v1/indoor/floors/$($floor.id)/runtime?locale=zh-Hant"
) -Context 'Re-fetch runtime after interactions'

Assert-True -Condition ($runtimeLogsCheck.floorId -eq $runtime.floorId) -Message 'Runtime re-fetch changed the target floor unexpectedly.'

$repoRoot = Get-RepoRoot
if (-not $SkipServerTests) {
  Invoke-CheckedCommand -Workdir (Join-Path $repoRoot 'packages/server') -Command @('mvn', '-q', '-Dtest=PublicIndoorRuntimeServiceTest,PublicIndoorRuntimeInteractionServiceTest', 'test')
}

if (-not $SkipBuild) {
  Invoke-CheckedCommand -Workdir (Join-Path $repoRoot 'packages/client') -Command @('npm.cmd', 'run', 'build:weapp')
}

[ordered]@{
  publicHealth = $publicHealth.status
  buildingCode = $building.buildingCode
  floorId = $runtime.floorId
  floorCode = $runtime.floorCode
  runtimeVersion = $runtime.runtimeVersion
  showcaseBehaviors = @(
    [ordered]@{ markerCode = $nightNode.markerCode; behaviorCode = $nightBehavior.behaviorCode; supported = $nightBehavior.supported },
    [ordered]@{ markerCode = $royalNode.markerCode; behaviorCode = $royalBehavior.behaviorCode; supported = $royalBehavior.supported; requiresAuth = $royalBehavior.requiresAuth },
    [ordered]@{ markerCode = $zipcityNode.markerCode; behaviorCode = $zipcityBehavior.behaviorCode; supported = $zipcityBehavior.supported }
  )
  zipcityInteraction = [ordered]@{
    accepted = $zipcityResponse.interactionAccepted
    matchedTriggerId = $zipcityResponse.matchedTriggerId
    effectCategories = @($zipcityResponse.effects | ForEach-Object { $_.category })
    interactionLogId = $zipcityResponse.interactionLogId
  }
  royalAnonymousInteraction = [ordered]@{
    accepted = $royalAnonymousResponse.interactionAccepted
    requiresAuth = $royalAnonymousResponse.requiresAuth
    blockedReason = $royalAnonymousResponse.blockedReason
    matchedTriggerId = $royalAnonymousResponse.matchedTriggerId
    interactionLogId = $royalAnonymousResponse.interactionLogId
  }
  checks = [ordered]@{
    serverTests = [bool](-not $SkipServerTests)
    clientBuild = [bool](-not $SkipBuild)
  }
} | ConvertTo-Json -Depth 8
