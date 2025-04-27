import { useState, useEffect } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import incidentService from '../../services/incidentService';

function CreateIncident() {
  const [formData, setFormData] = useState({
    title: '',
    description: '',
    priority: 'MEDIUM',
    tags: ''
  });
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [error, setError] = useState(null);
  const [submitTimeout, setSubmitTimeout] = useState(null);
  const navigate = useNavigate();
  
  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData({
      ...formData,
      [name]: value
    });
  };
  
  const handleSubmit = async (e) => {
    e.preventDefault();
    
    // Validation
    if (!formData.title.trim()) {
      setError('Title is required');
      return;
    }
    
    // Process tags
    const processedTags = formData.tags
      ? formData.tags.split(',').map(tag => tag.trim()).filter(tag => tag)
      : [];
    
    const incidentData = {
      ...formData,
      tags: processedTags
    };
    
    setIsSubmitting(true);
    setError(null);
    
    // Set a timeout to avoid stuck submitting state
    const timeout = setTimeout(() => {
      if (isSubmitting) {
        setError('The request is taking longer than expected. You can wait or try again.');
      }
    }, 10000);
    
    setSubmitTimeout(timeout);
    
    try {
      console.log('Submitting incident data:', incidentData);
      const response = await incidentService.createIncident(incidentData);
      console.log('Incident created successfully:', response);
      
      clearTimeout(timeout);
      
      if (response && response.id) {
        navigate(`/incidents/${response.id}`);
      } else {
        // Even if we don't have an ID, assume it was created and redirect to incidents list
        setError('Incident may have been created, but couldn\'t retrieve details. Redirecting to incidents list...');
        setTimeout(() => navigate('/incidents'), 2000);
      }
    } catch (err) {
      clearTimeout(timeout);
      console.error('Error creating incident:', err);
      
      // Better error message based on the error
      let errorMessage = 'Failed to create incident. Please try again.';
      
      if (err.response) {
        if (err.response.status === 400) {
          errorMessage = 'Invalid data provided. Please check the form fields.';
        } else if (err.response.status === 401 || err.response.status === 403) {
          errorMessage = 'Authentication error. You may need to log in again.';
        } else if (err.response.status === 500) {
          errorMessage = 'Server error. Please try again later.';
        }
        
        // Include any error message from the server
        if (err.response.data && err.response.data.message) {
          errorMessage += ` Server message: ${err.response.data.message}`;
        }
      } else if (err.message && err.message.includes('Network Error')) {
        errorMessage = 'Network error. Please check your connection and try again.';
      }
      
      setError(errorMessage);
      setIsSubmitting(false);
    }
  };
  
  // Clean up timeout when component unmounts
  useEffect(() => {
    return () => {
      if (submitTimeout) {
        clearTimeout(submitTimeout);
      }
    };
  }, [submitTimeout]);
  
  return (
    <div className="create-incident-container">
      <div className="page-header">
        <h1 className="page-title">Create New Incident</h1>
        <div className="header-actions">
          <Link to="/incidents" className="btn btn-text">
            Cancel
          </Link>
        </div>
      </div>
      
      {error && (
        <div className="error-message">
          {error}
        </div>
      )}
      
      <div className="card">
        <div className="card-body">
          <form onSubmit={handleSubmit}>
            <div className="form-group">
              <label htmlFor="title">Title *</label>
              <input
                type="text"
                id="title"
                name="title"
                value={formData.title}
                onChange={handleChange}
                placeholder="Enter incident title"
                required
                disabled={isSubmitting}
              />
              <small className="form-text">Provide a clear and descriptive title for the incident</small>
            </div>
            
            <div className="form-group">
              <label htmlFor="description">Description</label>
              <textarea
                id="description"
                name="description"
                value={formData.description}
                onChange={handleChange}
                placeholder="Describe the incident in detail"
                rows="5"
                disabled={isSubmitting}
              ></textarea>
              <small className="form-text">Include relevant details, steps to reproduce, and any other information that helps understand the incident</small>
            </div>
            
            <div className="form-group">
              <label htmlFor="priority">Priority</label>
              <select
                id="priority"
                name="priority"
                value={formData.priority}
                onChange={handleChange}
                disabled={isSubmitting}
              >
                <option value="HIGH">High</option>
                <option value="MEDIUM">Medium</option>
                <option value="LOW">Low</option>
              </select>
            </div>
            
            <div className="form-group">
              <label htmlFor="tags">Tags</label>
              <input
                type="text"
                id="tags"
                name="tags"
                value={formData.tags}
                onChange={handleChange}
                placeholder="Enter tags separated by commas"
                disabled={isSubmitting}
              />
              <small className="form-text">Optional. Add relevant tags separated by commas (e.g., server, network, database)</small>
            </div>
            
            <div className="form-actions">
              <button
                type="submit"
                className="btn btn-primary"
                disabled={isSubmitting}
              >
                {isSubmitting ? 'Creating...' : 'Create Incident'}
              </button>
              <Link to="/incidents" className="btn btn-text">
                Cancel
              </Link>
            </div>
          </form>
        </div>
      </div>
    </div>
  );
}

export default CreateIncident; 