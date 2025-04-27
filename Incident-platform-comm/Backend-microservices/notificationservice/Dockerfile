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

# Expose the port
EXPOSE 8085

# Set environment variables with defaults (can be overridden at runtime)
ENV SERVER_PORT=8085
ENV SPRING_DATASOURCE_URL=jdbc:mysql://mysql:3306/incident_comm_notifications?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=UTC
ENV SPRING_DATASOURCE_USERNAME=root
ENV SPRING_DATASOURCE_PASSWORD=12345
ENV APP_SERVICE_USER=http://user-service:8081
ENV APP_SERVICE_INCIDENT=http://incidentservice:8082
ENV APP_SERVICE_CHAT=http://chatservice:8083
ENV APP_SERVICE_FILE=http://fileservice:8084
ENV SPRING_WEBSOCKET_PATH=/ws-notification
ENV SPRING_MAIL_HOST=smtp.example.com
ENV SPRING_MAIL_PORT=587
ENV SPRING_MAIL_USERNAME=notifications@example.com
ENV SPRING_MAIL_PASSWORD=your-password
ENV MANAGEMENT_HEALTH_MAIL_ENABLED=false

# Command to run the application
CMD find target/ -name '*.jar' -exec java -jar {} \; 