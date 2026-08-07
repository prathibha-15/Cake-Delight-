# Use Eclipse Temurin Java 17
FROM eclipse-temurin:17-jdk

# Set working directory
WORKDIR /app

# Copy the jar file
COPY target/catalog-service-0.0.1-SNAPSHOT.jar app.jar

# Expose application port
EXPOSE 8081

# Run the application
ENTRYPOINT ["java","-jar","app.jar"]