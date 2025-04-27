import { useState, useEffect } from 'react';
import { useParams, Link } from 'react-router-dom';
import incidentService from '../../services/incidentService';

function IncidentDetail() {
  const { id } = useParams();
  const [incident, setIncident] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [history, setHistory] = useState([]);

  useEffect(() => {
    fetchIncidentData();
  }, [id]);

  const fetchIncidentData = async () => {
    try {
      setLoading(true);
      const incidentData = await incidentService.getIncidentById(id);
      setIncident(incidentData);
      
      // Get incident history
      const historyData = await incidentService.getIncidentHistory(id);
      setHistory(historyData);
      
      setError(null);
    } catch (err) {
      console.error('Error fetching incident details:', err);
      setError('Failed to load incident details. Please try again later.');
    } finally {
      setLoading(false);
    }
  };

  const updateStatus = async (status) => {
    try {
      await incidentService.updateStatus(id, status);
      fetchIncidentData();
    } catch (err) {
      console.error('Error updating status:', err);
      setError('Failed to update incident status.');
    }
  };

  const updatePriority = async (priority) => {
    try {
      await incidentService.updatePriority(id, priority);
      fetchIncidentData();
    } catch (err) {
      console.error('Error updating priority:', err);
      setError('Failed to update incident priority.');
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

  if (loading) {
    return (
      <div className="loading-container">
        <div className="loading-spinner"></div>
        <p>Loading incident details...</p>
      </div>
    );
  }

  if (error) {
    return (
      <div className="error-container">
        <h2>Error</h2>
        <p>{error}</p>
        <Link to="/incidents" className="btn btn-primary">
          Back to Incidents
        </Link>
      </div>
    );
  }

  if (!incident) {
    return (
      <div className="not-found-container">
        <h2>Incident Not Found</h2>
        <p>The incident you are looking for does not exist or has been removed.</p>
        <Link to="/incidents" className="btn btn-primary">
          Back to Incidents
        </Link>
      </div>
    );
  }

  return (
    <div className="incident-detail-container">
      <div className="page-header">
        <div className="title-section">
          <h1 className="page-title">{incident.title}</h1>
          <div className="incident-meta">
            <span className={`status-badge ${getStatusClass(incident.status)}`}>
              {incident.status?.replace('_', ' ')}
            </span>
            <span className={`priority-badge ${getPriorityClass(incident.priority)}`}>
              {incident.priority}
            </span>
            <span className="incident-id">ID: {incident.id}</span>
          </div>
        </div>
        <div className="header-actions">
          <Link to="/incidents" className="btn btn-text">
            Back to Incidents
          </Link>
        </div>
      </div>

      <div className="incident-details">
        <div className="incident-main">
          <div className="card">
            <div className="card-header">
              <h2>Description</h2>
            </div>
            <div className="card-body">
              <p>{incident.description || 'No description provided.'}</p>
            </div>
          </div>

          <div className="card mt-4">
            <div className="card-header">
              <h2>Activity History</h2>
            </div>
            <div className="card-body">
              {history.length === 0 ? (
                <p>No activity recorded yet.</p>
              ) : (
                <ul className="history-list">
                  {history.map((item, index) => (
                    <li key={index} className="history-item">
                      <div className="history-icon">
                        <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                          <circle cx="12" cy="12" r="10"></circle>
                          <polyline points="12 6 12 12 16 14"></polyline>
                        </svg>
                      </div>
                      <div className="history-content">
                        <p>{item.action}</p>
                        <div className="history-meta">
                          <span>By {item.user?.username || 'System'}</span>
                          <span>{formatDate(item.timestamp)}</span>
                        </div>
                      </div>
                    </li>
                  ))}
                </ul>
              )}
            </div>
          </div>
        </div>

        <div className="incident-sidebar">
          <div className="card">
            <div className="card-header">
              <h3>Details</h3>
            </div>
            <div className="card-body">
              <div className="detail-item">
                <span className="detail-label">Assignee</span>
                <span className="detail-value">{incident.assignee?.username || 'Unassigned'}</span>
              </div>
              <div className="detail-item">
                <span className="detail-label">Created</span>
                <span className="detail-value">{formatDate(incident.createdAt)}</span>
              </div>
              <div className="detail-item">
                <span className="detail-label">Updated</span>
                <span className="detail-value">{formatDate(incident.updatedAt)}</span>
              </div>
              <div className="detail-item">
                <span className="detail-label">Tags</span>
                <div className="detail-value">
                  {incident.tags && incident.tags.length > 0 ? (
                    <div className="tags-container">
                      {incident.tags.map((tag, index) => (
                        <span key={index} className="tag">{tag}</span>
                      ))}
                    </div>
                  ) : (
                    <span>No tags</span>
                  )}
                </div>
              </div>
            </div>
          </div>

          <div className="card mt-3">
            <div className="card-header">
              <h3>Actions</h3>
            </div>
            <div className="card-body">
              <div className="action-group">
                <label>Change Status</label>
                <div className="button-group">
                  <button 
                    className={`btn ${incident.status === 'OPEN' ? 'btn-primary' : 'btn-outline'}`}
                    onClick={() => updateStatus('OPEN')}
                  >
                    Open
                  </button>
                  <button 
                    className={`btn ${incident.status === 'IN_PROGRESS' ? 'btn-primary' : 'btn-outline'}`}
                    onClick={() => updateStatus('IN_PROGRESS')}
                  >
                    In Progress
                  </button>
                  <button 
                    className={`btn ${incident.status === 'RESOLVED' ? 'btn-primary' : 'btn-outline'}`}
                    onClick={() => updateStatus('RESOLVED')}
                  >
                    Resolved
                  </button>
                  <button 
                    className={`btn ${incident.status === 'CLOSED' ? 'btn-primary' : 'btn-outline'}`}
                    onClick={() => updateStatus('CLOSED')}
                  >
                    Closed
                  </button>
                </div>
              </div>

              <div className="action-group mt-3">
                <label>Change Priority</label>
                <div className="button-group">
                  <button 
                    className={`btn ${incident.priority === 'HIGH' ? 'btn-danger' : 'btn-outline'}`}
                    onClick={() => updatePriority('HIGH')}
                  >
                    High
                  </button>
                  <button 
                    className={`btn ${incident.priority === 'MEDIUM' ? 'btn-warning' : 'btn-outline'}`}
                    onClick={() => updatePriority('MEDIUM')}
                  >
                    Medium
                  </button>
                  <button 
                    className={`btn ${incident.priority === 'LOW' ? 'btn-success' : 'btn-outline'}`}
                    onClick={() => updatePriority('LOW')}
                  >
                    Low
                  </button>
                </div>
              </div>
            </div>
          </div>

          <div className="card mt-3">
            <div className="card-header">
              <h3>Related</h3>
            </div>
            <div className="card-body">
              <div className="action-buttons">
                <Link to={`/chats?incidentId=${incident.id}`} className="btn btn-block">
                  View Chat Rooms
                </Link>
                <Link to={`/files?incidentId=${incident.id}`} className="btn btn-block mt-2">
                  View Files
                </Link>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}

export default IncidentDetail; 