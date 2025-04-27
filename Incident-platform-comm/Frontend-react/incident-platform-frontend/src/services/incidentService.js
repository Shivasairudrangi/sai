import api from './api';

// Debug helper
const logApiRequest = (endpoint, params = null) => {
  console.log(`API Request: ${endpoint}`, params ? { params } : '');
};

// Debug helper for responses
const logApiResponse = (endpoint, response) => {
  console.log(`API Response from ${endpoint}:`, response);
  return response;
};

// Global flag for mock mode - set to false to use real backend
const MOCK_MODE = false;

// Mock data for testing
const MOCK_DATA = {
  incidents: [
    { 
      id: 1, 
      title: "Network Outage in Building B", 
      description: "All systems in Building B are experiencing connectivity issues. Initial troubleshooting underway.",
      status: "OPEN",
      priority: "HIGH",
      createdAt: new Date(Date.now() - 86400000).toISOString(),
      updatedAt: new Date(Date.now() - 43200000).toISOString(),
      createdBy: 1,
      assignedTo: 2,
      tags: ["network", "infrastructure", "urgent"]
    },
    { 
      id: 2, 
      title: "Database Server Slow Response", 
      description: "The production database server is responding slowly to queries. Users reporting timeouts.",
      status: "IN_PROGRESS",
      priority: "HIGH", 
      createdAt: new Date(Date.now() - 172800000).toISOString(),
      updatedAt: new Date(Date.now() - 21600000).toISOString(),
      createdBy: 2,
      assignedTo: 3,
      tags: ["database", "performance", "critical"]
    },
    { 
      id: 3, 
      title: "Login Page Errors", 
      description: "Users are occasionally seeing errors when trying to log in to the application.",
      status: "OPEN",
      priority: "MEDIUM",
      createdAt: new Date(Date.now() - 259200000).toISOString(),
      updatedAt: new Date(Date.now() - 259200000).toISOString(),
      createdBy: 1,
      assignedTo: null,
      tags: ["frontend", "authentication"]
    },
    { 
      id: 4, 
      title: "Email Notifications Not Sending", 
      description: "System emails for alerts are not being delivered to recipients.",
      status: "RESOLVED",
      priority: "MEDIUM",
      createdAt: new Date(Date.now() - 432000000).toISOString(),
      updatedAt: new Date(Date.now() - 259200000).toISOString(),
      resolvedAt: new Date(Date.now() - 259200000).toISOString(),
      createdBy: 3,
      assignedTo: 2,
      tags: ["email", "notifications"]
    },
    { 
      id: 5, 
      title: "Backup Failure", 
      description: "Last night's automated backup process failed to complete.",
      status: "RESOLVED",
      priority: "HIGH",
      createdAt: new Date(Date.now() - 518400000).toISOString(),
      updatedAt: new Date(Date.now() - 432000000).toISOString(),
      resolvedAt: new Date(Date.now() - 432000000).toISOString(),
      createdBy: 2,
      assignedTo: 1,
      tags: ["backup", "infrastructure"]
    }
  ],
  stats: {
    countByStatus: {
      OPEN: 2,
      IN_PROGRESS: 1,
      RESOLVED: 2,
      CLOSED: 0
    },
    countByPriority: {
      LOW: 0,
      MEDIUM: 2,
      HIGH: 3
    }
  }
};

const INCIDENT_ENDPOINTS = {
  BASE: '/api/incidents',
  BY_ID: (id) => `/api/incidents/${id}`,
  ASSIGNED: '/api/incidents/assigned',
  CREATED: '/api/incidents/created',
  STATUS: (status) => `/api/incidents/status/${status}`,
  PRIORITY: (priority) => `/api/incidents/priority/${priority}`,
  SEARCH: '/api/incidents/search',
  TAG: (tag) => `/api/incidents/tag/${tag}`,
  HIGH_PRIORITY: '/api/incidents/high-priority',
  HISTORY: (id) => `/api/incidents/${id}/history`,
  HISTORY_PAGED: (id) => `/api/incidents/${id}/history/paged`,
  STATS: '/api/incidents/stats'
};

// Mock API response helper
const mockResponse = (data, delay = 500) => {
  return new Promise(resolve => {
    setTimeout(() => {
      resolve(data);
    }, delay);
  });
};

const incidentService = {
  // Get incidents with pagination and optional filters
  getIncidents: async (pageParam = 0, sizeParam = 10, statusParam = null, priorityParam = null) => {
    if (MOCK_MODE) {
      console.log('MOCK MODE: getIncidents', { pageParam, sizeParam, statusParam, priorityParam });
      
      let filtered = [...MOCK_DATA.incidents];
      
      // Apply filters if provided
      if (statusParam) {
        filtered = filtered.filter(i => i.status === statusParam);
      }
      
      if (priorityParam) {
        filtered = filtered.filter(i => i.priority === priorityParam);
      }
      
      // Sort by creation date descending
      filtered.sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt));
      
      // Apply pagination
      const start = pageParam * sizeParam;
      const end = start + sizeParam;
      const paged = filtered.slice(start, end);
      
      return mockResponse({
        content: paged,
        pageable: {
          pageNumber: pageParam,
          pageSize: sizeParam
        },
        totalElements: filtered.length,
        totalPages: Math.ceil(filtered.length / sizeParam),
        last: end >= filtered.length,
        first: pageParam === 0,
        empty: paged.length === 0
      });
    }
    
    try {
      let page = pageParam;
      let size = sizeParam;
      let status = statusParam;
      let priority = priorityParam;
      let sort = null;
      let direction = "desc";
      
      // Check if first parameter is an object (for backward compatibility)
      if (typeof pageParam === 'object') {
        const params = pageParam;
        page = params.page || 0;
        size = params.size || 10;
        status = params.status || null;
        priority = params.priority || null;
        sort = params.sort || null;
        direction = params.direction || "desc";
      }
      
      let url = `${INCIDENT_ENDPOINTS.BASE}?page=${page}&size=${size}`;
      
      if (status) {
        url += `&status=${status}`;
      }
      
      if (priority) {
        url += `&priority=${priority}`;
      }
      
      if (sort) {
        url += `&sortBy=${sort}&direction=${direction}`;
      }
      
      logApiRequest(url);
      const response = await api.get(url);
      return logApiResponse('getIncidents', response);
    } catch (error) {
      console.error('Error fetching incidents:', error);
      throw error;
    }
  },
  
  // Get a single incident by ID
  getIncidentById: async (id) => {
    if (MOCK_MODE) {
      console.log('MOCK MODE: getIncidentById', { id });
      const incident = MOCK_DATA.incidents.find(i => i.id === Number(id));
      if (!incident) {
        throw new Error('Incident not found');
      }
      return mockResponse(incident);
    }
    
    try {
      const url = INCIDENT_ENDPOINTS.BY_ID(id);
      logApiRequest(url);
      const response = await api.get(url);
      return logApiResponse('getIncidentById', response);
    } catch (error) {
      console.error(`Error fetching incident #${id}:`, error);
      throw error;
    }
  },
  
  // Get incident statistics
  getIncidentStats: async () => {
    if (MOCK_MODE) {
      console.log('MOCK MODE: getIncidentStats');
      return mockResponse(MOCK_DATA.stats);
    }
    
    try {
      logApiRequest(INCIDENT_ENDPOINTS.STATS);
      const response = await api.get(INCIDENT_ENDPOINTS.STATS);
      return logApiResponse('getIncidentStats', response);
    } catch (error) {
      console.error('Error fetching incident statistics:', error);
      throw error;
    }
  },
  
  // Get incidents assigned to current user
  getIncidentsAssignedToMe: async () => {
    if (MOCK_MODE) {
      console.log('MOCK MODE: getIncidentsAssignedToMe');
      // For mock, assume current user is user #2
      const assigned = MOCK_DATA.incidents.filter(i => i.assignedTo === 2);
      return mockResponse(assigned);
    }
    
    try {
      logApiRequest(INCIDENT_ENDPOINTS.ASSIGNED);
      const response = await api.get(INCIDENT_ENDPOINTS.ASSIGNED);
      return logApiResponse('getIncidentsAssignedToMe', response);
    } catch (error) {
      console.error('Error fetching assigned incidents:', error);
      throw error;
    }
  },
  
  // Get high priority unresolved incidents
  getHighPriorityUnresolved: async () => {
    if (MOCK_MODE) {
      console.log('MOCK MODE: getHighPriorityUnresolved');
      const highPriority = MOCK_DATA.incidents.filter(
        i => i.priority === 'HIGH' && i.status !== 'RESOLVED' && i.status !== 'CLOSED'
      );
      return mockResponse(highPriority);
    }
    
    try {
      logApiRequest(INCIDENT_ENDPOINTS.HIGH_PRIORITY);
      const response = await api.get(INCIDENT_ENDPOINTS.HIGH_PRIORITY);
      return logApiResponse('getHighPriorityUnresolved', response);
    } catch (error) {
      console.error('Error fetching high priority incidents:', error);
      throw error;
    }
  },
  
  // Create new incident
  createIncident: async (incidentData) => {
    if (MOCK_MODE) {
      console.log('MOCK MODE: createIncident', { incidentData });
      const newId = Math.max(...MOCK_DATA.incidents.map(i => i.id)) + 1;
      const newIncident = {
        id: newId,
        ...incidentData,
        createdAt: new Date().toISOString(),
        updatedAt: new Date().toISOString()
      };
      MOCK_DATA.incidents.push(newIncident);
      return mockResponse(newIncident);
    }
    
    try {
      logApiRequest(INCIDENT_ENDPOINTS.BASE, incidentData);
      const response = await api.post(INCIDENT_ENDPOINTS.BASE, incidentData);
      return logApiResponse('createIncident', response);
    } catch (error) {
      console.error('Error creating incident:', error);
      throw error;
    }
  },
  
  // Update incident
  updateIncident: async (id, incidentData) => {
    if (MOCK_MODE) {
      console.log('MOCK MODE: updateIncident', { id, incidentData });
      const index = MOCK_DATA.incidents.findIndex(i => i.id === Number(id));
      if (index === -1) {
        throw new Error('Incident not found');
      }
      
      const updatedIncident = {
        ...MOCK_DATA.incidents[index],
        ...incidentData,
        updatedAt: new Date().toISOString()
      };
      
      MOCK_DATA.incidents[index] = updatedIncident;
      return mockResponse(updatedIncident);
    }
    
    try {
      const url = INCIDENT_ENDPOINTS.BY_ID(id);
      logApiRequest(url, incidentData);
      const response = await api.put(url, incidentData);
      return logApiResponse('updateIncident', response);
    } catch (error) {
      console.error(`Error updating incident #${id}:`, error);
      throw error;
    }
  },
  
  // Delete an incident
  deleteIncident: async (id) => {
    if (MOCK_MODE) {
      console.log('MOCK MODE: deleteIncident', { id });
      const index = MOCK_DATA.incidents.findIndex(i => i.id === Number(id));
      if (index === -1) {
        throw new Error('Incident not found');
      }
      
      MOCK_DATA.incidents.splice(index, 1);
      return mockResponse({ message: 'Incident deleted successfully' });
    }
    
    try {
      const url = INCIDENT_ENDPOINTS.BY_ID(id);
      logApiRequest(url);
      const response = await api.delete(url);
      return logApiResponse('deleteIncident', response);
    } catch (error) {
      console.error(`Error deleting incident #${id}:`, error);
      throw error;
    }
  },
  
  // Get incidents created by current user
  getIncidentsCreatedByMe: async () => {
    if (MOCK_MODE) {
      console.log('MOCK MODE: getIncidentsCreatedByMe');
      // For mock, assume current user is user #2
      const created = MOCK_DATA.incidents.filter(i => i.createdBy === 2);
      return mockResponse(created);
    }
    
    try {
      logApiRequest(INCIDENT_ENDPOINTS.CREATED);
      const response = await api.get(INCIDENT_ENDPOINTS.CREATED);
      return logApiResponse('getIncidentsCreatedByMe', response);
    } catch (error) {
      console.error('Error fetching created incidents:', error);
      throw error;
    }
  },
  
  // Get incidents by status
  getIncidentsByStatus: async (status) => {
    if (MOCK_MODE) {
      console.log('MOCK MODE: getIncidentsByStatus', { status });
      const incidents = MOCK_DATA.incidents.filter(i => i.status === status);
      return mockResponse(incidents);
    }
    
    try {
      const url = INCIDENT_ENDPOINTS.STATUS(status);
      logApiRequest(url);
      const response = await api.get(url);
      return logApiResponse('getIncidentsByStatus', response);
    } catch (error) {
      console.error(`Error fetching incidents with status ${status}:`, error);
      throw error;
    }
  },
  
  // Get incidents by priority
  getIncidentsByPriority: async (priority) => {
    if (MOCK_MODE) {
      console.log('MOCK MODE: getIncidentsByPriority', { priority });
      const incidents = MOCK_DATA.incidents.filter(i => i.priority === priority);
      return mockResponse(incidents);
    }
    
    try {
      const url = INCIDENT_ENDPOINTS.PRIORITY(priority);
      logApiRequest(url);
      const response = await api.get(url);
      return logApiResponse('getIncidentsByPriority', response);
    } catch (error) {
      console.error(`Error fetching incidents with priority ${priority}:`, error);
      throw error;
    }
  },
  
  // Get incidents by tag
  getIncidentsByTag: async (tag) => {
    if (MOCK_MODE) {
      console.log('MOCK MODE: getIncidentsByTag', { tag });
      const incidents = MOCK_DATA.incidents.filter(i => i.tags.includes(tag));
      return mockResponse(incidents);
    }
    
    try {
      const url = INCIDENT_ENDPOINTS.TAG(tag);
      logApiRequest(url);
      const response = await api.get(url);
      return logApiResponse('getIncidentsByTag', response);
    } catch (error) {
      console.error(`Error fetching incidents with tag ${tag}:`, error);
      throw error;
    }
  },
  
  // Get incident history
  getIncidentHistory: async (id) => {
    if (MOCK_MODE) {
      console.log('MOCK MODE: getIncidentHistory', { id });
      // Generate mock history
      const incident = MOCK_DATA.incidents.find(i => i.id === Number(id));
      if (!incident) {
        throw new Error('Incident not found');
      }
      
      // Create some mock history entries
      const now = new Date();
      const history = [
        {
          id: 1,
          incidentId: incident.id,
          field: 'status',
          oldValue: 'OPEN',
          newValue: incident.status,
          modifiedBy: 2,
          modifiedAt: new Date(now.getTime() - 86400000).toISOString()
        },
        {
          id: 2,
          incidentId: incident.id,
          field: 'assignee',
          oldValue: null,
          newValue: incident.assignedTo?.toString() || null,
          modifiedBy: 1,
          modifiedAt: new Date(now.getTime() - 172800000).toISOString()
        }
      ];
      
      return mockResponse(history);
    }
    
    try {
      const url = INCIDENT_ENDPOINTS.HISTORY(id);
      logApiRequest(url);
      const response = await api.get(url);
      return logApiResponse('getIncidentHistory', response);
    } catch (error) {
      console.error(`Error fetching history for incident #${id}:`, error);
      throw error;
    }
  },
  
  // Search incidents
  searchIncidents: async (query) => {
    if (MOCK_MODE) {
      console.log('MOCK MODE: searchIncidents', { query });
      const lowercaseQuery = query.toLowerCase();
      const results = MOCK_DATA.incidents.filter(
        i => i.title.toLowerCase().includes(lowercaseQuery) || 
             i.description.toLowerCase().includes(lowercaseQuery) ||
             i.tags.some(tag => tag.toLowerCase().includes(lowercaseQuery))
      );
      return mockResponse(results);
    }
    
    try {
      const url = `${INCIDENT_ENDPOINTS.SEARCH}?keyword=${encodeURIComponent(query)}`;
      logApiRequest(url);
      const response = await api.get(url);
      return logApiResponse('searchIncidents', response);
    } catch (error) {
      console.error('Error searching incidents:', error);
      throw error;
    }
  }
};

export default incidentService; 