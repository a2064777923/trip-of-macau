param(
    [string]$AdminBaseUrl = 'http://127.0.0.1:8081',
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
$LogoPath = Join-Path $RootDir 'packages\client\src\assets\logo.png'
$marker = "phase10-$([DateTimeOffset]::UtcNow.ToUnixTimeMilliseconds())"
$assetId = $null
$storylineId = $null
$headers = $null

function Assert-True {
    param(
        [Parameter(Mandatory = $true)][bool]$Condition,
        [Parameter(Mandatory = $true)][string]$Label
    )

    if (-not $Condition) {
        throw $Label
    }
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

function Invoke-ApiEnvelope {
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

    return Invoke-RestMethod @params
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

function Invoke-CurlMultipartUpload {
    param(
        [Parameter(Mandatory = $true)][string]$Token,
        [Parameter(Mandatory = $true)][string]$FilePath,
        [Parameter(Mandatory = $true)][string]$ClientRelativePath
    )

    $curlArgs = @(
        '-sS',
        '-X', 'POST',
        "$AdminBaseUrl/api/admin/v1/content/assets/upload",
        '-H', "Authorization: Bearer $Token",
        '-F', "file=@$FilePath;type=image/png",
        '-F', 'assetKind=image',
        '-F', 'localeCode=zh-Hant',
        '-F', 'status=draft',
        '-F', 'uploadSource=picker',
        '-F', "clientRelativePath=$ClientRelativePath"
    )
    $json = & curl.exe @curlArgs
    if ($LASTEXITCODE -ne 0) {
        throw 'curl upload failed.'
    }
    return $json | ConvertFrom-Json
}

try {
    Assert-True -Condition (Test-Path $LogoPath) -Label "Logo file not found: $LogoPath"

    Write-Host '[phase-10] logging in as admin...'
    $authData = Invoke-ApiJson -Method 'POST' -Url "$AdminBaseUrl/api/admin/v1/auth/login" -Body @{
        username = $AdminUsername
        password = $AdminPassword
    }
    $headers = @{ Authorization = "Bearer $($authData.token)" }

    Write-Host '[phase-10] uploading shared media asset through canonical endpoint...'
    $clientRelativePath = "smoke/$marker/logo.png"
    $uploadResponse = Invoke-CurlMultipartUpload -Token $authData.token -FilePath $LogoPath -ClientRelativePath $clientRelativePath
    Assert-Equals -Actual $uploadResponse.code -Expected 0 -Label 'Media upload response code'
    $assetId = [int64]$uploadResponse.data.id
    Assert-True -Condition ($assetId -gt 0) -Label 'Uploaded asset ID was not returned.'

    Write-Host '[phase-10] validating persisted media policy and uploader metadata...'
    $dbRow = Invoke-MySqlScalar "SELECT CONCAT_WS('|', processing_policy_code, upload_source, uploaded_by_admin_id, uploaded_by_admin_name, client_relative_path) FROM content_assets WHERE id = $assetId;"
    $parts = $dbRow -split '\|', 5
    Assert-True -Condition ($parts.Length -ge 5) -Label 'Uploaded content_assets row was not found.'
    Assert-True -Condition (-not [string]::IsNullOrWhiteSpace($parts[0])) -Label 'processing_policy_code should not be empty.'
    Assert-Equals -Actual $parts[1] -Expected 'picker' -Label 'upload_source'
    Assert-True -Condition (-not [string]::IsNullOrWhiteSpace($parts[2])) -Label 'uploaded_by_admin_id should not be empty.'
    Assert-True -Condition (-not [string]::IsNullOrWhiteSpace($parts[3])) -Label 'uploaded_by_admin_name should not be empty.'
    Assert-Equals -Actual $parts[4] -Expected $clientRelativePath -Label 'client_relative_path'

    Write-Host '[phase-10] querying media library filters...'
    $encodedKeyword = [uri]::EscapeDataString($marker)
    $assetList = Invoke-ApiJson -Method 'GET' -Url "$AdminBaseUrl/api/admin/v1/content/assets?pageNum=1&pageSize=20&uploadSource=picker&keyword=$encodedKeyword" -Headers $headers
    Assert-True -Condition (@($assetList.list | Where-Object { $_.id -eq $assetId }).Count -eq 1) -Label 'Uploaded asset was not returned by filtered media library query.'

    Write-Host '[phase-10] binding asset to a storyline direct asset field...'
    $storyline = Invoke-ApiJson -Method 'POST' -Url "$AdminBaseUrl/api/admin/v1/storylines" -Headers $headers -Body @{
        code = "smoke_story_$marker"
        nameZh = "Phase 10 Smoke $marker"
        difficulty = 'easy'
        estimatedMinutes = 5
        coverAssetId = $assetId
        status = 'draft'
    }
    $storylineId = [int64]$storyline.storylineId
    Assert-True -Condition ($storylineId -gt 0) -Label 'Storyline binding did not return an ID.'

    Write-Host '[phase-10] verifying usage trace...'
    $usageSummary = Invoke-ApiJson -Method 'GET' -Url "$AdminBaseUrl/api/admin/v1/content/assets/$assetId/usages" -Headers $headers
    Assert-True -Condition ($usageSummary.usageCount -ge 1) -Label 'Asset usage summary should contain at least one usage.'
    Assert-True -Condition (@($usageSummary.usages | Where-Object { $_.entityType -eq 'storyline' -and $_.fieldName -eq 'coverAssetId' }).Count -ge 1) -Label 'Expected storyline cover usage was not found.'

    Write-Host '[phase-10] confirming guarded delete is blocked while asset is in use...'
    $deleteEnvelope = Invoke-ApiEnvelope -Method 'DELETE' -Url "$AdminBaseUrl/api/admin/v1/content/assets/$assetId" -Headers $headers
    Assert-True -Condition ($deleteEnvelope.code -ne 0) -Label 'Delete should be blocked for an in-use asset.'

    Write-Host '[phase-10] smoke verification passed.'
} finally {
    if ($storylineId) {
        try {
            Write-Host '[phase-10] cleaning up storyline binding...'
            Invoke-ApiJson -Method 'DELETE' -Url "$AdminBaseUrl/api/admin/v1/storylines/$storylineId" -Headers $headers | Out-Null
        } catch {
            Write-Warning "Failed to remove smoke storyline ${storylineId}: $($_.Exception.Message)"
        }
    }

    if ($assetId) {
        try {
            Write-Host '[phase-10] cleaning up uploaded media asset...'
            $cleanupEnvelope = Invoke-ApiEnvelope -Method 'DELETE' -Url "$AdminBaseUrl/api/admin/v1/content/assets/$assetId" -Headers $headers
            if ($cleanupEnvelope.code -ne 0) {
                Write-Warning "Asset cleanup is still blocked: $($cleanupEnvelope.message)"
            }
        } catch {
            Write-Warning "Failed to remove smoke asset ${assetId}: $($_.Exception.Message)"
        }
    }
}
