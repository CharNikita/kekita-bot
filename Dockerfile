FROM eclipse-temurin:21-alpine
VOLUME /tmp
COPY build/libs/*.jar app.jar
ENTRYPOINT ["java", "-jar", "/app.jar"]