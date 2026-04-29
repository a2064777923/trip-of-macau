param(
  [string]$AdminBaseUrl = 'http://127.0.0.1:8081',
  [string]$PublicBaseUrl = 'http://127.0.0.1:8080',
  [string]$Username = 'admin',
  [string]$Password = 'admin123',
  [ValidateSet('author', 'seed')][string]$Scenario = 'author',
  [switch]$SkipTests,
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
    $Content = $Content | ConvertTo-Json -Depth 40
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
    [string]$JsonPath,
    [string[]]$FormFields
  )

  $arguments = @('-sS', '-X', $Method, $Url)
  if ($Token) {
    $arguments += @('-H', "Authorization: Bearer $Token")
  }
  if ($JsonPath) {
    $arguments += @('-H', 'Content-Type: application/json; charset=utf-8', '--data-binary', "@$JsonPath")
  }
  if ($FormFields) {
    foreach ($field in $FormFields) {
      $arguments += @('-F', $field)
    }
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
      $jsonPath = Join-Path $env:TEMP ("phase15-authoring-{0}.json" -f ([guid]::NewGuid().ToString('N')))
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

function Get-AdminToken {
  $loginBody = @{ username = $Username; password = $Password }
  $response = Invoke-JsonApi -Method POST -Url "$AdminBaseUrl/api/admin/v1/auth/login" -Body $loginBody
  $data = Ensure-ApiSuccess -Response $response -Context 'Admin login'
  if (-not $data.token) {
    throw 'Admin login did not return a token.'
  }
  return $data.token
}

function Ensure-LisboetaSeed {
  param([Parameter(Mandatory = $true)][string]$Token)

  $buildingList = Ensure-ApiSuccess -Response (Invoke-CurlJson -Method GET -Url "$AdminBaseUrl/api/admin/v1/map/indoor/buildings?pageNum=1&pageSize=200&keyword=lisboeta_macau" -Token $Token) -Context 'Fetch indoor buildings'
  $building = @($buildingList.list | Where-Object { $_.buildingCode -eq 'lisboeta_macau' } | Select-Object -First 1)[0]

  if (-not $building) {
    $repoRoot = Get-RepoRoot
    $seedScript = Join-Path $repoRoot 'scripts/local/seed-lisboeta-indoor.ps1'
    if (-not (Test-Path $seedScript)) {
      throw "Cannot find Lisboeta seed script at $seedScript"
    }
    Write-Host 'Lisboeta indoor baseline not found. Running seed-lisboeta-indoor.ps1...'
    & powershell.exe -ExecutionPolicy Bypass -File $seedScript -AdminBaseUrl $AdminBaseUrl -PublicBaseUrl $PublicBaseUrl -Username $Username -Password $Password
    if ($LASTEXITCODE -ne 0) {
      throw 'seed-lisboeta-indoor.ps1 failed.'
    }
    $buildingList = Ensure-ApiSuccess -Response (Invoke-CurlJson -Method GET -Url "$AdminBaseUrl/api/admin/v1/map/indoor/buildings?pageNum=1&pageSize=200&keyword=lisboeta_macau" -Token $Token) -Context 'Refetch Lisboeta building'
    $building = @($buildingList.list | Where-Object { $_.buildingCode -eq 'lisboeta_macau' } | Select-Object -First 1)[0]
  }

  if (-not $building) {
    throw 'Lisboeta indoor building is still unavailable after seeding.'
  }

  $buildingDetail = Ensure-ApiSuccess -Response (Invoke-CurlJson -Method GET -Url "$AdminBaseUrl/api/admin/v1/map/indoor/buildings/$($building.id)" -Token $Token) -Context 'Fetch Lisboeta building detail'
  $floor = @($buildingDetail.floors | Where-Object { $_.floorCode -eq '1F' } | Select-Object -First 1)[0]
  if (-not $floor) {
    throw 'Lisboeta 1F floor is unavailable.'
  }

  return @{
    Building = $buildingDetail
    Floor = $floor
  }
}

function Get-FloorNodes {
  param(
    [Parameter(Mandatory = $true)][string]$Token,
    [Parameter(Mandatory = $true)][long]$FloorId
  )

  return Ensure-ApiSuccess -Response (Invoke-CurlJson -Method GET -Url "$AdminBaseUrl/api/admin/v1/map/indoor/floors/$FloorId/nodes" -Token $Token) -Context "Fetch indoor nodes for floor $FloorId"
}

function Find-NodeByCode {
  param(
    [Parameter(Mandatory = $true)]$Nodes,
    [Parameter(Mandatory = $true)][string]$MarkerCode
  )

  return @($Nodes | Where-Object { $_.markerCode -eq $MarkerCode } | Select-Object -First 1)[0]
}

function Upsert-StructuredNode {
  param(
    [Parameter(Mandatory = $true)][string]$Token,
    [Parameter(Mandatory = $true)][long]$FloorId,
    [Parameter(Mandatory = $true)]$Payload
  )

  $existingNode = Find-NodeByCode -Nodes (Get-FloorNodes -Token $Token -FloorId $FloorId) -MarkerCode $Payload.markerCode
  $validateUrl = if ($existingNode) {
    "$AdminBaseUrl/api/admin/v1/map/indoor/nodes/validate-rule-graph?floorId=$FloorId&nodeId=$($existingNode.id)"
  } else {
    "$AdminBaseUrl/api/admin/v1/map/indoor/nodes/validate-rule-graph?floorId=$FloorId"
  }
  $validateData = Ensure-ApiSuccess -Response (Invoke-JsonApi -Method POST -Url $validateUrl -Token $Token -Body $Payload) -Context "Validate node $($Payload.markerCode)"
  Assert-True -Condition ($validateData.valid -eq $true) -Message "Rule validation did not pass for $($Payload.markerCode)"
  Assert-True -Condition ([int]$validateData.behaviorCount -eq [int]$Payload.behaviors.Count) -Message "Validation behavior count mismatch for $($Payload.markerCode)"

  if ($existingNode) {
    [void](Ensure-ApiSuccess -Response (Invoke-JsonApi -Method PUT -Url "$AdminBaseUrl/api/admin/v1/map/indoor/nodes/$($existingNode.id)" -Token $Token -Body $Payload) -Context "Update node $($Payload.markerCode)")
  } else {
    [void](Ensure-ApiSuccess -Response (Invoke-JsonApi -Method POST -Url "$AdminBaseUrl/api/admin/v1/map/indoor/floors/$FloorId/nodes" -Token $Token -Body $Payload) -Context "Create node $($Payload.markerCode)")
  }

  $storedNode = Find-NodeByCode -Nodes (Get-FloorNodes -Token $Token -FloorId $FloorId) -MarkerCode $Payload.markerCode
  if (-not $storedNode) {
    throw "Node $($Payload.markerCode) was not found after upsert."
  }
  return $storedNode
}

function Assert-BehaviorRoundTrip {
  param(
    [Parameter(Mandatory = $true)]$StoredNode,
    [Parameter(Mandatory = $true)]$ExpectedPayload
  )

  Assert-True -Condition ($StoredNode.presentationMode -eq $ExpectedPayload.presentationMode) -Message "presentationMode mismatch for $($ExpectedPayload.markerCode)"
  Assert-True -Condition ([string]$StoredNode.overlayType -eq [string]$ExpectedPayload.overlayType) -Message "overlayType mismatch for $($ExpectedPayload.markerCode)"
  Assert-True -Condition ([string]$StoredNode.runtimeSupportLevel -eq [string]$ExpectedPayload.runtimeSupportLevel) -Message "runtimeSupportLevel mismatch for $($ExpectedPayload.markerCode)"
  Assert-True -Condition (($StoredNode.behaviors | Measure-Object).Count -eq ($ExpectedPayload.behaviors | Measure-Object).Count) -Message "Behavior profile count mismatch for $($ExpectedPayload.markerCode)"

  $storedBehavior = @($StoredNode.behaviors | Select-Object -First 1)[0]
  $expectedBehavior = @($ExpectedPayload.behaviors | Select-Object -First 1)[0]
  Assert-True -Condition ($storedBehavior.behaviorCode -eq $expectedBehavior.behaviorCode) -Message "behaviorCode mismatch for $($ExpectedPayload.markerCode)"
  Assert-True -Condition ($storedBehavior.appearanceRules[0].category -eq $expectedBehavior.appearanceRules[0].category) -Message "appearanceRules mismatch for $($ExpectedPayload.markerCode)"
  Assert-True -Condition ($storedBehavior.triggerRules[0].category -eq $expectedBehavior.triggerRules[0].category) -Message "triggerRules mismatch for $($ExpectedPayload.markerCode)"
  Assert-True -Condition (($storedBehavior.effectRules | Where-Object { $_.category -eq 'path_motion' } | Measure-Object).Count -ge (($expectedBehavior.effectRules | Where-Object { $_.category -eq 'path_motion' } | Measure-Object).Count)) -Message "path_motion effect mismatch for $($ExpectedPayload.markerCode)"
  if ($null -eq $expectedBehavior.pathGraph) {
    Assert-True -Condition ($null -eq $storedBehavior.pathGraph -or ($storedBehavior.pathGraph.points | Measure-Object).Count -eq 0) -Message "pathGraph should be empty for $($ExpectedPayload.markerCode)"
  } else {
    Assert-True -Condition (($storedBehavior.pathGraph.points | Measure-Object).Count -eq ($expectedBehavior.pathGraph.points | Measure-Object).Count) -Message "pathGraph points mismatch for $($ExpectedPayload.markerCode)"
    Assert-True -Condition ([int]$storedBehavior.pathGraph.durationMs -eq [int]$expectedBehavior.pathGraph.durationMs) -Message "pathGraph duration mismatch for $($ExpectedPayload.markerCode)"
  }
}

function New-ShowcasePayloads {
  return @(
    [ordered]@{
      markerCode = '1f-phase15-night-market-overlay'
      nodeType = 'landmark'
      presentationMode = 'overlay'
      overlayType = 'polygon'
      nodeNameZh = U('\u591c\u5e02\u6d41\u5149\u6d6e\u5e55')
      nodeNameEn = 'Night Market Luminous Veil'
      nodeNameZht = U('\u591c\u5e02\u6d41\u5149\u6d6e\u5e55')
      nodeNamePt = 'Veu Luminoso do Mercado Noturno'
      descriptionZh = U('\u5f53\u66ae\u8272\u843d\u5728\u8461\u4eac\u4eba\u591c\u5e02\uff0c\u5149\u5e55\u4f1a\u6cbf\u7740\u644a\u6863\u8fb9\u754c\u4eae\u8d77\uff0c\u63d0\u9192\u65c5\u4eba\u8fd9\u4e00\u6bb5\u6545\u4e8b\u53ea\u5728\u591c\u91cc\u82cf\u9192\u3002')
      descriptionEn = 'A luminous overlay wakes along the night market boundary after dusk.'
      descriptionZht = U('\u7576\u66ae\u8272\u843d\u5728\u8461\u4eac\u4eba\u591c\u5e02\uff0c\u5149\u5e55\u6703\u6cbf\u8457\u6524\u6a94\u908a\u754c\u4eae\u8d77\uff0c\u63d0\u9192\u65c5\u4eba\u9019\u4e00\u6bb5\u6545\u4e8b\u53ea\u5728\u591c\u88e1\u7526\u9192\u3002')
      descriptionPt = 'Ao cair da noite, um veu luminoso desperta ao longo do mercado e conduz o visitante para a narrativa noturna.'
      relativeX = 0.249
      relativeY = 0.634
      tags = @('phase15', 'showcase', 'night-market', 'overlay')
      popupConfigJson = U('{"enabled":true,"mode":"sheet","title":"\u591c\u5e02\u5149\u5e55"}')
      displayConfigJson = '{"labelMode":"hover","showPulse":false,"theme":"night-market"}'
      overlayGeometry = @{
        geometryType = 'polygon'
        points = @(
          @{ x = 0.175; y = 0.585; order = 0 },
          @{ x = 0.316; y = 0.585; order = 1 },
          @{ x = 0.332; y = 0.680; order = 2 },
          @{ x = 0.196; y = 0.704; order = 3 }
        )
        properties = @{ label = U('\u591c\u5e02\u6d41\u5149\u5340'); theme = 'golden-night' }
      }
      inheritLinkedEntityRules = $false
      runtimeSupportLevel = 'phase16_supported'
      metadataJson = '{"showcase":"phase15","scene":"night_market_overlay"}'
      sortOrder = 9101
      status = 'published'
      behaviors = @(
        @{
          behaviorCode = 'night-market-schedule-overlay'
          behaviorNameZh = U('\u591c\u5e02\u5f00\u573a\u5149\u5e55')
          behaviorNameEn = 'Night Market Opening Veil'
          behaviorNameZht = U('\u591c\u5e02\u958b\u5834\u5149\u5e55')
          behaviorNamePt = 'Abertura Luminosa do Mercado'
          appearancePresetCode = 'night-market-schedule'
          triggerTemplateCode = 'proximity'
          effectTemplateCode = 'popup'
          appearanceRules = @(
            @{ id = 'appearance-night'; category = 'schedule_window'; label = U('\u591c\u9593\u6642\u6bb5'); config = @{ startAt = '19:00'; endAt = '23:30' } }
          )
          triggerRules = @(
            @{ id = 'trigger-night-near'; category = 'proximity'; label = U('\u9760\u8fd1\u591c\u5e02\u5165\u53e3'); config = @{ radiusMeters = 35; targetCode = 'night-market-gate' } }
          )
          effectRules = @(
            @{ id = 'effect-night-popup'; category = 'popup'; label = U('\u986f\u793a\u591c\u5e02\u6d6e\u5e55\u4ecb\u7d39'); config = @{ title = U('\u591c\u5e02\u6d41\u5149\u6d6e\u5e55'); body = U('\u66ae\u8272\u4eae\u8d77\u5f8c\uff0c\u6524\u6a94\u908a\u754c\u6703\u6210\u70ba\u6545\u4e8b\u821e\u53f0\u3002') } }
          )
          pathGraph = $null
          inheritMode = 'override'
          runtimeSupportLevel = 'phase16_supported'
          sortOrder = 0
          status = 'published'
        }
      )
    }
    [ordered]@{
      markerCode = '1f-phase15-royal-palace-dwell'
      nodeType = 'landmark'
      presentationMode = 'marker'
      overlayType = $null
      nodeNameZh = U('\u6d77\u9c9c\u822b\u56de\u58f0\u85cf\u7ae0')
      nodeNameEn = 'Royal Palace Echo Token'
      nodeNameZht = U('\u6d77\u9bae\u822b\u56de\u8072\u85cf\u7ae0')
      nodeNamePt = 'Eco do Royal Palace'
      descriptionZh = U('\u65c5\u4eba\u82e5\u5728\u7687\u5bb6\u6d77\u9c9c\u822b\u524d\u9759\u7acb\u7247\u523b\uff0c\u65e7\u65e5\u5bb4\u5e2d\u7684\u56de\u58f0\u4fbf\u4f1a\u6362\u6765\u4e00\u679a\u6545\u4e8b\u85cf\u7ae0\u3002')
      descriptionEn = 'Pause by Royal Palace and the banquet hall echo answers with a collectible story token.'
      descriptionZht = U('\u65c5\u4eba\u82e5\u5728\u7687\u5bb6\u6d77\u9bae\u822b\u524d\u975c\u7acb\u7247\u523b\uff0c\u820a\u65e5\u5bb4\u5e2d\u7684\u56de\u8072\u4fbf\u6703\u63db\u4f86\u4e00\u679a\u6545\u4e8b\u85cf\u7ae0\u3002')
      descriptionPt = 'Se o visitante permanecer por alguns instantes diante do Royal Palace, um eco narrativo concede um selo colecionavel.'
      relativeX = 0.272
      relativeY = 0.796
      tags = @('phase15', 'showcase', 'dwell', 'collectible')
      popupConfigJson = U('{"enabled":true,"mode":"popup","title":"\u6d77\u9bae\u822b\u56de\u8072"}')
      displayConfigJson = '{"labelMode":"always","showPulse":true,"theme":"royal-palace"}'
      inheritLinkedEntityRules = $false
      runtimeSupportLevel = 'phase16_supported'
      metadataJson = '{"showcase":"phase15","scene":"royal_palace_dwell"}'
      sortOrder = 9102
      status = 'published'
      behaviors = @(
        @{
          behaviorCode = 'royal-palace-dwell-reveal'
          behaviorNameZh = U('\u5bb4\u5e2d\u56de\u58f0\u85cf\u7ae0')
          behaviorNameEn = 'Banquet Echo Token'
          behaviorNameZht = U('\u5bb4\u5e2d\u56de\u8072\u85cf\u7ae0')
          behaviorNamePt = 'Selo do Eco do Banquete'
          appearancePresetCode = 'royal-palace-dwell'
          triggerTemplateCode = 'dwell'
          effectTemplateCode = 'collectible_grant'
          appearanceRules = @(
            @{ id = 'appearance-always'; category = 'always_on'; label = U('\u5e38\u99d0\u986f\u793a'); config = @{ note = U('\u7b49\u5f85\u65c5\u4eba\u99d0\u8db3') } }
          )
          triggerRules = @(
            @{ id = 'trigger-dwell-echo'; category = 'dwell'; label = U('\u505c\u7559\u8046\u807d\u56de\u8072'); config = @{ seconds = 10 } }
          )
          effectRules = @(
            @{ id = 'effect-echo-popup'; category = 'popup'; label = U('\u986f\u793a\u5bb4\u5e2d\u56de\u8072'); config = @{ title = U('\u6d77\u9bae\u822b\u56de\u8072'); body = U('\u4f60\u5728\u6b64\u505c\u7559\u5f97\u8db3\u5920\u4e45\uff0c\u4fbf\u807d\u898b\u5f80\u65e5\u5bb4\u5e2d\u7684\u4f4e\u8a9e\u3002') } },
            @{ id = 'effect-echo-collectible'; category = 'collectible_grant'; label = U('\u767c\u653e\u6545\u4e8b\u85cf\u7ae0'); config = @{ entityId = 1; quantity = 1 } }
          )
          pathGraph = $null
          inheritMode = 'override'
          runtimeSupportLevel = 'phase16_supported'
          sortOrder = 0
          status = 'published'
        }
      )
    }
    [ordered]@{
      markerCode = '1f-phase15-zipcity-path'
      nodeType = 'landmark'
      presentationMode = 'hybrid'
      overlayType = 'polyline'
      nodeNameZh = U('\u98de\u7d22\u5f15\u8def\u5149\u8ff9')
      nodeNameEn = 'Zipcity Guiding Trail'
      nodeNameZht = U('\u98db\u7d22\u5f15\u8def\u5149\u8de1')
      nodeNamePt = 'Rasto Guia da Zipcity'
      descriptionZh = U('\u5728\u98de\u7d22\u5165\u53e3\u89e6\u53d1\u7b2c\u4e00\u9053\u5149\u70b9\u540e\uff0c\u5149\u8ff9\u4f1a\u6cbf\u7740\u4e2d\u5ead\u8fb9\u7ebf\u7f13\u7f13\u6e38\u8d70\uff0c\u5e26\u73a9\u5bb6\u8d70\u5411\u4e0b\u4e00\u6bb5\u4e92\u52a8\u3002')
      descriptionEn = 'Once the first point is tapped at the Zipcity entrance, a guiding trail glides along the atrium edge toward the next interaction.'
      descriptionZht = U('\u5728\u98db\u7d22\u5165\u53e3\u89f8\u767c\u7b2c\u4e00\u9053\u5149\u9ede\u5f8c\uff0c\u5149\u8de1\u6703\u6cbf\u8457\u4e2d\u5ead\u908a\u7dda\u7de9\u7de9\u6e38\u8d70\uff0c\u5e36\u73a9\u5bb6\u8d70\u5411\u4e0b\u4e00\u6bb5\u4e92\u52d5\u3002')
      descriptionPt = 'Depois do primeiro toque na entrada da Zipcity, um rasto luminoso percorre lentamente o atrio e conduz ao proximo passo.'
      relativeX = 0.104
      relativeY = 0.633
      tags = @('phase15', 'showcase', 'path-motion', 'chain')
      popupConfigJson = U('{"enabled":true,"mode":"bubble","title":"\u98db\u7d22\u5f15\u8def"}')
      displayConfigJson = '{"labelMode":"always","showPulse":true,"theme":"zipcity"}'
      overlayGeometry = @{
        geometryType = 'polyline'
        points = @(
          @{ x = 0.104; y = 0.633; order = 0 },
          @{ x = 0.173; y = 0.604; order = 1 },
          @{ x = 0.238; y = 0.595; order = 2 },
          @{ x = 0.306; y = 0.586; order = 3 }
        )
        properties = @{ label = U('\u5149\u8de1\u6295\u5f71'); style = 'guiding-trail' }
      }
      inheritLinkedEntityRules = $true
      runtimeSupportLevel = 'phase16_supported'
      metadataJson = '{"showcase":"phase15","scene":"zipcity_path_chain"}'
      sortOrder = 9103
      status = 'published'
      behaviors = @(
        @{
          behaviorCode = 'zipcity-guiding-path'
          behaviorNameZh = U('\u98de\u7d22\u5f15\u8def\u5149\u8ff9')
          behaviorNameEn = 'Zipcity Guiding Trail'
          behaviorNameZht = U('\u98db\u7d22\u5f15\u8def\u5149\u8de1')
          behaviorNamePt = 'Rasto Guia da Zipcity'
          appearancePresetCode = 'zipcity-guiding-path'
          triggerTemplateCode = 'tap-chain'
          effectTemplateCode = 'path-motion'
          appearanceRules = @(
            @{ id = 'appearance-manual'; category = 'manual'; label = U('\u9ede\u4eae\u5f8c\u555f\u52d5'); config = @{ note = U('\u7b49\u5f85\u7b2c\u4e00\u9053\u89f8\u767c') } }
          )
          triggerRules = @(
            @{ id = 'trigger-zipcity-tap'; category = 'tap'; label = U('\u9ede\u64ca\u98db\u7d22\u5165\u53e3'); config = @{ targetHint = U('\u5165\u53e3\u5149\u9ede') } },
            @{ id = 'trigger-zipcity-follow'; category = 'proximity'; label = U('\u8ddf\u96a8\u5149\u8de1\u524d\u884c'); dependsOnTriggerId = 'trigger-zipcity-tap'; config = @{ radiusMeters = 18; targetCode = 'zipcity-trail-end' } }
          )
          effectRules = @(
            @{ id = 'effect-zipcity-path'; category = 'path_motion'; label = U('\u64ad\u653e\u5f15\u8def\u5149\u8de1'); config = @{ trail = 'zipcity-guiding' } },
            @{ id = 'effect-zipcity-popup'; category = 'popup'; label = U('\u63d0\u793a\u4e0b\u4e00\u7ad9'); config = @{ title = U('\u8ddf\u8457\u5149\u8de1\u524d\u884c'); body = U('\u6cbf\u8457\u5149\u8def\u8d70\uff0c\u4e0b\u4e00\u500b\u4e92\u52d5\u6703\u5728\u4e2d\u5ead\u908a\u7dda\u9192\u4f86\u3002') } }
          )
          pathGraph = @{
            points = @(
              @{ x = 0.104; y = 0.633; order = 0 },
              @{ x = 0.158; y = 0.612; order = 1 },
              @{ x = 0.226; y = 0.602; order = 2 },
              @{ x = 0.306; y = 0.586; order = 3 }
            )
            durationMs = 3600
            holdMs = 600
            loop = $false
            easing = 'ease-in-out'
          }
          inheritMode = 'append'
          runtimeSupportLevel = 'phase16_supported'
          sortOrder = 0
          status = 'published'
        }
      )
    }
  )
}

$publicHealth = Invoke-RestMethod -Uri "$PublicBaseUrl/api/v1/health" -Method Get
if ($publicHealth.code -ne 0) {
  throw 'Public server health check failed.'
}

$token = Get-AdminToken
$baseline = Ensure-LisboetaSeed -Token $token
$floor = $baseline.Floor

Write-Host ("Using Lisboeta floor {0} ({1}) for Phase 15 smoke." -f $floor.id, $floor.floorCode)

if ($Scenario -eq 'seed') {
  $seedResults = foreach ($payload in (New-ShowcasePayloads)) {
    $storedNode = Upsert-StructuredNode -Token $token -FloorId $floor.id -Payload $payload
    Assert-BehaviorRoundTrip -StoredNode $storedNode -ExpectedPayload $payload
    [pscustomobject]@{
      markerCode = $storedNode.markerCode
      nodeId = $storedNode.id
      presentationMode = $storedNode.presentationMode
      behaviorCount = ($storedNode.behaviors | Measure-Object).Count
    }
  }

  Write-Host 'Phase 15 showcase seed scenarios have been authored successfully:'
  $seedResults | Format-Table -AutoSize | Out-String | Write-Host
  exit 0
}

$smokePayload = [ordered]@{
  markerCode = '1f-phase15-smoke-roundtrip'
  nodeType = 'landmark'
  presentationMode = 'hybrid'
  overlayType = 'polyline'
  nodeNameZh = 'Phase 15 Smoke Trail'
  nodeNameEn = 'Phase 15 Smoke Trail'
  nodeNameZht = 'Phase 15 Smoke Trail'
  nodeNamePt = 'Trilho de Smoke da Fase 15'
  descriptionZh = 'Used to verify structured indoor rule persistence through the admin APIs.'
  descriptionEn = 'Used to verify that structured indoor rule graphs can persist and round-trip through the admin APIs.'
  descriptionZht = 'Used to verify structured indoor rule persistence through the admin APIs.'
  descriptionPt = 'Usado para validar a persistencia e leitura de regras estruturadas indoor pela API administrativa.'
  relativeX = 0.352
  relativeY = 0.622
  tags = @('phase15', 'smoke', 'roundtrip')
  popupConfigJson = '{"enabled":true,"mode":"sheet","title":"Phase 15 Smoke"}'
  displayConfigJson = '{"labelMode":"always","showPulse":true,"theme":"phase15-smoke"}'
  overlayGeometry = @{
    geometryType = 'polyline'
    points = @(
      @{ x = 0.352; y = 0.622; order = 0 },
      @{ x = 0.396; y = 0.604; order = 1 },
      @{ x = 0.438; y = 0.594; order = 2 }
    )
    properties = @{ label = 'Smoke Trail'; style = 'verification' }
  }
  inheritLinkedEntityRules = $false
  runtimeSupportLevel = 'phase15_storage_only'
  metadataJson = '{"smoke":"phase15","purpose":"roundtrip"}'
  sortOrder = 9901
  status = 'published'
  behaviors = @(
    @{
      behaviorCode = 'phase15-smoke-rule'
      behaviorNameZh = 'Smoke Verification Rule'
      behaviorNameEn = 'Smoke Verification Rule'
      behaviorNameZht = 'Smoke Verification Rule'
      behaviorNamePt = 'Regra de Verificacao'
      appearancePresetCode = 'smoke-schedule'
      triggerTemplateCode = 'tap-chain'
      effectTemplateCode = 'path-motion'
      appearanceRules = @(
        @{ id = 'appearance-smoke'; category = 'schedule_window'; label = 'Operating window'; config = @{ startAt = '10:00'; endAt = '22:00' } }
      )
      triggerRules = @(
        @{ id = 'trigger-smoke-tap'; category = 'tap'; label = 'Tap the start point'; config = @{ targetHint = 'Smoke Start' } },
        @{ id = 'trigger-smoke-follow'; category = 'proximity'; label = 'Approach the end point'; dependsOnTriggerId = 'trigger-smoke-tap'; config = @{ radiusMeters = 15; targetCode = 'phase15-smoke-end' } }
      )
      effectRules = @(
        @{ id = 'effect-smoke-popup'; category = 'popup'; label = 'Show prompt'; config = @{ title = 'Phase 15 Smoke'; body = 'This node proves structured authoring round-trip.' } },
        @{ id = 'effect-smoke-path'; category = 'path_motion'; label = 'Play motion path'; config = @{ trail = 'phase15-smoke' } }
      )
      pathGraph = @{
        points = @(
          @{ x = 0.352; y = 0.622; order = 0 },
          @{ x = 0.386; y = 0.608; order = 1 },
          @{ x = 0.421; y = 0.600; order = 2 },
          @{ x = 0.458; y = 0.590; order = 3 }
        )
        durationMs = 3200
        holdMs = 500
        loop = $false
        easing = 'ease-in-out'
      }
      inheritMode = 'append'
      runtimeSupportLevel = 'phase15_storage_only'
      sortOrder = 0
      status = 'published'
    }
  )
}

$storedSmokeNode = Upsert-StructuredNode -Token $token -FloorId $floor.id -Payload $smokePayload
Assert-BehaviorRoundTrip -StoredNode $storedSmokeNode -ExpectedPayload $smokePayload

Write-Host ("Structured indoor authoring round-trip passed for node {0} (id={1})." -f $storedSmokeNode.markerCode, $storedSmokeNode.id)

$repoRoot = Get-RepoRoot
if (-not $SkipTests) {
  Invoke-CheckedCommand -Workdir (Join-Path $repoRoot 'packages/admin/aoxiaoyou-admin-backend') -Command @('mvn', '-q', '-Dtest=IndoorRuleAuthoringServiceTest', 'test')
}

if (-not $SkipBuild) {
  Invoke-CheckedCommand -Workdir (Join-Path $repoRoot 'packages/admin/aoxiaoyou-admin-ui') -Command @('npm', 'run', 'build')
}

Write-Host 'Phase 15 indoor authoring smoke completed successfully.'
