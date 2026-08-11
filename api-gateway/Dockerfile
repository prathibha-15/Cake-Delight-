FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /workspace
COPY pom.xml .
COPY src ./src
RUN mvn -q -DskipTests package

FROM eclipse-temurin:17-jdk
WORKDIR /app
COPY --from=build /workspace/target/api-gateway-0.0.1-SNAPSHOT.jar app.jar
COPY src/main/resources/static ./static
COPY src/main/resources/application.yml ./config/application.yml
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar","--spring.config.additional-location=/app/config/"]

