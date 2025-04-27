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
EXPOSE 8083

# Set environment variables with defaults (can be overridden at runtime)
ENV SERVER_PORT=8083
ENV SPRING_DATASOURCE_URL=jdbc:mysql://mysql:3306/incident_comm_chat?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=UTC
ENV SPRING_DATASOURCE_USERNAME=root
ENV SPRING_DATASOURCE_PASSWORD=12345
ENV APP_SERVICE_USER=http://user-service:8081
ENV SPRING_WEBSOCKET_PATH=/ws-chat

# Command to run the application
CMD find target/ -name '*.jar' -exec java -jar {} \; 