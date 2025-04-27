import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import notificationService from '../../services/notificationService';
import { notificationWebSocketService } from '../../services/websocketService';
import { useAuth } from '../../utils/AuthContext';

function Notifications() {
  const [notifications, setNotifications] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const { currentUser } = useAuth();

  useEffect(() => {
    // Connect to WebSocket for real-time notifications
    notificationWebSocketService.connect('notification', () => {
      // Subscribe to user's notifications
      notificationWebSocketService.subscribe(`/topic/notifications/${currentUser.id}`, (notification) => {
        // Add new notification to the list
        setNotifications(prevNotifications => [notification, ...prevNotifications]);
      });
    });

    // Fetch existing notifications
    fetchNotifications();

    return () => {
      // Unsubscribe when component unmounts
      notificationWebSocketService.unsubscribe(`/topic/notifications/${currentUser.id}`);
    };
  }, [currentUser.id]);

  const fetchNotifications = async () => {
    try {
      setLoading(true);
      const notificationsData = await notificationService.getNotifications();
      setNotifications(notificationsData);
    } catch (err) {
      console.error('Error fetching notifications:', err);
      setError('Failed to load notifications. Please try again later.');
    } finally {
      setLoading(false);
    }
  };

  const handleMarkAsRead = async (id) => {
    try {
      await notificationService.markAsRead(id);
      
      // Update notification in the list
      setNotifications(notifications.map(notification => 
        notification.id === id 
          ? { ...notification, read: true }
          : notification
      ));
    } catch (err) {
      console.error('Error marking notification as read:', err);
    }
  };

  const handleMarkAllAsRead = async () => {
    try {
      await notificationService.markAllAsRead();
      
      // Update all notifications in the list
      setNotifications(notifications.map(notification => ({ ...notification, read: true })));
    } catch (err) {
      console.error('Error marking all notifications as read:', err);
    }
  };

  const handleDeleteNotification = async (id) => {
    try {
      await notificationService.deleteNotification(id);
      
      // Remove notification from the list
      setNotifications(notifications.filter(notification => notification.id !== id));
    } catch (err) {
      console.error('Error deleting notification:', err);
    }
  };

  // Get unread notification count
  const unreadCount = notifications.filter(notification => !notification.read).length;

  // Group notifications by date
  const groupedNotifications = notifications.reduce((groups, notification) => {
    const date = new Date(notification.timestamp).toLocaleDateString();
    if (!groups[date]) {
      groups[date] = [];
    }
    groups[date].push(notification);
    return groups;
  }, {});

  return (
    <div className="notifications-container">
      <header className="page-header">
        <h1>Notifications</h1>
        <div className="notification-actions">
          <button 
            onClick={handleMarkAllAsRead} 
            className="btn btn-secondary"
            disabled={unreadCount === 0}
          >
            Mark All as Read
          </button>
        </div>
      </header>

      {loading ? (
        <div className="loading-indicator">Loading notifications...</div>
      ) : error ? (
        <div className="error-message">{error}</div>
      ) : (
        <>
          {notifications.length === 0 ? (
            <div className="empty-state">
              <p>No notifications yet. Notifications will appear here when you receive them.</p>
            </div>
          ) : (
            <div className="notifications-list">
              {Object.entries(groupedNotifications).map(([date, dateNotifications]) => (
                <div key={date} className="notification-group">
                  <div className="notification-date">{date}</div>
                  {dateNotifications.map(notification => (
                    <div 
                      key={notification.id} 
                      className={`notification-item ${notification.read ? 'read' : 'unread'}`}
                    >
                      <div className="notification-icon">
                        {getNotificationIcon(notification.type)}
                      </div>
                      <div className="notification-content">
                        <div className="notification-header">
                          <span className="notification-title">{notification.title}</span>
                          <span className="notification-time">
                            {new Date(notification.timestamp).toLocaleTimeString()}
                          </span>
                        </div>
                        <div className="notification-message">{notification.message}</div>
                        {notification.actionLink && (
                          <Link to={notification.actionLink} className="notification-link">
                            View Details
                          </Link>
                        )}
                      </div>
                      <div className="notification-actions">
                        {!notification.read && (
                          <button 
                            onClick={() => handleMarkAsRead(notification.id)}
                            className="btn btn-sm btn-secondary"
                          >
                            Mark as Read
                          </button>
                        )}
                        <button 
                          onClick={() => handleDeleteNotification(notification.id)}
                          className="btn btn-sm btn-danger"
                        >
                          Delete
                        </button>
                      </div>
                    </div>
                  ))}
                </div>
              ))}
            </div>
          )}
        </>
      )}
    </div>
  );
}

// Helper function to get icon based on notification type
function getNotificationIcon(type) {
  switch (type) {
    case 'INCIDENT_CREATED':
    case 'INCIDENT_UPDATED':
    case 'INCIDENT_ASSIGNED':
      return '🚨';
    case 'CHAT_MESSAGE':
      return '💬';
    case 'FILE_UPLOAD':
      return '📁';
    case 'MENTION':
      return '@';
    default:
      return '📌';
  }
}

export default Notifications; 