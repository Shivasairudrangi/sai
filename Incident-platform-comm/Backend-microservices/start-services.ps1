# Start all services
Write-Host "Starting all services..."

# Start API Gateway (add gateway service first - it should be launched before other services)
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd apigateway; ./mvnw spring-boot:run"

# Start Notification Service
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd notificationservice; ./mvnw spring-boot:run"

# Start Incident Service
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd incidentservice; ./mvnw spring-boot:run"

# Start File Service
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd fileservice; ./mvnw spring-boot:run"

# Start Chat Service
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd chatservice; ./mvnw spring-boot:run"

# Start User Service
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd user-service; ./mvnw spring-boot:run"

Write-Host "All services have been started in separate windows."
Write-Host "Please check each window for any startup errors."

# Run health check script
Write-Host "Running health check script..."
Start-Process powershell -ArgumentList "-NoExit", "-Command", ".\test-services.ps1"