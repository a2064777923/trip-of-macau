param(
  [string]$AdminBaseUrl = '',
  [string]$ManifestPath = ''
)

$ErrorActionPreference = 'Stop'
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
Add-Type -AssemblyName System.Net.Http

function Get-EnvValue {
  param([Parameter(Mandatory = $true)][string]$Name)

  $value = [Environment]::GetEnvironmentVariable($Name)
  if ([string]::IsNullOrWhiteSpace($value)) { return $null }
  return $value.Trim()
}

function Resolve-Default {
  param([string]$Value, [string]$EnvName, [string]$Fallback)

  if (-not [string]::IsNullOrWhiteSpace($Value)) { return $Value.TrimEnd('/') }
  $envValue = Get-EnvValue -Name $EnvName
  if ($envValue) { return $envValue.TrimEnd('/') }
  return $Fallback.TrimEnd('/')
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

function Get-AdminCredentialsFromFile {
  param([Parameter(Mandatory = $true)][string]$ProjectRoot)

  $path = Join-Path $ProjectRoot 'tmp-admin-login.json'
  if (-not (Test-Path -LiteralPath $path)) { return $null }
  return Get-Content -LiteralPath $path -Raw -Encoding UTF8 | ConvertFrom-Json
}

function Get-AdminToken {
  param([Parameter(Mandatory = $true)][string]$ProjectRoot)

  $bearer = Normalize-BearerToken -Token (Get-EnvValue -Name 'PHASE33_ADMIN_BEARER')
  if ($bearer) { return $bearer }

  $fileCredentials = Get-AdminCredentialsFromFile -ProjectRoot $ProjectRoot
  if ($fileCredentials) {
    $fileTokenCandidate = $null
    if ($fileCredentials.PSObject.Properties.Name -contains 'token') {
      $fileTokenCandidate = [string]$fileCredentials.token
    } elseif ($fileCredentials.PSObject.Properties.Name -contains 'accessToken') {
      $fileTokenCandidate = [string]$fileCredentials.accessToken
    }
    $fileToken = Normalize-BearerToken -Token $fileTokenCandidate
    if ($fileToken) { return $fileToken }
  }
  return $null
}

function Invoke-UploadAsset {
  param(
    [Parameter(Mandatory = $true)][string]$Url,
    [Parameter(Mandatory = $true)][string]$Token,
    [Parameter(Mandatory = $true)][string]$FilePath,
    [Parameter(Mandatory = $true)][string]$AssetKind,
    [string]$ClientRelativePath
  )

  $client = New-Object System.Net.Http.HttpClient
  $content = New-Object System.Net.Http.MultipartFormDataContent
  try {
    $client.DefaultRequestHeaders.Authorization = New-Object System.Net.Http.Headers.AuthenticationHeaderValue('Bearer', (Normalize-BearerToken -Token $Token))

    $fileBytes = [System.IO.File]::ReadAllBytes($FilePath)
    $fileContent = New-Object System.Net.Http.ByteArrayContent(,$fileBytes)
    $fileContent.Headers.ContentType = New-Object System.Net.Http.Headers.MediaTypeHeaderValue('application/octet-stream')
    $content.Add($fileContent, 'file', [System.IO.Path]::GetFileName($FilePath))
    $content.Add((New-Object System.Net.Http.StringContent($AssetKind, [System.Text.Encoding]::UTF8)), 'assetKind')
    $content.Add((New-Object System.Net.Http.StringContent('zh-Hant', [System.Text.Encoding]::UTF8)), 'localeCode')
    $content.Add((New-Object System.Net.Http.StringContent('seed', [System.Text.Encoding]::UTF8)), 'uploadSource')
    $content.Add((New-Object System.Net.Http.StringContent('draft', [System.Text.Encoding]::UTF8)), 'status')
    if (-not [string]::IsNullOrWhiteSpace($ClientRelativePath)) {
      $content.Add((New-Object System.Net.Http.StringContent($ClientRelativePath, [System.Text.Encoding]::UTF8)), 'clientRelativePath')
    }

    $response = $client.PostAsync($Url, $content).GetAwaiter().GetResult()
    $bytes = $response.Content.ReadAsByteArrayAsync().GetAwaiter().GetResult()
    $raw = [System.Text.Encoding]::UTF8.GetString($bytes)
    if (-not $response.IsSuccessStatusCode) {
      throw "Upload failed with status $([int]$response.StatusCode): $raw"
    }
    if ([string]::IsNullOrWhiteSpace($raw)) { return $null }
    $json = $raw | ConvertFrom-Json
    if ($json.code -ne 0 -and $json.code -ne 200) {
      throw "Upload API failed: code=$($json.code), message=$($json.message)"
    }
    return $json.data
  } finally {
    $content.Dispose()
    $client.Dispose()
  }
}

$ProjectRoot = (Resolve-Path (Join-Path $PSScriptRoot '..\..')).Path
$AdminBaseUrl = Resolve-Default -Value $AdminBaseUrl -EnvName 'PHASE33_ADMIN_BASE_URL' -Fallback 'http://127.0.0.1:8081'
if ([string]::IsNullOrWhiteSpace($ManifestPath)) {
  $ManifestPath = Join-Path $ProjectRoot 'docs/content-packages/east-west-war-and-coexistence/content-manifest.json'
}
$manifest = Get-Content -LiteralPath $ManifestPath -Raw -Encoding UTF8 | ConvertFrom-Json
$localRoot = Join-Path $ProjectRoot $manifest.localRoot
$uploadUrl = "$AdminBaseUrl/api/admin/v1/content/assets/upload"
$token = $null
$summary = New-Object System.Collections.Generic.List[object]

foreach ($material in @($manifest.materials)) {
  $localPath = Join-Path $localRoot ([string]$material.localPath)
  if (-not (Test-Path -LiteralPath $localPath -PathType Leaf)) {
    Write-Host "planned-only: $($material.itemKey) -> $($material.localPath)"
    $summary.Add([ordered]@{
      itemKey = [string]$material.itemKey
      localPath = [string]$material.localPath
      uploaded = $false
      assetId = $material.contentAssetId
      cosObjectKey = [string]$material.cosObjectKey
      status = 'planned-only'
    })
    continue
  }

  if (-not $token) {
    $token = Get-AdminToken -ProjectRoot $ProjectRoot
    if (-not $token) {
      throw 'PHASE33_ADMIN_BEARER or tmp-admin-login.json token is required when local files exist for upload.'
    }
  }

  $uploaded = Invoke-UploadAsset -Url $uploadUrl -Token $token -FilePath $localPath -AssetKind ([string]$material.assetKind) -ClientRelativePath ([string]$material.localPath)
  $summary.Add([ordered]@{
    itemKey = [string]$material.itemKey
    localPath = [string]$material.localPath
    uploaded = $true
    assetId = $uploaded.id
    cosObjectKey = $uploaded.objectKey
    status = 'uploaded'
  })
}

$summaryJson = $summary | ConvertTo-Json -Depth 20
[Console]::Out.WriteLine($summaryJson)
