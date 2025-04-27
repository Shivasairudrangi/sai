# Incident Platform Frontend

A modern React application that serves as the frontend for the Incident Communication Platform microservices.

## Features

- User authentication and authorization
- Dashboard with incident overview
- Incident management (create, view, update)
- Real-time chat functionality
- File management (upload, download, share)
- Notification system

## Tech Stack

- React 18 with JavaScript
- React Router for navigation
- Sass for styling (with external stylesheets)
- Axios for API communication
- SockJS and STOMP for WebSocket connections

## Getting Started

### Prerequisites

- Node.js 16+ and npm
- The backend services should be running (see main project README)

### Installation

1. Navigate to the frontend directory:
   ```bash
   cd frontend/incident-platform-frontend
   ```

2. Install dependencies:
   ```bash
   npm install
   ```

3. Start the development server:
   ```bash
   npm run dev
   ```

4. Open your browser and navigate to [http://localhost:3000](http://localhost:3000)

## Development

### Project Structure

```
incident-platform-frontend/
├── public/               # Static files
├── src/
│   ├── components/       # Reusable UI components
│   │   ├── layout/       # Layout components (Header, Sidebar, Footer)
│   │   └── ...
│   ├── pages/            # Page components
│   │   ├── auth/         # Authentication pages
│   │   ├── dashboard/    # Dashboard pages
│   │   ├── incidents/    # Incident management pages
│   │   ├── chat/         # Chat pages
│   │   ├── files/        # File management pages
│   │   └── ...
│   ├── services/         # API services
│   ├── styles/           # SCSS styles (external stylesheets)
│   ├── utils/            # Utility functions and context providers
│   ├── App.jsx           # Main application component
│   └── main.jsx          # Application entry point
└── package.json
```

### Build for Production

To create a production build:

```bash
npm run build
```

The built files will be in the `dist` directory.

### Connecting to Backend Services

The application connects to the backend API Gateway, which routes requests to the appropriate microservices. The connection is configured in `vite.config.js` using a proxy.

## WebSocket Connections

Real-time features like chat and notifications use WebSocket connections. The connections are established through the API Gateway, which forwards the messages to the appropriate microservices.

## Authentication

The application uses JWT-based authentication. The token is stored in localStorage and included in API requests and WebSocket connections.

## Styling

The application uses SCSS for styling with a component-based approach. It uses CSS variables for theming and responsive design.

## License

This project is licensed under the MIT License. 