import { useState, useEffect } from 'react';
import { Link, useNavigate, useLocation } from 'react-router-dom';
import { useAuth } from '../../utils/AuthContext';

function Login() {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [rememberMe, setRememberMe] = useState(false);
  const [error, setError] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const [successMessage, setSuccessMessage] = useState('');
  
  const { login } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  
  // Get the redirect path from location state or default to dashboard
  const from = location.state?.from || '/dashboard';
  
  // Check for success message from registration
  useEffect(() => {
    if (location.state?.registered) {
      setSuccessMessage(location.state.message || 'Account created successfully! Please login.');
    }
  }, [location.state]);
  
  const handleSubmit = async (e) => {
    e.preventDefault();
    
    // Clear previous messages
    setError('');
    setSuccessMessage('');
    
    // Validation
    if (!username.trim() || !password.trim()) {
      setError('Username and password are required');
      return;
    }
    
    setIsLoading(true);
    
    try {
      // If using the test user, show a testing message
      if (username === 'testuser') {
        setSuccessMessage('TEST MODE: Logging in with test user...');
        // Small delay to simulate network
        await new Promise(resolve => setTimeout(resolve, 800));
      }
      
      const result = await login(username, password);
      
      if (result.success) {
        // Always navigate to dashboard after successful login
        navigate('/dashboard', { replace: true });
      } else {
        setError(result.message || 'Invalid username or password');
      }
    } catch (err) {
      console.error('Login error:', err);
      setError('An unexpected error occurred. Please try again.');
    } finally {
      setIsLoading(false);
    }
  };
  
  const handleForgotPassword = () => {
    // Implement forgot password functionality or navigation
    console.log('Forgot password clicked');
  };
  
  return (
    <div className="auth-page">
      <div className="auth-container">
        <div className="auth-left-panel">
          <div className="auth-logo">
            <div className="logo-icon">
              <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" width="32" height="32" fill="currentColor">
                <path d="M20,2H4A2,2 0 0,0 2,4V22L6,18H20A2,2 0 0,0 22,16V4A2,2 0 0,0 20,2M20,16H6L4,18V4H20V16Z" />
              </svg>
            </div>
            <h2>ICP</h2>
          </div>
          
          <div className="auth-info">
            <h1>Incident Communication Platform</h1>
            <p>Real-time collaboration for incident management and resolution.</p>
            
            <div className="feature-list">
              <div className="feature-item">
                <div className="feature-icon">
                  <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" width="24" height="24" fill="currentColor">
                    <path d="M7,2V13H10V22L17,10H13L17,2H7Z" />
                  </svg>
                </div>
                <p>Real-time incident updates and messaging</p>
              </div>
              
              <div className="feature-item">
                <div className="feature-icon">
                  <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" width="24" height="24" fill="currentColor">
                    <path d="M12,5.5A3.5,3.5 0 0,1 15.5,9A3.5,3.5 0 0,1 12,12.5A3.5,3.5 0 0,1 8.5,9A3.5,3.5 0 0,1 12,5.5M5,8C5.56,8 6.08,8.15 6.53,8.42C6.38,9.85 6.8,11.27 7.66,12.38C7.16,13.34 6.16,14 5,14A3,3 0 0,1 2,11A3,3 0 0,1 5,8M19,8A3,3 0 0,1 22,11A3,3 0 0,1 19,14C17.84,14 16.84,13.34 16.34,12.38C17.2,11.27 17.62,9.85 17.47,8.42C17.92,8.15 18.44,8 19,8M5.5,18.25C5.5,16.18 8.41,14.5 12,14.5C15.59,14.5 18.5,16.18 18.5,18.25V20H5.5V18.25M0,20V18.5C0,17.11 1.89,15.94 4.45,15.6C3.86,16.28 3.5,17.22 3.5,18.25V20H0M24,20H20.5V18.25C20.5,17.22 20.14,16.28 19.55,15.6C22.11,15.94 24,17.11 24,18.5V20Z" />
                  </svg>
                </div>
                <p>Team collaboration with role-based access</p>
              </div>
              
              <div className="feature-item">
                <div className="feature-icon">
                  <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" width="24" height="24" fill="currentColor">
                    <path d="M6,2A2,2 0 0,0 4,4V20A2,2 0 0,0 6,22H18A2,2 0 0,0 20,20V8L14,2H6M6,4H13V9H18V20H6V4M8,12V14H16V12H8M8,16V18H13V16H8Z" />
                  </svg>
                </div>
                <p>Document sharing and incident history</p>
              </div>
            </div>
          </div>
        </div>
        
        <div className="auth-right-panel">
          <div className="auth-form-container">
            <div className="auth-heading">
              <h2>Welcome Back</h2>
              <p>Log in to your account to continue</p>
            </div>
            
            {successMessage && (
              <div className="success-container">
                {successMessage}
              </div>
            )}
            
            {error && (
              <div className="error-container">
                {error}
              </div>
            )}
            
            <form className="auth-form" onSubmit={handleSubmit}>
              <div className="form-group">
                <label htmlFor="username">Username <span className="required-field">*</span></label>
                <input
                  id="username"
                  type="text"
                  value={username}
                  onChange={(e) => setUsername(e.target.value)}
                  disabled={isLoading}
                  placeholder="Enter your username"
                  autoComplete="username"
                />
              </div>
              
              <div className="form-group">
                <label htmlFor="password">Password</label>
                <div className="password-input-container">
                  <input
                    id="password"
                    type="password"
                    value={password}
                    onChange={(e) => setPassword(e.target.value)}
                    disabled={isLoading}
                    placeholder="Enter your password"
                    autoComplete="current-password"
                  />
                  <button type="button" className="password-toggle-btn" aria-label="Toggle password visibility">
                    <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" width="24" height="24" fill="currentColor">
                      <path d="M12,9A3,3 0 0,1 15,12A3,3 0 0,1 12,15A3,3 0 0,1 9,12A3,3 0 0,1 12,9M12,4.5C17,4.5 21.27,7.61 23,12C21.27,16.39 17,19.5 12,19.5C7,19.5 2.73,16.39 1,12C2.73,7.61 7,4.5 12,4.5M3.18,12C4.83,15.36 8.24,17.5 12,17.5C15.76,17.5 19.17,15.36 20.82,12C19.17,8.64 15.76,6.5 12,6.5C8.24,6.5 4.83,8.64 3.18,12Z" />
                    </svg>
                  </button>
                </div>
              </div>
              
              <div className="form-options">
                <div className="remember-me">
                  <input
                    id="remember-me"
                    type="checkbox"
                    checked={rememberMe}
                    onChange={(e) => setRememberMe(e.target.checked)}
                    disabled={isLoading}
                  />
                  <label htmlFor="remember-me">Remember me</label>
                </div>
                <div className="forgot-password">
                  <Link to="/forgot-password">Forgot password?</Link>
                </div>
              </div>
              
              <div className="form-action">
                <button
                  type="submit"
                  className="btn btn-primary btn-lg"
                  disabled={isLoading}
                >
                  {isLoading ? 'Signing in...' : 'Log In'}
                </button>
              </div>
            </form>
            
            <div className="auth-footer">
              <p>
                Don't have an account? <Link to="/register">Register now</Link>
              </p>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}

export default Login; 