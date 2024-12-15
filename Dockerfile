# 1. Java 17 기반 이미지 사용
FROM openjdk:17-jdk-slim

# 2. 빌드된 JAR 파일을 컨테이너로 복사
ARG JAR_FILE=api/build/libs/api-0.0.1-SNAPSHOT.jar
COPY ${JAR_FILE} app.jar

# 3. 애플리케이션 실행
ENTRYPOINT ["java", "-jar", "/app.jar"]
