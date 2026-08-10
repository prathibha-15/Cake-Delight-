FROM eclipse-temurin:17-jre
WORKDIR /app
COPY target/api-gateway-0.0.1-SNAPSHOT.jar app.jar
COPY src/main/resources/static ./static
COPY src/main/resources/application.yml ./config/application.yml
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar","--spring.config.additional-location=/app/config/"]
