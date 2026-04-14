@echo off
setlocal
set "JAVA_HOME=C:\Program Files\Java\jdk-17"
set "PATH=C:\Program Files\Java\jdk-17\bin;%PATH%"
set "DB_URL=jdbc:mysql://127.0.0.1:3306/aoxiaoyou?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true&connectTimeout=3000&socketTimeout=10000&tcpKeepAlive=true"
set "DB_USERNAME=root"
set "DB_PASSWORD=Abc123456"
set "MONGODB_URI=mongodb://root:root@127.0.0.1:27017/aoxiaoyou_doc?authSource=admin"
set "SERVER_PORT=18081"
set "SPRING_PROFILES_ACTIVE=local"
set "JWT_SECRET=please-change-this-secret-in-production"
cd /d D:\Archive\trip-of-macau\packages\admin\aoxiaoyou-admin-backend
mvn -DskipTests spring-boot:run
