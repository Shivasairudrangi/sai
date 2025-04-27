import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../../utils/AuthContext';

function Register() {
  const [formData, setFormData] = useState({
    username: '',
    email: '',
    password: '',
    confirmPassword: '',
    firstName: '',
    lastName: '',
    role: ''
  });
  
  const [error, setError] = useState('');
  const [passwordStrength, setPasswordStrength] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const [termsAccepted, setTermsAccepted] = useState(false);
  
  const { register } = useAuth();
  const navigate = useNavigate();
  
  // Handle form input changes
  const handleChange = (e) => {
    const { name, value } = e.target;
    
    setFormData(prevData => ({
      ...prevData,
      [name]: value
    }));
    
    // Check password strength when password changes
    if (name === 'password') {
      checkPasswordStrength(value);
    }
  };
  
  // Calculate password strength
  const checkPasswordStrength = (password) => {
    if (!password) {
      setPasswordStrength('');
      return;
    }
    
    const hasUpperCase = /[A-Z]/.test(password);
    const hasLowerCase = /[a-z]/.test(password);
    const hasNumbers = /\d/.test(password);
    const hasSpecialChars = /[!@#$%^&*(),.?":{}|<>]/.test(password);
    const isLongEnough = password.length >= 8;
    
    const strength = [hasUpperCase, hasLowerCase, hasNumbers, hasSpecialChars, isLongEnough].filter(Boolean).length;
    
    if (strength < 2) {
      setPasswordStrength('weak');
    } else if (strength < 4) {
      setPasswordStrength('fair');
    } else if (strength < 5) {
      setPasswordStrength('good');
    } else {
      setPasswordStrength('strong');
    }
  };
  
  // Handle form submission
  const handleSubmit = async (e) => {
    e.preventDefault();
    
    // Validation
    const { username, email, password, confirmPassword, firstName, lastName, role } = formData;
    
    if (!username.trim() || !email.trim() || !password.trim() || !role) {
      setError('Username, email, password, and role are required');
      return;
    }
    
    if (password !== confirmPassword) {
      setError('Passwords do not match');
      return;
    }
    
    if (passwordStrength === 'weak') {
      setError('Password is too weak. Please choose a stronger password.');
      return;
    }
    
    if (!termsAccepted) {
      setError('You must agree to the Terms of Service and Privacy Policy');
      return;
    }
    
    setIsLoading(true);
    setError('');
    
    try {
      // For testing only - simulate a successful registration without backend
      if (process.env.NODE_ENV === 'development' && username === 'testuser') {
        // Simulate successful registration for testing
        console.log('TEST MODE: Registration success simulation');
        
        setTimeout(() => {
          setIsLoading(false);
          navigate('/login', { state: { registered: true, message: 'TEST MODE: Account created successfully!' } });
        }, 1000);
        
        return;
      }
      
      // Exactly match the RegisterRequest structure from backend
      const userData = {
        username: username.trim(),
        email: email.trim(),
        password: password,
        firstName: firstName.trim(),
        lastName: lastName.trim(),
        role: role,
        roles: [role] // Convert the selected role to an array for the backend
      };
      
      console.log('Attempting to register with data:', userData);
      
      const result = await register(userData);
      
      if (result.success) {
        // Redirect to login page on successful registration
        navigate('/login', { state: { registered: true } });
      } else {
        setError(result.message || 'Registration failed. Please try again.');
      }
    } catch (err) {
      console.error('Registration error in component:', err);
      
      // More descriptive error message
      let errorMessage = 'An error occurred during registration. Please try again.';
      
      if (err.message && err.message.includes('Network Error')) {
        errorMessage = 'Cannot connect to the server. Please check your internet connection or try again later.';
      } else if (err.response && err.response.status === 500) {
        errorMessage = 'The server encountered an error. This might be due to database connectivity issues.';
      }
      
      setError(errorMessage);
    } finally {
      setIsLoading(false);
    }
  };
  
  const roleOptions = [
    {
      id: 'l1_support',
      name: 'L1 Support',
      description: 'First line of defense for incident response',
      icon: (
        <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" width="24" height="24" fill="currentColor">
          <path d="M12,1C7,1 3,5 3,10V17A3,3 0 0,0 6,20H9V12H5V10A7,7 0 0,1 12,3A7,7 0 0,1 19,10V12H15V20H18A3,3 0 0,0 21,17V10C21,5 17,1 12,1Z" />
        </svg>
      )
    },
    {
      id: 'l2_support',
      name: 'L2 Support',
      description: 'Advanced technical support and troubleshooting',
      icon: (
        <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" width="24" height="24" fill="currentColor">
          <path d="M4,6H20V16H4M20,18A2,2 0 0,0 22,16V6C22,4.89 21.1,4 20,4H4C2.89,4 2,4.89 2,6V16A2,2 0 0,0 4,18H0V20H24V18H20Z" />
        </svg>
      )
    },
    {
      id: 'manager',
      name: 'Manager',
      description: 'Team coordination and incident prioritization',
      icon: (
        <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" width="24" height="24" fill="currentColor">
          <path d="M12,5.5A3.5,3.5 0 0,1 15.5,9A3.5,3.5 0 0,1 12,12.5A3.5,3.5 0 0,1 8.5,9A3.5,3.5 0 0,1 12,5.5M5,8C5.56,8 6.08,8.15 6.53,8.42C6.38,9.85 6.8,11.27 7.66,12.38C7.16,13.34 6.16,14 5,14A3,3 0 0,1 2,11A3,3 0 0,1 5,8M19,8A3,3 0 0,1 22,11A3,3 0 0,1 19,14C17.84,14 16.84,13.34 16.34,12.38C17.2,11.27 17.62,9.85 17.47,8.42C17.92,8.15 18.44,8 19,8M5.5,18.25C5.5,16.18 8.41,14.5 12,14.5C15.59,14.5 18.5,16.18 18.5,18.25V20H5.5V18.25M0,20V18.5C0,17.11 1.89,15.94 4.45,15.6C3.86,16.28 3.5,17.22 3.5,18.25V20H0M24,20H20.5V18.25C20.5,17.22 20.14,16.28 19.55,15.6C22.11,15.94 24,17.11 24,18.5V20Z" />
        </svg>
      )
    },
    {
      id: 'admin',
      name: 'Admin',
      description: 'Platform administration and user management',
      icon: (
        <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" width="24" height="24" fill="currentColor">
          <path d="M12,8A4,4 0 0,1 16,12A4,4 0 0,1 12,16A4,4 0 0,1 8,12A4,4 0 0,1 12,8M12,10A2,2 0 0,0 10,12A2,2 0 0,0 12,14A2,2 0 0,0 14,12A2,2 0 0,0 12,10M10,22C9.75,22 9.54,21.82 9.5,21.58L9.13,18.93C8.5,18.68 7.96,18.34 7.44,17.94L4.95,18.95C4.73,19.03 4.46,18.95 4.34,18.73L2.34,15.27C2.21,15.05 2.27,14.78 2.46,14.63L4.57,12.97L4.5,12L4.57,11L2.46,9.37C2.27,9.22 2.21,8.95 2.34,8.73L4.34,5.27C4.46,5.05 4.73,4.96 4.95,5.05L7.44,6.05C7.96,5.66 8.5,5.32 9.13,5.07L9.5,2.42C9.54,2.18 9.75,2 10,2H14C14.25,2 14.46,2.18 14.5,2.42L14.87,5.07C15.5,5.32 16.04,5.66 16.56,6.05L19.05,5.05C19.27,4.96 19.54,5.05 19.66,5.27L21.66,8.73C21.79,8.95 21.73,9.22 21.54,9.37L19.43,11L19.5,12L19.43,13L21.54,14.63C21.73,14.78 21.79,15.05 21.66,15.27L19.66,18.73C19.54,18.95 19.27,19.04 19.05,18.95L16.56,17.95C16.04,18.34 15.5,18.68 14.87,18.93L14.5,21.58C14.46,21.82 14.25,22 14,22H10M11.25,4L10.88,6.61C9.68,6.86 8.62,7.5 7.85,8.39L5.44,7.35L4.69,8.65L6.8,10.2C6.4,11.37 6.4,12.64 6.8,13.8L4.68,15.36L5.43,16.66L7.86,15.62C8.63,16.5 9.68,17.14 10.87,17.38L11.24,20H12.76L13.13,17.39C14.32,17.14 15.37,16.5 16.14,15.62L18.57,16.66L19.32,15.36L17.2,13.81C17.6,12.64 17.6,11.37 17.2,10.2L19.31,8.65L18.56,7.35L16.15,8.39C15.38,7.5 14.32,6.86 13.12,6.62L12.75,4H11.25Z" />
        </svg>
      )
    }
  ];
  
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
              <h2>Create Account</h2>
              <p>Fill in your details to get started</p>
            </div>
            
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
                  name="username"
                  type="text"
                  value={formData.username}
                  onChange={handleChange}
                  disabled={isLoading}
                  placeholder="Choose a username"
                  autoComplete="username"
                  required
                />
              </div>
              
              <div className="form-group">
                <label htmlFor="email">Email Address <span className="required-field">*</span></label>
                <input
                  id="email"
                  name="email"
                  type="email"
                  value={formData.email}
                  onChange={handleChange}
                  disabled={isLoading}
                  placeholder="Enter your email"
                  autoComplete="email"
                  required
                />
              </div>
              
              <div className="form-row">
                <div className="form-group">
                  <label htmlFor="firstName">First Name</label>
                  <input
                    id="firstName"
                    name="firstName"
                    type="text"
                    value={formData.firstName}
                    onChange={handleChange}
                    disabled={isLoading}
                    placeholder="Enter your first name"
                    autoComplete="given-name"
                  />
                </div>
                
                <div className="form-group">
                  <label htmlFor="lastName">Last Name</label>
                  <input
                    id="lastName"
                    name="lastName"
                    type="text"
                    value={formData.lastName}
                    onChange={handleChange}
                    disabled={isLoading}
                    placeholder="Enter your last name"
                    autoComplete="family-name"
                  />
                </div>
              </div>
              
              <div className="form-group">
                <label htmlFor="password">Password <span className="required-field">*</span></label>
                <div className="password-input-container">
                  <input
                    id="password"
                    name="password"
                    type="password"
                    value={formData.password}
                    onChange={handleChange}
                    disabled={isLoading}
                    placeholder="Create a password"
                    autoComplete="new-password"
                    required
                  />
                  <button type="button" className="password-toggle-btn" aria-label="Toggle password visibility">
                    <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" width="24" height="24" fill="currentColor">
                      <path d="M12,9A3,3 0 0,1 15,12A3,3 0 0,1 12,15A3,3 0 0,1 9,12A3,3 0 0,1 12,9M12,4.5C17,4.5 21.27,7.61 23,12C21.27,16.39 17,19.5 12,19.5C7,19.5 2.73,16.39 1,12C2.73,7.61 7,4.5 12,4.5M3.18,12C4.83,15.36 8.24,17.5 12,17.5C15.76,17.5 19.17,15.36 20.82,12C19.17,8.64 15.76,6.5 12,6.5C8.24,6.5 4.83,8.64 3.18,12Z" />
                    </svg>
                  </button>
                </div>
                
                {passwordStrength && (
                  <div className="password-strength">
                    <div className="strength-meter">
                      <div className={`meter-fill ${passwordStrength}`}></div>
                    </div>
                    <div className="strength-text">
                      <span className={passwordStrength}>
                        {passwordStrength.charAt(0).toUpperCase() + passwordStrength.slice(1)}
                      </span>
                    </div>
                  </div>
                )}
              </div>
              
              <div className="form-group">
                <label htmlFor="confirmPassword">Confirm Password <span className="required-field">*</span></label>
                <input
                  id="confirmPassword"
                  name="confirmPassword"
                  type="password"
                  value={formData.confirmPassword}
                  onChange={handleChange}
                  disabled={isLoading}
                  placeholder="Confirm your password"
                  autoComplete="new-password"
                  required
                />
              </div>
              
              <div className="form-group">
                <label htmlFor="role">Select Role <span className="required-field">*</span></label>
                <div className="role-selection">
                  {roleOptions.map(role => (
                    <div 
                      key={role.id}
                      className={`role-option ${formData.role === role.id ? 'selected' : ''}`}
                      onClick={() => setFormData({...formData, role: role.id})}
                    >
                      <div className="role-icon">{role.icon}</div>
                      <div className="role-info">
                        <div className="role-name">{role.name}</div>
                        <div className="role-description">{role.description}</div>
                      </div>
                    </div>
                  ))}
                </div>
              </div>
              
              <div className="form-terms">
                <input
                  id="terms"
                  type="checkbox"
                  checked={termsAccepted}
                  onChange={(e) => setTermsAccepted(e.target.checked)}
                  disabled={isLoading}
                  required
                />
                <label htmlFor="terms">
                  I agree to the <Link to="/terms">Terms of Service</Link> and <Link to="/privacy">Privacy Policy</Link>
                </label>
              </div>
              
              <div className="form-action">
                <button
                  type="submit"
                  className="btn btn-primary btn-lg"
                  disabled={isLoading}
                >
                  {isLoading ? 'Creating Account...' : 'Create Account'}
                </button>
              </div>
            </form>
            
            <div className="auth-footer">
              <p>
                Already have an account? <Link to="/login">Log in</Link>
              </p>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}

export default Register; 