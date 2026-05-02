param(
  [Parameter(Mandatory = $true)]
  [string]$Config,
  [switch]$ValidateOnly,
  [switch]$ConfirmProduction,
  [switch]$Upload,
  [ValidateSet("uploaded", "approved", "published")]
  [string]$Promote,
  [string]$FfmpegPath = ''
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

function Resolve-FfmpegExecutable {
  param([string]$ExplicitPath)
  $candidates = @()
  if (-not [string]::IsNullOrWhiteSpace($ExplicitPath)) {
    $candidates += $ExplicitPath
  }
  if (-not [string]::IsNullOrWhiteSpace($env:PHASE36_FFMPEG_PATH)) {
    $candidates += $env:PHASE36_FFMPEG_PATH
  }
  $command = Get-Command ffmpeg -ErrorAction SilentlyContinue
  if ($command) {
    $candidates += $command.Source
  }
  $pythonProbe = @'
try:
    import imageio_ffmpeg
    print(imageio_ffmpeg.get_ffmpeg_exe())
except Exception:
    pass
'@
  $portable = $pythonProbe | python -
  if (-not [string]::IsNullOrWhiteSpace($portable)) {
    $candidates += $portable.Trim()
  }
  foreach ($candidate in $candidates) {
    if (-not [string]::IsNullOrWhiteSpace($candidate) -and (Test-Path -LiteralPath $candidate)) {
      return (Resolve-Path -LiteralPath $candidate).Path
    }
  }
  return $null
}

function Test-FfmpegFilter {
  param(
    [string]$FfmpegExecutable,
    [string]$FilterName
  )
  if ([string]::IsNullOrWhiteSpace($FfmpegExecutable)) {
    return $false
  }
  $result = Invoke-NativeProcess -FilePath $FfmpegExecutable -Arguments @("-hide_banner", "-filters")
  if ($result.ExitCode -ne 0) {
    return $false
  }
  $filters = $result.Output
  return ($filters -match "(^|\s)$([regex]::Escape($FilterName))(\s|$)")
}

function Invoke-NativeProcess {
  param(
    [Parameter(Mandatory = $true)][string]$FilePath,
    [Parameter(Mandatory = $true)][string[]]$Arguments,
    [switch]$Echo
  )
  $startInfo = New-Object System.Diagnostics.ProcessStartInfo
  $startInfo.FileName = $FilePath
  $startInfo.Arguments = ($Arguments | ForEach-Object { ConvertTo-ProcessArgument -Value $_ }) -join " "
  $startInfo.UseShellExecute = $false
  $startInfo.RedirectStandardOutput = $true
  $startInfo.RedirectStandardError = $true
  $process = New-Object System.Diagnostics.Process
  $process.StartInfo = $startInfo
  [void]$process.Start()
  $stdout = $process.StandardOutput.ReadToEnd()
  $stderr = $process.StandardError.ReadToEnd()
  $process.WaitForExit()
  $output = ($stdout + "`n" + $stderr).Trim()
  if ($Echo -and -not [string]::IsNullOrWhiteSpace($output)) {
    Write-Host $output
  }
  return [pscustomobject]@{
    ExitCode = $process.ExitCode
    Output = $output
  }
}

function Invoke-NativeProcessQuiet {
  param(
    [Parameter(Mandatory = $true)][string]$FilePath,
    [Parameter(Mandatory = $true)][string[]]$Arguments
  )
  $startInfo = New-Object System.Diagnostics.ProcessStartInfo
  $startInfo.FileName = $FilePath
  $startInfo.Arguments = ($Arguments | ForEach-Object { ConvertTo-ProcessArgument -Value $_ }) -join " "
  $startInfo.UseShellExecute = $false
  $startInfo.RedirectStandardOutput = $false
  $startInfo.RedirectStandardError = $false
  $startInfo.CreateNoWindow = $true
  $process = New-Object System.Diagnostics.Process
  $process.StartInfo = $startInfo
  [void]$process.Start()
  $process.WaitForExit()
  return $process.ExitCode
}

function ConvertTo-ProcessArgument {
  param([string]$Value)
  if ($null -eq $Value) {
    return '""'
  }
  if ($Value -notmatch '[\s"]') {
    return $Value
  }
  return '"' + $Value.Replace('"', '\"') + '"'
}

function Get-MediaDurationSeconds {
  param(
    [string]$FfmpegExecutable,
    [string]$MediaPath
  )
  $probe = Invoke-NativeProcess -FilePath $FfmpegExecutable -Arguments @("-hide_banner", "-i", $MediaPath)
  $joined = $probe.Output
  $match = [regex]::Match($joined, "Duration:\s*(\d{2}):(\d{2}):(\d{2}(?:\.\d+)?)")
  if (-not $match.Success) {
    return 30.0
  }
  return ([double]$match.Groups[1].Value * 3600.0) + ([double]$match.Groups[2].Value * 60.0) + [double]$match.Groups[3].Value
}

function ConvertTo-FfmpegFilterPath {
  param([string]$Path)
  $normalized = $Path.Replace('\', '/')
  $normalized = $normalized.Replace("'", "\\'")
  return ($normalized -replace ":", "\:")
}

function Get-MarkdownSection {
  param(
    [string]$Path,
    [string]$Anchor
  )
  if (-not (Test-Path -LiteralPath $Path) -or [string]::IsNullOrWhiteSpace($Anchor)) {
    return ""
  }
  $lines = Get-Content -LiteralPath $Path -Encoding UTF8
  $capturing = $false
  $captured = New-Object System.Collections.Generic.List[string]
  foreach ($line in $lines) {
    $trimmed = $line.Trim()
    if ($trimmed.StartsWith("## ")) {
      $heading = $trimmed.Substring(3).Trim().ToLowerInvariant().Replace(" ", "-")
      if ($capturing) { break }
      $capturing = ($heading -eq $Anchor.ToLowerInvariant() -or $heading.StartsWith($Anchor.ToLowerInvariant() + "-"))
      continue
    }
    if ($capturing) {
      $captured.Add($line)
    }
  }
  return ($captured -join "`n").Trim()
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

function Resolve-LocalInput {
  param(
    [object]$Row,
    [string]$LocalRoot,
    [string]$Kind
  )
  $localPath = [string]$Row.localPath
  if ([string]::IsNullOrWhiteSpace($localPath)) {
    $extension = if ($Kind -eq "audio") { ".mp3" } else { "" }
    $localPath = "audio/$($Row.itemKey)$extension"
  }
  $resolved = Join-Path $LocalRoot $localPath
  if ((Test-Path -LiteralPath $resolved) -or [string]::IsNullOrWhiteSpace([string]$Row.canonicalUrl)) {
    return $resolved
  }
  New-Item -ItemType Directory -Force -Path (Split-Path -Parent $resolved) | Out-Null
  Invoke-WebRequest -Uri ([string]$Row.canonicalUrl) -OutFile $resolved -UseBasicParsing | Out-Null
  return $resolved
}

function Ensure-SubtitleFile {
  param(
    [object]$Job,
    [string]$LocalRoot,
    [string]$AudioScriptsPath
  )
  $subtitlePath = Join-Path $LocalRoot $Job.subtitleTextFile
  if (Test-Path -LiteralPath $subtitlePath) {
    return $subtitlePath
  }
  $chapterAnchor = ($Job.audioItemKey -replace "^audio_", "" -replace "_narration$", "") -replace "_.*$", ""
  $scriptText = Get-MarkdownSection -Path $AudioScriptsPath -Anchor $chapterAnchor
  if ([string]::IsNullOrWhiteSpace($scriptText)) {
    Fail-Phase36 "SUBTITLE_SOURCE_MISSING" "Could not derive subtitle text for $($Job.jobCode) from audio-scripts.md"
  }
  New-Item -ItemType Directory -Force -Path (Split-Path -Parent $subtitlePath) | Out-Null
  Set-Content -LiteralPath $subtitlePath -Value $scriptText -Encoding UTF8
  return $subtitlePath
}

function Invoke-ImportVideo {
  param(
    [object]$Job,
    [string]$Manifest,
    [string]$SubtitleMetadataFile,
    [string]$BuildMode
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
    "--model-code", $BuildMode,
    "--asset-kind", "video",
    "--subtitle-metadata-file", $SubtitleMetadataFile,
    "--poster-fallback-item-key", $Job.posterFallbackItemKey,
    "--verification-note", "Phase 36 chapter video import",
    "--confirm-production"
  )
  if ($Promote) {
    $args += @("--promote", $Promote)
  }
  $output = & python @args
  if ($LASTEXITCODE -ne 0) {
    Fail-Phase36 "VIDEO_IMPORT_FAILED" "Video import failed for $($Job.jobCode)"
  }
  $jsonText = ($output | Out-String).Trim()
  if ([string]::IsNullOrWhiteSpace($jsonText)) {
    return $null
  }
  try {
    return $jsonText | ConvertFrom-Json
  } catch {
    return [pscustomobject]@{ rawOutput = $jsonText }
  }
}

function Update-ProductionReport {
  param(
    [string]$ReportPath,
    [object]$Job,
    [object]$ImportResult,
    [string]$BuildMode,
    [string]$SubtitlePath,
    [double]$DurationSeconds
  )
  $report = $null
  if (Test-Path -LiteralPath $ReportPath) {
    $report = Read-Utf8Json -Path $ReportPath
  } else {
    $report = [pscustomobject]@{
      schemaVersion = 1
      packageCode = $configJson.packageCode
      liveUpload = $false
      promote = $Promote
      rows = @()
      verificationItems = @()
    }
  }
  $rows = @()
  if ($report.rows) {
    $rows += @($report.rows | Where-Object { $_.itemKey -ne $Job.jobCode })
  }
  $row = [pscustomobject]@{
    itemKey = $Job.jobCode
    chapterCode = $Job.chapterCode
    localPath = $Job.outputRelativePath
    providerName = "ffmpeg-local"
    modelCode = $BuildMode
    estimatedCost = "0"
    actualCost = "0"
    assetKind = "video"
    status = if ($ImportResult -and $ImportResult.promotionStatus) { $ImportResult.promotionStatus } elseif ($Upload) { "uploaded" } else { "built" }
    audioItemKey = $Job.audioItemKey
    heroImageItemKey = $Job.heroImageItemKey
    subtitleTextFile = $Job.subtitleTextFile
    subtitleLocalPath = $Job.subtitleTextFile
    durationSeconds = [math]::Round($DurationSeconds, 3)
    versionId = if ($ImportResult) { $ImportResult.versionId } else { $null }
    assetId = if ($ImportResult) { $ImportResult.assetId } else { $null }
    promotionStatus = if ($ImportResult) { $ImportResult.promotionStatus } else { $null }
    cosObjectKey = if ($ImportResult) { $ImportResult.cosObjectKey } else { $Job.forcedCosObjectKey }
    canonicalUrl = if ($ImportResult) { $ImportResult.canonicalUrl } else { $null }
  }
  $rows += $row
  $report | Add-Member -NotePropertyName rows -NotePropertyValue $rows -Force
  $report | Add-Member -NotePropertyName liveUpload -NotePropertyValue ([bool]($report.liveUpload -or $Upload)) -Force
  if ($Promote) {
    $report | Add-Member -NotePropertyName promote -NotePropertyValue $Promote -Force
  }
  $json = $report | ConvertTo-Json -Depth 100
  Write-Utf8NoBom -Path $ReportPath -Value $json
}

function Write-Utf8NoBom {
  param(
    [Parameter(Mandatory = $true)][string]$Path,
    [Parameter(Mandatory = $true)][string]$Value
  )
  $utf8NoBom = New-Object System.Text.UTF8Encoding($false)
  [System.IO.File]::WriteAllText($Path, $Value, $utf8NoBom)
}

function Invoke-VideoBuild {
  param(
    [string]$FfmpegExecutable,
    [string]$HeroPath,
    [string]$AudioPath,
    [string]$SubtitlePath,
    [string]$OutputPath,
    [double]$DurationSeconds,
    [bool]$UseSubtitles
  )
  $baseFilter = "scale=1280:720:force_original_aspect_ratio=increase,crop=1280:720,zoompan=z='min(zoom+0.0007,1.08)':x='iw/2-(iw/zoom/2)':y='ih/2-(ih/zoom/2)':d=1:s=1280x720:fps=25"
  if ($UseSubtitles) {
    $subtitleEscaped = ConvertTo-FfmpegFilterPath -Path $SubtitlePath
    $filter = "$baseFilter,subtitles='$subtitleEscaped'"
  } else {
    $filter = $baseFilter
  }
  $exitCode = Invoke-NativeProcessQuiet -FilePath $FfmpegExecutable -Arguments @(
    "-hide_banner",
    "-loglevel", "error",
    "-y",
    "-loop", "1",
    "-framerate", "25",
    "-i", $HeroPath,
    "-i", $AudioPath,
    "-vf", $filter,
    "-t", [string]([math]::Ceiling($DurationSeconds + 0.5)),
    "-shortest",
    "-c:v", "libx264",
    "-preset", "veryfast",
    "-pix_fmt", "yuv420p",
    "-c:a", "aac",
    "-movflags", "+faststart",
    $OutputPath
  )
  return $exitCode
}

$configJson = Read-Utf8Json -Path $Config
$configDir = Split-Path -Parent (Resolve-Path -LiteralPath $Config)
$manifestPath = Join-Path (Get-Location) "docs/content-packages/east-west-war-and-coexistence/content-manifest.json"
$localRoot = Join-Path (Get-Location) $configJson.localRoot
$reportPath = Join-Path (Get-Location) $configJson.productionReport
$audioScriptsPath = Join-Path (Get-Location) "docs/content-packages/east-west-war-and-coexistence/audio-scripts.md"

$ffmpegExecutable = Resolve-FfmpegExecutable -ExplicitPath $FfmpegPath
if ([string]::IsNullOrWhiteSpace($ffmpegExecutable)) {
  Fail-Phase36 "FFMPEG_UNAVAILABLE" "ffmpeg is missing. Install ffmpeg, set PHASE36_FFMPEG_PATH, or install Python package imageio-ffmpeg."
}
$ffmpegHasZoompan = Test-FfmpegFilter -FfmpegExecutable $ffmpegExecutable -FilterName "zoompan"
if (-not $ffmpegHasZoompan) {
  Fail-Phase36 "FFMPEG_ZOOMPAN_UNAVAILABLE" "ffmpeg -filters does not include zoompan."
}
$ffmpegHasSubtitles = Test-FfmpegFilter -FfmpegExecutable $ffmpegExecutable -FilterName "subtitles"
$videoBuildMode = if ($ffmpegHasSubtitles) { "zoompan-subtitles-v1" } else { "zoompan-external-subtitles-v1" }
if (-not $ffmpegHasSubtitles) {
  Write-Warning "ffmpeg subtitles filter is unavailable; MP4 will be built without burned-in subtitles and subtitle metadata will be retained."
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
  $audioPath = Resolve-LocalInput -Row $audioRow -LocalRoot $localRoot -Kind "audio"
  $subtitlePath = Ensure-SubtitleFile -Job $job -LocalRoot $localRoot -AudioScriptsPath $audioScriptsPath
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
    Write-Host "VALID $($job.jobCode) ffmpeg=$ffmpegExecutable mode=$videoBuildMode"
    continue
  }

  if (-not $ConfirmProduction) {
    Fail-Phase36 "CONFIRMATION_REQUIRED" "Video build requires -ConfirmProduction unless -ValidateOnly is used."
  }

  New-Item -ItemType Directory -Force -Path (Split-Path -Parent $outputPath) | Out-Null
  $durationSeconds = Get-MediaDurationSeconds -FfmpegExecutable $ffmpegExecutable -MediaPath $audioPath
  $exitCode = Invoke-VideoBuild -FfmpegExecutable $ffmpegExecutable -HeroPath $heroPath -AudioPath $audioPath -SubtitlePath $subtitlePath -OutputPath $outputPath -DurationSeconds $durationSeconds -UseSubtitles $ffmpegHasSubtitles
  if ($exitCode -ne 0 -and $ffmpegHasSubtitles) {
    Write-Warning "Burned-in subtitle build failed for $($job.jobCode); retrying without burned-in subtitles."
    $videoBuildMode = "zoompan-external-subtitles-v1"
    $exitCode = Invoke-VideoBuild -FfmpegExecutable $ffmpegExecutable -HeroPath $heroPath -AudioPath $audioPath -SubtitlePath $subtitlePath -OutputPath $outputPath -DurationSeconds $durationSeconds -UseSubtitles $false
  }
  if ($exitCode -ne 0) {
    Fail-Phase36 "VIDEO_BUILD_FAILED" "ffmpeg failed for $($job.jobCode)"
  }

  $importResult = $null
  if ($Upload) {
    $subtitleMetadata = @{
      schemaVersion = 1
      subtitleTextFile = $job.subtitleTextFile
      audioItemKey = $job.audioItemKey
      heroImageItemKey = $job.heroImageItemKey
      buildMode = $videoBuildMode
    }
    $metadataPath = Join-Path $localRoot ("metadata/$($job.jobCode).subtitle.json")
    New-Item -ItemType Directory -Force -Path (Split-Path -Parent $metadataPath) | Out-Null
    Write-Utf8NoBom -Path $metadataPath -Value ($subtitleMetadata | ConvertTo-Json -Depth 20)
    $importResult = Invoke-ImportVideo -Job $job -Manifest $manifestPath -SubtitleMetadataFile $metadataPath -BuildMode $videoBuildMode
  }
  Update-ProductionReport -ReportPath $reportPath -Job $job -ImportResult $importResult -BuildMode $videoBuildMode -SubtitlePath $subtitlePath -DurationSeconds $durationSeconds
}

Write-Host "Phase 36 video validation/build completed. Live command: powershell -NoProfile -ExecutionPolicy Bypass -File scripts/local/material-production/phase36-build-video.ps1 -Config docs/content-packages/east-west-war-and-coexistence/production-runs/phase36-video-jobs.json -ConfirmProduction -Upload -Promote published"
