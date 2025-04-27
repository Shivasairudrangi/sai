// API URLs
export const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api';
export const AUTH_API_URL = `${API_BASE_URL}/auth`;
export const USERS_API_URL = `${API_BASE_URL}/users`;
export const INCIDENTS_API_URL = `${API_BASE_URL}/incidents`;
export const CHAT_API_URL = `${API_BASE_URL}/chats`;
export const NOTIFICATIONS_API_URL = `${API_BASE_URL}/notifications`;
export const FILES_API_URL = `${API_BASE_URL}/files`;
export const SETTINGS_API_URL = `${API_BASE_URL}/settings`;

// Authentication constants
export const TOKEN_KEY = 'incident_platform_token';
export const USER_KEY = 'incident_platform_user';
export const TOKEN_EXPIRY_KEY = 'incident_platform_token_expiry';

// Pagination defaults
export const DEFAULT_PAGE_SIZE = 10;
export const DEFAULT_PAGE = 0;

// Status values
export const INCIDENT_STATUSES = {
  OPEN: 'OPEN',
  IN_PROGRESS: 'IN_PROGRESS',
  RESOLVED: 'RESOLVED',
  CLOSED: 'CLOSED'
};

export const INCIDENT_PRIORITIES = {
  LOW: 'LOW',
  MEDIUM: 'MEDIUM',
  HIGH: 'HIGH'
};

// User roles
export const USER_ROLES = {
  USER: 'USER',
  RESPONDER: 'RESPONDER',
  MANAGER: 'MANAGER',
  ADMIN: 'ADMIN'
};

// WebSocket topics
export const WS_TOPICS = {
  INCIDENTS: '/topic/incidents',
  INCIDENT_DETAIL: (id) => `/topic/incident/${id}`,
  CHAT_ROOM: (id) => `/topic/rooms/${id}/messages`,
  CHAT_USERS: (id) => `/topic/rooms/${id}/users`,
  NOTIFICATIONS: '/user/queue/notifications'
};

// Local storage keys
export const STORAGE_KEYS = {
  DARK_MODE: 'dark_mode',
  LANGUAGE: 'language',
  LAST_VISITED: 'last_visited'
};

// App settings
export const DEFAULT_LANGUAGE = 'en';
export const SUPPORTED_LANGUAGES = ['en', 'es', 'fr', 'de'];
export const AUTO_REFRESH_INTERVAL = 60000; // 1 minute in milliseconds
export const MAX_UPLOAD_SIZE = 10 * 1024 * 1024; // 10MB in bytes

// Error messages
export const ERROR_MESSAGES = {
  NETWORK_ERROR: 'Network error. Please check your connection and try again.',
  SERVER_ERROR: 'Server error. Please try again later.',
  UNAUTHORIZED: 'You are not authorized to perform this action.',
  FORBIDDEN: 'Access denied. You do not have permission to perform this action.',
  NOT_FOUND: 'The requested resource was not found.',
  VALIDATION_ERROR: 'Please check the form for errors.',
  DEFAULT: 'An unexpected error occurred. Please try again.'
};

// Success messages
export const SUCCESS_MESSAGES = {
  INCIDENT_CREATED: 'Incident created successfully.',
  INCIDENT_UPDATED: 'Incident updated successfully.',
  CHAT_ROOM_CREATED: 'Chat room created successfully.',
  MESSAGE_SENT: 'Message sent successfully.',
  FILE_UPLOADED: 'File uploaded successfully.',
  SETTINGS_SAVED: 'Settings saved successfully.'
}; 