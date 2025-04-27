import { useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { FaBell, FaUser, FaSignOutAlt, FaCog } from 'react-icons/fa';
import { useAuth } from '../utils/AuthContext';
import notificationService from '../services/notificationService';

function Header() {
  const { currentUser, logout } = useAuth();
  const [unreadCount, setUnreadCount] = useState(0);
  const [showDropdown, setShowDropdown] = useState(false);
  const navigate = useNavigate();

  // Get unread notification count
  const getUnreadCount = async () => {
    try {
      const data = await notificationService.getUnreadNotifications();
      setUnreadCount(data?.length || 0);
    } catch (error) {
      console.error('Error fetching unread notifications:', error);
      // Don't set error state, just silently fail
      setUnreadCount(0);
    }
  };

  useEffect(() => {
    if (currentUser) {
      getUnreadCount();
      
      // Refresh notification count every minute
      const interval = setInterval(getUnreadCount, 60000);
      return () => clearInterval(interval);
    }
  }, [currentUser]);

  const toggleDropdown = () => {
    setShowDropdown(!showDropdown);
  };

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  return (
    <header className="app-header">
      <div className="logo">
        <Link to="/">Incident Platform</Link>
      </div>
      
      {currentUser && (
        <div className="header-actions">
          <div className="notification-bell">
            <Link to="/notifications" className="icon-button">
              <FaBell />
              {unreadCount > 0 && <span className="notification-badge">{unreadCount}</span>}
            </Link>
          </div>
          
          <div className="user-menu">
            <button className="user-button" onClick={toggleDropdown}>
              <span className="user-avatar">
                <FaUser />
              </span>
              <span className="user-name">{currentUser.username}</span>
            </button>
            
            {showDropdown && (
              <div className="dropdown-menu">
                <Link to="/profile" className="dropdown-item">
                  <FaUser />
                  <span>Profile</span>
                </Link>
                <Link to="/settings" className="dropdown-item">
                  <FaCog />
                  <span>Settings</span>
                </Link>
                <button className="dropdown-item" onClick={handleLogout}>
                  <FaSignOutAlt />
                  <span>Logout</span>
                </button>
              </div>
            )}
          </div>
        </div>
      )}
    </header>
  );
}

export default Header; 