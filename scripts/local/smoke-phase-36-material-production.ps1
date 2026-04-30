param(
  [switch]$ValidateOnly,
  [switch]$IncludeVideo,
  [switch]$RunProduction,
  [string]$AdminBaseUrl = ''
)

$ErrorActionPreference = 'Stop'
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
$OutputEncoding = [System.Text.Encoding]::UTF8
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

function Assert-True {
  param([Parameter(Mandatory = $true)][bool]$Condition, [Parameter(Mandatory = $true)][string]$Message)
  if (-not $Condition) { throw $Message }
}

function Invoke-Api {
  param(
    [Parameter(Mandatory = $true)][string]$Method,
    [Parameter(Mandatory = $true)][string]$Url,
    [string]$Token,
    $Body
  )

  $client = New-Object System.Net.Http.HttpClient
  $request = New-Object System.Net.Http.HttpRequestMessage((New-Object System.Net.Http.HttpMethod($Method)), $Url)
  try {
    if ($Token) {
      $request.Headers.Authorization = New-Object System.Net.Http.Headers.AuthenticationHeaderValue('Bearer', (Normalize-BearerToken -Token $Token))
    }
    if ($null -ne $Body) {
      $json = $Body | ConvertTo-Json -Depth 100 -Compress
      $request.Content = New-Object System.Net.Http.StringContent($json, [System.Text.Encoding]::UTF8, 'application/json')
    }
    $response = $client.SendAsync($request).GetAwaiter().GetResult()
    $bytes = $response.Content.ReadAsByteArrayAsync().GetAwaiter().GetResult()
    $raw = [System.Text.Encoding]::UTF8.GetString($bytes)
    if (-not $response.IsSuccessStatusCode) {
      throw "$Method $Url failed with status $([int]$response.StatusCode): $raw"
    }
    if ([string]::IsNullOrWhiteSpace($raw)) { return $null }
    $envelope = $raw | ConvertFrom-Json
    if ($envelope.code -ne 0 -and $envelope.code -ne 200) {
      throw "$Method $Url failed: code=$($envelope.code), message=$($envelope.message)"
    }
    return $envelope.data
  } finally {
    if ($request) { $request.Dispose() }
    $client.Dispose()
  }
}

function Invoke-Head {
  param([Parameter(Mandatory = $true)][string]$Url)
  $client = New-Object System.Net.Http.HttpClient
  $request = New-Object System.Net.Http.HttpRequestMessage((New-Object System.Net.Http.HttpMethod('HEAD')), $Url)
  try {
    $response = $client.SendAsync($request).GetAwaiter().GetResult()
    return [int]$response.StatusCode
  } finally {
    if ($request) { $request.Dispose() }
    $client.Dispose()
  }
}

function Test-FfmpegSubtitleSupport {
  $command = Get-Command ffmpeg -ErrorAction SilentlyContinue
  if (-not $command) { return $false }
  $filters = & ffmpeg -hide_banner -filters 2>$null
  return ($filters -match 'subtitles')
}

function Read-Utf8Json {
  param([Parameter(Mandatory = $true)][string]$Path)
  return Get-Content -LiteralPath $Path -Raw -Encoding UTF8 | ConvertFrom-Json
}

function Require-Text {
  param(
    [Parameter(Mandatory = $true)][string]$Path,
    [Parameter(Mandatory = $true)][string[]]$Needles
  )
  $raw = Get-Content -LiteralPath $Path -Raw -Encoding UTF8
  foreach ($needle in $Needles) {
    Assert-True -Condition ($raw.Contains($needle)) -Message "$Path is missing required token: $needle"
  }
}

function Get-PackageItem {
  param(
    [Parameter(Mandatory = $true)]$PackageDetail,
    [Parameter(Mandatory = $true)][string]$ItemKey
  )
  $item = @($PackageDetail.items | Where-Object { [string]$_.itemKey -eq $ItemKey } | Select-Object -First 1)[0]
  Assert-True -Condition ($null -ne $item) -Message "Package item not found: $ItemKey"
  return $item
}

function Get-ItemVersions {
  param(
    [Parameter(Mandatory = $true)][string]$BaseUrl,
    [Parameter(Mandatory = $true)][string]$Token,
    [Parameter(Mandatory = $true)][long]$PackageId,
    [Parameter(Mandatory = $true)]$Item
  )
  return @(Invoke-Api -Method GET -Url "$BaseUrl/api/admin/v1/content/material-packages/$PackageId/items/$($Item.id)/versions" -Token $Token)
}

function Assert-SafeVersion {
  param([Parameter(Mandatory = $true)]$Version)
  Assert-True -Condition (-not [string]::IsNullOrWhiteSpace([string]$Version.localPath)) -Message "Version $($Version.id) has blank localPath"
  Assert-True -Condition (-not [string]::IsNullOrWhiteSpace([string]$Version.cosObjectKey)) -Message "Version $($Version.id) has blank cosObjectKey"
  Assert-True -Condition (-not ([string]$Version.localPath).Contains('..')) -Message "Version $($Version.id) localPath contains traversal"
  Assert-True -Condition (-not ([string]$Version.cosObjectKey).Contains('..')) -Message "Version $($Version.id) cosObjectKey contains traversal"
  Assert-True -Condition (-not [string]::IsNullOrWhiteSpace([string]$Version.providerName)) -Message "Version $($Version.id) has blank providerName"
  Assert-True -Condition (-not [string]::IsNullOrWhiteSpace([string]$Version.modelCode)) -Message "Version $($Version.id) has blank modelCode"
  Assert-True -Condition (-not [string]::IsNullOrWhiteSpace([string]$Version.promotionStatus)) -Message "Version $($Version.id) has blank promotionStatus"
}

$projectRoot = (Resolve-Path (Join-Path $PSScriptRoot '..\..')).Path
$manifestPath = Join-Path $projectRoot 'docs/content-packages/east-west-war-and-coexistence/content-manifest.json'
$batchPath = Join-Path $projectRoot 'docs/content-packages/east-west-war-and-coexistence/production-runs/phase36-batch.json'
$boardConfigPath = Join-Path $projectRoot 'docs/content-packages/east-west-war-and-coexistence/production-runs/phase36-board-slices.json'
$videoConfigPath = Join-Path $projectRoot 'docs/content-packages/east-west-war-and-coexistence/production-runs/phase36-video-jobs.json'
$preflightScript = Join-Path $projectRoot 'scripts/local/material-production/phase36-preflight.py'
$batchScript = Join-Path $projectRoot 'scripts/local/material-production/phase36-batch-produce.py'
$sliceScript = Join-Path $projectRoot 'scripts/local/material-production/phase36-slice-board.py'
$videoScript = Join-Path $projectRoot 'scripts/local/material-production/phase36-build-video.ps1'
$packageCode = 'east_west_war_and_coexistence_package'
$requiredItemKeys = @(
  'story_cover_copper_mirror',
  'hero_ch01_ama_coast',
  'pickup_ming_coastal_token',
  'title_harbour_witness_final',
  'audio_ch01_narration',
  'sfx_reward_unlock'
)

Assert-True -Condition (Test-Path -LiteralPath $manifestPath) -Message "Missing manifest: $manifestPath"
Assert-True -Condition (Test-Path -LiteralPath $batchPath) -Message "Missing batch config: $batchPath"
Assert-True -Condition (Test-Path -LiteralPath $boardConfigPath) -Message "Missing board slice config: $boardConfigPath"
Assert-True -Condition (Test-Path -LiteralPath $videoConfigPath) -Message "Missing video job config: $videoConfigPath"
Assert-True -Condition (Test-Path -LiteralPath $preflightScript) -Message "Missing phase36-preflight.py"
Assert-True -Condition (Test-Path -LiteralPath $batchScript) -Message "Missing phase36-batch-produce.py"
Assert-True -Condition (Test-Path -LiteralPath $sliceScript) -Message "Missing phase36-slice-board.py"
Assert-True -Condition (Test-Path -LiteralPath $videoScript) -Message "Missing phase36-build-video.ps1"

Require-Text -Path $videoScript -Needles @('FFMPEG_SUBTITLES_UNAVAILABLE', 'audioItemKey', 'subtitleMetadataJson', 'posterFallbackItemKey')
Require-Text -Path $videoConfigPath -Needles @('video_ch01_mirror_sea_clash', 'audio_ch01_narration', 'subtitleTextFile', 'posterFallbackItemKey', 'forcedCosObjectKey')

$preflightArgs = @(
  $preflightScript,
  '--manifest', $manifestPath,
  '--batch', $batchPath,
  '--dry-run'
)
& python @preflightArgs
Assert-True -Condition ($LASTEXITCODE -eq 0) -Message 'phase36-preflight.py dry-run failed'

$sliceDryRunArgs = @($sliceScript, '--config', $boardConfigPath, '--dry-run')
& python @sliceDryRunArgs
Assert-True -Condition ($LASTEXITCODE -eq 0) -Message 'phase36-slice-board.py --config dry-run failed'

$videoGateReady = Test-FfmpegSubtitleSupport
if (-not $videoGateReady) {
  Write-Host 'MAT-04 blocked: ffmpeg -filters does not expose subtitles support.'
}

$secretChecks = [ordered]@{
  PHASE36_ADMIN_BEARER_TOKEN = [bool](Normalize-BearerToken -Token (Get-EnvValue -Name 'PHASE36_ADMIN_BEARER_TOKEN'))
  OPENAI_API_KEY = [bool](Get-EnvValue -Name 'OPENAI_API_KEY')
  PHASE36_COS_READY = [bool](Get-EnvValue -Name 'PHASE36_COS_READY')
}

if ($ValidateOnly) {
  Write-Host "ValidateOnly dependency snapshot: adminToken=$($secretChecks.PHASE36_ADMIN_BEARER_TOKEN), openai=$($secretChecks.OPENAI_API_KEY), cos=$($secretChecks.PHASE36_COS_READY), ffmpegSubtitles=$videoGateReady"
  Write-Host 'Phase 36 material production validate-only checks passed'
  exit 0
}

$AdminBaseUrl = Resolve-Default -Value $AdminBaseUrl -EnvName 'PHASE36_ADMIN_BASE_URL' -Fallback 'http://127.0.0.1:8081'
$adminToken = Normalize-BearerToken -Token (Get-EnvValue -Name 'PHASE36_ADMIN_BEARER_TOKEN')
Assert-True -Condition (-not [string]::IsNullOrWhiteSpace($adminToken)) -Message 'PHASE36_ADMIN_BEARER_TOKEN is required for live Phase 36 smoke.'

$packagePage = Invoke-Api -Method GET -Url "$AdminBaseUrl/api/admin/v1/content/material-packages?pageNum=1&pageSize=50&keyword=$packageCode" -Token $adminToken
$package = @($packagePage.list | Where-Object { [string]$_.code -eq $packageCode } | Select-Object -First 1)[0]
Assert-True -Condition ($null -ne $package) -Message "Package not found: $packageCode"

$detail = Invoke-Api -Method GET -Url "$AdminBaseUrl/api/admin/v1/content/material-packages/$($package.id)" -Token $adminToken
Assert-True -Condition ([string]$detail.code -eq $packageCode) -Message "Unexpected package code: $($detail.code)"

$preflightBody = @{
  itemKeys = $requiredItemKeys
  targetAssetKinds = @('image', 'icon', 'audio', 'video')
  estimatedTotalCost = 0
  verificationNote = 'Phase 36 smoke /production/preflight'
}
$serverPreflight = Invoke-Api -Method POST -Url "$AdminBaseUrl/api/admin/v1/content/material-packages/$($package.id)/production/preflight" -Token $adminToken -Body $preflightBody
Assert-True -Condition ([int]$serverPreflight.itemCount -ge 1) -Message '/production/preflight returned no items'

if ($RunProduction) {
  Assert-True -Condition $secretChecks.OPENAI_API_KEY -Message 'OPENAI_API_KEY is required before live production.'
  $produceArgs = @(
    $batchScript,
    '--manifest', $manifestPath,
    '--batch', $batchPath,
    '--confirm-production', '--upload', '--promote', 'published'
  )
  # Required live command shape: --confirm-production --upload --promote published
  & python @produceArgs
  Assert-True -Condition ($LASTEXITCODE -eq 0) -Message 'phase36-batch-produce.py live command failed'

  $sliceLiveArgs = @(
    $sliceScript,
    '--config', $boardConfigPath,
    '--confirm-production', '--upload', '--promote', 'published'
  )
  # Required live command shape: phase36-slice-board.py --config ... --confirm-production --upload --promote published
  & python @sliceLiveArgs
  Assert-True -Condition ($LASTEXITCODE -eq 0) -Message 'phase36-slice-board.py live command failed'
}

$detail = Invoke-Api -Method GET -Url "$AdminBaseUrl/api/admin/v1/content/material-packages/$($package.id)" -Token $adminToken
$sampleVersions = @()
foreach ($itemKey in $requiredItemKeys) {
  $item = Get-PackageItem -PackageDetail $detail -ItemKey $itemKey
  $versions = Get-ItemVersions -BaseUrl $AdminBaseUrl -Token $adminToken -PackageId ([long]$package.id) -Item $item
  if ($itemKey -eq 'sfx_reward_unlock' -and $versions.Count -eq 0) {
    Write-Host 'sfx_reward_unlock remains manual_import_required / retry_required; no substitute audio was published.'
    continue
  }
  Assert-True -Condition ($versions.Count -ge 1) -Message "No version history for $itemKey in story_material_package_item_versions"
  $current = @($versions | Where-Object { $_.id -eq $item.currentVersionId } | Select-Object -First 1)[0]
  if (-not $current) { $current = @($versions | Select-Object -First 1)[0] }
  Assert-SafeVersion -Version $current
  $sampleVersions += [pscustomobject]@{ ItemKey = $itemKey; Item = $item; Version = $current }
}

$headSamples = @($sampleVersions | Where-Object { -not [string]::IsNullOrWhiteSpace([string]$_.Version.canonicalUrl) } | Select-Object -First 3)
foreach ($sample in $headSamples) {
  $statusCode = Invoke-Head -Url ([string]$sample.Version.canonicalUrl)
  Assert-True -Condition ($statusCode -ge 200 -and $statusCode -lt 400) -Message "HEAD failed for $($sample.ItemKey): status=$statusCode"
}

$rollbackSample = @($sampleVersions | Where-Object { $_.Version.id -and $_.Item.id } | Select-Object -First 1)[0]
if ($rollbackSample) {
  $promoteResult = Invoke-Api -Method POST -Url "$AdminBaseUrl/api/admin/v1/content/material-packages/$($package.id)/items/$($rollbackSample.Item.id)/production/promote" -Token $adminToken -Body @{
    versionId = $rollbackSample.Version.id
    targetStatus = 'published'
    verificationNote = 'Phase 36 smoke publish restore'
    superAdminConfirmation = $true
  }
  Assert-True -Condition ([string]$promoteResult.targetStatus -eq 'published') -Message 'Publish restore failed before /production/rollback smoke'

  $rollbackResult = Invoke-Api -Method POST -Url "$AdminBaseUrl/api/admin/v1/content/material-packages/$($package.id)/items/$($rollbackSample.Item.id)/production/rollback" -Token $adminToken -Body @{
    rollbackVersionId = $rollbackSample.Version.id
    verificationNote = 'Phase 36 smoke /production/rollback restore'
    superAdminConfirmation = $true
  }
  Assert-True -Condition ($rollbackResult.currentVersionId -eq $rollbackSample.Version.id) -Message '/production/rollback did not restore the expected version'
}

if ($IncludeVideo) {
  Assert-True -Condition $videoGateReady -Message 'IncludeVideo requires ffmpeg -filters subtitles support.'
  & powershell -NoProfile -ExecutionPolicy Bypass -File $videoScript -Config $videoConfigPath -ValidateOnly
  Assert-True -Condition ($LASTEXITCODE -eq 0) -Message 'phase36-build-video.ps1 -ValidateOnly failed'
}

Write-Host 'Phase 36 material production smoke passed'
