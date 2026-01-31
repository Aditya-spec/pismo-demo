# Use an official Java 21 runtime as a parent image
# "slim" images are smaller and faster
FROM eclipse-temurin:21-jdk-alpine

# Set the working directory inside the container
WORKDIR /app

# Copy the built jar file into the container
# Make sure the name matches your target jar (usually demo-0.0.1-SNAPSHOT.jar)
COPY target/demo-0.0.1-SNAPSHOT.jar app.jar

# Expose port 8080 (the default Spring Boot port)
EXPOSE 8080

# Run the jar file
ENTRYPOINT ["java", "-jar", "app.jar"]