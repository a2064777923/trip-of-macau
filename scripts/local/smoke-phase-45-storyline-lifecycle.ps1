param(
  [string]$PublicBaseUrl = '',
  [string]$AdminBaseUrl = '',
  [string]$AdminToken = '',
  [string]$ExpectedStoryCode = 'east_west_war_and_coexistence',
  [string]$OldStoryCode = 'macau_fire_route'
)

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
$OutputEncoding = [System.Text.Encoding]::UTF8

if ([string]::IsNullOrWhiteSpace($PublicBaseUrl)) {
  $PublicBaseUrl = [Environment]::GetEnvironmentVariable('PHASE45_PUBLIC_BASE_URL')
}
if ([string]::IsNullOrWhiteSpace($PublicBaseUrl)) {
  $PublicBaseUrl = 'http://127.0.0.1:8080'
}
if ([string]::IsNullOrWhiteSpace($AdminBaseUrl)) {
  $AdminBaseUrl = [Environment]::GetEnvironmentVariable('PHASE45_ADMIN_BASE_URL')
}
if ([string]::IsNullOrWhiteSpace($AdminBaseUrl)) {
  $AdminBaseUrl = 'http://127.0.0.1:8081'
}
if ([string]::IsNullOrWhiteSpace($AdminToken)) {
  $AdminToken = [Environment]::GetEnvironmentVariable('PHASE45_ADMIN_TOKEN')
}

$PublicBaseUrl = $PublicBaseUrl.TrimEnd('/')
$AdminBaseUrl = $AdminBaseUrl.TrimEnd('/')
$results = New-Object System.Collections.Generic.List[object]

function Add-Result {
  param(
    [Parameter(Mandatory = $true)][string]$Check,
    [Parameter(Mandatory = $true)][ValidateSet('PASS', 'FAIL', 'SKIP')][string]$Status,
    [AllowEmptyString()][string]$Evidence
  )
  $results.Add([pscustomobject]@{
    Check = $Check
    Status = $Status
    Evidence = $Evidence
  })
}

function Assert-LocalUrl {
  param([Parameter(Mandatory = $true)][string]$Url)
  $uri = [Uri]$Url
  if (@('127.0.0.1', 'localhost', '::1') -notcontains $uri.Host.ToLowerInvariant()) {
    throw "Refusing non-local URL '$Url'."
  }
}

function Invoke-ApiGet {
  param(
    [Parameter(Mandatory = $true)][string]$Url,
    [hashtable]$Headers = @{}
  )
  return Invoke-RestMethod -Method Get -Uri $Url -Headers $Headers -TimeoutSec 15
}

Assert-LocalUrl -Url $PublicBaseUrl
Assert-LocalUrl -Url $AdminBaseUrl

$storyList = Invoke-ApiGet -Url "$PublicBaseUrl/api/v1/storylines?locale=zh-Hant"
if ($storyList.code -ne 0) {
  throw "Public story list returned code $($storyList.code): $($storyList.message)"
}
$stories = @($storyList.data)
$oldStories = @($stories | Where-Object { $_.code -eq $OldStoryCode })
$newStories = @($stories | Where-Object { $_.code -eq $ExpectedStoryCode })

if ($oldStories.Count -eq 0) {
  Add-Result -Check 'old duplicate hidden from public list' -Status 'PASS' -Evidence "$OldStoryCode absent"
} else {
  Add-Result -Check 'old duplicate hidden from public list' -Status 'FAIL' -Evidence "$OldStoryCode still returned $($oldStories.Count) time(s)"
}

if ($newStories.Count -eq 1) {
  Add-Result -Check 'new flagship present in public list' -Status 'PASS' -Evidence "id=$($newStories[0].id), code=$($newStories[0].code)"
} else {
  Add-Result -Check 'new flagship present in public list' -Status 'FAIL' -Evidence "$ExpectedStoryCode count=$($newStories.Count)"
}

if ($newStories.Count -ge 1) {
  $runtime = Invoke-ApiGet -Url "$PublicBaseUrl/api/v1/storylines/$($newStories[0].id)/runtime?locale=zh-Hant"
  $chapterCount = @($runtime.data.chapters).Count
  if ($runtime.code -eq 0 -and $chapterCount -eq 5) {
    Add-Result -Check 'new flagship runtime has five chapters' -Status 'PASS' -Evidence "storylineId=$($newStories[0].id), chapters=$chapterCount"
  } else {
    Add-Result -Check 'new flagship runtime has five chapters' -Status 'FAIL' -Evidence "code=$($runtime.code), chapters=$chapterCount"
  }
} else {
  Add-Result -Check 'new flagship runtime has five chapters' -Status 'SKIP' -Evidence 'new flagship not found'
}

if ([string]::IsNullOrWhiteSpace($AdminToken)) {
  Add-Result -Check 'old duplicate hard delete blocked by admin impact' -Status 'SKIP' -Evidence 'Set PHASE45_ADMIN_TOKEN or pass -AdminToken for admin impact check.'
} else {
  $adminHeaders = @{ Authorization = "Bearer $AdminToken" }
  $adminList = Invoke-ApiGet -Url "$AdminBaseUrl/api/admin/v1/storylines?pageNum=1&pageSize=100&keyword=$OldStoryCode" -Headers $adminHeaders
  $oldAdminStory = @($adminList.data.list | Where-Object { $_.code -eq $OldStoryCode } | Select-Object -First 1)
  if ($oldAdminStory.Count -eq 0) {
    Add-Result -Check 'old duplicate hard delete blocked by admin impact' -Status 'FAIL' -Evidence 'old duplicate not found in admin list'
  } else {
    $impact = Invoke-ApiGet -Url "$AdminBaseUrl/api/admin/v1/storylines/$($oldAdminStory[0].storylineId)/delete-impact" -Headers $adminHeaders
    if ($impact.code -ne 0 -or $null -eq $impact.data) {
      Add-Result -Check 'old duplicate hard delete blocked by admin impact' -Status 'FAIL' -Evidence "impact endpoint code=$($impact.code), message=$($impact.message)"
    } elseif ($impact.data.hardDeleteAllowed -eq $false) {
      Add-Result -Check 'old duplicate hard delete blocked by admin impact' -Status 'PASS' -Evidence "storylineId=$($oldAdminStory[0].storylineId), hardDeleteAllowed=false"
    } else {
      Add-Result -Check 'old duplicate hard delete blocked by admin impact' -Status 'FAIL' -Evidence "hardDeleteAllowed=$($impact.data.hardDeleteAllowed)"
    }
  }
}

$results | Format-Table -AutoSize
$failed = @($results | Where-Object { $_.Status -eq 'FAIL' })
if ($failed.Count -gt 0) {
  exit 1
}
exit 0
