import axios from 'axios';

// Use relative URL for API calls to leverage Vite's proxy
const API_URL = '';  // Empty string means use relative URLs

// Check if we're in test mode
const isTestMode = process.env.NODE_ENV === 'development';

const api = axios.create({
  baseURL: API_URL,
  headers: {
    'Content-Type': 'application/json',
  },
  // Add withCredentials for CORS with credentials
  withCredentials: true
});

// Add a request interceptor for authentication
api.interceptors.request.use(
  (config) => {
    // Skip token for authentication endpoints
    const isAuthEndpoint = (
      config.url.includes('/api/auth/signin') || 
      config.url.includes('/api/auth/signup')
    );
    
    if (!isAuthEndpoint) {
      const token = localStorage.getItem('token');
      if (token) {
        // Check if token already includes "Bearer " prefix
        if (token.startsWith('Bearer ')) {
          config.headers.Authorization = token;
        } else {
          config.headers.Authorization = `Bearer ${token}`;
        }
        
        // Add user ID header if available
        const userId = localStorage.getItem('userId');
        if (userId) {
          config.headers['X-User-ID'] = userId;
        }
        
        // For debugging - log the auth header being sent
        console.log('Sending auth header:', config.headers.Authorization);
      }
    }
    
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// Add a response interceptor for error handling
api.interceptors.response.use(
  (response) => {
    // Return the response - don't extract data, leave that to the service
    return response.data;
  },
  (error) => {
    // Log the error in development
    if (process.env.NODE_ENV === 'development') {
      console.error('API Error:', error);
      
      // Enhanced error logging
      if (error.response) {
        console.error('Status:', error.response.status);
        console.error('Data:', error.response.data);
        console.error('Headers:', error.response.headers);
      } else if (error.request) {
        console.error('No response received:', error.request);
      } else {
        console.error('Error setting up request:', error.message);
      }
      
      // Log CORS issues
      if (error.message && error.message.includes('Network Error')) {
        console.error('This might be a CORS issue. Check browser console for details.');
      }
    }
    
    // Handle token expiration
    if (error.response && error.response.status === 401) {
      // Clear token and redirect to login if token expired
      if (!error.config.url.includes('/api/auth/')) {
        localStorage.removeItem('token');
        localStorage.removeItem('userId');
        window.location.href = '/login';
      }
    }
    
    return Promise.reject(error);
  }
);

// Custom function for making API requests that handles CORS errors gracefully
const makeRequest = async (method, url, data = null, options = {}) => {
  try {
    const config = {
      method,
      url,
      ...options
    };
    
    if (data) {
      config.data = data;
    }
    
    return await api(config);
  } catch (error) {
    console.error(`Error making ${method} request to ${url}:`, error);
    throw error;
  }
};

// Export request methods with our wrapper
export const apiRequest = {
  get: (url, options) => makeRequest('get', url, null, options),
  post: (url, data, options) => makeRequest('post', url, data, options),
  put: (url, data, options) => makeRequest('put', url, data, options),
  delete: (url, options) => makeRequest('delete', url, null, options)
};

// Export the default API instance
export default api;

// Export a utility for checking test mode
export const testModeEnabled = isTestMode; 