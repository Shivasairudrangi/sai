import { useState, useEffect, useRef } from 'react';
import { Link } from 'react-router-dom';
import incidentService from '../../services/incidentService';
import notificationService from '../../services/notificationService';
import { incidentWebSocketService } from '../../services/websocketService';
import { useAuth } from '../../utils/AuthContext';

function Dashboard() {
  const { currentUser } = useAuth();
  const [stats, setStats] = useState({
    open: 0,
    inProgress: 0,
    resolved: 0,
    closed: 0,
    highPriority: 0
  });
  const [recentIncidents, setRecentIncidents] = useState([]);
  const [notifications, setNotifications] = useState([]);
  const [assignedToMe, setAssignedToMe] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [lastUpdated, setLastUpdated] = useState(new Date());
  const wsConnected = useRef(false);

  // Connect to WebSocket for real-time updates
  useEffect(() => {
    if (!wsConnected.current) {
      incidentWebSocketService.connect('dashboard', () => {
        console.log('Connected to incident websocket');
        
        // Subscribe to incident updates
        incidentWebSocketService.subscribe('/topic/incidents', (message) => {
          console.log('Received incident update:', message);
          // Refresh data when receiving an update
          fetchDashboardData();
        });
        
        wsConnected.current = true;
      });
    }
    
    return () => {
      if (wsConnected.current) {
        incidentWebSocketService.disconnect();
        wsConnected.current = false;
      }
    };
  }, []);

  // Fetch dashboard data on mount
  useEffect(() => {
    fetchDashboardData();
    
    // Set a timeout to ensure loading doesn't get stuck
    const timeoutId = setTimeout(() => {
      if (loading) setLoading(false);
    }, 5000);
    
    return () => clearTimeout(timeoutId);
  }, []);

  // Periodically refresh data (every 5 minutes)
  useEffect(() => {
    const interval = setInterval(() => {
      fetchDashboardData();
    }, 300000); // 5 minutes
    
    return () => clearInterval(interval);
  }, []);

  const fetchDashboardData = async () => {
    try {
      setLoading(true);
      setError(null);
      
      // Create an array of promises to fetch data in parallel
      const promises = [
        // Get incident statistics
        (async () => {
          try {
            const incidentStats = await incidentService.getIncidentStats();
            console.log('Stats response:', incidentStats);
            if (incidentStats && incidentStats.countByStatus) {
              setStats({
                open: incidentStats.countByStatus.OPEN || 0,
                inProgress: incidentStats.countByStatus.IN_PROGRESS || 0,
                resolved: incidentStats.countByStatus.RESOLVED || 0,
                closed: incidentStats.countByStatus.CLOSED || 0,
                highPriority: incidentStats.countByPriority?.HIGH || 0
              });
            }
          } catch (statsError) {
            console.error('Error fetching incident statistics:', statsError);
          }
        })(),
        
        // Get recent incidents
        (async () => {
          try {
            const incidents = await incidentService.getIncidents(0, 5);
            console.log('Recent incidents response:', incidents);
            if (incidents && incidents.content) {
              setRecentIncidents(incidents.content || []);
            } else if (Array.isArray(incidents)) {
              setRecentIncidents(incidents);
            }
          } catch (incidentsError) {
            console.error('Error fetching recent incidents:', incidentsError);
          }
        })(),
        
        // Get notifications
        (async () => {
          try {
            const notificationData = await notificationService.getRecentNotifications(5);
            console.log('Notifications response:', notificationData);
            if (notificationData) {
              setNotifications(Array.isArray(notificationData) ? notificationData : []);
            }
          } catch (notifError) {
            console.error('Error fetching notifications:', notifError);
          }
        })(),
        
        // Get incidents assigned to current user
        (async () => {
          try {
            if (currentUser?.id) {
              // Make sure we have the current user ID before making this call
              const myIncidents = await incidentService.getIncidentsAssignedToMe();
              console.log('Assigned incidents response:', myIncidents);
              if (myIncidents) {
                setAssignedToMe(Array.isArray(myIncidents) ? myIncidents : []);
              }
            }
          } catch (assignedError) {
            console.error('Error fetching assigned incidents:', assignedError);
          }
        })()
      ];
      
      // Wait for all promises to settle (completed or failed)
      const results = await Promise.allSettled(promises);
      
      // Check if all promises were rejected
      const allFailed = results.every(result => result.status === 'rejected');
      if (allFailed) {
        setError('Failed to load dashboard data. Please try again later.');
      }
      
      setLastUpdated(new Date());
    } catch (err) {
      console.error('Error fetching dashboard data:', err);
      setError('Failed to load dashboard data. Please try again later.');
    } finally {
      setLoading(false);
    }
  };
  
  const formatDate = (dateString) => {
    if (!dateString) return 'N/A';
    
    const date = new Date(dateString);
    const now = new Date();
    const diffMs = now - date;
    const diffMins = Math.floor(diffMs / 60000);
    const diffHours = Math.floor(diffMins / 60);
    const diffDays = Math.floor(diffHours / 24);
    
    if (diffMins < 1) return 'Just now';
    if (diffMins < 60) return `${diffMins}m ago`;
    if (diffHours < 24) return `${diffHours}h ago`;
    if (diffDays < 7) return `${diffDays}d ago`;
    
    return date.toLocaleDateString();
  };
  
  const getPriorityClass = (priority) => {
    switch (priority) {
      case 'HIGH': return 'priority-high';
      case 'MEDIUM': return 'priority-medium';
      case 'LOW': return 'priority-low';
      default: return '';
    }
  };
  
  const getStatusClass = (status) => {
    switch (status) {
      case 'OPEN': return 'status-open';
      case 'IN_PROGRESS': return 'status-in-progress';
      case 'RESOLVED': return 'status-resolved';
      case 'CLOSED': return 'status-closed';
      default: return '';
    }
  };
  
  const getNotificationIcon = (type) => {
    switch (type) {
      case 'INCIDENT_CREATED':
        return (
          <svg viewBox="0 0 24 24" width="16" height="16" stroke="currentColor" strokeWidth="2" fill="none">
            <polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"></polygon>
          </svg>
        );
      case 'INCIDENT_UPDATED':
        return (
          <svg viewBox="0 0 24 24" width="16" height="16" stroke="currentColor" strokeWidth="2" fill="none">
            <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"></path>
            <path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"></path>
          </svg>
        );
      case 'MESSAGE_RECEIVED':
        return (
          <svg viewBox="0 0 24 24" width="16" height="16" stroke="currentColor" strokeWidth="2" fill="none">
            <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"></path>
          </svg>
        );
      case 'STATUS_CHANGED':
        return (
          <svg viewBox="0 0 24 24" width="16" height="16" stroke="currentColor" strokeWidth="2" fill="none">
            <path d="M14 9V5a3 3 0 0 0-3-3l-4 9v11h11.28a2 2 0 0 0 2-1.7l1.38-9a2 2 0 0 0-2-2.3zM7 22H4a2 2 0 0 1-2-2v-7a2 2 0 0 1 2-2h3"></path>
          </svg>
        );
      case 'ASSIGNMENT_CHANGED':
        return (
          <svg viewBox="0 0 24 24" width="16" height="16" stroke="currentColor" strokeWidth="2" fill="none">
            <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"></path>
            <circle cx="9" cy="7" r="4"></circle>
            <path d="M23 21v-2a4 4 0 0 0-3-3.87"></path>
            <path d="M16 3.13a4 4 0 0 1 0 7.75"></path>
          </svg>
        );
      default:
        return (
          <svg viewBox="0 0 24 24" width="16" height="16" stroke="currentColor" strokeWidth="2" fill="none">
            <circle cx="12" cy="12" r="10"></circle>
            <line x1="12" y1="8" x2="12" y2="12"></line>
            <line x1="12" y1="16" x2="12.01" y2="16"></line>
          </svg>
        );
    }
  };
  
  if (loading && recentIncidents.length === 0) {
    return (
      <div className="loading-container">
        <div className="loading-spinner"></div>
        <p>Loading dashboard data...</p>
      </div>
    );
  }

  return (
    <div className="dashboard-container">
      <div className="dashboard-header">
        <div className="dashboard-title">
          <h1>Dashboard</h1>
          <div className="dashboard-subtitle">
            <span>Welcome back, {currentUser?.name || currentUser?.username}</span>
            <span className="last-updated">Last updated: {lastUpdated.toLocaleTimeString()}</span>
            <button 
              className="refresh-button" 
              onClick={fetchDashboardData}
              disabled={loading}
            >
              <svg viewBox="0 0 24 24" width="16" height="16" stroke="currentColor" strokeWidth="2" fill="none">
                <path d="M23 4v6h-6"></path>
                <path d="M1 20v-6h6"></path>
                <path d="M3.51 9a9 9 0 0 1 14.85-3.36L23 10M1 14l4.64 4.36A9 9 0 0 0 20.49 15"></path>
              </svg>
              <span>{loading ? 'Refreshing...' : 'Refresh'}</span>
            </button>
          </div>
        </div>
        
        {error && (
          <div className="error-message">
            {error}
            <button 
              className="retry-button" 
              onClick={fetchDashboardData}
              disabled={loading}
            >
              Retry
            </button>
          </div>
        )}
        
        <div className="stats-grid">
          <div className="stat-card open">
            <div className="stat-icon">
              <svg viewBox="0 0 24 24" width="24" height="24" stroke="currentColor" strokeWidth="2" fill="none">
                <circle cx="12" cy="12" r="10"></circle>
                <line x1="12" y1="8" x2="12" y2="12"></line>
                <line x1="12" y1="16" x2="12.01" y2="16"></line>
              </svg>
            </div>
            <div className="stat-info">
              <div className="stat-title">Open Incidents</div>
              <div className="stat-value">{stats.open}</div>
            </div>
            <Link to="/incidents?status=OPEN" className="stat-link">View all</Link>
          </div>
          
          <div className="stat-card in-progress">
            <div className="stat-icon">
              <svg viewBox="0 0 24 24" width="24" height="24" stroke="currentColor" strokeWidth="2" fill="none">
                <path d="M18.2 18.5c.5-.7.8-1.6.8-2.5 0-2.8-2.2-5-5-5 .8-1.3 2.1-2 3.5-2 2.4 0 4.3 2 4.5 4.5 2 .3 3.5 2 3.5 4s-1.8 3.5-3.5 3.5h-10"></path>
                <polyline points="9 17 6 20 3 17"></polyline>
                <line x1="6" y1="20" x2="6" y2="10"></line>
              </svg>
            </div>
            <div className="stat-info">
              <div className="stat-title">In Progress</div>
              <div className="stat-value">{stats.inProgress}</div>
            </div>
            <Link to="/incidents?status=IN_PROGRESS" className="stat-link">View all</Link>
          </div>
          
          <div className="stat-card resolved">
            <div className="stat-icon">
              <svg viewBox="0 0 24 24" width="24" height="24" stroke="currentColor" strokeWidth="2" fill="none">
                <polyline points="20 6 9 17 4 12"></polyline>
              </svg>
            </div>
            <div className="stat-info">
              <div className="stat-title">Resolved</div>
              <div className="stat-value">{stats.resolved}</div>
            </div>
            <Link to="/incidents?status=RESOLVED" className="stat-link">View all</Link>
          </div>
          
          <div className="stat-card high-priority">
            <div className="stat-icon">
              <svg viewBox="0 0 24 24" width="24" height="24" stroke="currentColor" strokeWidth="2" fill="none">
                <path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z"></path>
                <line x1="12" y1="9" x2="12" y2="13"></line>
                <line x1="12" y1="17" x2="12.01" y2="17"></line>
              </svg>
            </div>
            <div className="stat-info">
              <div className="stat-title">High Priority</div>
              <div className="stat-value">{stats.highPriority}</div>
            </div>
            <Link to="/incidents?priority=HIGH" className="stat-link">View all</Link>
          </div>
        </div>
      </div>
      
      <div className="dashboard-content">
        <div className="dashboard-grid">
          {/* My Assignments Section */}
          <div className="dashboard-section my-assignments">
            <div className="section-header">
              <h2>My Assignments</h2>
              <Link to="/incidents?assigned=me" className="view-all">View all</Link>
            </div>
            
            <div className="card">
              <div className="card-body">
                {assignedToMe.length === 0 ? (
                  <div className="empty-state">
                    <svg viewBox="0 0 24 24" width="40" height="40" stroke="currentColor" strokeWidth="1" fill="none">
                      <rect x="3" y="3" width="18" height="18" rx="2" ry="2"></rect>
                      <line x1="9" y1="9" x2="15" y2="15"></line>
                      <line x1="15" y1="9" x2="9" y2="15"></line>
                    </svg>
                    <p>No incidents assigned to you</p>
                  </div>
                ) : (
                  <div className="incident-list">
                    {assignedToMe.map(incident => (
                      <div className="incident-item" key={incident.id}>
                        <div className={`priority-indicator ${getPriorityClass(incident.priority)}`}></div>
                        <div className="incident-content">
                          <Link to={`/incidents/${incident.id}`} className="incident-title">
                            {incident.title}
                          </Link>
                          <div className="incident-meta">
                            <span className={`status-badge ${getStatusClass(incident.status)}`}>
                              {incident.status?.replace('_', ' ')}
                            </span>
                            <span className="incident-time">
                              {formatDate(incident.updatedAt || incident.createdAt)}
                            </span>
                          </div>
                        </div>
                      </div>
                    ))}
                  </div>
                )}
              </div>
            </div>
          </div>
          
          {/* Recent Incidents Section */}
          <div className="dashboard-section recent-incidents">
            <div className="section-header">
              <h2>Recent Incidents</h2>
              <Link to="/incidents" className="view-all">View all</Link>
            </div>
            
            <div className="card">
              <div className="card-body">
                {recentIncidents.length === 0 ? (
                  <div className="empty-state">
                    <svg viewBox="0 0 24 24" width="40" height="40" stroke="currentColor" strokeWidth="1" fill="none">
                      <circle cx="12" cy="12" r="10"></circle>
                      <line x1="12" y1="8" x2="12" y2="12"></line>
                      <line x1="12" y1="16" x2="12.01" y2="16"></line>
                    </svg>
                    <p>No incidents found</p>
                    <Link to="/incidents/new" className="btn btn-primary">
                      Create Incident
                    </Link>
                  </div>
                ) : (
                  <div className="incident-list">
                    {recentIncidents.map(incident => (
                      <div className="incident-item" key={incident.id}>
                        <div className={`priority-indicator ${getPriorityClass(incident.priority)}`}></div>
                        <div className="incident-content">
                          <Link to={`/incidents/${incident.id}`} className="incident-title">
                            {incident.title}
                          </Link>
                          <div className="incident-meta">
                            <span className={`status-badge ${getStatusClass(incident.status)}`}>
                              {incident.status?.replace('_', ' ')}
                            </span>
                            <span className="incident-assignee">
                              {incident.assignedTo ? `Assigned to: ${incident.assignee?.name || 'User #' + incident.assignedTo}` : 'Unassigned'}
                            </span>
                            <span className="incident-time">
                              {formatDate(incident.createdAt)}
                            </span>
                          </div>
                        </div>
                      </div>
                    ))}
                  </div>
                )}
              </div>
            </div>
          </div>
          
          {/* Recent Notifications Section */}
          <div className="dashboard-section notifications">
            <div className="section-header">
              <h2>Recent Notifications</h2>
              <Link to="/notifications" className="view-all">View all</Link>
            </div>
            
            <div className="card">
              <div className="card-body">
                {notifications.length === 0 ? (
                  <div className="empty-state">
                    <svg viewBox="0 0 24 24" width="40" height="40" stroke="currentColor" strokeWidth="1" fill="none">
                      <path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9"></path>
                      <path d="M13.73 21a2 2 0 0 1-3.46 0"></path>
                    </svg>
                    <p>No new notifications</p>
                  </div>
                ) : (
                  <div className="notification-list">
                    {notifications.map(notification => (
                      <div className="notification-item" key={notification.id}>
                        <div className="notification-icon">
                          {getNotificationIcon(notification.type)}
                        </div>
                        <div className="notification-content">
                          <div className="notification-message">
                            {notification.message}
                          </div>
                          <div className="notification-time">
                            {formatDate(notification.createdAt)}
                          </div>
                        </div>
                      </div>
                    ))}
                  </div>
                )}
              </div>
            </div>
          </div>
          
          {/* Quick Actions Section */}
          <div className="dashboard-section quick-actions">
            <div className="section-header">
              <h2>Quick Actions</h2>
            </div>
            
            <div className="card">
              <div className="card-body">
                <div className="actions-grid">
                  <Link to="/incidents/new" className="action-button">
                    <svg viewBox="0 0 24 24" width="24" height="24" stroke="currentColor" strokeWidth="2" fill="none">
                      <circle cx="12" cy="12" r="10"></circle>
                      <line x1="12" y1="8" x2="12" y2="16"></line>
                      <line x1="8" y1="12" x2="16" y2="12"></line>
                    </svg>
                    <span>Create Incident</span>
                  </Link>
                  
                  <Link to="/chats/new" className="action-button">
                    <svg viewBox="0 0 24 24" width="24" height="24" stroke="currentColor" strokeWidth="2" fill="none">
                      <path d="M21 11.5a8.38 8.38 0 0 1-.9 3.8 8.5 8.5 0 0 1-7.6 4.7 8.38 8.38 0 0 1-3.8-.9L3 21l1.9-5.7a8.38 8.38 0 0 1-.9-3.8 8.5 8.5 0 0 1 4.7-7.6 8.38 8.38 0 0 1 3.8-.9h.5a8.48 8.48 0 0 1 8 8v.5z"></path>
                    </svg>
                    <span>New Chat Room</span>
                  </Link>
                  
                  <Link to="/files" className="action-button">
                    <svg viewBox="0 0 24 24" width="24" height="24" stroke="currentColor" strokeWidth="2" fill="none">
                      <path d="M13 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V9z"></path>
                      <polyline points="13 2 13 9 20 9"></polyline>
                    </svg>
                    <span>Upload Files</span>
                  </Link>
                  
                  <Link to="/incidents?status=RESOLVED" className="action-button">
                    <svg viewBox="0 0 24 24" width="24" height="24" stroke="currentColor" strokeWidth="2" fill="none">
                      <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"></path>
                      <polyline points="22 4 12 14.01 9 11.01"></polyline>
                    </svg>
                    <span>View Resolved</span>
                  </Link>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}

export default Dashboard; 