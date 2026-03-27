FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

ARG JAR_FILE=build/libs/*.jar
COPY ${JAR_FILE} app.jar

ENTRYPOINT ["java", "-Xmx400M", "-Dspring.profiles.active=prod", "-jar", "app.jar"]