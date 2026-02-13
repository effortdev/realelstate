# 1. 실행 환경 (Java 17 JRE 사용으로 이미지 용량 최소화)
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# 2. JAR 파일 복사
# Gradle 빌드 시 생성되는 파일명 패턴에 맞게 설정
ARG JAR_FILE=build/libs/*.jar
COPY ${JAR_FILE} app.jar

# 3. 1GB 저사양 서버를 위한 JVM 메모리 최적화 설정
# -Xmx400M: 최대 메모리를 400MB로 제한하여 서버 다운 방지
ENTRYPOINT ["java", "-Xmx400M", "-Dspring.profiles.active=prod", "-jar", "app.jar"]