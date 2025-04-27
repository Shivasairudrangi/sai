import { useState, useEffect } from 'react';
import { NavLink, useLocation } from 'react-router-dom';
import { useAuth } from '../../utils/AuthContext';
import PropTypes from 'prop-types';

function Sidebar({ isOpen, darkMode }) {
  const location = useLocation();
  const { currentUser } = useAuth();
  const [expandedGroup, setExpandedGroup] = useState(null);

  // Determine which group to expand based on current path
  useEffect(() => {
    if (location.pathname.includes('/incidents')) {
      setExpandedGroup('incidents');
    } else if (location.pathname.includes('/chats')) {
      setExpandedGroup('communication');
    } else if (location.pathname.includes('/reports')) {
      setExpandedGroup('analytics');
    }
  }, [location.pathname]);

  const toggleGroup = (group) => {
    setExpandedGroup(expandedGroup === group ? null : group);
  };

  const hasRole = (role) => {
    return currentUser?.roles?.includes(role) || false;
  };

  return (
    <aside className={`app-sidebar ${isOpen ? 'sidebar-open' : ''} ${darkMode ? 'dark' : ''}`}>
      <nav className="sidebar-nav">
        <div className="sidebar-section">
          <NavLink to="/dashboard" className={({ isActive }) => 
            `sidebar-item ${isActive ? 'active' : ''}`
          }>
            <svg viewBox="0 0 24 24" width="18" height="18" stroke="currentColor" strokeWidth="2" fill="none">
              <rect x="3" y="3" width="7" height="9"></rect>
              <rect x="14" y="3" width="7" height="5"></rect>
              <rect x="14" y="12" width="7" height="9"></rect>
              <rect x="3" y="16" width="7" height="5"></rect>
            </svg>
            <span>Dashboard</span>
          </NavLink>
        </div>

        <div className="sidebar-section">
          <div 
            className={`sidebar-group-header ${expandedGroup === 'incidents' ? 'expanded' : ''}`}
            onClick={() => toggleGroup('incidents')}
          >
            <svg viewBox="0 0 24 24" width="18" height="18" stroke="currentColor" strokeWidth="2" fill="none">
              <polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"></polygon>
            </svg>
            <span>Incident Management</span>
            <svg className="expand-icon" viewBox="0 0 24 24" width="18" height="18" stroke="currentColor" strokeWidth="2" fill="none">
              <polyline points="6 9 12 15 18 9"></polyline>
            </svg>
          </div>
          
          <div className={`sidebar-group ${expandedGroup === 'incidents' ? 'expanded' : ''}`}>
            <NavLink to="/incidents" className={({ isActive }) => 
              `sidebar-item ${isActive && location.pathname === "/incidents" ? 'active' : ''}`
            }>
              <span>All Incidents</span>
            </NavLink>
            
            <NavLink to="/incidents/new" className={({ isActive }) => 
              `sidebar-item ${isActive ? 'active' : ''}`
            }>
              <span>Create Incident</span>
            </NavLink>
            
            <NavLink to="/incidents?status=OPEN" className={({ isActive }) => 
              `sidebar-item ${location.pathname === "/incidents" && location.search.includes("status=OPEN") ? 'active' : ''}`
            }>
              <span>Open Incidents</span>
            </NavLink>
            
            <NavLink to="/incidents?status=IN_PROGRESS" className={({ isActive }) => 
              `sidebar-item ${location.pathname === "/incidents" && location.search.includes("status=IN_PROGRESS") ? 'active' : ''}`
            }>
              <span>In Progress</span>
            </NavLink>
            
            <NavLink to="/incidents?assigned=me" className={({ isActive }) => 
              `sidebar-item ${location.pathname === "/incidents" && location.search.includes("assigned=me") ? 'active' : ''}`
            }>
              <span>My Assignments</span>
            </NavLink>
          </div>
        </div>

        <div className="sidebar-section">
          <div 
            className={`sidebar-group-header ${expandedGroup === 'communication' ? 'expanded' : ''}`}
            onClick={() => toggleGroup('communication')}
          >
            <svg viewBox="0 0 24 24" width="18" height="18" stroke="currentColor" strokeWidth="2" fill="none">
              <path d="M21 11.5a8.38 8.38 0 0 1-.9 3.8 8.5 8.5 0 0 1-7.6 4.7 8.38 8.38 0 0 1-3.8-.9L3 21l1.9-5.7a8.38 8.38 0 0 1-.9-3.8 8.5 8.5 0 0 1 4.7-7.6 8.38 8.38 0 0 1 3.8-.9h.5a8.48 8.48 0 0 1 8 8v.5z"></path>
            </svg>
            <span>Communication</span>
            <svg className="expand-icon" viewBox="0 0 24 24" width="18" height="18" stroke="currentColor" strokeWidth="2" fill="none">
              <polyline points="6 9 12 15 18 9"></polyline>
            </svg>
          </div>
          
          <div className={`sidebar-group ${expandedGroup === 'communication' ? 'expanded' : ''}`}>
            <NavLink to="/chats" className={({ isActive }) => 
              `sidebar-item ${isActive && location.pathname === "/chats" ? 'active' : ''}`
            }>
              <span>Chat Rooms</span>
            </NavLink>
            
            <NavLink to="/chats/new" className={({ isActive }) => 
              `sidebar-item ${isActive ? 'active' : ''}`
            }>
              <span>Create Chat Room</span>
            </NavLink>
            
            <NavLink to="/notifications" className={({ isActive }) => 
              `sidebar-item ${isActive ? 'active' : ''}`
            }>
              <span>Notifications</span>
            </NavLink>
          </div>
        </div>

        <div className="sidebar-section">
          <NavLink to="/files" className={({ isActive }) => 
            `sidebar-item ${isActive ? 'active' : ''}`
          }>
            <svg viewBox="0 0 24 24" width="18" height="18" stroke="currentColor" strokeWidth="2" fill="none">
              <path d="M13 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V9z"></path>
              <polyline points="13 2 13 9 20 9"></polyline>
            </svg>
            <span>Files & Documents</span>
          </NavLink>
        </div>

        {(hasRole('ADMIN') || hasRole('MANAGER')) && (
          <div className="sidebar-section">
            <div 
              className={`sidebar-group-header ${expandedGroup === 'analytics' ? 'expanded' : ''}`}
              onClick={() => toggleGroup('analytics')}
            >
              <svg viewBox="0 0 24 24" width="18" height="18" stroke="currentColor" strokeWidth="2" fill="none">
                <line x1="18" y1="20" x2="18" y2="10"></line>
                <line x1="12" y1="20" x2="12" y2="4"></line>
                <line x1="6" y1="20" x2="6" y2="14"></line>
              </svg>
              <span>Analytics & Reports</span>
              <svg className="expand-icon" viewBox="0 0 24 24" width="18" height="18" stroke="currentColor" strokeWidth="2" fill="none">
                <polyline points="6 9 12 15 18 9"></polyline>
              </svg>
            </div>
            
            <div className={`sidebar-group ${expandedGroup === 'analytics' ? 'expanded' : ''}`}>
              <NavLink to="/reports/incidents" className={({ isActive }) => 
                `sidebar-item ${isActive ? 'active' : ''}`
              }>
                <span>Incident Reports</span>
              </NavLink>
              
              <NavLink to="/reports/performance" className={({ isActive }) => 
                `sidebar-item ${isActive ? 'active' : ''}`
              }>
                <span>Performance</span>
              </NavLink>
              
              <NavLink to="/reports/usage" className={({ isActive }) => 
                `sidebar-item ${isActive ? 'active' : ''}`
              }>
                <span>Usage Statistics</span>
              </NavLink>
            </div>
          </div>
        )}

        {hasRole('ADMIN') && (
          <div className="sidebar-section">
            <NavLink to="/settings" className={({ isActive }) => 
              `sidebar-item ${isActive ? 'active' : ''}`
            }>
              <svg viewBox="0 0 24 24" width="18" height="18" stroke="currentColor" strokeWidth="2" fill="none">
                <circle cx="12" cy="12" r="3"></circle>
                <path d="M19.4 15a1.65 1.65 0 0 0 .33 1.82l.06.06a2 2 0 0 1 0 2.83 2 2 0 0 1-2.83 0l-.06-.06a1.65 1.65 0 0 0-1.82-.33 1.65 1.65 0 0 0-1 1.51V21a2 2 0 0 1-2 2 2 2 0 0 1-2-2v-.09A1.65 1.65 0 0 0 9 19.4a1.65 1.65 0 0 0-1.82.33l-.06.06a2 2 0 0 1-2.83 0 2 2 0 0 1 0-2.83l.06-.06a1.65 1.65 0 0 0 .33-1.82 1.65 1.65 0 0 0-1.51-1H3a2 2 0 0 1-2-2 2 2 0 0 1 2-2h.09A1.65 1.65 0 0 0 4.6 9a1.65 1.65 0 0 0-.33-1.82l-.06-.06a2 2 0 0 1 0-2.83 2 2 0 0 1 2.83 0l.06.06a1.65 1.65 0 0 0 1.82.33H9a1.65 1.65 0 0 0 1-1.51V3a2 2 0 0 1 2-2 2 2 0 0 1 2 2v.09a1.65 1.65 0 0 0 1 1.51 1.65 1.65 0 0 0 1.82-.33l.06-.06a2 2 0 0 1 2.83 0 2 2 0 0 1 0 2.83l-.06.06a1.65 1.65 0 0 0-.33 1.82V9a1.65 1.65 0 0 0 1.51 1H21a2 2 0 0 1 2 2 2 2 0 0 1-2 2h-.09a1.65 1.65 0 0 0-1.51 1z"></path>
              </svg>
              <span>System Settings</span>
            </NavLink>
            
            <NavLink to="/users" className={({ isActive }) => 
              `sidebar-item ${isActive ? 'active' : ''}`
            }>
              <svg viewBox="0 0 24 24" width="18" height="18" stroke="currentColor" strokeWidth="2" fill="none">
                <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"></path>
                <circle cx="9" cy="7" r="4"></circle>
                <path d="M23 21v-2a4 4 0 0 0-3-3.87"></path>
                <path d="M16 3.13a4 4 0 0 1 0 7.75"></path>
              </svg>
              <span>User Management</span>
            </NavLink>
          </div>
        )}
      </nav>
      
      <div className="sidebar-footer">
        <div className="app-version">
          <span>Version 1.0.0</span>
        </div>
      </div>
    </aside>
  );
}

Sidebar.propTypes = {
  isOpen: PropTypes.bool.isRequired,
  darkMode: PropTypes.bool
};

Sidebar.defaultProps = {
  darkMode: false
};

export default Sidebar; 