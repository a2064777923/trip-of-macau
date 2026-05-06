$ErrorActionPreference = 'Stop'

$RootDir = (Resolve-Path (Join-Path $PSScriptRoot '..\..')).Path
$ServerDir = Join-Path $RootDir 'packages\server'
$LogDir = Join-Path $RootDir 'logs\dev-runtime'
$JarPath = Join-Path $ServerDir 'target\trip-of-macau-server-0.1.0.jar'
$LogPath = Join-Path $LogDir 'public-8080-jar.log'

New-Item -ItemType Directory -Force -Path $LogDir | Out-Null

function Get-RequiredEnv {
  param([Parameter(Mandatory = $true)][string] $Name)
  $value = [Environment]::GetEnvironmentVariable($Name)
  if ([string]::IsNullOrWhiteSpace($value)) {
    throw "$Name is required. Set it in the current PowerShell session or an ignored local env file before running this helper."
  }
  return $value
}

function Get-OptionalEnv {
  param(
    [Parameter(Mandatory = $true)][string] $Name,
    [Parameter(Mandatory = $true)][string] $DefaultValue
  )
  $value = [Environment]::GetEnvironmentVariable($Name)
  if ([string]::IsNullOrWhiteSpace($value)) {
    return $DefaultValue
  }
  return $value
}

$dbUrl = Get-OptionalEnv -Name 'DB_URL' -DefaultValue 'jdbc:mysql://127.0.0.1:3306/aoxiaoyou?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true&connectTimeout=3000&socketTimeout=10000&tcpKeepAlive=true'
$dbUsername = Get-OptionalEnv -Name 'DB_USERNAME' -DefaultValue 'root'
$dbPassword = Get-RequiredEnv -Name 'DB_PASSWORD'
$jwtSecret = Get-RequiredEnv -Name 'JWT_SECRET'

$listeners = Get-NetTCPConnection -LocalPort 8080 -State Listen -ErrorAction SilentlyContinue
foreach ($listener in $listeners) {
  if ($listener.OwningProcess -and $listener.OwningProcess -ne $PID) {
    Stop-Process -Id $listener.OwningProcess -Force -ErrorAction SilentlyContinue
  }
}

if (-not (Test-Path -LiteralPath $JarPath)) {
  throw "Jar not found: $JarPath"
}

Remove-Item -LiteralPath $LogPath -Force -ErrorAction SilentlyContinue

$javaHome = if ($env:JAVA_HOME) { $env:JAVA_HOME } else { 'C:\Program Files\Java\jdk-17' }
$javaExe = Join-Path $javaHome 'bin\java.exe'
if (-not (Test-Path -LiteralPath $javaExe)) {
  $javaExe = 'java.exe'
}

$commands = @(
  '@echo off',
  'chcp 65001 > nul',
  "set ""JAVA_HOME=$javaHome""",
  'set "PATH=%JAVA_HOME%\bin;%PATH%"',
  "set ""DB_URL=$dbUrl""",
  "set ""DB_USERNAME=$dbUsername""",
  "set ""DB_PASSWORD=$dbPassword""",
  "set ""JWT_SECRET=$jwtSecret""",
  'set "SERVER_PORT=8080"',
  'set "SPRING_PROFILES_ACTIVE=local"',
  'set "REDIS_HOST=127.0.0.1"',
  'set "REDIS_PORT=6379"',
  'set "WECHAT_DEV_BYPASS_ENABLED=true"',
  'set "APP_LOG_LEVEL=INFO"',
  "cd /d ""$ServerDir""",
  """$javaExe"" -jar ""$JarPath"" > ""$LogPath"" 2>&1"
)

$CmdPath = Join-Path $LogDir 'run-public-8080.cmd'
Set-Content -LiteralPath $CmdPath -Value ($commands -join "`r`n") -Encoding ASCII

$process = Start-Process -FilePath $CmdPath -WorkingDirectory $ServerDir -WindowStyle Hidden -PassThru

[pscustomobject]@{
  ProcessId = $process.Id
  Jar = $JarPath
  Log = $LogPath
  Runner = $CmdPath
}
