FROM eclipse-temurin:21-jdk

WORKDIR /app

# Copy the Maven wrapper and POM
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./

# Build the application (download dependencies first for better caching)
RUN chmod +x ./mvnw && ./mvnw dependency:go-offline -B

# Copy source code
COPY src ./src

# Package the application
RUN ./mvnw package -DskipTests

# Expose the port
EXPOSE 8075

# Command to run the application
CMD find target/ -name '*.jar' -exec java -jar {} \; 