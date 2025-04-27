import api from './api';

const NOTIFICATION_ENDPOINTS = {
  ALL: '/api/notifications',
  UNREAD: '/api/notifications/unread',
  MARK_READ: '/api/notifications/mark-read',
  PREFERENCES: '/api/notifications/preferences',
  SUMMARY: '/api/notifications/summary'
};

const notificationService = {
  // Get all notifications for the current user
  getAllNotifications: async (page = 0, size = 10) => {
    try {
      const response = await api.get(`${NOTIFICATION_ENDPOINTS.ALL}?page=${page}&size=${size}`);
      return response.data;
    } catch (error) {
      console.error('Error fetching notifications:', error);
      throw error;
    }
  },

  // Get only unread notifications
  getUnreadNotifications: async () => {
    try {
      const response = await api.get(NOTIFICATION_ENDPOINTS.UNREAD);
      return response.data;
    } catch (error) {
      console.error('Error fetching unread notifications:', error);
      throw error;
    }
  },

  // Get notification summary (recent + counts)
  getSummary: async () => {
    try {
      const response = await api.get(NOTIFICATION_ENDPOINTS.SUMMARY);
      return response.data || { recentNotifications: [], unreadCount: 0 };
    } catch (error) {
      console.error('Error fetching notification summary:', error);
      // Return a default object instead of throwing to prevent UI errors
      return { recentNotifications: [], unreadCount: 0 };
    }
  },

  // Mark notification as read
  markAsRead: async (notificationId) => {
    try {
      const response = await api.put(`${NOTIFICATION_ENDPOINTS.MARK_READ}/${notificationId}`);
      return response.data;
    } catch (error) {
      console.error('Error marking notification as read:', error);
      throw error;
    }
  },

  // Update notification preferences
  updatePreferences: async (preferences) => {
    try {
      const response = await api.put(NOTIFICATION_ENDPOINTS.PREFERENCES, preferences);
      return response.data;
    } catch (error) {
      console.error('Error updating notification preferences:', error);
      throw error;
    }
  }
};

export default notificationService; 