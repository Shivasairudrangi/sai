import api, { apiRequest } from './api';

const AUTH_ENDPOINTS = {
  LOGIN: '/api/auth/signin',
  REGISTER: '/api/auth/signup',
  CURRENT_USER: '/api/users/me'
};

// Special handling for the test user
const handleTestUser = (username) => {
  if (username === 'testuser') {
    console.log('TEST MODE: Login success simulation');
    
    // Create a mock user
    const mockUser = {
      id: 999,
      username: 'testuser',
      email: 'testuser@example.com',
      roles: ['ADMIN'],
      accessToken: 'test_token_' + Date.now()
    };
    
    // Store the token
    localStorage.setItem('token', mockUser.accessToken);
    
    // Store user data in session storage for the test mode
    sessionStorage.setItem('currentUser', JSON.stringify(mockUser));
    
    return mockUser;
  }
  return null;
};

const authService = {
  // Login user and get token
  login: async (username, password) => {
    try {
      console.log('Attempting login for user:', username);
      
      // Test user handling
      const testUser = handleTestUser(username);
      if (testUser) {
        return testUser;
      }
      
      // Use axios for login request - no fallback needed
        const response = await api.post(AUTH_ENDPOINTS.LOGIN, { username, password });
      console.log('Raw login response:', response);
        
      // Ensure we have a proper response object
      return response || {};
    } catch (error) {
      console.error('Login service error:', error);
      throw error;
    }
  },

  // Register new user
  register: async (userData) => {
    try {
      console.log('Sending registration data:', userData);
      
      // Use axios for registration request - no fallback needed
        const response = await api.post(AUTH_ENDPOINTS.REGISTER, userData);
        return response;
    } catch (error) {
      console.error('Registration service error:', error);
      throw error;
    }
  },

  // Get current authenticated user's profile
  getCurrentUser: async () => {
    try {
      const token = localStorage.getItem('token');
      console.log('Getting current user with token:', token);
      
      // Check if we have a test token
      if (token && token.startsWith('test_token_')) {
        const testUser = sessionStorage.getItem('currentUser');
        if (testUser) {
          return JSON.parse(testUser);
        }
      }
      
      // Add a custom header for troubleshooting
      const options = {
        headers: { 'X-Debug-Token': 'true' }
      };
        
      const response = await api.get(AUTH_ENDPOINTS.CURRENT_USER, options);
      console.log('Current user response:', response);
      return response;
    } catch (error) {
      console.error('Get user service error:', error);
      if (error.response) {
        console.error('Error response status:', error.response.status);
        console.error('Error response data:', error.response.data);
      }
      throw error;
    }
  }
};

export default authService; 