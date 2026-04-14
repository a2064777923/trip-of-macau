param(
    [string]$AdminBaseUrl = 'http://127.0.0.1:8081',
    [string]$PublicBaseUrl = 'http://127.0.0.1:8080',
    [string]$AdminUsername = 'admin',
    [string]$AdminPassword = 'admin123'
)

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'

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
        centerLat = $City.centerLat
        centerLng = $City.centerLng
        defaultZoom = $City.defaultZoom
        unlockType = $City.unlockType
        unlockConditionJson = $City.unlockConditionJson
        coverAssetId = $City.coverAssetId
        bannerAssetId = $City.bannerAssetId
        descriptionZh = $City.descriptionZh
        descriptionEn = $City.descriptionEn
        descriptionZht = $City.descriptionZht
        descriptionPt = $City.descriptionPt
        sortOrder = $City.sortOrder
        status = $City.status
        publishedAt = $publishedAt
    }
}

$marker = "phase8-$([DateTimeOffset]::UtcNow.ToUnixTimeMilliseconds())"
$restoreCity = $null
$cityId = $null

try {
    Write-Host '[phase-08] waiting for public backend...'
    $publicHealth = Wait-JsonEndpoint -Url "$PublicBaseUrl/api/v1/health"
    Assert-Equals -Actual $publicHealth.code -Expected 0 -Label 'public health response code'

    Write-Host '[phase-08] waiting for admin backend...'
    $adminHealth = Wait-JsonEndpoint -Url "$AdminBaseUrl/api/v1/health"
    Assert-Equals -Actual $adminHealth.code -Expected 200 -Label 'admin health response code'

    Write-Host '[phase-08] logging in to admin backend...'
    $session = Invoke-ApiJson -Method 'Post' -Url "$AdminBaseUrl/api/admin/v1/auth/login" -Body @{
        username = $AdminUsername
        password = $AdminPassword
    }
    $adminHeaders = @{ Authorization = "Bearer $($session.token)" }

    Write-Host '[phase-08] reading translation settings...'
    $settings = Invoke-ApiJson -Method 'Get' -Url "$AdminBaseUrl/api/admin/v1/system/translation-settings" -Headers $adminHeaders
    Assert-True -Condition (@('zh-Hant', 'zh-Hans', 'en', 'pt') -contains $settings.primaryAuthoringLocale) -Label 'primary authoring locale should be supported'
    Assert-True -Condition (@($settings.enginePriority).Count -gt 0) -Label 'engine priority should not be empty'

    Write-Host '[phase-08] selecting a city for localization smoke...'
    $citiesPage = Invoke-ApiJson -Method 'Get' -Url "$AdminBaseUrl/api/admin/v1/map/cities?pageNum=1&pageSize=20&status=published" -Headers $adminHeaders
    if (@($citiesPage.list).Count -eq 0) {
        $citiesPage = Invoke-ApiJson -Method 'Get' -Url "$AdminBaseUrl/api/admin/v1/map/cities?pageNum=1&pageSize=20" -Headers $adminHeaders
    }
    Assert-True -Condition (@($citiesPage.list).Count -gt 0) -Label 'at least one city must exist for phase 8 smoke'
    $cityId = [int]$citiesPage.list[0].id
    $cityDetail = Invoke-ApiJson -Method 'Get' -Url "$AdminBaseUrl/api/admin/v1/map/cities/$cityId" -Headers $adminHeaders
    $restoreCity = Convert-CityToUpsertBody -City $cityDetail

    $ptSubtitle = "Portuguese smoke $marker"
    $zhFallbackSubtitle = "Fallback smoke $marker"

    $mutated = Convert-CityToUpsertBody -City $cityDetail
    $mutated.subtitlePt = $ptSubtitle
    $mutated.subtitleZht = ''
    $mutated.subtitleZh = $zhFallbackSubtitle
    $mutated.status = 'published'

    Write-Host '[phase-08] updating city localized fields through admin API...'
    Invoke-ApiJson -Method 'Put' -Url "$AdminBaseUrl/api/admin/v1/map/cities/$cityId" -Headers $adminHeaders -Body @{
        upsert = $mutated
    } | Out-Null

    Start-Sleep -Seconds 2

    Write-Host '[phase-08] verifying public locale reads...'
    $publicCitiesPt = Invoke-ApiJson -Method 'Get' -Url "$PublicBaseUrl/api/v1/cities?locale=pt"
    $publicCityPt = @($publicCitiesPt | Where-Object { $_.code -eq $cityDetail.code }) | Select-Object -First 1
    Assert-True -Condition ($null -ne $publicCityPt) -Label 'updated city should be readable from public pt catalog'
    Assert-Equals -Actual $publicCityPt.subtitle -Expected $ptSubtitle -Label 'pt subtitle'

    $publicCitiesZht = Invoke-ApiJson -Method 'Get' -Url "$PublicBaseUrl/api/v1/cities?locale=zh-Hant"
    $publicCityZht = @($publicCitiesZht | Where-Object { $_.code -eq $cityDetail.code }) | Select-Object -First 1
    Assert-True -Condition ($null -ne $publicCityZht) -Label 'updated city should be readable from public zh-Hant catalog'
    Assert-Equals -Actual $publicCityZht.subtitle -Expected $zhFallbackSubtitle -Label 'zh-Hant fallback subtitle'

    if ($settings.bridgeEnabled) {
        Write-Host '[phase-08] verifying translation bridge endpoint...'
        $translation = Invoke-ApiJson -Method 'Post' -Url "$AdminBaseUrl/api/admin/v1/system/translate" -Headers $adminHeaders -Body @{
            sourceLocale = 'zh-Hant'
            targetLocales = @('en', 'pt')
            text = '澳門城市探索'
            overwriteFilledLocales = $false
        }
        Assert-True -Condition (@($translation.results).Count -eq 2) -Label 'translation endpoint should return per-locale results'
        $successfulTranslations = @($translation.results | Where-Object { $_.status -eq 'success' -and "$($_.translatedText)".Trim() -ne '' })
        Assert-True -Condition (@($successfulTranslations).Count -gt 0) -Label 'translation bridge should produce at least one successful translation result when enabled'
    } else {
        Write-Host '[phase-08] translation bridge disabled; skipping live translation call.'
    }

    Write-Host '[phase-08] localization smoke checks passed.'
    Write-Host "[phase-08] verified cityId=$cityId code=$($cityDetail.code) marker=$marker"
} finally {
    if ($cityId -and $null -ne $restoreCity) {
        try {
            $restoreLogin = Invoke-RestMethod -Method Post -Uri "$AdminBaseUrl/api/admin/v1/auth/login" -ContentType 'application/json' -Body (@{
                username = $AdminUsername
                password = $AdminPassword
            } | ConvertTo-Json)
            if ($restoreLogin.code -eq 0 -and $restoreLogin.data.token) {
                $restoreHeaders = @{ Authorization = "Bearer $($restoreLogin.data.token)" }
                Invoke-ApiJson -Method 'Put' -Url "$AdminBaseUrl/api/admin/v1/map/cities/$cityId" -Headers $restoreHeaders -Body @{
                    upsert = $restoreCity
                } | Out-Null
            }
        } catch {
        }
    }
}
