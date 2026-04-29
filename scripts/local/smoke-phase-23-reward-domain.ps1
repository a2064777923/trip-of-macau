param(
  [string]$AdminBaseUrl = 'http://127.0.0.1:8081',
  [string]$PublicBaseUrl = 'http://127.0.0.1:8080',
  [string]$Username = 'admin',
  [string]$Password = 'admin123'
)

$ErrorActionPreference = 'Stop'
Add-Type -AssemblyName System.Net.Http

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
      $request.Headers.Authorization = New-Object System.Net.Http.Headers.AuthenticationHeaderValue('Bearer', $Token)
    }

    if ($null -ne $Body) {
      $json = $Body | ConvertTo-Json -Depth 20
      $request.Content = New-Object System.Net.Http.StringContent($json, [System.Text.Encoding]::UTF8, 'application/json')
    }

    $response = $client.SendAsync($request).GetAwaiter().GetResult()
    $bytes = $response.Content.ReadAsByteArrayAsync().GetAwaiter().GetResult()
    $raw = [System.Text.Encoding]::UTF8.GetString($bytes)

    if (-not $response.IsSuccessStatusCode) {
      throw "$Method $Url failed with status $([int]$response.StatusCode): $raw"
    }

    $raw | ConvertFrom-Json
  } finally {
    if ($request) {
      $request.Dispose()
    }
    $client.Dispose()
  }
}

function Ensure-Success {
  param(
    [Parameter(Mandatory = $true)]$Response,
    [Parameter(Mandatory = $true)][string]$Context
  )

  if ($Response.code -ne 0 -and $Response.code -ne 200) {
    throw "$Context failed: $($Response.message)"
  }
  return $Response.data
}

function Assert-True {
  param(
    [Parameter(Mandatory = $true)][bool]$Condition,
    [Parameter(Mandatory = $true)][string]$Message
  )

  if (-not $Condition) {
    throw $Message
  }
}

function Assert-ZhReadableText {
  param(
    [Parameter(Mandatory = $true)][AllowEmptyString()][string]$Value,
    [Parameter(Mandatory = $true)][string]$Context
  )

  $trimmed = $Value.Trim()
  $hasCjk = $false
  foreach ($char in $trimmed.ToCharArray()) {
    $codePoint = [int][char]$char
    if (($codePoint -ge 0x3400 -and $codePoint -le 0x9FFF) -or ($codePoint -ge 0xF900 -and $codePoint -le 0xFAFF)) {
      $hasCjk = $true
      break
    }
  }

  Assert-True -Condition (-not [string]::IsNullOrWhiteSpace($trimmed)) -Message "$Context is blank"
  Assert-True -Condition ($trimmed -notmatch '^\?+$') -Message "$Context is corrupted with question marks: $trimmed"
  Assert-True -Condition (-not $trimmed.Contains([string][char]0xFFFD)) -Message "$Context contains replacement characters: $trimmed"
  Assert-True -Condition $hasCjk -Message "$Context is not readable Traditional Chinese text: $trimmed"
}

function Find-ByCode {
  param(
    [Parameter(Mandatory = $true)]$Items,
    [Parameter(Mandatory = $true)][string]$Code
  )

  @($Items | Where-Object { $_.code -eq $Code }) | Select-Object -First 1
}

$login = Ensure-Success -Context 'admin login' -Response (
  Invoke-Api -Method POST -Url "$AdminBaseUrl/api/admin/v1/auth/login" -Body @{
    username = $Username
    password = $Password
  }
)

$token = $login.token
Assert-True -Condition (-not [string]::IsNullOrWhiteSpace($token)) -Message 'admin login token is missing'

$governance = Ensure-Success -Context 'reward governance overview' -Response (
  Invoke-Api -Method GET -Url "$AdminBaseUrl/api/admin/v1/reward-governance" -Token $token
)
Assert-True -Condition ($governance.summary.ruleCount -ge 5) -Message 'reward governance rule count is unexpectedly low'
Assert-True -Condition ($governance.summary.presentationCount -ge 1) -Message 'reward governance presentation count is unexpectedly empty'
Assert-True -Condition ($governance.summary.linkedIndoorBehaviorCount -ge 1) -Message 'reward governance does not expose any indoor shared-rule ownership'

$indoorLinkedRule = @(
  @($governance.rules) | Where-Object {
    @($_.linkedOwners | Where-Object { $_.ownerDomain -eq 'indoor_behavior' }).Count -ge 1
  } | Select-Object -First 1
)
Assert-True -Condition ($indoorLinkedRule.Count -ge 1) -Message 'reward governance does not expose a shared rule bound to indoor behavior owners'
Assert-ZhReadableText -Value ([string]$indoorLinkedRule[0].nameZht) -Context 'admin indoor-linked shared rule name'
Assert-ZhReadableText -Value ([string]$indoorLinkedRule[0].summaryText) -Context 'admin indoor-linked shared rule summary'
Assert-True -Condition (
  @($indoorLinkedRule[0].linkedOwners | Where-Object { $_.ownerDomain -eq 'game_reward' }).Count -ge 1
) -Message 'indoor-linked shared rule is missing the paired reward-domain owner witness'
Assert-True -Condition (
  @($indoorLinkedRule[0].linkedOwners | Where-Object { $_.ownerDomain -eq 'indoor_behavior' }).Count -ge 1
) -Message 'indoor-linked shared rule is missing the indoor behavior owner witness'

$adminPrizePage = Ensure-Success -Context 'admin redeemable prizes' -Response (
  Invoke-Api -Method GET -Url "$AdminBaseUrl/api/admin/v1/redeemable-prizes?pageNum=1&pageSize=20" -Token $token
)
$adminRulePage = Ensure-Success -Context 'admin reward rules' -Response (
  Invoke-Api -Method GET -Url "$AdminBaseUrl/api/admin/v1/reward-rules?pageNum=1&pageSize=50" -Token $token
)
$adminHonorPage = Ensure-Success -Context 'admin honors' -Response (
  Invoke-Api -Method GET -Url "$AdminBaseUrl/api/admin/v1/game-rewards?pageNum=1&pageSize=50&honorsOnly=true" -Token $token
)

$adminPrizes = @($adminPrizePage.list)
$adminRules = @($adminRulePage.list)
$adminHonors = @($adminHonorPage.list)

Assert-True -Condition ($adminPrizes.Count -ge 2) -Message 'admin redeemable prize list is unexpectedly small'
Assert-True -Condition ($adminRules.Count -ge 5) -Message 'admin reward rule list is unexpectedly small'
Assert-True -Condition ($adminHonors.Count -ge 5) -Message 'admin honor list is unexpectedly small'

$adminOfflinePrize = Find-ByCode -Items $adminPrizes -Code 'prize_lisboeta_offline_postcard'
Assert-True -Condition ($null -ne $adminOfflinePrize) -Message 'admin offline prize seed is missing'
Assert-ZhReadableText -Value ([string]$adminOfflinePrize.nameZht) -Context 'admin offline prize name'
Assert-ZhReadableText -Value ([string]$adminOfflinePrize.subtitleZht) -Context 'admin offline prize subtitle'

$adminMirrorRule = Find-ByCode -Items $adminRules -Code 'rule_harbor_title_showcase'
Assert-True -Condition ($null -ne $adminMirrorRule) -Message 'admin harbor title rule is missing'
Assert-ZhReadableText -Value ([string]$adminMirrorRule.nameZht) -Context 'admin harbor title rule name'
Assert-ZhReadableText -Value ([string]$adminMirrorRule.summaryText) -Context 'admin harbor title rule summary'

$adminExpectedHonorCodes = @(
  'reward_title_mirror_harbor_chronicler',
  'reward_title_peninsula_memory_keeper',
  'reward_title_taipa_twilight_balladeer',
  'reward_title_coloane_tide_listener'
)

foreach ($code in $adminExpectedHonorCodes) {
  $item = Find-ByCode -Items $adminHonors -Code $code
  Assert-True -Condition ($null -ne $item) -Message "admin honor seed is missing: $code"
  Assert-ZhReadableText -Value ([string]$item.nameZht) -Context "admin honor name for $code"
}

$redeemablePrizes = @(
  Ensure-Success -Context 'public redeemable prizes' -Response (
    Invoke-Api -Method GET -Url "$PublicBaseUrl/api/v1/redeemable-prizes?locale=zh-Hant"
  )
)
Assert-True -Condition ($redeemablePrizes.Count -ge 2) -Message 'public redeemable prizes are unexpectedly empty'

$gameRewards = @(
  Ensure-Success -Context 'public game rewards' -Response (
    Invoke-Api -Method GET -Url "$PublicBaseUrl/api/v1/game-rewards?locale=zh-Hant"
  )
)
Assert-True -Condition ($gameRewards.Count -ge 5) -Message 'public game rewards are unexpectedly empty'

$honors = @(
  Ensure-Success -Context 'public honors' -Response (
    Invoke-Api -Method GET -Url "$PublicBaseUrl/api/v1/game-rewards?locale=zh-Hant&honorsOnly=true"
  )
)
Assert-True -Condition ($honors.Count -ge 5) -Message 'public honors are unexpectedly empty'

$offlinePrize = Find-ByCode -Items $redeemablePrizes -Code 'prize_lisboeta_offline_postcard'
Assert-True -Condition ($null -ne $offlinePrize) -Message 'public offline prize seed is missing'
Assert-True -Condition (@($offlinePrize.ruleSummaries).Count -ge 1) -Message 'public offline prize rule summary is missing'
Assert-ZhReadableText -Value ([string]$offlinePrize.name) -Context 'public offline prize name'
Assert-ZhReadableText -Value ([string]$offlinePrize.subtitle) -Context 'public offline prize subtitle'
Assert-ZhReadableText -Value ([string]$offlinePrize.description) -Context 'public offline prize description'
Assert-ZhReadableText -Value ([string]$offlinePrize.ruleSummaries[0].summaryText) -Context 'public offline prize rule summary'

$titleRewards = @($honors | Where-Object { $_.rewardType -eq 'title' })
Assert-True -Condition ($titleRewards.Count -ge 4) -Message 'public title rewards are unexpectedly low'

foreach ($code in $adminExpectedHonorCodes) {
  $item = Find-ByCode -Items $titleRewards -Code $code
  Assert-True -Condition ($null -ne $item) -Message "public title reward seed is missing: $code"
  Assert-True -Condition (@($item.ruleSummaries).Count -ge 1) -Message "public title rule summary is missing for $code"
  Assert-ZhReadableText -Value ([string]$item.name) -Context "public title name for $code"
  Assert-ZhReadableText -Value ([string]$item.description) -Context "public title description for $code"
  Assert-ZhReadableText -Value ([string]$item.ruleSummaries[0].summaryText) -Context "public title rule summary for $code"
}

$peninsulaTitle = Find-ByCode -Items $titleRewards -Code 'reward_title_peninsula_memory_keeper'
Assert-True -Condition (@($peninsulaTitle.relatedSubMaps | Where-Object { $_.code -eq 'macau-peninsula' }).Count -ge 1) -Message 'peninsula title does not expose the Macau Peninsula binding'

$taipaTitle = Find-ByCode -Items $titleRewards -Code 'reward_title_taipa_twilight_balladeer'
Assert-True -Condition (@($taipaTitle.relatedSubMaps | Where-Object { $_.code -eq 'taipa' }).Count -ge 1) -Message 'Taipa title does not expose the Taipa binding'

$coloaneTitle = Find-ByCode -Items $titleRewards -Code 'reward_title_coloane_tide_listener'
Assert-True -Condition (@($coloaneTitle.relatedSubMaps | Where-Object { $_.code -eq 'coloane' }).Count -ge 1) -Message 'Coloane title does not expose the Coloane binding'

$presentationId = $null
if ($offlinePrize.presentationId) {
  $presentationId = $offlinePrize.presentationId
} elseif ($gameRewards[0].presentationId) {
  $presentationId = $gameRewards[0].presentationId
}
Assert-True -Condition ($null -ne $presentationId) -Message 'no reward presentation id was found in public split reward payloads'

$presentation = Ensure-Success -Context 'public reward presentation' -Response (
  Invoke-Api -Method GET -Url "$PublicBaseUrl/api/v1/reward-presentations/${presentationId}?locale=zh-Hant"
)
Assert-True -Condition (-not [string]::IsNullOrWhiteSpace($presentation.presentationType)) -Message 'reward presentation type is missing'
Assert-True -Condition ($presentation.PSObject.Properties.Name -contains 'queuePolicy') -Message 'reward presentation queuePolicy is missing'
Assert-True -Condition ($presentation.PSObject.Properties.Name -contains 'minimumDisplayMs') -Message 'reward presentation minimumDisplayMs is missing'
Assert-ZhReadableText -Value ([string]$presentation.name) -Context 'public reward presentation name'
Assert-ZhReadableText -Value ([string]$presentation.summaryText) -Context 'public reward presentation summary'

foreach ($step in @($presentation.steps)) {
  Assert-ZhReadableText -Value ([string]$step.titleText) -Context "public reward presentation step title $($step.stepCode)"
}

Write-Host 'Phase 23 reward domain smoke passed with readable Traditional Chinese reward copy.'
