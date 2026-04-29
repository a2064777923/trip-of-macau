param(
  [string]$AdminBaseUrl = 'http://127.0.0.1:8081',
  [string]$Username = 'admin',
  [string]$Password = 'admin123'
)

$ErrorActionPreference = 'Stop'

function Invoke-Api {
  param(
    [Parameter(Mandatory = $true)][string]$Method,
    [Parameter(Mandatory = $true)][string]$Url,
    [string]$Token,
    $Body
  )

  $headers = @{}
  if ($Token) {
    $headers.Authorization = "Bearer $Token"
  }

  $params = @{
    Method = $Method
    Uri = $Url
    Headers = $headers
    ContentType = 'application/json; charset=utf-8'
  }
  if ($null -ne $Body) {
    $params.Body = ($Body | ConvertTo-Json -Depth 20)
  }

  return Invoke-RestMethod @params
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

$login = Ensure-Success -Context 'admin login' -Response (
  Invoke-Api -Method POST -Url "$AdminBaseUrl/api/admin/v1/auth/login" -Body @{
    username = $Username
    password = $Password
  }
)

$token = $login.token
Assert-True -Condition (-not [string]::IsNullOrWhiteSpace($token)) -Message 'admin login token is missing'

$overview = Ensure-Success -Context 'ai overview' -Response (
  Invoke-Api -Method GET -Url "$AdminBaseUrl/api/admin/v1/ai/overview" -Token $token
)
Assert-True -Condition ($null -ne $overview.summary) -Message 'overview summary is missing'

$capabilities = Ensure-Success -Context 'ai capabilities' -Response (
  Invoke-Api -Method GET -Url "$AdminBaseUrl/api/admin/v1/ai/capabilities" -Token $token
)
Assert-True -Condition (($capabilities | Measure-Object).Count -ge 5) -Message 'capability list is unexpectedly short'

$providers = Ensure-Success -Context 'ai providers' -Response (
  Invoke-Api -Method GET -Url "$AdminBaseUrl/api/admin/v1/ai/providers" -Token $token
)
Assert-True -Condition (($providers | Measure-Object).Count -ge 1) -Message 'provider list is empty'
Assert-True -Condition ($providers[0].PSObject.Properties.Name -notcontains 'apiKey') -Message 'provider read payload leaked apiKey field'

$tempProviderCode = "phase18-smoke-$([Guid]::NewGuid().ToString('N').Substring(0,8))"
$createdProvider = Ensure-Success -Context 'create ai provider' -Response (
  Invoke-Api -Method POST -Url "$AdminBaseUrl/api/admin/v1/ai/providers" -Token $token -Body @{
    providerName = $tempProviderCode
    displayName = 'Phase 18 Smoke Provider'
    providerType = 'custom'
    endpointStyle = 'openai_compatible'
    apiBaseUrl = 'https://example.invalid/v1'
    modelName = 'smoke-model'
    capabilityCodes = @('admin_prompt_drafting')
    featureFlagsJson = '{"smoke":true}'
    requestTimeoutMs = 3000
    maxRetries = 0
    quotaDaily = 1
    status = 0
  }
)
Assert-True -Condition ($createdProvider.providerName -eq $tempProviderCode) -Message 'created provider code mismatch'
Assert-True -Condition ($createdProvider.hasApiKey -eq 0) -Message 'smoke provider should not report an api key'

$deleteProvider = Ensure-Success -Context 'delete ai provider' -Response (
  Invoke-Api -Method DELETE -Url "$AdminBaseUrl/api/admin/v1/ai/providers/$($createdProvider.id)" -Token $token
)
Assert-True -Condition ($deleteProvider -eq $true) -Message 'delete provider did not return true'

$jobs = Ensure-Success -Context 'ai generation jobs' -Response (
  Invoke-Api -Method GET -Url "$AdminBaseUrl/api/admin/v1/ai/generation-jobs?pageNum=1&pageSize=5" -Token $token
)
Assert-True -Condition ($null -ne $jobs.list) -Message 'generation jobs page shape is invalid'

$logs = Ensure-Success -Context 'ai logs' -Response (
  Invoke-Api -Method GET -Url "$AdminBaseUrl/api/admin/v1/ai/logs?pageNum=1&pageSize=5" -Token $token
)
Assert-True -Condition ($null -ne $logs.list) -Message 'ai logs page shape is invalid'

Write-Host 'Phase 18 AI capability center smoke passed.'
