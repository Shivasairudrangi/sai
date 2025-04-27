import api from './api';

const FILE_ENDPOINTS = {
  BASE: '/files',
  BY_ID: (id) => `/files/${id}`,
  UPLOAD: '/files/upload',
  DOWNLOAD: (id) => `/files/download/${id}`,
  SEARCH: '/files/search',
  TAGS: '/files/tags',
  ADD_TAG: (id) => `/files/${id}/tags`,
  REMOVE_TAG: (id, tag) => `/files/${id}/tags/${tag}`,
  SHARE: (id) => `/files/${id}/share`,
  INCIDENT_FILES: (incidentId) => `/files/incident/${incidentId}`
};

const fileService = {
  // Get all files with optional filters
  getFiles: async (params = {}) => {
    const response = await api.get(FILE_ENDPOINTS.BASE, { params });
    return response.data;
  },

  // Get file by ID
  getFileById: async (id) => {
    const response = await api.get(FILE_ENDPOINTS.BY_ID(id));
    return response.data;
  },

  // Upload a file
  uploadFile: async (formData) => {
    const response = await api.post(FILE_ENDPOINTS.UPLOAD, formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    });
    return response.data;
  },

  // Update file metadata
  updateFile: async (id, fileData) => {
    const response = await api.put(FILE_ENDPOINTS.BY_ID(id), fileData);
    return response.data;
  },

  // Delete a file
  deleteFile: async (id) => {
    const response = await api.delete(FILE_ENDPOINTS.BY_ID(id));
    return response.data;
  },

  // Download a file
  downloadFile: async (id) => {
    const response = await api.get(FILE_ENDPOINTS.DOWNLOAD(id), {
      responseType: 'blob'
    });
    
    // Create a download link and trigger download
    const url = window.URL.createObjectURL(new Blob([response.data]));
    const link = document.createElement('a');
    link.href = url;
    
    // Try to get filename from content-disposition header
    const contentDisposition = response.headers['content-disposition'];
    let filename = 'download';
    
    if (contentDisposition) {
      const filenameMatch = contentDisposition.match(/filename="(.+)"/);
      if (filenameMatch.length === 2) {
        filename = filenameMatch[1];
      }
    }
    
    link.setAttribute('download', filename);
    document.body.appendChild(link);
    link.click();
    link.remove();
    
    return response.data;
  },

  // Search files
  searchFiles: async (query) => {
    const response = await api.get(FILE_ENDPOINTS.SEARCH, { params: { query } });
    return response.data;
  },

  // Get all tags
  getTags: async () => {
    const response = await api.get(FILE_ENDPOINTS.TAGS);
    return response.data;
  },

  // Add tag to file
  addTag: async (id, tag) => {
    const response = await api.post(FILE_ENDPOINTS.ADD_TAG(id), { tag });
    return response.data;
  },

  // Remove tag from file
  removeTag: async (id, tag) => {
    const response = await api.delete(FILE_ENDPOINTS.REMOVE_TAG(id, tag));
    return response.data;
  },

  // Share file with user
  shareFile: async (id, userId) => {
    const response = await api.post(FILE_ENDPOINTS.SHARE(id), { userId });
    return response.data;
  },

  // Get files for specific incident
  getIncidentFiles: async (incidentId) => {
    const response = await api.get(FILE_ENDPOINTS.INCIDENT_FILES(incidentId));
    return response.data;
  }
};

export default fileService; 