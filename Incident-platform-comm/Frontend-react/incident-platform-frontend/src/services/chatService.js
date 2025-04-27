import api from './api';

const CHAT_ENDPOINTS = {
  ROOMS: '/api/chat/rooms',
  ROOM_BY_ID: (id) => `/api/chat/rooms/${id}`,
  ROOM_USERS: (id) => `/api/chat/rooms/${id}/users`,
  MESSAGES: '/api/chat/messages',
  ROOM_MESSAGES: (roomId) => `/api/chat/rooms/${roomId}/messages`,
  MESSAGE_BY_ID: (id) => `/api/chat/messages/${id}`,
  INCIDENT_CHATS: (incidentId) => `/api/chat/rooms/incident/${incidentId}`
};

const chatService = {
  // Chat rooms operations
  getRooms: async () => {
    try {
      const response = await api.get(CHAT_ENDPOINTS.ROOMS);
      return response.data || [];
    } catch (error) {
      console.error('Error getting chat rooms:', error);
      // Return empty array instead of throwing
      return [];
    }
  },

  getRoomById: async (id) => {
    try {
      const response = await api.get(CHAT_ENDPOINTS.ROOM_BY_ID(id));
      return response.data;
    } catch (error) {
      console.error(`Error getting chat room #${id}:`, error);
      throw error;
    }
  },

  createRoom: async (roomData) => {
    try {
      const response = await api.post(CHAT_ENDPOINTS.ROOMS, roomData);
      return response.data;
    } catch (error) {
      console.error('Error creating chat room:', error);
      throw error;
    }
  },

  updateRoom: async (id, roomData) => {
    const response = await api.put(CHAT_ENDPOINTS.ROOM_BY_ID(id), roomData);
    return response.data;
  },

  deleteRoom: async (id) => {
    const response = await api.delete(CHAT_ENDPOINTS.ROOM_BY_ID(id));
    return response.data;
  },

  // Room users operations
  getRoomUsers: async (roomId) => {
    const response = await api.get(CHAT_ENDPOINTS.ROOM_USERS(roomId));
    return response.data;
  },

  addUserToRoom: async (roomId, userId) => {
    const response = await api.post(CHAT_ENDPOINTS.ROOM_USERS(roomId), { userId });
    return response.data;
  },

  removeUserFromRoom: async (roomId, userId) => {
    const response = await api.delete(`${CHAT_ENDPOINTS.ROOM_USERS(roomId)}/${userId}`);
    return response.data;
  },

  // Messages operations
  getMessages: async (roomId, params = {}) => {
    const response = await api.get(CHAT_ENDPOINTS.ROOM_MESSAGES(roomId), { params });
    return response.data;
  },

  sendMessage: async (messageData) => {
    const response = await api.post(CHAT_ENDPOINTS.MESSAGES, messageData);
    return response.data;
  },

  updateMessage: async (id, content) => {
    const response = await api.put(CHAT_ENDPOINTS.MESSAGE_BY_ID(id), { content });
    return response.data;
  },

  deleteMessage: async (id) => {
    const response = await api.delete(CHAT_ENDPOINTS.MESSAGE_BY_ID(id));
    return response.data;
  },

  // Get chat rooms for specific incident
  getIncidentChats: async (incidentId) => {
    const response = await api.get(CHAT_ENDPOINTS.INCIDENT_CHATS(incidentId));
    return response.data;
  }
};

export default chatService; 