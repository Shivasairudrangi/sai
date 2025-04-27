import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import incidentService from '../../services/incidentService';

function Incidents() {
  const [incidents, setIncidents] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [filter, setFilter] = useState({
    status: '',
    priority: '',
    assignee: ''
  });

  useEffect(() => {
    fetchIncidents();
  }, [filter]);

  const fetchIncidents = async () => {
    try {
      setLoading(true);
      const response = await incidentService.getIncidents(filter);
      setIncidents(response);
      setError(null);
    } catch (err) {
      console.error('Error fetching incidents:', err);
      setError('Failed to load incidents. Please try again later.');
    } finally {
      setLoading(false);
    }
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

  const formatDate = (dateString) => {
    if (!dateString) return 'N/A';
    const date = new Date(dateString);
    return date.toLocaleString();
  };

  const handleFilterChange = (e) => {
    const { name, value } = e.target;
    setFilter(prev => ({ ...prev, [name]: value }));
  };

  return (
    <div className="incidents-container">
      <div className="page-header">
        <h1 className="page-title">Incidents</h1>
        <div className="header-actions">
          <Link to="/incidents/new" className="btn btn-primary">
            <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
              <circle cx="12" cy="12" r="10"></circle>
              <line x1="12" y1="8" x2="12" y2="16"></line>
              <line x1="8" y1="12" x2="16" y2="12"></line>
            </svg>
            Create Incident
          </Link>
        </div>
      </div>

      <div className="filter-section">
        <div className="filter-group">
          <label htmlFor="status">Status</label>
          <select 
            id="status" 
            name="status" 
            value={filter.status} 
            onChange={handleFilterChange}
          >
            <option value="">All Statuses</option>
            <option value="OPEN">Open</option>
            <option value="IN_PROGRESS">In Progress</option>
            <option value="RESOLVED">Resolved</option>
            <option value="CLOSED">Closed</option>
          </select>
        </div>
        
        <div className="filter-group">
          <label htmlFor="priority">Priority</label>
          <select 
            id="priority" 
            name="priority" 
            value={filter.priority} 
            onChange={handleFilterChange}
          >
            <option value="">All Priorities</option>
            <option value="HIGH">High</option>
            <option value="MEDIUM">Medium</option>
            <option value="LOW">Low</option>
          </select>
        </div>
      </div>

      {error && (
        <div className="error-message">
          {error}
        </div>
      )}

      {loading ? (
        <div className="loading-container">
          <div className="loading-spinner"></div>
          <p>Loading incidents...</p>
        </div>
      ) : (
        <div className="incidents-list">
          {incidents.length === 0 ? (
            <div className="empty-state">
              <p>No incidents found.</p>
            </div>
          ) : (
            <table className="incidents-table">
              <thead>
                <tr>
                  <th>ID</th>
                  <th>Title</th>
                  <th>Status</th>
                  <th>Priority</th>
                  <th>Assignee</th>
                  <th>Created</th>
                  <th>Updated</th>
                  <th>Actions</th>
                </tr>
              </thead>
              <tbody>
                {incidents.map(incident => (
                  <tr key={incident.id}>
                    <td>{incident.id}</td>
                    <td>
                      <Link to={`/incidents/${incident.id}`} className="incident-title">
                        {incident.title}
                      </Link>
                    </td>
                    <td>
                      <span className={`status-badge ${getStatusClass(incident.status)}`}>
                        {incident.status?.replace('_', ' ')}
                      </span>
                    </td>
                    <td>
                      <span className={`priority-badge ${getPriorityClass(incident.priority)}`}>
                        {incident.priority}
                      </span>
                    </td>
                    <td>{incident.assignee?.username || 'Unassigned'}</td>
                    <td>{formatDate(incident.createdAt)}</td>
                    <td>{formatDate(incident.updatedAt)}</td>
                    <td>
                      <div className="action-buttons">
                        <Link to={`/incidents/${incident.id}`} className="btn btn-icon" title="View">
                          <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                            <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"></path>
                            <circle cx="12" cy="12" r="3"></circle>
                          </svg>
                        </Link>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </div>
      )}
    </div>
  );
}

export default Incidents; 