import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import chatService from '../../services/chatService';
import { useAuth } from '../../utils/AuthContext';

function CreateChatRoom() {
  const [roomName, setRoomName] = useState('');
  const [description, setDescription] = useState('');
  const [isIncidentRelated, setIsIncidentRelated] = useState(false);
  const [incidentId, setIncidentId] = useState('');
  const [isPrivate, setIsPrivate] = useState(false);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  
  const navigate = useNavigate();
  const { currentUser } = useAuth();

  const handleSubmit = async (e) => {
    e.preventDefault();
    
    // Basic validation
    if (!roomName.trim()) {
      setError('Room name is required');
      return;
    }
    
    if (isIncidentRelated && !incidentId) {
      setError('Please select an incident');
      return;
    }
    
    try {
      setLoading(true);
      setError(null);
      
      const roomData = {
        name: roomName,
        description,
        private: isPrivate,
        createdBy: currentUser.id
      };
      
      // Add incident ID if incident-related
      if (isIncidentRelated && incidentId) {
        roomData.incidentId = incidentId;
      }
      
      const newRoom = await chatService.createRoom(roomData);
      
      // Navigate to the newly created chat room
      navigate(`/chats/${newRoom.id}`);
    } catch (err) {
      console.error('Error creating chat room:', err);
      setError('Failed to create chat room. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="create-chat-container">
      <header className="page-header">
        <h1>Create New Chat Room</h1>
      </header>
      
      {error && (
        <div className="error-message">
          {error}
        </div>
      )}
      
      <form className="create-chat-form" onSubmit={handleSubmit}>
        <div className="form-group">
          <label htmlFor="roomName">Room Name*</label>
          <input
            id="roomName"
            type="text"
            value={roomName}
            onChange={(e) => setRoomName(e.target.value)}
            placeholder="Enter room name"
            required
            disabled={loading}
          />
        </div>
        
        <div className="form-group">
          <label htmlFor="description">Description</label>
          <textarea
            id="description"
            value={description}
            onChange={(e) => setDescription(e.target.value)}
            placeholder="Describe the purpose of this chat room"
            rows={3}
            disabled={loading}
          />
        </div>
        
        <div className="form-group checkbox-group">
          <input
            id="isIncidentRelated"
            type="checkbox"
            checked={isIncidentRelated}
            onChange={(e) => {
              setIsIncidentRelated(e.target.checked);
              if (!e.target.checked) {
                setIncidentId('');
              }
            }}
            disabled={loading}
          />
          <label htmlFor="isIncidentRelated">Associate with an Incident</label>
        </div>
        
        {isIncidentRelated && (
          <div className="form-group">
            <label htmlFor="incidentId">Select Incident</label>
            <select
              id="incidentId"
              value={incidentId}
              onChange={(e) => setIncidentId(e.target.value)}
              disabled={loading}
              required
            >
              <option value="">Select an incident</option>
              {/* This would be populated with real incident data */}
              <option value="1">Incident #1 - Server Outage</option>
              <option value="2">Incident #2 - Network Failure</option>
              <option value="3">Incident #3 - Security Breach</option>
            </select>
          </div>
        )}
        
        <div className="form-group checkbox-group">
          <input
            id="isPrivate"
            type="checkbox"
            checked={isPrivate}
            onChange={(e) => setIsPrivate(e.target.checked)}
            disabled={loading}
          />
          <label htmlFor="isPrivate">Private Room (invite only)</label>
        </div>
        
        <div className="form-actions">
          <button
            type="button"
            className="btn btn-secondary"
            onClick={() => navigate('/chats')}
            disabled={loading}
          >
            Cancel
          </button>
          <button
            type="submit"
            className="btn btn-primary"
            disabled={loading}
          >
            {loading ? 'Creating...' : 'Create Chat Room'}
          </button>
        </div>
      </form>
    </div>
  );
}

export default CreateChatRoom; 