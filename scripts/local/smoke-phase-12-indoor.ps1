$ErrorActionPreference = 'Stop'

function Write-Utf8File {
  param(
    [Parameter(Mandatory = $true)][string]$Path,
    [Parameter(Mandatory = $true)][string]$Content
  )

  $encoding = New-Object System.Text.UTF8Encoding($false)
  [System.IO.File]::WriteAllText($Path, $Content, $encoding)
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
    [int]$Attempts = 5,
    [int]$DelaySeconds = 1
  )

  $lastResponse = $null
  for ($attempt = 1; $attempt -le $Attempts; $attempt++) {
    $lastResponse = Invoke-CurlJson -Method GET -Url $Url
    if ($null -ne $lastResponse.code -and [int]$lastResponse.code -eq $ExpectedCode) {
      return $lastResponse
    }
    if ($attempt -lt $Attempts) {
      Start-Sleep -Seconds $DelaySeconds
    }
  }

  $serialized = if ($lastResponse) { $lastResponse | ConvertTo-Json -Depth 8 } else { '<no response>' }
  throw "Request did not reach expected code $ExpectedCode after $Attempts attempts. URL=$Url Response=$serialized"
}

function New-IndoorDemoAssets {
  param(
    [Parameter(Mandatory = $true)][string]$BaseDir,
    [Parameter(Mandatory = $true)][string]$Label
  )

  Add-Type -AssemblyName System.Drawing
  if (Test-Path $BaseDir) {
    Remove-Item $BaseDir -Recurse -Force
  }

  New-Item -ItemType Directory -Path $BaseDir | Out-Null
  $tileDir = Join-Path $BaseDir 'tiles'
  New-Item -ItemType Directory -Path (Join-Path $tileDir '0') -Force | Out-Null

  $width = 1024
  $height = 768
  $tileSize = 512
  $bitmap = New-Object System.Drawing.Bitmap $width, $height
  $graphics = [System.Drawing.Graphics]::FromImage($bitmap)
  $graphics.SmoothingMode = [System.Drawing.Drawing2D.SmoothingMode]::AntiAlias
  $graphics.Clear([System.Drawing.Color]::FromArgb(24, 45, 84))

  $panelBrush = New-Object System.Drawing.SolidBrush ([System.Drawing.Color]::FromArgb(61, 105, 190))
  $lightBrush = New-Object System.Drawing.SolidBrush ([System.Drawing.Color]::FromArgb(239, 246, 255))
  $accentBrush = New-Object System.Drawing.SolidBrush ([System.Drawing.Color]::FromArgb(255, 189, 89))
  $linePen = New-Object System.Drawing.Pen ([System.Drawing.Color]::FromArgb(160, 255, 255, 255)), 4
  $fontTitle = New-Object System.Drawing.Font('Arial', 32, [System.Drawing.FontStyle]::Bold)
  $fontBody = New-Object System.Drawing.Font('Arial', 18)

  $graphics.FillRectangle($panelBrush, 32, 32, 960, 704)
  $graphics.FillRectangle($lightBrush, 72, 120, 424, 220)
  $graphics.FillRectangle($lightBrush, 528, 120, 424, 220)
  $graphics.FillRectangle($lightBrush, 72, 390, 880, 296)
  $graphics.DrawString("Phase 12 Indoor $Label", $fontTitle, [System.Drawing.Brushes]::White, 60, 48)
  $graphics.DrawString('Lobby Route / Story Zone', $fontBody, $accentBrush, 64, 92)

  for ($x = 72; $x -le 952; $x += 88) {
    $graphics.DrawLine($linePen, $x, 390, $x, 686)
  }
  for ($y = 390; $y -le 686; $y += 74) {
    $graphics.DrawLine($linePen, 72, $y, 952, $y)
  }

  $graphics.FillEllipse($accentBrush, 160, 204, 48, 48)
  $graphics.FillEllipse($accentBrush, 620, 214, 48, 48)
  $graphics.FillEllipse($accentBrush, 420, 520, 48, 48)
  $graphics.DrawString('Info Desk', $fontBody, [System.Drawing.Brushes]::MidnightBlue, 224, 214)
  $graphics.DrawString('Main Gate', $fontBody, [System.Drawing.Brushes]::MidnightBlue, 682, 224)
  $graphics.DrawString('Story Stop', $fontBody, [System.Drawing.Brushes]::MidnightBlue, 484, 530)

  $imagePath = Join-Path $BaseDir 'floor.png'
  $bitmap.Save($imagePath, [System.Drawing.Imaging.ImageFormat]::Png)

  for ($row = 0; $row -lt 2; $row++) {
    for ($col = 0; $col -lt 2; $col++) {
      $rect = New-Object System.Drawing.Rectangle ($col * $tileSize), ($row * $tileSize), ([Math]::Min($tileSize, $width - ($col * $tileSize))), ([Math]::Min($tileSize, $height - ($row * $tileSize)))
      $tile = $bitmap.Clone($rect, $bitmap.PixelFormat)
      $tile.Save((Join-Path (Join-Path $tileDir '0') ("{0}_{1}.png" -f $col, $row)), [System.Drawing.Imaging.ImageFormat]::Png)
      $tile.Dispose()
    }
  }

  $graphics.Dispose()
  $bitmap.Dispose()

  $zipPath = Join-Path $BaseDir 'tiles.zip'
  Compress-Archive -Path (Join-Path $tileDir '*') -DestinationPath $zipPath -Force

  $csvRows = @(
    [pscustomobject][ordered]@{
      markerCode = "${Label}-gate"
      nodeType = 'entrance'
      nameZh = 'Main Gate CN'
      nameEn = 'Main Entrance'
      nameZht = 'Main Gate TC'
      namePt = 'Entrada Principal'
      descriptionZh = 'Indoor route starting point.'
      descriptionEn = 'Starting point of the indoor exploration route.'
      descriptionZht = 'Indoor route starting point.'
      descriptionPt = 'Ponto inicial do percurso interior.'
      relativeX = '0.670'
      relativeY = '0.280'
      relatedPoiId = ''
      iconAssetId = ''
      animationAssetId = ''
      linkedEntityType = 'chapter'
      linkedEntityId = '1'
      tagsJson = '["entry","guide"]'
      popupConfigJson = '{"enabled":true}'
      displayConfigJson = '{"labelMode":"always"}'
      metadataJson = '{"hint":"chapter-start"}'
      sortOrder = '20'
      status = 'published'
    }
    [pscustomobject][ordered]@{
      markerCode = "${Label}-story"
      nodeType = 'landmark'
      nameZh = 'Story Stop CN'
      nameEn = 'Story Stop'
      nameZht = 'Story Stop TC'
      namePt = 'Paragem da Historia'
      descriptionZh = 'Interactive story and narration point.'
      descriptionEn = 'Interactive point for chapter stories and narration.'
      descriptionZht = 'Interactive story and narration point.'
      descriptionPt = 'Ponto interativo para historia e narracao.'
      relativeX = '0.460'
      relativeY = '0.690'
      relatedPoiId = ''
      iconAssetId = ''
      animationAssetId = ''
      linkedEntityType = 'event'
      linkedEntityId = '12'
      tagsJson = '["story","audio"]'
      popupConfigJson = '{"enabled":true}'
      displayConfigJson = '{"labelMode":"always"}'
      metadataJson = '{"hint":"narration"}'
      sortOrder = '30'
      status = 'published'
    }
  )

  $csvPath = Join-Path $BaseDir 'markers.csv'
  $csv = $csvRows | ConvertTo-Csv -NoTypeInformation
  Write-Utf8File -Path $csvPath -Content (($csv -join [Environment]::NewLine) + [Environment]::NewLine)

  return @{
    Image = $imagePath
    Zip = $zipPath
    Csv = $csvPath
  }
}

$publicHealth = Invoke-RestMethod -Uri 'http://127.0.0.1:8080/api/v1/health' -Method Get
if ($publicHealth.code -ne 0) {
  throw 'Public server health check failed.'
}

$loginBody = @{ username = 'admin'; password = 'admin123' } | ConvertTo-Json
$login = Invoke-RestMethod -Uri 'http://127.0.0.1:8081/api/admin/v1/auth/login' -Method Post -ContentType 'application/json' -Body $loginBody
$token = $login.data.token
if (-not $token) {
  throw 'Admin login did not return a token.'
}

$buildings = Invoke-CurlJson -Method GET -Url 'http://127.0.0.1:8081/api/admin/v1/map/indoor/buildings?pageNum=1&pageSize=20' -Token $token
$building = $buildings.data.list | Where-Object { $_.buildingCode -eq 'galaxy_macau' } | Select-Object -First 1
if (-not $building) {
  $building = $buildings.data.list | Select-Object -First 1
}
if (-not $building) {
  throw 'No indoor building is available for the smoke test.'
}

$buildingDetail = Invoke-CurlJson -Method GET -Url "http://127.0.0.1:8081/api/admin/v1/map/indoor/buildings/$($building.id)" -Token $token
$maxFloor = 0
if ($buildingDetail.data.floors) {
  $maxFloor = (($buildingDetail.data.floors | Measure-Object -Property floorNumber -Maximum).Maximum)
  if (-not $maxFloor) {
    $maxFloor = 0
  }
}

$floorNumber = [int]$maxFloor + 1
$label = "phase12-$floorNumber"
$tempRoot = Join-Path $env:TEMP "phase12-smoke-$label"
$assets = New-IndoorDemoAssets -BaseDir $tempRoot -Label $label

$floorPayloadPath = Join-Path $tempRoot 'floor.json'
$floorPayload = [ordered]@{
  floorNumber = $floorNumber
  floorCode = "F$floorNumber"
  floorNameZh = "Indoor Smoke Floor $floorNumber CN"
  floorNameEn = "Indoor Smoke Floor $floorNumber"
  floorNameZht = "Indoor Smoke Floor $floorNumber TC"
  floorNamePt = "Piso Smoke $floorNumber"
  descriptionZh = 'Auto-created indoor smoke verification floor.'
  descriptionEn = 'Auto-created floor for phase 12 smoke verification.'
  descriptionZht = 'Auto-created indoor smoke verification floor.'
  descriptionPt = 'Piso criado automaticamente para o smoke test da fase 12.'
  areaSqm = 1600
  zoomMin = 0.5
  defaultZoom = 1.0
  zoomMax = 2.8
  popupConfigJson = '{"enabled":false}'
  displayConfigJson = '{"layout":"card"}'
  attachmentAssetIds = @()
  status = 'published'
} | ConvertTo-Json -Depth 8
Write-Utf8File -Path $floorPayloadPath -Content $floorPayload

$floorCreate = Invoke-CurlJson -Method POST -Url "http://127.0.0.1:8081/api/admin/v1/map/indoor/buildings/$($building.id)/floors" -Token $token -JsonPath $floorPayloadPath
if ($floorCreate.code -ne 0) {
  throw "Floor creation failed: $($floorCreate.message)"
}
$floorId = $floorCreate.data.id

$zipPreview = Invoke-CurlJson -Method POST -Url "http://127.0.0.1:8081/api/admin/v1/map/indoor/floors/$floorId/tile-import/zip-preview?tileSizePx=512" -Token $token -FormFields @("file=@$($assets.Zip)")
if ($zipPreview.code -ne 0) {
  throw "ZIP preview failed: $($zipPreview.message)"
}

$imageImport = Invoke-CurlJson -Method POST -Url "http://127.0.0.1:8081/api/admin/v1/map/indoor/floors/$floorId/tile-import/image?tileSizePx=512" -Token $token -FormFields @("file=@$($assets.Image)")
if ($imageImport.code -ne 0) {
  throw "Image import failed: $($imageImport.message)"
}

$manualMarkerPayloadPath = Join-Path $tempRoot 'manual-marker.json'
$manualMarkerPayload = [ordered]@{
  markerCode = "$label-manual"
  nodeType = 'service'
  nodeNameZh = 'Info Desk CN'
  nodeNameEn = 'Info Desk'
  nodeNameZht = 'Info Desk TC'
  nodeNamePt = 'Balcao de Informacao'
  descriptionZh = 'Indoor meetup and guidance desk.'
  descriptionEn = 'Indoor guidance and meetup desk.'
  descriptionZht = 'Indoor meetup and guidance desk.'
  descriptionPt = 'Balcao para orientacao e encontro.'
  relativeX = 0.18
  relativeY = 0.24
  linkedEntityType = 'activity'
  linkedEntityId = 2
  tagsJson = '["service","guide"]'
  popupConfigJson = '{"enabled":true}'
  displayConfigJson = '{"labelMode":"always"}'
  metadataJson = '{"note":"manual-created"}'
  sortOrder = 10
  status = 'published'
} | ConvertTo-Json -Depth 8
Write-Utf8File -Path $manualMarkerPayloadPath -Content $manualMarkerPayload

$manualMarker = Invoke-CurlJson -Method POST -Url "http://127.0.0.1:8081/api/admin/v1/map/indoor/floors/$floorId/markers" -Token $token -JsonPath $manualMarkerPayloadPath
if ($manualMarker.code -ne 0) {
  throw "Manual marker creation failed: $($manualMarker.message)"
}

$csvPreview = Invoke-CurlJson -Method POST -Url "http://127.0.0.1:8081/api/admin/v1/map/indoor/floors/$floorId/markers/csv-preview" -Token $token -FormFields @("file=@$($assets.Csv)")
if ($csvPreview.code -ne 0) {
  throw "CSV preview failed: $($csvPreview.message)"
}

$confirmPayloadPath = Join-Path $tempRoot 'csv-confirm.json'
$confirmPayload = [ordered]@{
  sourceFilename = [System.IO.Path]::GetFileName($assets.Csv)
  rows = @($csvPreview.data.rows | ForEach-Object {
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
} | ConvertTo-Json -Depth 8
Write-Utf8File -Path $confirmPayloadPath -Content $confirmPayload

$csvConfirm = Invoke-CurlJson -Method POST -Url "http://127.0.0.1:8081/api/admin/v1/map/indoor/floors/$floorId/markers/csv-confirm" -Token $token -JsonPath $confirmPayloadPath
if ($csvConfirm.code -ne 0) {
  throw "CSV confirm failed: $($csvConfirm.message)"
}

$publicFloor = Wait-ForJsonSuccess -Url "http://127.0.0.1:8080/api/v1/indoor/floors/$($floorId)?locale=zh-Hant"
$publicBuilding = Wait-ForJsonSuccess -Url "http://127.0.0.1:8080/api/v1/indoor/buildings/$($building.id)?locale=zh-Hant"

if ($publicFloor.data.tileEntryCount -lt 4) {
  throw "Expected at least 4 tiles but got $($publicFloor.data.tileEntryCount)."
}
if ($publicFloor.data.markers.Count -lt 3) {
  throw "Expected at least 3 markers but got $($publicFloor.data.markers.Count)."
}

[ordered]@{
  publicHealth = $publicHealth.data.status
  buildingId = $building.id
  floorId = $floorId
  floorCode = $publicFloor.data.floorCode
  zipPreviewTiles = $zipPreview.data.tileEntryCount
  importedTileSource = $imageImport.data.tileSourceType
  importedTileRootUrl = $imageImport.data.tileRootUrl
  manualMarkerId = $manualMarker.data.id
  csvImportedRows = $csvConfirm.data.importedRows
  publicMarkerCount = $publicFloor.data.markers.Count
  publicBuildingFloorCount = $publicBuilding.data.floors.Count
  sampleAssets = $assets
} | ConvertTo-Json -Depth 6
