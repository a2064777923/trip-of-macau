# 澳小遊 Trip of Macau - Spring Boot Dockerfile
# 用于 CloudRun 容器部署

FROM maven:3.9-eclipse-temurin-17-alpine as build

# 设置工作目录
WORKDIR /app

# 复制 pom.xml 和 settings.xml
COPY settings.xml pom.xml ./

# 复制源代码
COPY src ./src

# 设置 Maven 镜像并构建
RUN mvn -s /app/settings.xml -f /app/pom.xml clean package -DskipTests

# 运行阶段 - 使用轻量级 JRE 镜像
FROM eclipse-temurin:17-jre-alpine

# 设置时区为上海
RUN apk add tzdata && cp /usr/share/zoneinfo/Asia/Shanghai /etc/localtime && echo Asia/Shanghai > /etc/timezone

# 安装证书（用于 HTTPS 调用）
RUN apk add --no-cache ca-certificates

# 创建非 root 用户
RUN addgroup -g 1001 -S appgroup && adduser -u 1001 -S appuser -G appgroup

# 设置工作目录
WORKDIR /app

# 复制构建产物
COPY --from=build /app/target/*.jar app.jar

# 修改文件所有者
RUN chown -R appuser:appgroup /app

# 切换到非 root 用户
USER appuser

# 暴露端口
EXPOSE 8080

# 健康检查
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

# 启动命令
ENTRYPOINT ["java", "-jar", "-Xms256m", "-Xmx512m", "/app/app.jar"]
