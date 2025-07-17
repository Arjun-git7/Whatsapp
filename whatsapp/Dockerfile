# -------- Stage 1: Build the app --------
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app

# Copy pom.xml and source files
COPY whatsapp/pom.xml .
COPY src /whatsapp/src

# Build the project (skip tests for faster builds)
RUN mvn clean package -DskipTests

# -------- Stage 2: Run the app --------
#FROM openjdk:17-jdk-slim
FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app

# Copy the JAR from the build stage
COPY --from=build /whatsapp/target/whatsapp-0.0.1-SNAPSHOT.jar app.jar

# Expose the app port
EXPOSE 8080

# Run the Spring Boot app
ENTRYPOINT ["java", "-jar", "app.jar"]
