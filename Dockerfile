FROM amazoncorretto:17
LABEL authors="pavelkonkin"

COPY target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "/app.jar"]