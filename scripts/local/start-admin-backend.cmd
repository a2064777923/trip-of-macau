@echo off
setlocal
set "JAVA_HOME=C:\Program Files\Java\jdk-17"
set "PATH=%JAVA_HOME%\bin;%PATH%"
set "DB_URL=jdbc:mysql://127.0.0.1:3306/aoxiaoyou?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true&connectTimeout=3000&socketTimeout=10000&tcpKeepAlive=true"
set "DB_USERNAME=root"
set "DB_PASSWORD=root"
set "MONGODB_URI=mongodb://127.0.0.1:27017/aoxiaoyou_doc"
set "SERVER_PORT=8081"
set "SPRING_PROFILES_ACTIVE=local"
set "JWT_SECRET=please-change-this-secret-in-production"
cd /d %~dp0..\..\packages\admin\aoxiaoyou-admin-backend
echo [admin-backend] Using JAVA_HOME=%JAVA_HOME%
java -version
mvn -DskipTests spring-boot:run
