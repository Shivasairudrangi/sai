FROM eclipse-temurin:21-jdk

WORKDIR /app

# Copy the Maven wrapper and POM
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./

# Build dependencies
RUN chmod +x ./mvnw && ./mvnw dependency:go-offline -B

# Copy source code
COPY src ./src

# Package the application
RUN ./mvnw package -DskipTests

# Create upload directory
RUN mkdir -p ./uploads

# Expose the port
EXPOSE 8084

# Set environment variables with defaults (can be overridden at runtime)
ENV SERVER_PORT=8084
ENV SPRING_DATASOURCE_URL=jdbc:mysql://mysql:3306/incident_comm_files?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
ENV SPRING_DATASOURCE_USERNAME=root
ENV SPRING_DATASOURCE_PASSWORD=12345
ENV APP_SERVICE_USER=http://user-service:8081
ENV APP_SERVICE_INCIDENT=http://incidentservice:8082
ENV APP_SERVICE_CHAT=http://chatservice:8083
ENV FILE_UPLOAD_DIR=./uploads

# Volume for persistent file storage
VOLUME ["/app/uploads"]

# Command to run the application
CMD find target/ -name '*.jar' -exec java -jar {} \; 