param(
  [Parameter(Mandatory = $true)]
  [string]$Config,
  [switch]$ValidateOnly,
  [switch]$ConfirmProduction,
  [switch]$Upload,
  [ValidateSet("uploaded", "approved", "published")]
  [string]$Promote
)

$ErrorActionPreference = "Stop"

function Fail-Phase36 {
  param([string]$Code, [string]$Message)
  Write-Error "$Code $Message"
  exit 1
}

function Read-Utf8Json {
  param([string]$Path)
  if (-not (Test-Path -LiteralPath $Path)) {
    Fail-Phase36 "CONFIG_MISSING" "Config file not found: $Path"
  }
  $raw = Get-Content -LiteralPath $Path -Raw -Encoding UTF8
  return $raw | ConvertFrom-Json
}

function Test-FfmpegSubtitles {
  $command = Get-Command ffmpeg -ErrorAction SilentlyContinue
  if (-not $command) {
    return $false
  }
  $filters = & ffmpeg -hide_banner -filters 2>$null
  return ($filters -match "subtitles")
}

function Get-ReportRow {
  param(
    [object]$Report,
    [string]$ItemKey
  )
  if (-not $Report -or -not $Report.rows) {
    return $null
  }
  return @($Report.rows | Where-Object { $_.itemKey -eq $ItemKey } | Select-Object -First 1)[0]
}

function Assert-ReadyInput {
  param(
    [object]$Report,
    [string]$ItemKey,
    [string]$Kind
  )
  $row = Get-ReportRow -Report $Report -ItemKey $ItemKey
  if (-not $row) {
    Fail-Phase36 "INPUT_NOT_PRODUCED" "$Kind item is missing from phase36-production-report.json: $ItemKey"
  }
  if ($row.promotionStatus -ne "published" -and $row.status -ne "published") {
    Fail-Phase36 "INPUT_NOT_PUBLISHED" "$Kind item is not published: $ItemKey"
  }
  return $row
}

function Invoke-ImportVideo {
  param(
    [object]$Job,
    [string]$Manifest,
    [string]$SubtitleMetadataJson
  )
  # phase36-import-assets.py performs the authenticated package-scoped /production/import call.
  $scriptPath = Join-Path $PSScriptRoot "phase36-import-assets.py"
  $args = @(
    $scriptPath,
    "--manifest", $Manifest,
    "--item-key", $Job.jobCode,
    "--relative-local-path", $Job.outputRelativePath,
    "--forced-cos-object-key", $Job.forcedCosObjectKey,
    "--provider-name", "ffmpeg-local",
    "--model-code", "zoompan-v1",
    "--asset-kind", "video",
    "--subtitle-metadata-json", $SubtitleMetadataJson,
    "--poster-fallback-item-key", $Job.posterFallbackItemKey,
    "--verification-note", "Phase 36 chapter video import",
    "--confirm-production"
  )
  if ($Promote) {
    $args += @("--promote", $Promote)
  }
  & python @args
  if ($LASTEXITCODE -ne 0) {
    Fail-Phase36 "VIDEO_IMPORT_FAILED" "Video import failed for $($Job.jobCode)"
  }
}

$configJson = Read-Utf8Json -Path $Config
$configDir = Split-Path -Parent (Resolve-Path -LiteralPath $Config)
$manifestPath = Join-Path (Get-Location) "docs/content-packages/east-west-war-and-coexistence/content-manifest.json"
$localRoot = Join-Path (Get-Location) $configJson.localRoot
$reportPath = Join-Path (Get-Location) $configJson.productionReport

if (-not (Test-FfmpegSubtitles)) {
  Fail-Phase36 "FFMPEG_SUBTITLES_UNAVAILABLE" "ffmpeg is missing or ffmpeg -filters does not include subtitles."
}

if ($Upload -and -not $ConfirmProduction) {
  Fail-Phase36 "CONFIRMATION_REQUIRED" "-Upload requires -ConfirmProduction"
}

if ($Promote -and -not $Upload) {
  Fail-Phase36 "UPLOAD_REQUIRED" "-Promote requires -Upload so status changes stay tied to an imported video asset."
}

$productionReport = $null
if (Test-Path -LiteralPath $reportPath) {
  $productionReport = Read-Utf8Json -Path $reportPath
}

foreach ($job in $configJson.jobs) {
  $heroRow = Assert-ReadyInput -Report $productionReport -ItemKey $job.heroImageItemKey -Kind "hero image"
  $audioRow = Assert-ReadyInput -Report $productionReport -ItemKey $job.audioItemKey -Kind "audioItemKey"

  $heroPath = Join-Path $localRoot $heroRow.localPath
  $audioPath = Join-Path $localRoot $audioRow.localPath
  $subtitlePath = Join-Path $localRoot $job.subtitleTextFile
  $outputPath = Join-Path $localRoot $job.outputRelativePath

  if (-not (Test-Path -LiteralPath $heroPath)) {
    Fail-Phase36 "HERO_IMAGE_MISSING" "Hero image file not found: $heroPath"
  }
  if (-not (Test-Path -LiteralPath $audioPath)) {
    Fail-Phase36 "NARRATION_AUDIO_MISSING" "Narration audio file not found: $audioPath"
  }
  if (-not (Test-Path -LiteralPath $subtitlePath)) {
    Fail-Phase36 "SUBTITLE_FILE_MISSING" "subtitleTextFile not found: $subtitlePath"
  }

  if ($ValidateOnly) {
    Write-Host "VALID $($job.jobCode)"
    continue
  }

  if (-not $ConfirmProduction) {
    Fail-Phase36 "CONFIRMATION_REQUIRED" "Video build requires -ConfirmProduction unless -ValidateOnly is used."
  }

  New-Item -ItemType Directory -Force -Path (Split-Path -Parent $outputPath) | Out-Null
  $subtitleEscaped = ($subtitlePath -replace "\\", "/") -replace ":", "\\:"
  $filter = "scale=1280:720:force_original_aspect_ratio=increase,crop=1280:720,zoompan=z='min(zoom+0.0008,1.08)':d=180:s=1280x720,subtitles='$subtitleEscaped'"
  & ffmpeg -y -loop 1 -i $heroPath -i $audioPath -vf $filter -shortest -c:v libx264 -pix_fmt yuv420p -c:a aac $outputPath
  if ($LASTEXITCODE -ne 0) {
    Fail-Phase36 "VIDEO_BUILD_FAILED" "ffmpeg failed for $($job.jobCode)"
  }

  if ($Upload) {
    $subtitleMetadataJson = @{
      schemaVersion = 1
      subtitleTextFile = $job.subtitleTextFile
      audioItemKey = $job.audioItemKey
      heroImageItemKey = $job.heroImageItemKey
    } | ConvertTo-Json -Compress
    Invoke-ImportVideo -Job $job -Manifest $manifestPath -SubtitleMetadataJson $subtitleMetadataJson
  }
}

Write-Host "Phase 36 video validation/build completed. Live command: powershell -NoProfile -ExecutionPolicy Bypass -File scripts/local/material-production/phase36-build-video.ps1 -Config docs/content-packages/east-west-war-and-coexistence/production-runs/phase36-video-jobs.json -ConfirmProduction -Upload -Promote published"
