param(
  [string]$AdminBaseUrl = 'http://127.0.0.1:8081',
  [string]$Username = 'admin',
  [string]$Password = 'admin123',
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
      $jsonPath = Join-Path $env:TEMP ("phase16-smoke-{0}.json" -f ([guid]::NewGuid().ToString('N')))
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

  Write-Host 'Refreshing canonical Lisboa 1F witness set before Phase 16 governance smoke...'
  & powershell.exe -NoProfile -ExecutionPolicy Bypass -File $phase15Script `
    -AdminBaseUrl $AdminBaseUrl `
    -PublicBaseUrl 'http://127.0.0.1:8080' `
    -Username $Username `
    -Password $Password `
    -Scenario seed `
    -SkipTests `
    -SkipBuild

  if ($LASTEXITCODE -ne 0) {
    throw 'Phase 15 witness seed failed before Phase 16 governance smoke.'
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

function Get-LisboetaFixture {
  param([Parameter(Mandatory = $true)][string]$Token)

  $buildingPage = Ensure-ApiSuccess -Response (Invoke-CurlJson -Method GET -Url "$AdminBaseUrl/api/admin/v1/map/indoor/buildings?pageNum=1&pageSize=200&keyword=lisboeta_macau" -Token $Token) -Context 'Fetch indoor building list'
  $building = @($buildingPage.list | Where-Object { $_.buildingCode -eq 'lisboeta_macau' } | Select-Object -First 1)[0]
  Assert-True -Condition ($null -ne $building) -Message 'Lisboeta indoor fixture is missing.'

  $detail = Ensure-ApiSuccess -Response (Invoke-CurlJson -Method GET -Url "$AdminBaseUrl/api/admin/v1/map/indoor/buildings/$($building.id)" -Token $Token) -Context 'Fetch Lisboeta detail'
  $floor = @($detail.floors | Where-Object { $_.floorCode -eq '1F' } | Select-Object -First 1)[0]
  if (-not $floor) {
    $floor = @($detail.floors | Select-Object -First 1)[0]
  }
  Assert-True -Condition ($null -ne $floor) -Message 'Lisboeta indoor floor fixture is missing.'

  return @{
    Building = $detail
    Floor = $floor
  }
}

function Get-FloorNodes {
  param(
    [Parameter(Mandatory = $true)][string]$Token,
    [Parameter(Mandatory = $true)][long]$FloorId
  )

  return Ensure-ApiSuccess -Response (Invoke-CurlJson -Method GET -Url "$AdminBaseUrl/api/admin/v1/map/indoor/floors/$FloorId/nodes" -Token $Token) -Context "Fetch nodes for floor $FloorId"
}

function Convert-NodeToPayload {
  param(
    [Parameter(Mandatory = $true)]$Node,
    [Parameter(Mandatory = $true)]$Behaviors
  )

  return [ordered]@{
    markerCode = $Node.markerCode
    nodeType = $Node.nodeType
    presentationMode = if ($Node.presentationMode) { $Node.presentationMode } else { 'marker' }
    overlayType = $Node.overlayType
    nodeNameZh = $Node.nodeNameZh
    nodeNameEn = $Node.nodeNameEn
    nodeNameZht = $Node.nodeNameZht
    nodeNamePt = $Node.nodeNamePt
    descriptionZh = $Node.descriptionZh
    descriptionEn = $Node.descriptionEn
    descriptionZht = $Node.descriptionZht
    descriptionPt = $Node.descriptionPt
    relativeX = $Node.relativeX
    relativeY = $Node.relativeY
    relatedPoiId = $Node.relatedPoiId
    iconAssetId = $Node.iconAssetId
    animationAssetId = $Node.animationAssetId
    linkedEntityType = $Node.linkedEntityType
    linkedEntityId = $Node.linkedEntityId
    tags = @($Node.tags)
    tagsJson = $Node.tagsJson
    popupConfigJson = $Node.popupConfigJson
    displayConfigJson = $Node.displayConfigJson
    overlayGeometry = $Node.overlayGeometry
    inheritLinkedEntityRules = [bool]$Node.inheritLinkedEntityRules
    runtimeSupportLevel = if ($Node.runtimeSupportLevel) { $Node.runtimeSupportLevel } else { 'phase16_supported' }
    metadataJson = if ($Node.metadataJson) { $Node.metadataJson } else { '{}' }
    behaviors = $Behaviors
    sortOrder = $Node.sortOrder
    status = if ($Node.status) { $Node.status } else { 'draft' }
  }
}

function Upsert-NodePayload {
  param(
    [Parameter(Mandatory = $true)][string]$Token,
    [Parameter(Mandatory = $true)][long]$FloorId,
    [Parameter(Mandatory = $true)][long]$NodeId,
    [Parameter(Mandatory = $true)]$Payload
  )

  $validation = Ensure-ApiSuccess -Response (
    Invoke-JsonApi -Method POST -Url "$AdminBaseUrl/api/admin/v1/map/indoor/nodes/validate-rule-graph?floorId=$FloorId&nodeId=$NodeId" -Token $Token -Body $Payload
  ) -Context "Validate node $($Payload.markerCode)"
  Assert-True -Condition ($validation.valid -eq $true) -Message "Rule validation failed for $($Payload.markerCode)"

  [void](Ensure-ApiSuccess -Response (
    Invoke-JsonApi -Method PUT -Url "$AdminBaseUrl/api/admin/v1/map/indoor/nodes/$NodeId" -Token $Token -Body $Payload
  ) -Context "Update node $($Payload.markerCode)")
}

function Find-BehaviorNode {
  param([Parameter(Mandatory = $true)]$Nodes)
  return @($Nodes | Where-Object { $_.behaviors -and ($_.behaviors | Measure-Object).Count -gt 0 } | Select-Object -First 1)[0]
}

$token = Get-AdminToken
Ensure-CanonicalWitnessSet
$fixture = Get-LisboetaFixture -Token $token
$floorId = [long]$fixture.Floor.id
$nodes = Get-FloorNodes -Token $token -FloorId $floorId
$node = Find-BehaviorNode -Nodes $nodes
Assert-True -Condition ($null -ne $node) -Message 'Lisboeta floor does not contain any behavior-bearing node for Phase 16 smoke.'

$originalBehaviors = @($node.behaviors | ConvertTo-Json -Depth 60 | ConvertFrom-Json)
$existingBehavior = @($originalBehaviors | Select-Object -First 1)[0]
Assert-True -Condition ($null -ne $existingBehavior) -Message 'Target node is missing its first behavior payload.'

$overview = Ensure-ApiSuccess -Response (
  Invoke-CurlJson -Method GET -Url "$AdminBaseUrl/api/admin/v1/map/indoor/rules/overview?buildingId=$($fixture.Building.id)&floorId=$floorId" -Token $token
) -Context 'Fetch rule overview'
$existingOverview = @($overview | Where-Object { $_.behaviorCode -eq $existingBehavior.behaviorCode } | Select-Object -First 1)[0]
Assert-True -Condition ($null -ne $existingOverview) -Message 'Existing behavior is missing from governance overview.'
Assert-True -Condition ($null -ne $existingOverview.behaviorId) -Message 'Governance overview did not return behaviorId.'
Assert-True -Condition (-not [string]::IsNullOrWhiteSpace($existingOverview.behaviorCode)) -Message 'Governance overview did not return behaviorCode.'
Assert-True -Condition ($null -ne $existingOverview.runtimeSupportLevel) -Message 'Governance overview did not return runtimeSupportLevel.'
Assert-True -Condition ($null -ne $existingOverview.conflictCount) -Message 'Governance overview did not return conflictCount.'

$existingDetail = Ensure-ApiSuccess -Response (
  Invoke-CurlJson -Method GET -Url "$AdminBaseUrl/api/admin/v1/map/indoor/rules/behaviors/$($existingOverview.behaviorId)" -Token $token
) -Context 'Fetch rule detail'
Assert-True -Condition ($existingDetail.behaviorId -eq $existingOverview.behaviorId) -Message 'Governance detail behaviorId mismatch.'

$conflicts = Ensure-ApiSuccess -Response (
  Invoke-CurlJson -Method GET -Url "$AdminBaseUrl/api/admin/v1/map/indoor/rules/conflicts?buildingId=$($fixture.Building.id)&floorId=$floorId" -Token $token
) -Context 'Fetch rule conflicts'
Assert-True -Condition ($null -ne $conflicts) -Message 'Governance conflicts endpoint returned null.'

$timestamp = Get-Date -Format 'yyyyMMddHHmmss'
$smokeBehaviorCode = "phase16-smoke-$timestamp"
$smokeBehaviorName = "Phase 16 Smoke Verification $timestamp"
$smokeBehavior = [ordered]@{
  behaviorCode = $smokeBehaviorCode
  behaviorNameZh = $smokeBehaviorName
  behaviorNameZht = $smokeBehaviorName
  behaviorNameEn = "Phase 16 Smoke $timestamp"
  behaviorNamePt = "Fumo da Fase 16 $timestamp"
  appearancePresetCode = 'always-on-smoke'
  triggerTemplateCode = 'tap-smoke'
  effectTemplateCode = 'popup-smoke'
  appearanceRules = @(
    [ordered]@{
      id = "appearance-$timestamp"
      category = 'always_on'
      label = 'Smoke always on'
      config = @{ note = 'phase16 smoke appearance' }
    }
  )
  triggerRules = @(
    [ordered]@{
      id = "trigger-$timestamp"
      category = 'tap'
      label = 'Smoke tap'
      config = @{ targetHint = 'tap the smoke marker' }
    }
  )
  effectRules = @(
    [ordered]@{
      id = "effect-$timestamp"
      category = 'popup'
      label = 'Smoke popup'
      config = @{ title = 'Phase 16 Smoke'; body = 'Verify staged workbench changes' }
    }
  )
  pathGraph = $null
  inheritMode = 'override'
  runtimeSupportLevel = 'phase16_supported'
  sortOrder = @($originalBehaviors).Count
  status = 'draft'
}

$nextBehaviors = @($originalBehaviors) + $smokeBehavior
$updatePayload = Convert-NodeToPayload -Node $node -Behaviors $nextBehaviors

try {
  Upsert-NodePayload -Token $token -FloorId $floorId -NodeId $node.id -Payload $updatePayload

  $updatedNode = @(
    (Get-FloorNodes -Token $token -FloorId $floorId) |
    Where-Object { $_.id -eq $node.id } |
    Select-Object -First 1
  )[0]
  Assert-True -Condition ($null -ne $updatedNode) -Message 'Updated node could not be reloaded.'
  $storedSmokeBehavior = @($updatedNode.behaviors | Where-Object { $_.behaviorCode -eq $smokeBehaviorCode } | Select-Object -First 1)[0]
  Assert-True -Condition ($null -ne $storedSmokeBehavior) -Message 'Smoke behavior was not stored on the node.'
  Assert-True -Condition ($storedSmokeBehavior.behaviorNameZht -eq $smokeBehaviorName) -Message 'behaviorNameZht did not round-trip through node authoring.'
  Assert-True -Condition ([int]$storedSmokeBehavior.sortOrder -eq [int]$smokeBehavior.sortOrder) -Message 'sortOrder did not round-trip through node authoring.'

  $smokeOverview = Ensure-ApiSuccess -Response (
    Invoke-CurlJson -Method GET -Url "$AdminBaseUrl/api/admin/v1/map/indoor/rules/overview?buildingId=$($fixture.Building.id)&floorId=$floorId&keyword=$smokeBehaviorCode" -Token $token
  ) -Context 'Fetch smoke overview'
  $smokeRow = @($smokeOverview | Where-Object { $_.behaviorCode -eq $smokeBehaviorCode } | Select-Object -First 1)[0]
  Assert-True -Condition ($null -ne $smokeRow) -Message 'Smoke behavior is missing from governance overview.'
  Assert-True -Condition ($smokeRow.behaviorNameZht -eq $smokeBehaviorName) -Message 'Governance overview did not return the updated behaviorNameZht.'
  Assert-True -Condition ($smokeRow.status -eq 'draft') -Message 'Governance overview did not return the draft status for the smoke behavior.'
  Assert-True -Condition ($null -ne $smokeRow.conflictCount) -Message 'Governance overview did not return conflictCount for the smoke behavior.'

  $smokeDetail = Ensure-ApiSuccess -Response (
    Invoke-CurlJson -Method GET -Url "$AdminBaseUrl/api/admin/v1/map/indoor/rules/behaviors/$($smokeRow.behaviorId)" -Token $token
  ) -Context 'Fetch smoke detail'
  Assert-True -Condition ($smokeDetail.behaviorNameZht -eq $smokeBehaviorName) -Message 'Governance detail did not return the updated behaviorNameZht.'

  $patchedStatus = Ensure-ApiSuccess -Response (
    Invoke-JsonApi -Method PATCH -Url "$AdminBaseUrl/api/admin/v1/map/indoor/rules/behaviors/$($smokeRow.behaviorId)/status" -Token $token -Body @{ status = 'disabled' }
  ) -Context 'Patch smoke behavior status'
  Assert-True -Condition ($patchedStatus.behaviorId -eq $smokeRow.behaviorId) -Message 'Status patch did not return the expected behaviorId.'
  Assert-True -Condition ($patchedStatus.status -eq 'disabled') -Message 'Status patch did not return the disabled status.'

  $smokeDetailAfterPatch = Ensure-ApiSuccess -Response (
    Invoke-CurlJson -Method GET -Url "$AdminBaseUrl/api/admin/v1/map/indoor/rules/behaviors/$($smokeRow.behaviorId)" -Token $token
  ) -Context 'Fetch smoke detail after patch'
  Assert-True -Condition ($smokeDetailAfterPatch.status -eq 'disabled') -Message 'Governance detail did not reflect the patched status.'

  if (-not $SkipBuild) {
    Invoke-CheckedCommand -Workdir (Join-Path (Get-RepoRoot) 'packages/admin/aoxiaoyou-admin-ui') -Command @('npm.cmd', 'run', 'build')
  }

  Write-Host ''
  Write-Host 'Phase 16 indoor rule governance smoke passed.' -ForegroundColor Green
  Write-Host ("Building: {0}  Floor: {1}" -f $fixture.Building.buildingCode, $fixture.Floor.floorCode)
  Write-Host ("Smoke behavior: {0}  behaviorId: {1}" -f $smokeBehaviorCode, $smokeRow.behaviorId)
} finally {
  $restorePayload = Convert-NodeToPayload -Node $node -Behaviors $originalBehaviors
  Upsert-NodePayload -Token $token -FloorId $floorId -NodeId $node.id -Payload $restorePayload
}
