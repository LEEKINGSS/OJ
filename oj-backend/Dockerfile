FROM openjdk:17-jdk-slim
WORKDIR /app

# 复制本地编译好的 jar 包
COPY target/oj-backend.jar app.jar

EXPOSE 8121

ENTRYPOINT ["java", "-jar", "app.jar"]
