param(
[string]$AdminBaseUrl = 'http://127.0.0.1:8081',
  [string]$PublicBaseUrl = 'http://127.0.0.1:8080',
  [string]$Username = 'admin',
  [string]$Password = 'admin123'
)

$ErrorActionPreference = 'Stop'

function Write-Utf8File {
  param(
    [Parameter(Mandatory = $true)][string]$Path,
    [Parameter(Mandatory = $true)]$Content
  )

  $encoding = New-Object System.Text.UTF8Encoding($false)
  if ($Content -isnot [string]) {
    $Content = $Content | ConvertTo-Json -Depth 20
  }
  [System.IO.File]::WriteAllText($Path, [string]$Content, $encoding)
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

function Wait-ForJsonSuccess {
  param(
    [Parameter(Mandatory = $true)][string]$Url,
    [int]$ExpectedCode = 0,
    [int]$Attempts = 8,
    [int]$DelaySeconds = 1
  )

  $lastResponse = $null
  for ($attempt = 1; $attempt -le $Attempts; $attempt++) {
    try {
      $lastResponse = Invoke-CurlJson -Method GET -Url $Url
    } catch {
      $lastResponse = $null
    }
    if ($lastResponse -and $null -ne $lastResponse.code -and [int]$lastResponse.code -eq $ExpectedCode) {
      return $lastResponse
    }
    if ($attempt -lt $Attempts) {
      Start-Sleep -Seconds $DelaySeconds
    }
  }

  $serialized = if ($lastResponse) { $lastResponse | ConvertTo-Json -Depth 8 } else { '<no response>' }
  throw "Request did not reach expected code $ExpectedCode after $Attempts attempts. URL=$Url Response=$serialized"
}

function Ensure-ApiSuccess {
  param(
    [Parameter(Mandatory = $true)]$Response,
    [Parameter(Mandatory = $true)][string]$Context
  )

  if ($Response.code -ne 0 -or -not $Response.data) {
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

function New-Slug {
  param([string]$Value)

  if (-not $Value) {
    return 'item'
  }
  $slug = $Value.ToLowerInvariant() -replace '[^a-z0-9]+', '-' -replace '(^-+|-+$)', ''
  if (-not $slug) {
    return 'item'
  }
  return $slug
}

function Get-PreferredText {
  param(
    [string]$Primary,
    [string]$Fallback
  )

  if ([string]::IsNullOrWhiteSpace($Primary)) {
    return $Fallback
  }
  return $Primary
}

function Get-UniqueMarkerCode {
  param(
    [string]$FloorId,
    [string]$BaseCode,
    [hashtable]$Seen
  )

  $candidate = New-Slug "$FloorId-$BaseCode"
  if (-not $Seen.ContainsKey($candidate)) {
    $Seen[$candidate] = 1
    return $candidate
  }

  $Seen[$candidate] = [int]$Seen[$candidate] + 1
  return "$candidate-$($Seen[$candidate])"
}

function Get-NodeType {
  param([string]$Category)

  switch ($Category) {
    'food_beverage' { return 'shop' }
    'shopping' { return 'shop' }
    'entertainment' { return 'landmark' }
    'car_showroom' { return 'landmark' }
    default { return 'custom' }
  }
}

function Parse-ManifestJs {
  param([Parameter(Mandatory = $true)][string]$Path)

  $raw = Get-Content -Raw -Encoding utf8 $Path
  $prefix = 'window.__MAP_MANIFEST__ ='
  if ($raw.StartsWith($prefix)) {
    $raw = $raw.Substring($prefix.Length)
  }
  $raw = $raw.Trim()
  if ($raw.EndsWith(';')) {
    $raw = $raw.Substring(0, $raw.Length - 1)
  }
  return $raw | ConvertFrom-Json
}

function Upload-Asset {
  param(
    [Parameter(Mandatory = $true)][string]$Token,
    [Parameter(Mandatory = $true)][string]$FilePath,
    [Parameter(Mandatory = $true)][string]$AssetKind,
    [Parameter(Mandatory = $true)][string]$ClientRelativePath,
    [string]$UploadSource = 'seed-lisboeta'
  )

  $response = Invoke-CurlJson -Method POST -Url "$AdminBaseUrl/api/admin/v1/content/assets/upload" -Token $Token -FormFields @(
    "file=@$FilePath",
    "assetKind=$AssetKind",
    "status=published",
    "uploadSource=$UploadSource",
    "clientRelativePath=$ClientRelativePath"
  )
  if ($response.code -ne 0 -or -not $response.data) {
    throw "Asset upload failed for $ClientRelativePath : $($response.message)"
  }
  return $response.data
}

function Convert-PoisToCsv {
  param(
    [Parameter(Mandatory = $true)][object[]]$Pois,
    [Parameter(Mandatory = $true)][object]$FloorMeta,
    [Parameter(Mandatory = $true)][string]$OutPath
  )

  $seenCodes = @{}
  $rows = foreach ($poi in ($Pois | Sort-Object sort_order, code)) {
    $baseCode = if ($poi.code) { [string]$poi.code } else { [string]$poi.name_en }
    $markerCode = Get-UniqueMarkerCode -FloorId ([string]$FloorMeta.floor_id) -BaseCode $baseCode -Seen $seenCodes
    $relativeX = [math]::Round(($poi.center_x / $FloorMeta.width), 6)
    $relativeY = [math]::Round(($poi.center_y / $FloorMeta.height), 6)
    $descriptionZh = if ($poi.notes) { $poi.notes } else { "樓層示例點位：$($poi.name_sc)" }
    $descriptionZht = if ($poi.notes) { $poi.notes } else { "樓層示例點位：$($poi.name_tc)" }
    $descriptionEn = if ($poi.notes) { $poi.notes } else { "Indoor showcase marker: $($poi.name_en)" }
    $descriptionPt = if ($poi.notes) { $poi.notes } else { "Marcador indoor de demonstracao: $($poi.name_en)" }
    [pscustomobject][ordered]@{
      markerCode = $markerCode
      nodeType = Get-NodeType -Category ([string]$poi.category)
      nameZh = $poi.name_sc
      nameEn = $poi.name_en
      nameZht = $poi.name_tc
      namePt = Get-PreferredText -Primary $poi.name_en -Fallback $poi.name_tc
      descriptionZh = $descriptionZh
      descriptionEn = $descriptionEn
      descriptionZht = $descriptionZht
      descriptionPt = $descriptionPt
      relativeX = '{0:N6}' -f $relativeX
      relativeY = '{0:N6}' -f $relativeY
      relatedPoiId = ''
      iconAssetId = ''
      animationAssetId = ''
      linkedEntityType = ''
      linkedEntityId = ''
      tagsJson = (@($poi.category, $poi.source_type, $FloorMeta.floor_id) | ConvertTo-Json -Compress)
      popupConfigJson = '{"enabled":false,"mode":"bubble"}'
      displayConfigJson = '{"labelMode":"always","showPulse":false}'
      metadataJson = (@{
          sourceType = $poi.source_type
          notes = $poi.notes
          tileId = $poi.tile_id
          tileCol = $poi.tile_col
          tileRow = $poi.tile_row
        } | ConvertTo-Json -Compress)
      sortOrder = $poi.sort_order
      status = 'published'
    }
  }

  $csv = $rows | ConvertTo-Csv -NoTypeInformation
  Write-Utf8File -Path $OutPath -Content (($csv -join [Environment]::NewLine) + [Environment]::NewLine)
}

function Invoke-Phase15WitnessSeed {
  param(
    [Parameter(Mandatory = $true)][string]$RepoRoot
  )

  $phase15Script = Join-Path $RepoRoot 'scripts/local/smoke-phase-15-indoor-authoring.ps1'
  if (-not (Test-Path $phase15Script)) {
    throw "Cannot find Phase 15 witness seed script at $phase15Script"
  }

  Write-Host 'Ensuring canonical Phase 21 witness set on Lisboa 1F...'
  & powershell.exe -NoProfile -ExecutionPolicy Bypass -File $phase15Script `
    -AdminBaseUrl $AdminBaseUrl `
    -PublicBaseUrl $PublicBaseUrl `
    -Username $Username `
    -Password $Password `
    -Scenario seed `
    -SkipTests `
    -SkipBuild

  if ($LASTEXITCODE -ne 0) {
    throw 'Phase 15 witness seed did not complete successfully.'
  }
}

$repoRoot = Resolve-Path (Join-Path $PSScriptRoot '..\..')
$indoorAssetRoot = Join-Path $repoRoot 'packages/client/src/assets/indoor'
$manifestJsPath = Join-Path $indoorAssetRoot 'manifest.js'
$poisPath = Join-Path $indoorAssetRoot 'pois.json'
$poisCsvPath = Join-Path $indoorAssetRoot 'pois.csv'

if (-not (Test-Path $manifestJsPath)) {
  throw "Cannot find indoor manifest script at $manifestJsPath"
}
if (-not (Test-Path $poisPath)) {
  throw "Cannot find indoor POI dataset at $poisPath"
}

$manifest = Parse-ManifestJs -Path $manifestJsPath
$pois = Get-Content -Raw -Encoding utf8 $poisPath | ConvertFrom-Json

$health = Invoke-RestMethod -Uri "$PublicBaseUrl/api/v1/health" -Method Get
if ($health.code -ne 0) {
  throw 'Public server health check failed.'
}

$loginBodyPath = Join-Path $env:TEMP 'lisboeta-indoor-login.json'
Write-Utf8File -Path $loginBodyPath -Content (@{ username = $Username; password = $Password } | ConvertTo-Json)
$login = Invoke-CurlJson -Method POST -Url "$AdminBaseUrl/api/admin/v1/auth/login" -JsonPath $loginBodyPath
$token = $login.data.token
if (-not $token) {
  throw 'Admin login did not return a token.'
}

$cityResponse = Invoke-CurlJson -Method GET -Url "$AdminBaseUrl/api/admin/v1/map/cities?pageNum=1&pageSize=200" -Token $token
$macauCity = $cityResponse.data.list | Where-Object { $_.code -eq 'macau' } | Select-Object -First 1
if (-not $macauCity) {
  throw 'Macau city was not found in the admin dataset.'
}

$subMapResponse = Invoke-CurlJson -Method GET -Url "$AdminBaseUrl/api/admin/v1/map/sub-maps?pageNum=1&pageSize=200&cityId=$($macauCity.id)" -Token $token
$taipaSubMap = $subMapResponse.data.list | Where-Object { $_.code -eq 'taipa' } | Select-Object -First 1
if (-not $taipaSubMap) {
  throw 'Taipa sub-map was not found in the admin dataset.'
}

$tempRoot = Join-Path $env:TEMP 'lisboeta-indoor-seed'
if (Test-Path $tempRoot) {
  Remove-Item $tempRoot -Recurse -Force
}
New-Item -ItemType Directory -Path $tempRoot | Out-Null

$manifestJsonPath = Join-Path $tempRoot 'manifest.json'
Write-Utf8File -Path $manifestJsonPath -Content ($manifest | ConvertTo-Json -Depth 20)

$assetMap = [ordered]@{}
$assetMap.cover = Upload-Asset -Token $token -FilePath (Join-Path $indoorAssetRoot 'floors/1f.png') -AssetKind 'image' -ClientRelativePath 'indoor/lisboeta-macau/cover/1f.png'
$assetMap.manifestJson = Upload-Asset -Token $token -FilePath $manifestJsonPath -AssetKind 'json' -ClientRelativePath 'indoor/lisboeta-macau/source/manifest.json'
$assetMap.manifestJs = Upload-Asset -Token $token -FilePath $manifestJsPath -AssetKind 'file' -ClientRelativePath 'indoor/lisboeta-macau/source/manifest.js'
$assetMap.poisJson = Upload-Asset -Token $token -FilePath $poisPath -AssetKind 'json' -ClientRelativePath 'indoor/lisboeta-macau/source/pois.json'
$assetMap.poisCsv = Upload-Asset -Token $token -FilePath $poisCsvPath -AssetKind 'file' -ClientRelativePath 'indoor/lisboeta-macau/source/pois.csv'

$buildingsResponse = Invoke-CurlJson -Method GET -Url "$AdminBaseUrl/api/admin/v1/map/indoor/buildings?pageNum=1&pageSize=200&keyword=lisboeta_macau" -Token $token
$existingBuilding = $buildingsResponse.data.list | Where-Object { $_.buildingCode -eq 'lisboeta_macau' } | Select-Object -First 1

$buildingPayloadPath = Join-Path $tempRoot 'lisboeta-building.json'
$buildingPayload = [ordered]@{
  buildingCode = 'lisboeta_macau'
  bindingMode = 'map'
  cityId = $macauCity.id
  subMapId = $taipaSubMap.id
  nameZh = '澳门葡京人'
  nameEn = 'Lisboeta Macau'
  nameZht = '澳門葡京人'
  namePt = 'Lisboeta Macau'
  addressZh = '澳门路氹溜冰路，位于路氹城葡京人综合体'
  addressEn = 'R. da Patinagem, Cotai, Macau'
  addressZht = '澳門路氹溜冰路，位於路氹城葡京人綜合體'
  addressPt = 'Rua da Patinagem, Cotai, Macau'
  sourceCoordinateSystem = 'GCJ02'
  sourceLatitude = 22.1414
  sourceLongitude = 113.5737
  lat = 22.1414
  lng = 113.5737
  totalFloors = 3
  basementFloors = 1
  coverAssetId = $assetMap.cover.id
  descriptionZh = '澳门葡京人室内示范地图，包含地下、一楼、二楼的瓦片与商户标记数据，可直接供小程序室内页读取。'
  descriptionEn = 'Indoor demo dataset for Lisboeta Macau, including ground, first, and second floor tiles plus venue markers for the mini-program indoor page.'
  descriptionZht = '澳門葡京人室內示範地圖，包含地下、一樓、二樓的瓦片與商戶標記資料，可直接供小程序室內頁讀取。'
  descriptionPt = 'Conjunto de demonstracao indoor do Lisboeta Macau com pisos G, 1F e 2F, azulejos e marcadores de lojas para a pagina indoor do mini-programa.'
  popupConfigJson = '{"enabled":true,"mode":"sheet","mediaUsageType":"cover"}'
  displayConfigJson = '{"layout":"card","theme":"lisboeta","showSubtitle":true}'
  attachmentAssetIds = @($assetMap.manifestJson.id, $assetMap.manifestJs.id, $assetMap.poisJson.id, $assetMap.poisCsv.id)
  status = 'published'
  sortOrder = -50
}
Write-Utf8File -Path $buildingPayloadPath -Content ($buildingPayload | ConvertTo-Json -Depth 20)

if ($existingBuilding) {
  $buildingResponse = Invoke-CurlJson -Method PUT -Url "$AdminBaseUrl/api/admin/v1/map/indoor/buildings/$($existingBuilding.id)" -Token $token -JsonPath $buildingPayloadPath
} else {
  $buildingResponse = Invoke-CurlJson -Method POST -Url "$AdminBaseUrl/api/admin/v1/map/indoor/buildings" -Token $token -JsonPath $buildingPayloadPath
}
if ($buildingResponse.code -ne 0 -or -not $buildingResponse.data) {
  throw "Indoor building upsert failed: $($buildingResponse.message)"
}

$buildingId = $buildingResponse.data.id
$floorSummary = @()

foreach ($floorMeta in ($manifest.floors | Sort-Object { switch ($_.floor_id) { 'g' { 0 }; '1f' { 1 }; '2f' { 2 }; default { 99 } } })) {
  $floorIdCode = [string]$floorMeta.floor_id
  $floorNumber = switch ($floorIdCode) {
    'g' { -1 }
    '1f' { 1 }
    '2f' { 2 }
    default { throw "Unsupported floor id: $floorIdCode" }
  }
  $floorCode = $floorIdCode.ToUpperInvariant()
  $floorAssetPath = Join-Path $indoorAssetRoot ("floors/{0}.png" -f $floorIdCode)
  $floorAsset = Upload-Asset -Token $token -FilePath $floorAssetPath -AssetKind 'image' -ClientRelativePath ("indoor/lisboeta-macau/floors/{0}.png" -f $floorIdCode)
  $floorPois = @($pois | Where-Object { $_.floor_id -eq $floorIdCode })
  $generatedCsvPath = Join-Path $tempRoot ("lisboeta-{0}-markers.csv" -f $floorIdCode)
  Convert-PoisToCsv -Pois $floorPois -FloorMeta $floorMeta -OutPath $generatedCsvPath
  $generatedCsvAsset = Upload-Asset -Token $token -FilePath $generatedCsvPath -AssetKind 'file' -ClientRelativePath ("indoor/lisboeta-macau/generated/{0}-markers.csv" -f $floorIdCode)

  $areaSqm = [math]::Round(($floorMeta.width * $floorMeta.height) / 600, 0)
  $floorPayload = [ordered]@{
    floorNumber = $floorNumber
    floorCode = $floorCode
    floorNameZh = $floorMeta.labels.sc
    floorNameEn = $floorMeta.labels.en
    floorNameZht = $floorMeta.labels.tc
    floorNamePt = switch ($floorIdCode) {
      'g' { 'Piso Terreo' }
      '1f' { 'Primeiro Piso' }
      '2f' { 'Segundo Piso' }
      default { $floorMeta.labels.en }
    }
    descriptionZh = "澳门葡京人 $($floorMeta.labels.sc) 室内导览示范楼层。"
    descriptionEn = "Lisboeta Macau indoor showcase layer for $($floorMeta.labels.en)."
    descriptionZht = "澳門葡京人 $($floorMeta.labels.tc) 室內導覽示範樓層。"
    descriptionPt = "Camada de demonstracao indoor do Lisboeta Macau para $($floorMeta.labels.en)."
    coverAssetId = $floorAsset.id
    floorPlanAssetId = $floorAsset.id
    altitudeMeters = 0
    areaSqm = $areaSqm
    zoomMin = 0.5
    defaultZoom = 1.0
    zoomMax = 3.5
    popupConfigJson = '{"enabled":true,"mode":"sheet"}'
    displayConfigJson = '{"layout":"card","theme":"lisboeta-floor"}'
    attachmentAssetIds = @($generatedCsvAsset.id)
    sortOrder = switch ($floorIdCode) {
      'g' { -10 }
      '1f' { 10 }
      '2f' { 20 }
      default { 100 }
    }
    status = 'published'
  }

  $buildingDetail = Invoke-CurlJson -Method GET -Url "$AdminBaseUrl/api/admin/v1/map/indoor/buildings/$buildingId" -Token $token
  $existingFloor = $buildingDetail.data.floors | Where-Object { $_.floorNumber -eq $floorNumber } | Select-Object -First 1
  $floorPayloadPath = Join-Path $tempRoot ("floor-{0}.json" -f $floorIdCode)
  Write-Utf8File -Path $floorPayloadPath -Content ($floorPayload | ConvertTo-Json -Depth 20)

  if ($existingFloor) {
    $floorResponse = Invoke-CurlJson -Method PUT -Url "$AdminBaseUrl/api/admin/v1/map/indoor/floors/$($existingFloor.id)" -Token $token -JsonPath $floorPayloadPath
  } else {
    $floorResponse = Invoke-CurlJson -Method POST -Url "$AdminBaseUrl/api/admin/v1/map/indoor/buildings/$buildingId/floors" -Token $token -JsonPath $floorPayloadPath
  }
  if ($floorResponse.code -ne 0 -or -not $floorResponse.data) {
    throw "Floor upsert failed for $floorIdCode : $($floorResponse.message)"
  }

  $floorId = $floorResponse.data.id
  $floorDetailBeforeImport = Invoke-CurlJson -Method GET -Url "$AdminBaseUrl/api/admin/v1/map/indoor/floors/$floorId" -Token $token
  foreach ($marker in @($floorDetailBeforeImport.data.markers)) {
    [void](Invoke-CurlJson -Method DELETE -Url "$AdminBaseUrl/api/admin/v1/map/indoor/markers/$($marker.id)" -Token $token)
  }

  $importResponse = Invoke-CurlJson -Method POST -Url "$AdminBaseUrl/api/admin/v1/map/indoor/floors/$floorId/tile-import/image?tileSizePx=$($floorMeta.tile_size)" -Token $token -FormFields @(
    "file=@$floorAssetPath"
  )
  if ($importResponse.code -ne 0 -or -not $importResponse.data) {
    throw "Image slicing import failed for $floorIdCode : $($importResponse.message)"
  }

  $finalFloorPayload = [ordered]@{
    indoorMapId = $importResponse.data.indoorMapId
    floorNumber = $floorNumber
    floorCode = $floorCode
    floorNameZh = $floorPayload.floorNameZh
    floorNameEn = $floorPayload.floorNameEn
    floorNameZht = $floorPayload.floorNameZht
    floorNamePt = $floorPayload.floorNamePt
    descriptionZh = $floorPayload.descriptionZh
    descriptionEn = $floorPayload.descriptionEn
    descriptionZht = $floorPayload.descriptionZht
    descriptionPt = $floorPayload.descriptionPt
    coverAssetId = $floorAsset.id
    floorPlanAssetId = $floorAsset.id
    tilePreviewImageUrl = $importResponse.data.tilePreviewImageUrl
    altitudeMeters = 0
    areaSqm = $areaSqm
    zoomMin = $importResponse.data.zoomMin
    defaultZoom = $importResponse.data.defaultZoom
    zoomMax = $importResponse.data.zoomMax
    popupConfigJson = $floorPayload.popupConfigJson
    displayConfigJson = $floorPayload.displayConfigJson
    attachmentAssetIds = @($generatedCsvAsset.id)
    sortOrder = $floorPayload.sortOrder
    status = 'published'
  }
  $finalFloorPayloadPath = Join-Path $tempRoot ("floor-final-{0}.json" -f $floorIdCode)
  Write-Utf8File -Path $finalFloorPayloadPath -Content ($finalFloorPayload | ConvertTo-Json -Depth 20)
  $finalFloorResponse = Invoke-CurlJson -Method PUT -Url "$AdminBaseUrl/api/admin/v1/map/indoor/floors/$floorId" -Token $token -JsonPath $finalFloorPayloadPath
  if ($finalFloorResponse.code -ne 0 -or -not $finalFloorResponse.data) {
    throw "Final floor update failed for $floorIdCode : $($finalFloorResponse.message)"
  }

  $csvPreviewResponse = Invoke-CurlJson -Method POST -Url "$AdminBaseUrl/api/admin/v1/map/indoor/floors/$floorId/markers/csv-preview" -Token $token -FormFields @(
    "file=@$generatedCsvPath"
  )
  if ($csvPreviewResponse.code -ne 0 -or -not $csvPreviewResponse.data) {
    throw "CSV preview failed for $floorIdCode : $($csvPreviewResponse.message)"
  }
  if ($csvPreviewResponse.data.invalidRows -gt 0) {
    throw "CSV preview reported invalid rows for $floorIdCode"
  }

  $csvConfirmPath = Join-Path $tempRoot ("csv-confirm-{0}.json" -f $floorIdCode)
  $csvConfirmPayload = [ordered]@{
    sourceFilename = [System.IO.Path]::GetFileName($generatedCsvPath)
    rows = @($csvPreviewResponse.data.rows | ForEach-Object {
        [ordered]@{
          rowNumber = $_.rowNumber
          markerCode = $_.markerCode
          nodeType = $_.nodeType
          nodeNameZh = $_.nodeNameZh
          nodeNameEn = $_.nodeNameEn
          nodeNameZht = $_.nodeNameZht
          nodeNamePt = $_.nodeNamePt
          descriptionZh = $_.descriptionZh
          descriptionEn = $_.descriptionEn
          descriptionZht = $_.descriptionZht
          descriptionPt = $_.descriptionPt
          relativeX = $_.relativeX
          relativeY = $_.relativeY
          relatedPoiId = $_.relatedPoiId
          iconAssetId = $_.iconAssetId
          animationAssetId = $_.animationAssetId
          linkedEntityType = $_.linkedEntityType
          linkedEntityId = $_.linkedEntityId
          tagsJson = $_.tagsJson
          popupConfigJson = $_.popupConfigJson
          displayConfigJson = $_.displayConfigJson
          metadataJson = $_.metadataJson
          sortOrder = $_.sortOrder
          status = $_.status
        }
      })
  }
  Write-Utf8File -Path $csvConfirmPath -Content ($csvConfirmPayload | ConvertTo-Json -Depth 20)

  $csvConfirmResponse = Invoke-CurlJson -Method POST -Url "$AdminBaseUrl/api/admin/v1/map/indoor/floors/$floorId/markers/csv-confirm" -Token $token -JsonPath $csvConfirmPath
  if ($csvConfirmResponse.code -ne 0 -or -not $csvConfirmResponse.data) {
    throw "CSV confirm failed for $floorIdCode : $($csvConfirmResponse.message)"
  }

  $publicFloor = Wait-ForJsonSuccess -Url ("{0}/api/v1/indoor/floors/{1}?locale=zh-Hant" -f $PublicBaseUrl, $floorId)

  $floorSummary += [pscustomobject]@{
    floorId = $floorId
    floorCode = $floorCode
    tileEntryCount = $publicFloor.data.tileEntryCount
    markerCount = $publicFloor.data.markers.Count
    tileRootUrl = $publicFloor.data.tileRootUrl
  }
}

$publicBuilding = Wait-ForJsonSuccess -Url ("{0}/api/v1/indoor/buildings/{1}?locale=zh-Hant" -f $PublicBaseUrl, $buildingId)

Invoke-Phase15WitnessSeed -RepoRoot $repoRoot

$witnessNodeCodes = @(
  '1f-phase15-night-market-overlay',
  '1f-phase15-royal-palace-dwell',
  '1f-phase15-zipcity-path'
)
$witnessBehaviorCodes = @(
  'night-market-schedule-overlay',
  'royal-palace-dwell-reveal',
  'zipcity-guiding-path'
)

$buildingDetailAfterWitness = Ensure-ApiSuccess -Response (
  Invoke-CurlJson -Method GET -Url "$AdminBaseUrl/api/admin/v1/map/indoor/buildings/$buildingId" -Token $token
) -Context 'Fetch Lisboeta detail after witness seed'

$witnessFloor = @($buildingDetailAfterWitness.floors | Where-Object { $_.floorCode -eq '1F' } | Select-Object -First 1)[0]
Assert-True -Condition ($null -ne $witnessFloor) -Message 'Lisboeta 1F witness floor is missing after witness seed.'

$witnessNodes = Ensure-ApiSuccess -Response (
  Invoke-CurlJson -Method GET -Url "$AdminBaseUrl/api/admin/v1/map/indoor/floors/$($witnessFloor.id)/nodes" -Token $token
) -Context 'Fetch Lisboeta 1F witness nodes'

$witnessNodeSummary = foreach ($markerCode in $witnessNodeCodes) {
  $node = @($witnessNodes | Where-Object { $_.markerCode -eq $markerCode } | Select-Object -First 1)[0]
  Assert-True -Condition ($null -ne $node) -Message "Witness node $markerCode is missing after Phase 15 seed."

  [pscustomobject]@{
    markerCode = $node.markerCode
    nodeId = $node.id
    nodeNameZht = $node.nodeNameZht
    runtimeSupportLevel = $node.runtimeSupportLevel
    behaviorCodes = @($node.behaviors | ForEach-Object { $_.behaviorCode })
  }
}

foreach ($behaviorCode in $witnessBehaviorCodes) {
  $behaviorMatch = @(
    $witnessNodes |
    ForEach-Object { @($_.behaviors) } |
    Where-Object { $_.behaviorCode -eq $behaviorCode } |
    Select-Object -First 1
  )[0]
  Assert-True -Condition ($null -ne $behaviorMatch) -Message "Witness behavior $behaviorCode is missing after Phase 15 seed."
}

$witnessRuntimeResponse = Wait-ForJsonSuccess -Url ("{0}/api/v1/indoor/floors/{1}/runtime?locale=zh-Hant" -f $PublicBaseUrl, $witnessFloor.id)
$witnessRuntime = $witnessRuntimeResponse.data

foreach ($markerCode in $witnessNodeCodes) {
  $runtimeNode = @($witnessRuntime.nodes | Where-Object { $_.markerCode -eq $markerCode } | Select-Object -First 1)[0]
  Assert-True -Condition ($null -ne $runtimeNode) -Message "Witness runtime node $markerCode is missing from public runtime."
  Assert-True -Condition (-not [string]::IsNullOrWhiteSpace($runtimeNode.name)) -Message "Witness runtime node $markerCode has no localized name."
  Assert-True -Condition ($runtimeNode.name -notmatch '^[?]+$') -Message "Witness runtime node $markerCode still has corrupted localized copy."
}

[ordered]@{
  buildingId = $buildingId
  buildingName = $publicBuilding.data.name
  floorCount = $publicBuilding.data.floors.Count
  floors = $floorSummary
  coverAssetId = $assetMap.cover.id
  sourceAssets = @{
    manifestJson = $assetMap.manifestJson.id
    manifestJs = $assetMap.manifestJs.id
    poisJson = $assetMap.poisJson.id
    poisCsv = $assetMap.poisCsv.id
  }
  phase21WitnessSet = [ordered]@{
    floorId = $witnessFloor.id
    floorCode = $witnessFloor.floorCode
    witnessNodeCodes = $witnessNodeCodes
    witnessBehaviorCodes = $witnessBehaviorCodes
    nodes = $witnessNodeSummary
  }
} | ConvertTo-Json -Depth 20
