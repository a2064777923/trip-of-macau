$ErrorActionPreference = 'Stop'

$RootDir = (Resolve-Path (Join-Path $PSScriptRoot '..\..')).Path
$ServerDir = Join-Path $RootDir 'packages\server'
$ComposeFile = Join-Path $RootDir 'docker-compose.local.yml'

if (-not $env:JAVA_HOME -or [string]::IsNullOrWhiteSpace($env:JAVA_HOME)) {
  $env:JAVA_HOME = 'C:\Program Files\Java\jdk-17'
}
$env:PATH = "$($env:JAVA_HOME)\bin;$($env:PATH)"

$env:DB_URL = 'jdbc:mysql://127.0.0.1:3306/aoxiaoyou?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true&connectTimeout=3000&socketTimeout=10000&tcpKeepAlive=true'
$env:DB_USERNAME = 'root'
$env:DB_PASSWORD = 'Abc123456'
$env:REDIS_HOST = '127.0.0.1'
$env:REDIS_PORT = '6379'
$env:SERVER_PORT = '8080'
$env:SPRING_PROFILES_ACTIVE = 'local'
$env:JWT_SECRET = 'please-change-this-secret-in-production'

Write-Host "[public-backend] ROOT_DIR=$RootDir" -ForegroundColor Cyan
Write-Host "[public-backend] SERVER_DIR=$ServerDir" -ForegroundColor Cyan
Write-Host "[public-backend] JAVA_HOME=$($env:JAVA_HOME)" -ForegroundColor Cyan

if (-not (Test-Path (Join-Path $ServerDir 'pom.xml'))) {
  throw '[public-backend] packages/server 不存在或 pom.xml 缺失'
}

if (-not (Get-Command java -ErrorAction SilentlyContinue)) {
  throw '[public-backend] 未找到 java，请先安装 JDK 17 或设置 JAVA_HOME'
}

if (-not (Get-Command mvn -ErrorAction SilentlyContinue)) {
  throw '[public-backend] 未找到 mvn，请先安装 Maven 并加入 PATH'
}

Write-Host '[public-backend] Java version:' -ForegroundColor Yellow
& java -version

$dockerCmd = Get-Command docker -ErrorAction SilentlyContinue
if ($dockerCmd) {
  if (Test-Path $ComposeFile) {
    Write-Host '[public-backend] 正在拉起本地 MySQL / Mongo 容器...' -ForegroundColor Yellow
    & docker compose -f $ComposeFile up -d mysql mongodb
  }
  else {
    Write-Host '[public-backend] 未找到 docker-compose.local.yml，跳过容器启动' -ForegroundColor DarkYellow
  }
}
else {
  Write-Host '[public-backend] 未找到 docker，跳过本地容器启动，请确保 MySQL 已在 127.0.0.1:3306 可用' -ForegroundColor DarkYellow
}

Set-Location $ServerDir
Write-Host "[public-backend] 正在启动 Spring Boot，端口 $($env:SERVER_PORT)" -ForegroundColor Green
Write-Host "[public-backend] Swagger: http://127.0.0.1:$($env:SERVER_PORT)/swagger-ui.html" -ForegroundColor Green
Write-Host "[public-backend] API Docs: http://127.0.0.1:$($env:SERVER_PORT)/v3/api-docs" -ForegroundColor Green

try {
  & mvn -DskipTests spring-boot:run
}
catch {
  Write-Host "[public-backend] 启动失败：$($_.Exception.Message)" -ForegroundColor Red
  throw
}
