import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import chatService from '../../services/chatService';
import { useAuth } from '../../utils/AuthContext';

function ChatRooms() {
  const [chatRooms, setChatRooms] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const { currentUser } = useAuth();

  useEffect(() => {
    const fetchChatRooms = async () => {
      try {
        setLoading(true);
        const rooms = await chatService.getRooms();
        setChatRooms(Array.isArray(rooms) ? rooms : []);
        setError(null);
      } catch (err) {
        console.error('Error fetching chat rooms:', err);
        setError('Failed to load chat rooms. Please try again later.');
        // Provide empty array as fallback
        setChatRooms([]);
      } finally {
        // Ensure loading state is cleared even if there's an error
        setLoading(false);
      }
    };

    fetchChatRooms();
    
    // Set a maximum loading time to prevent stuck loading states
    const timeoutId = setTimeout(() => {
      if (loading) {
        setLoading(false);
        if (chatRooms.length === 0) {
          setError('Loading chat rooms timed out. Please refresh the page to try again.');
        }
      }
    }, 5000);
    
    return () => clearTimeout(timeoutId);
  }, []);

  return (
    <div className="chat-rooms-container">
      <header className="page-header">
        <h1>Chat Rooms</h1>
        <Link to="/chats/new" className="btn btn-primary">
          Create New Chat Room
        </Link>
      </header>

      {loading ? (
        <div className="loading-indicator">Loading chat rooms...</div>
      ) : error ? (
        <div className="error-message">
          <p>{error}</p>
          <button onClick={() => window.location.reload()} className="btn btn-primary btn-sm mt-2">
            Retry
          </button>
        </div>
      ) : (
        <>
          {chatRooms.length === 0 ? (
            <div className="empty-state">
              <p>No chat rooms available. Create a new chat room to get started.</p>
              <Link to="/chats/new" className="btn btn-primary btn-sm">
                Create Chat Room
              </Link>
            </div>
          ) : (
            <div className="chat-rooms-grid">
              {chatRooms.map((room, index) => (
                <div key={room.id || index} className="chat-room-card">
                  <h3 className="chat-room-title">{room.name || 'Unnamed Room'}</h3>
                  <p className="chat-room-description">{room.description || 'No description'}</p>
                  <div className="chat-room-meta">
                    <span>{room.userCount || 0} participants</span>
                    <span>{room.unreadCount > 0 ? `${room.unreadCount} unread` : 'No unread messages'}</span>
                  </div>
                  <div className="chat-room-footer">
                    <span>Created {room.createdAt ? new Date(room.createdAt).toLocaleDateString() : 'recently'}</span>
                    <Link to={`/chats/${room.id}`} className="btn btn-secondary">
                      Join Chat
                    </Link>
                  </div>
                </div>
              ))}
            </div>
          )}
        </>
      )}
    </div>
  );
}

export default ChatRooms; 