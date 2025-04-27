import { useState, useEffect } from 'react';
import { Routes, Route, Navigate, useLocation } from 'react-router-dom';
import Layout from './components/layout/Layout';
import Login from './pages/auth/Login';
import Register from './pages/auth/Register';
import Dashboard from './pages/dashboard/Dashboard';
import Incidents from './pages/incidents/Incidents';
import IncidentDetail from './pages/incidents/IncidentDetail';
import CreateIncident from './pages/incidents/CreateIncident';
import ChatRooms from './pages/chat/ChatRooms';
import ChatRoom from './pages/chat/ChatRoom';
import CreateChatRoom from './pages/chat/CreateChatRoom';
import FileManager from './pages/files/FileManager';
import Notifications from './pages/notifications/Notifications';
import NotFound from './pages/NotFound';
import { AuthProvider, useAuth } from './utils/AuthContext';

// Protected route component
const ProtectedRoute = ({ children }) => {
  const { isAuthenticated, loading } = useAuth();
  const location = useLocation();

  // Show loading screen while checking authentication
  if (loading) {
    return <div className="loading-screen">Loading...</div>;
  }

  // Redirect to login if not authenticated
  if (!isAuthenticated) {
    return <Navigate to="/login" state={{ from: location.pathname }} replace />;
  }

  return children;
};

function App() {
  return (
    <AuthProvider>
      <Routes>
        <Route path="/login" element={<Login />} />
        <Route path="/register" element={<Register />} />
        
        <Route path="/" element={
          <ProtectedRoute>
            <Layout />
          </ProtectedRoute>
        }>
          <Route index element={<Navigate to="/dashboard" replace />} />
          <Route path="dashboard" element={<Dashboard />} />
          
          <Route path="incidents">
            <Route index element={<Incidents />} />
            <Route path="new" element={<CreateIncident />} />
            <Route path=":id" element={<IncidentDetail />} />
          </Route>
          
          <Route path="chats">
            <Route index element={<ChatRooms />} />
            <Route path="new" element={<CreateChatRoom />} />
            <Route path=":id" element={<ChatRoom />} />
          </Route>
          
          <Route path="files" element={<FileManager />} />
          <Route path="notifications" element={<Notifications />} />
        </Route>
        
        <Route path="*" element={<NotFound />} />
      </Routes>
    </AuthProvider>
  );
}

export default App; 