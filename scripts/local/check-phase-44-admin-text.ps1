param(
  [string]$FixturePath,
  [string]$ProjectRoot
)

$ErrorActionPreference = 'Stop'
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
$OutputEncoding = [System.Text.Encoding]::UTF8

if ([string]::IsNullOrWhiteSpace($ProjectRoot)) {
  $ProjectRoot = (Resolve-Path -LiteralPath (Join-Path $PSScriptRoot '..\..')).Path
}

if ([string]::IsNullOrWhiteSpace($FixturePath)) {
  $FixturePath = Join-Path $ProjectRoot 'scripts/local/fixtures/phase-44-admin-ia-text-checks.json'
}

function Read-Utf8Text {
  param([Parameter(Mandatory = $true)][string]$Path)
  return Get-Content -LiteralPath $Path -Encoding UTF8 -Raw
}

function Assert-ContainsText {
  param(
    [Parameter(Mandatory = $true)][string]$Content,
    [Parameter(Mandatory = $true)][string]$Needle,
    [Parameter(Mandatory = $true)][string]$Label
  )
  if (-not $Content.Contains($Needle)) {
    Write-Error "missing required text: $Label"
  }
}

function Assert-NotContainsText {
  param(
    [Parameter(Mandatory = $true)][string]$Content,
    [Parameter(Mandatory = $true)][string]$Needle,
    [Parameter(Mandatory = $true)][string]$Label
  )
  if ($Content.Contains($Needle)) {
    Write-Error "forbidden visible text found: $Label"
  }
}

function Assert-TargetedText {
  param(
    [Parameter(Mandatory = $true)][object[]]$Items,
    [Parameter(Mandatory = $true)][string]$GroupName
  )

  foreach ($item in $Items) {
    $targetFile = [string]$item.file
    $requiredText = [string]$item.text
    if ([string]::IsNullOrWhiteSpace($targetFile) -or [string]::IsNullOrWhiteSpace($requiredText)) {
      Write-Error "invalid fixture entry: $GroupName"
    }
    $targetPath = Join-Path $ProjectRoot $targetFile
    $targetContent = Read-Utf8Text -Path $targetPath
    Assert-ContainsText -Content $targetContent -Needle $requiredText -Label "$GroupName::$targetFile"
  }
}

$fixture = Get-Content -LiteralPath $FixturePath -Encoding UTF8 | ConvertFrom-Json
$layoutPath = Join-Path $ProjectRoot 'packages/admin/aoxiaoyou-admin-ui/src/layouts/DefaultLayout.tsx'
$layoutContent = Read-Utf8Text -Path $layoutPath

foreach ($required in @($fixture.requiredLayoutText)) {
  Assert-ContainsText -Content $layoutContent -Needle ([string]$required) -Label "requiredLayoutText"
}

foreach ($hidden in @($fixture.hiddenVisibleRouteText)) {
  Assert-NotContainsText -Content $layoutContent -Needle ([string]$hidden) -Label "hiddenVisibleRouteText"
}

foreach ($groupName in @('requiredPageMissionText', 'requiredUsabilityText', 'requiredMediaText')) {
  if ($fixture.PSObject.Properties.Name -contains $groupName) {
    $items = @($fixture.$groupName)
    if ($items.Count -gt 0) {
      Assert-TargetedText -Items $items -GroupName $groupName
    }
  }
}

Write-Host 'phase 44 admin text checks passed'
