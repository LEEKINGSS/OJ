FROM openjdk:17-jdk-slim
WORKDIR /app

# 复制本地编译好的 jar 包
COPY target/oj-sandbox.jar app.jar

EXPOSE 8122

ENTRYPOINT ["java", "-jar", "app.jar"]
