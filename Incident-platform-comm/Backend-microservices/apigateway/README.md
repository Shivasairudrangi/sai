# API Gateway for Incident Communication Platform

This API Gateway serves as a single entry point for all clients to the Incident Communication Platform microservices.

## Features

- Centralized routing to microservices
- Authentication and authorization using JWT
- Request logging
- Rate limiting
- CORS configuration
- Error handling
- WebSocket proxying

## Services Routed

The API Gateway routes requests to the following microservices:

- **User Service** (port 8081): Authentication and user management
- **Incident Service** (port 8082): Incident management
- **Chat Service** (port 8083): Real-time chat functionality
- **File Service** (port 8084): File upload and management
- **Notification Service** (port 8085): User notifications

## API Endpoints

All API endpoints are available through the gateway at `http://localhost:8080`:

- **Authentication**: `/api/auth/**`
- **Users**: `/api/users/**`
- **Incidents**: `/api/incidents/**`
- **Chats**: `/api/chats/**`
- **Files**: `/api/files/**`
- **Notifications**: `/api/notifications/**`

## WebSocket Endpoints

WebSocket connections are also proxied through the gateway:

- **Chat WebSocket**: `/ws-chat/**`
- **Incident WebSocket**: `/ws-incident/**`
- **Notification WebSocket**: `/ws-notification/**`

## Running the Gateway

To run the API Gateway:

```
./mvnw spring-boot:run
```

The API Gateway will start on port 8080.

## Configuration

Configuration can be modified in `src/main/resources/application.properties`:

- Port configuration
- Service URLs
- Rate limiting settings
- JWT settings
- Logging levels 