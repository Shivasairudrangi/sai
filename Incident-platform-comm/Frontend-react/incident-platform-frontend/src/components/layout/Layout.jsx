import { useState, useEffect } from 'react';
import { Outlet, useNavigate, useLocation } from 'react-router-dom';
import { useAuth } from '../../utils/AuthContext';
import Header from './Header';
import Sidebar from './Sidebar';
import Footer from './Footer';

function Layout() {
  const { isAuthenticated, loading } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const [sidebarOpen, setSidebarOpen] = useState(false);
  const [darkMode, setDarkMode] = useState(() => {
    return localStorage.getItem('darkMode') === 'true' || 
           window.matchMedia('(prefers-color-scheme: dark)').matches;
  });

  // Apply dark mode class to body
  useEffect(() => {
    if (darkMode) {
      document.body.classList.add('dark-mode');
    } else {
      document.body.classList.remove('dark-mode');
    }
    localStorage.setItem('darkMode', darkMode);
  }, [darkMode]);

  // Redirect to login if not authenticated
  useEffect(() => {
    if (!loading && !isAuthenticated) {
      navigate('/login', { state: { from: location.pathname } });
    }
  }, [isAuthenticated, loading, navigate, location]);

  // Handle sidebar toggle
  const toggleSidebar = () => {
    setSidebarOpen(prevState => !prevState);
  };

  // Handle dark mode toggle
  const toggleDarkMode = () => {
    setDarkMode(prevMode => !prevMode);
  };

  // Close sidebar when clicking outside on mobile
  const handleBackdropClick = () => {
    setSidebarOpen(false);
  };

  // Loading state
  if (loading) {
    return (
      <div className="loading-container">
        <div className="loading-spinner"></div>
        <span className="loading-text">Loading Incident Platform...</span>
      </div>
    );
  }

  // If not authenticated, don't render anything (redirect will happen)
  if (!isAuthenticated) return null;

  return (
    <div className={`app-layout ${darkMode ? 'dark-mode' : 'light-mode'}`}>
      <Header 
        toggleSidebar={toggleSidebar} 
        darkMode={darkMode}
        toggleDarkMode={toggleDarkMode}
      />
      
      <div className="content-wrapper">
        <Sidebar isOpen={sidebarOpen} darkMode={darkMode} />
        
        {/* Backdrop for mobile sidebar */}
        <div 
          className={`sidebar-backdrop ${sidebarOpen ? 'visible' : ''}`} 
          onClick={handleBackdropClick}
          aria-hidden="true"
        ></div>
        
        <main className="main-content">
          {/* Outlet renders the child routes */}
          <Outlet />
        </main>
      </div>
      
      <Footer darkMode={darkMode} />
    </div>
  );
}

export default Layout; 