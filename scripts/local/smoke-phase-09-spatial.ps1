param(
    [string]$AdminBaseUrl = 'http://127.0.0.1:8081',
    [string]$PublicBaseUrl = 'http://127.0.0.1:8080',
    [string]$AdminUsername = 'admin',
    [string]$AdminPassword = 'admin123',
    [string]$MySqlHost = '127.0.0.1',
    [int]$MySqlPort = 3306,
    [string]$MySqlDatabase = 'aoxiaoyou',
    [string]$MySqlUser = 'root',
    [string]$MySqlPassword = 'Abc123456'
)

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'

$RootDir = (Resolve-Path (Join-Path $PSScriptRoot '..\..')).Path
$ClientDir = Join-Path $RootDir 'packages\client'
$marker = "phase9-$([DateTimeOffset]::UtcNow.ToUnixTimeMilliseconds())"
$cityId = $null
$restoreCityStatus = $null
$restoreCityPublishedAt = $null

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

    if ("$Actual" -ne "$Expected") {
        throw "$Label expected '$Expected' but received '$Actual'."
    }
}

function Assert-True {
    param(
        [Parameter(Mandatory = $true)][bool]$Condition,
        [Parameter(Mandatory = $true)][string]$Label
    )

    if (-not $Condition) {
        throw $Label
    }
}

function Invoke-ApiJson {
    param(
        [Parameter(Mandatory = $true)][string]$Method,
        [Parameter(Mandatory = $true)][string]$Url,
        [object]$Body,
        [hashtable]$Headers
    )

    $params = @{
        Method = $Method
        Uri = $Url
        TimeoutSec = 30
    }
    if ($Headers) {
        $params.Headers = $Headers
    }
    if ($null -ne $Body) {
        $params.ContentType = 'application/json'
        $params.Body = ($Body | ConvertTo-Json -Depth 20)
    }

    $response = Invoke-RestMethod @params
    Assert-Equals -Actual $response.code -Expected 0 -Label "API success for $Method $Url"
    return $response.data
}

function Invoke-MySqlScalar {
    param(
        [Parameter(Mandatory = $true)][string]$Query
    )

    $previousPassword = $env:MYSQL_PWD
    try {
        $env:MYSQL_PWD = $MySqlPassword
        $result = & mysql `
            --default-character-set=utf8mb4 `
            --protocol=TCP `
            --host=$MySqlHost `
            --port=$MySqlPort `
            --user=$MySqlUser `
            --database=$MySqlDatabase `
            --batch `
            --raw `
            --skip-column-names `
            --execute=$Query
        if ($LASTEXITCODE -ne 0) {
            throw "MySQL query failed: $Query"
        }
        return ($result | Out-String).Trim()
    } finally {
        if ($null -eq $previousPassword) {
            Remove-Item Env:MYSQL_PWD -ErrorAction SilentlyContinue
        } else {
            $env:MYSQL_PWD = $previousPassword
        }
    }
}

function Invoke-MySqlNonQuery {
    param(
        [Parameter(Mandatory = $true)][string]$Query
    )

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
            --execute=$Query | Out-Null
        if ($LASTEXITCODE -ne 0) {
            throw "MySQL command failed."
        }
    } finally {
        if ($null -eq $previousPassword) {
            Remove-Item Env:MYSQL_PWD -ErrorAction SilentlyContinue
        } else {
            $env:MYSQL_PWD = $previousPassword
        }
    }
}

function Test-MySqlTableExists {
    param(
        [Parameter(Mandatory = $true)][string]$TableName
    )

    return [int](Invoke-MySqlScalar "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = '$MySqlDatabase' AND table_name = '$TableName';") -gt 0
}

function Test-MySqlColumnExists {
    param(
        [Parameter(Mandatory = $true)][string]$TableName,
        [Parameter(Mandatory = $true)][string]$ColumnName
    )

    return [int](Invoke-MySqlScalar "SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = '$MySqlDatabase' AND table_name = '$TableName' AND column_name = '$ColumnName';") -gt 0
}

function Ensure-MySqlColumn {
    param(
        [Parameter(Mandatory = $true)][string]$TableName,
        [Parameter(Mandatory = $true)][string]$ColumnName,
        [Parameter(Mandatory = $true)][string]$AlterSql
    )

    if (-not (Test-MySqlColumnExists -TableName $TableName -ColumnName $ColumnName)) {
        Invoke-MySqlNonQuery $AlterSql
    }
}

function Convert-JsonFieldToString {
    param(
        $Value
    )

    if ($null -eq $Value) {
        return $null
    }
    if ($Value -is [string]) {
        return $Value
    }
    return ($Value | ConvertTo-Json -Depth 20 -Compress)
}

function Convert-CityToUpsertBody {
    param(
        [Parameter(Mandatory = $true)]$City
    )

    $publishedAt = $null
    if ($null -ne $City.publishedAt -and "$($City.publishedAt)" -ne '') {
        if ($City.publishedAt -is [DateTime]) {
            $publishedAt = $City.publishedAt.ToString('yyyy-MM-ddTHH:mm:ss')
        } else {
            $publishedAt = [string]$City.publishedAt
        }
    }

    return @{
        code = $City.code
        nameZh = $City.nameZh
        nameEn = $City.nameEn
        nameZht = $City.nameZht
        namePt = $City.namePt
        subtitleZh = $City.subtitleZh
        subtitleEn = $City.subtitleEn
        subtitleZht = $City.subtitleZht
        subtitlePt = $City.subtitlePt
        countryCode = $City.countryCode
        customCountryName = $City.customCountryName
        sourceCoordinateSystem = $City.sourceCoordinateSystem
        sourceCenterLat = $City.sourceCenterLat
        sourceCenterLng = $City.sourceCenterLng
        centerLat = $City.centerLat
        centerLng = $City.centerLng
        defaultZoom = $City.defaultZoom
        unlockType = $City.unlockType
        unlockConditionJson = Convert-JsonFieldToString $City.unlockConditionJson
        coverAssetId = $City.coverAssetId
        bannerAssetId = $City.bannerAssetId
        descriptionZh = $City.descriptionZh
        descriptionEn = $City.descriptionEn
        descriptionZht = $City.descriptionZht
        descriptionPt = $City.descriptionPt
        popupConfigJson = Convert-JsonFieldToString $City.popupConfigJson
        displayConfigJson = Convert-JsonFieldToString $City.displayConfigJson
        attachments = @($City.attachments)
        sortOrder = $City.sortOrder
        status = $City.status
        publishedAt = $publishedAt
    }
}

try {
    Write-Host '[phase-09] ensuring Phase 9 schema columns exist...'
    Ensure-MySqlColumn -TableName 'cities' -ColumnName 'source_coordinate_system' -AlterSql "ALTER TABLE cities ADD COLUMN source_coordinate_system VARCHAR(32) NOT NULL DEFAULT 'GCJ02' AFTER country_code;"
    Ensure-MySqlColumn -TableName 'cities' -ColumnName 'custom_country_name' -AlterSql "ALTER TABLE cities ADD COLUMN custom_country_name VARCHAR(128) NULL AFTER country_code;"
    Ensure-MySqlColumn -TableName 'cities' -ColumnName 'source_center_lat' -AlterSql "ALTER TABLE cities ADD COLUMN source_center_lat DECIMAL(10,7) NULL AFTER source_coordinate_system;"
    Ensure-MySqlColumn -TableName 'cities' -ColumnName 'source_center_lng' -AlterSql "ALTER TABLE cities ADD COLUMN source_center_lng DECIMAL(10,7) NULL AFTER source_center_lat;"
    Ensure-MySqlColumn -TableName 'cities' -ColumnName 'popup_config_json' -AlterSql "ALTER TABLE cities ADD COLUMN popup_config_json JSON NULL AFTER description_pt;"
    Ensure-MySqlColumn -TableName 'cities' -ColumnName 'display_config_json' -AlterSql "ALTER TABLE cities ADD COLUMN display_config_json JSON NULL AFTER popup_config_json;"

    if (-not (Test-MySqlTableExists -TableName 'sub_maps')) {
        Invoke-MySqlNonQuery @'
CREATE TABLE sub_maps (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  city_id BIGINT NOT NULL,
  code VARCHAR(64) NOT NULL,
  name_zh VARCHAR(128) NOT NULL,
  name_en VARCHAR(128) NOT NULL DEFAULT '',
  name_zht VARCHAR(128) NOT NULL DEFAULT '',
  name_pt VARCHAR(128) NOT NULL DEFAULT '',
  subtitle_zh VARCHAR(255) NOT NULL DEFAULT '',
  subtitle_en VARCHAR(255) NOT NULL DEFAULT '',
  subtitle_zht VARCHAR(255) NOT NULL DEFAULT '',
  subtitle_pt VARCHAR(255) NOT NULL DEFAULT '',
  description_zh TEXT NULL,
  description_en TEXT NULL,
  description_zht TEXT NULL,
  description_pt TEXT NULL,
  cover_asset_id BIGINT NULL,
  source_coordinate_system VARCHAR(32) NOT NULL DEFAULT 'GCJ02',
  source_center_lat DECIMAL(10,7) NULL,
  source_center_lng DECIMAL(10,7) NULL,
  center_lat DECIMAL(10,7) NULL,
  center_lng DECIMAL(10,7) NULL,
  bounds_json JSON NULL,
  popup_config_json JSON NULL,
  display_config_json JSON NULL,
  sort_order INT NOT NULL DEFAULT 0,
  status VARCHAR(16) NOT NULL DEFAULT 'draft',
  published_at DATETIME NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted TINYINT NOT NULL DEFAULT 0,
  UNIQUE KEY uk_sub_maps_code (code),
  KEY idx_sub_maps_city_status_sort (city_id, status, sort_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
'@
    }

    if (-not (Test-MySqlTableExists -TableName 'content_asset_links')) {
        Invoke-MySqlNonQuery @'
CREATE TABLE content_asset_links (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  entity_type VARCHAR(32) NOT NULL,
  entity_id BIGINT NOT NULL,
  usage_type VARCHAR(32) NOT NULL,
  asset_id BIGINT NOT NULL,
  title_zh VARCHAR(255) NOT NULL DEFAULT '',
  title_en VARCHAR(255) NOT NULL DEFAULT '',
  title_zht VARCHAR(255) NOT NULL DEFAULT '',
  title_pt VARCHAR(255) NOT NULL DEFAULT '',
  description_zh TEXT NULL,
  description_en TEXT NULL,
  description_zht TEXT NULL,
  description_pt TEXT NULL,
  display_config_json JSON NULL,
  sort_order INT NOT NULL DEFAULT 0,
  status VARCHAR(16) NOT NULL DEFAULT 'draft',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted TINYINT NOT NULL DEFAULT 0,
  KEY idx_content_asset_links_entity_status_sort (entity_type, entity_id, status, sort_order),
  KEY idx_content_asset_links_usage_type (usage_type),
  KEY idx_content_asset_links_asset_id (asset_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
'@
    }

    Ensure-MySqlColumn -TableName 'pois' -ColumnName 'sub_map_id' -AlterSql "ALTER TABLE pois ADD COLUMN sub_map_id BIGINT NULL AFTER city_id;"
    Ensure-MySqlColumn -TableName 'pois' -ColumnName 'source_coordinate_system' -AlterSql "ALTER TABLE pois ADD COLUMN source_coordinate_system VARCHAR(32) NOT NULL DEFAULT 'GCJ02' AFTER address_pt;"
    Ensure-MySqlColumn -TableName 'pois' -ColumnName 'source_latitude' -AlterSql "ALTER TABLE pois ADD COLUMN source_latitude DECIMAL(10,7) NULL AFTER source_coordinate_system;"
    Ensure-MySqlColumn -TableName 'pois' -ColumnName 'source_longitude' -AlterSql "ALTER TABLE pois ADD COLUMN source_longitude DECIMAL(10,7) NULL AFTER source_latitude;"
    Ensure-MySqlColumn -TableName 'pois' -ColumnName 'map_icon_asset_id' -AlterSql "ALTER TABLE pois ADD COLUMN map_icon_asset_id BIGINT NULL AFTER cover_asset_id;"
    Ensure-MySqlColumn -TableName 'pois' -ColumnName 'popup_config_json' -AlterSql "ALTER TABLE pois ADD COLUMN popup_config_json JSON NULL AFTER intro_summary_pt;"
    Ensure-MySqlColumn -TableName 'pois' -ColumnName 'display_config_json' -AlterSql "ALTER TABLE pois ADD COLUMN display_config_json JSON NULL AFTER popup_config_json;"

    Invoke-MySqlNonQuery @'
UPDATE cities
SET
  source_coordinate_system = COALESCE(NULLIF(source_coordinate_system, ''), 'GCJ02'),
  source_center_lat = COALESCE(source_center_lat, center_lat),
  source_center_lng = COALESCE(source_center_lng, center_lng);

UPDATE pois
SET
  source_coordinate_system = COALESCE(NULLIF(source_coordinate_system, ''), 'GCJ02'),
  source_latitude = COALESCE(source_latitude, latitude),
  source_longitude = COALESCE(source_longitude, longitude);
'@

    Write-Host '[phase-09] reapplying canonical seed...'
    & (Join-Path $PSScriptRoot 'apply-phase-06-mock-seed.ps1') `
        -MySqlHost $MySqlHost `
        -MySqlPort $MySqlPort `
        -MySqlDatabase $MySqlDatabase `
        -MySqlUser $MySqlUser `
        -MySqlPassword $MySqlPassword

    Write-Host '[phase-09] waiting for public backend...'
    $publicHealth = Wait-JsonEndpoint -Url "$PublicBaseUrl/api/v1/health"
    Assert-Equals -Actual $publicHealth.code -Expected 0 -Label 'public health response code'

    Write-Host '[phase-09] waiting for admin backend...'
    $adminHealth = Wait-JsonEndpoint -Url "$AdminBaseUrl/api/v1/health"
    Assert-Equals -Actual $adminHealth.code -Expected 200 -Label 'admin health response code'

    Write-Host '[phase-09] logging in to admin backend...'
    $session = Invoke-ApiJson -Method 'Post' -Url "$AdminBaseUrl/api/admin/v1/auth/login" -Body @{
        username = $AdminUsername
        password = $AdminPassword
    }
    $adminHeaders = @{ Authorization = "Bearer $($session.token)" }

    Write-Host '[phase-09] validating canonical spatial rows in MySQL...'
    Assert-Equals -Actual (Invoke-MySqlScalar "SELECT COUNT(*) FROM cities WHERE status = 'published' AND code IN ('macau','hengqin','hong-kong','ecnu');") -Expected 4 -Label 'published canonical city count'
    Assert-Equals -Actual (Invoke-MySqlScalar "SELECT COUNT(*) FROM cities WHERE status = 'published' AND code IN ('taipa','coloane');") -Expected 0 -Label 'legacy pseudo-city published count'
    Assert-Equals -Actual (Invoke-MySqlScalar "SELECT COUNT(*) FROM sub_maps WHERE status = 'published' AND code IN ('macau-peninsula','taipa','coloane');") -Expected 3 -Label 'published Macau sub-map count'
    Assert-True -Condition ([int](Invoke-MySqlScalar "SELECT COUNT(*) FROM pois WHERE city_id = (SELECT id FROM cities WHERE code = 'macau' LIMIT 1) AND sub_map_id = (SELECT id FROM sub_maps WHERE code = 'taipa' LIMIT 1);") -gt 0) -Label 'Taipa POI count should be greater than zero'

    Write-Host '[phase-09] validating public city / sub-map / POI reads...'
    $cities = Invoke-ApiJson -Method 'Get' -Url "$PublicBaseUrl/api/v1/cities?locale=en"
    Assert-Equals -Actual @($cities).Count -Expected 4 -Label 'public canonical city count'
    $cityCodes = @($cities | ForEach-Object { $_.code })
    foreach ($expectedCode in @('macau', 'hengqin', 'hong-kong', 'ecnu')) {
        Assert-True -Condition ($cityCodes -contains $expectedCode) -Label "missing public city code $expectedCode"
    }
    Assert-True -Condition (-not ($cityCodes -contains 'taipa')) -Label 'legacy top-level city taipa should not be public'
    Assert-True -Condition (-not ($cityCodes -contains 'coloane')) -Label 'legacy top-level city coloane should not be public'

    $macau = @($cities | Where-Object { $_.code -eq 'macau' }) | Select-Object -First 1
    Assert-True -Condition ($null -ne $macau) -Label 'Macau city should be public'
    $macauSubMapsProperty = $macau.PSObject.Properties['subMaps']
    if ($null -ne $macauSubMapsProperty) {
        Assert-True -Condition (@($macauSubMapsProperty.Value).Count -eq 3) -Label 'Macau city should expose three nested sub-maps when embedded'
    }

    $subMaps = Invoke-ApiJson -Method 'Get' -Url "$PublicBaseUrl/api/v1/sub-maps?locale=en&cityCode=macau"
    Assert-Equals -Actual @($subMaps).Count -Expected 3 -Label 'public Macau sub-map count'
    $subMapCodes = @($subMaps | ForEach-Object { $_.code })
    foreach ($expectedSubMap in @('macau-peninsula', 'taipa', 'coloane')) {
        Assert-True -Condition ($subMapCodes -contains $expectedSubMap) -Label "missing public sub-map code $expectedSubMap"
    }

    $taipaPois = Invoke-ApiJson -Method 'Get' -Url "$PublicBaseUrl/api/v1/pois?locale=en&cityCode=macau&subMapCode=taipa"
    Assert-True -Condition (@($taipaPois).Count -gt 0) -Label 'Taipa public POI list should not be empty'
    $firstTaipaPoi = @($taipaPois)[0]
    Assert-Equals -Actual $firstTaipaPoi.cityCode -Expected 'macau' -Label 'POI cityCode'
    Assert-Equals -Actual $firstTaipaPoi.subMapCode -Expected 'taipa' -Label 'POI subMapCode'
    Assert-True -Condition ([bool]$firstTaipaPoi.subMapId) -Label 'POI subMapId should be present'
    Assert-True -Condition ([bool]$firstTaipaPoi.subMapName) -Label 'POI subMapName should be present'
    Assert-True -Condition ([bool]$firstTaipaPoi.sourceCoordinateSystem) -Label 'POI sourceCoordinateSystem should be present'
    Assert-True -Condition ($null -ne $firstTaipaPoi.sourceLatitude) -Label 'POI sourceLatitude should be present'
    Assert-True -Condition ($null -ne $firstTaipaPoi.sourceLongitude) -Label 'POI sourceLongitude should be present'

    Write-Host '[phase-09] validating admin write -> DB -> public propagation...'
    $cityPage = Invoke-ApiJson -Method 'Get' -Url "$AdminBaseUrl/api/admin/v1/map/cities?pageNum=1&pageSize=20" -Headers $adminHeaders
    $city = @($cityPage.list | Where-Object { $_.code -eq 'hong-kong' }) | Select-Object -First 1
    Assert-True -Condition ($null -ne $city) -Label 'admin city page should return Hong Kong'
    $cityId = [int]$city.id
    $restoreCityStatus = Invoke-MySqlScalar "SELECT status FROM cities WHERE id = $cityId;"
    $restoreCityPublishedAt = Invoke-MySqlScalar "SELECT COALESCE(DATE_FORMAT(published_at, '%Y-%m-%d %H:%i:%s'), '') FROM cities WHERE id = $cityId;"

    Invoke-MySqlNonQuery "UPDATE cities SET status = 'draft', published_at = NULL WHERE id = $cityId;"

    Start-Sleep -Seconds 2

    $publicCitiesBeforePublish = Invoke-ApiJson -Method 'Get' -Url "$PublicBaseUrl/api/v1/cities?locale=en"
    Assert-True -Condition (-not (@($publicCitiesBeforePublish | ForEach-Object { $_.code }) -contains 'hong-kong')) -Label 'Hong Kong should disappear from public cities while draft'

    $publishResponse = Invoke-ApiJson -Method 'Put' -Url "$AdminBaseUrl/api/admin/v1/map/cities/$cityId/publish" -Headers $adminHeaders
    Assert-Equals -Actual $publishResponse.status -Expected 'published' -Label 'admin publish response status'
    Assert-Equals -Actual (Invoke-MySqlScalar "SELECT status FROM cities WHERE id = $cityId;") -Expected 'published' -Label 'MySQL city status after admin publish'

    $publicCitiesAfterPublish = Invoke-ApiJson -Method 'Get' -Url "$PublicBaseUrl/api/v1/cities?locale=en"
    Assert-True -Condition (@($publicCitiesAfterPublish | ForEach-Object { $_.code }) -contains 'hong-kong') -Label 'Hong Kong should return to the public city list after admin publish'

    Write-Host '[phase-09] validating mini-program build compatibility...'
    Push-Location $ClientDir
    try {
        & npm run build:weapp
        if ($LASTEXITCODE -ne 0) {
            throw 'mini-program build failed'
        }
    } finally {
        Pop-Location
    }
    Assert-True -Condition (Test-Path (Join-Path $ClientDir 'dist\pages\map\index.js')) -Label 'built map page should exist'
    Assert-True -Condition (Test-Path (Join-Path $ClientDir 'dist\pages\index\index.js')) -Label 'built home page should exist'

    Write-Host '[phase-09] spatial smoke checks passed.'
    Write-Host "[phase-09] marker=$marker cityId=$cityId taipaPoi=$($firstTaipaPoi.code)"
} finally {
    if ($cityId -and $null -ne $restoreCityStatus) {
        try {
            if ($restoreCityStatus -eq 'published') {
                $publishedAtSql = if ([string]::IsNullOrWhiteSpace($restoreCityPublishedAt)) { 'NOW()' } else { "'$restoreCityPublishedAt'" }
                Invoke-MySqlNonQuery "UPDATE cities SET status = 'published', published_at = $publishedAtSql WHERE id = $cityId;"
            } else {
                $publishedAtSql = if ([string]::IsNullOrWhiteSpace($restoreCityPublishedAt)) { 'NULL' } else { "'$restoreCityPublishedAt'" }
                Invoke-MySqlNonQuery "UPDATE cities SET status = '$restoreCityStatus', published_at = $publishedAtSql WHERE id = $cityId;"
            }
        } catch {
        }
    }
}
