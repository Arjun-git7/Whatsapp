# Use OpenJDK image
FROM openjdk:17-jdk-slim

# Set working directory inside container
WORKDIR /app

# Copy jar file (replace with your actual jar name)
COPY target/whatsapp-0.0.1-SNAPSHOT.jar app.jar

# Expose the port
EXPOSE 8081

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
