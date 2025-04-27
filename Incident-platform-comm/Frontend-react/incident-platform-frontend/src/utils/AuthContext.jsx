import { createContext, useState, useEffect, useContext } from 'react';
import { useNavigate } from 'react-router-dom';
import authService from '../services/authService';

const AuthContext = createContext(null);

export const useAuth = () => useContext(AuthContext);

export const AuthProvider = ({ children }) => {
  const [currentUser, setCurrentUser] = useState(null);
  const [token, setToken] = useState(localStorage.getItem('token'));
  const [loading, setLoading] = useState(true);
  const navigate = useNavigate();

  useEffect(() => {
    const initAuth = async () => {
      setLoading(true);
      try {
        if (token) {
          // Check for test token
          if (token.startsWith('test_token_')) {
            // Get the test user from session storage if available
            const testUser = sessionStorage.getItem('currentUser');
            if (testUser) {
              setCurrentUser(JSON.parse(testUser));
              setLoading(false);
              return;
            }
          }
          
          // This will help us debug token issues
          console.log('Initializing auth with token:', token);
          
          try {
          // Get user profile with the token
          const userData = await authService.getCurrentUser();
            if (userData) {
          setCurrentUser(userData);
              console.log('User data retrieved successfully');
            } else {
              console.error('No user data returned');
              logout();
            }
          } catch (apiError) {
            console.error('API error when getting user profile:', apiError);
            // Don't logout on all errors - only specific ones
            if (apiError.response && (apiError.response.status === 401 || apiError.response.status === 403)) {
              console.log('Authentication error - logging out');
              logout();
            }
          }
        }
      } catch (error) {
        console.error('Authentication error in initAuth:', error);
      } finally {
        setLoading(false);
      }
    };

    initAuth();
  }, [token]);

  const login = async (username, password) => {
    try {
      // Special case for test user
      if (username === 'testuser') {
        const testResponse = await authService.login(username, password);
        setCurrentUser(testResponse);
        setToken(testResponse.accessToken);
        return { success: true };
      }
      
      console.log('Attempting login for:', username);
      const response = await authService.login(username, password);
      console.log('Login response received:', JSON.stringify(response));
      
      // Support both token and accessToken property names
      const newToken = response.accessToken || response.token;
      
      if (!response || !newToken) {
        console.error('Invalid login response:', response);
        return { success: false, message: 'Invalid server response. Please try again.' };
      }
      
      // Log token for debugging
      console.log('Token received:', newToken);
      
      const userData = {
        id: response.id,
        username: response.username,
        email: response.email,
        roles: response.roles || []
      };
      
      localStorage.setItem('token', newToken);
      setToken(newToken);
      setCurrentUser(userData);
      
      return { success: true };
    } catch (error) {
      console.error('Login error in AuthContext:', error);
      let message = 'Login failed. Please check your credentials.';
      
      if (error.response) {
        if (error.response.data && error.response.data.message) {
          message = error.response.data.message;
        } else if (error.response.status === 401) {
          message = 'Invalid username or password.';
        } else if (error.response.status === 500) {
          message = 'Server error. Please try again later.';
        }
      } else if (error.message && error.message.includes('Network Error')) {
        message = 'Network error. Please check your connection.';
      } else if (error.message && error.message.includes('Failed to fetch')) {
        message = 'Connection error. The server may be unavailable.';
      }
      
      return { success: false, message };
    }
  };

  const register = async (userData) => {
    try {
      await authService.register(userData);
      return { success: true };
    } catch (error) {
      console.error('Registration error:', error);
      // Extract the detailed error message from the response
      let errorMessage = 'Registration failed. Please try again.';
      
      if (error.response) {
        if (error.response.data && error.response.data.message) {
          errorMessage = error.response.data.message;
        } else if (error.response.status === 500) {
          errorMessage = 'Server error. Please contact support or try again later.';
        }
      } else if (error.message && error.message.includes('Failed to fetch')) {
        errorMessage = 'Connection error. The server may be unavailable.';
      }
      
      return { success: false, message: errorMessage };
    }
  };

  const logout = () => {
    localStorage.removeItem('token');
    sessionStorage.removeItem('currentUser');
    setToken(null);
    setCurrentUser(null);
    navigate('/login');
  };

  const value = {
    currentUser,
    token,
    login,
    register,
    logout,
    isAuthenticated: !!currentUser,
    loading
  };

  return (
    <AuthContext.Provider value={value}>
      {children}
    </AuthContext.Provider>
  );
};

export default AuthContext; 