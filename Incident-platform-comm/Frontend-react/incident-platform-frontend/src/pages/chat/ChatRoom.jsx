import { useState, useEffect, useRef } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import chatService from '../../services/chatService';
import { chatWebSocketService } from '../../services/websocketService';
import { useAuth } from '../../utils/AuthContext';

function ChatRoom() {
  const { id } = useParams();
  const navigate = useNavigate();
  const [roomDetails, setRoomDetails] = useState(null);
  const [messages, setMessages] = useState([]);
  const [messageText, setMessageText] = useState('');
  const [participants, setParticipants] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const messagesEndRef = useRef(null);
  const { currentUser } = useAuth();

  // Connect to WebSocket when component mounts
  useEffect(() => {
    chatWebSocketService.connect('chat', () => {
      // Subscribe to room messages
      chatWebSocketService.subscribe(`/topic/rooms/${id}/messages`, (message) => {
        setMessages(prevMessages => [...prevMessages, message]);
      });
      
      // Subscribe to user presence
      chatWebSocketService.subscribe(`/topic/rooms/${id}/users`, (update) => {
        if (update.type === 'JOIN' || update.type === 'LEAVE') {
          // Update participants list
          fetchParticipants();
        }
      });
    });

    return () => {
      // Unsubscribe and disconnect when component unmounts
      chatWebSocketService.unsubscribe(`/topic/rooms/${id}/messages`);
      chatWebSocketService.unsubscribe(`/topic/rooms/${id}/users`);
    };
  }, [id]);

  // Fetch room details and messages
  useEffect(() => {
    const fetchRoomData = async () => {
      try {
        setLoading(true);
        // Get room details
        const roomData = await chatService.getRoomById(id);
        setRoomDetails(roomData);
        
        // Get messages history
        const messagesData = await chatService.getMessages(id);
        setMessages(messagesData);
        
        // Fetch participants
        await fetchParticipants();
      } catch (err) {
        console.error('Error fetching chat room data:', err);
        setError('Failed to load chat room. It may not exist or you may not have access.');
      } finally {
        setLoading(false);
      }
    };

    fetchRoomData();
  }, [id]);

  // Scroll to bottom when new messages arrive
  useEffect(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [messages]);

  const fetchParticipants = async () => {
    try {
      const usersData = await chatService.getRoomUsers(id);
      setParticipants(usersData);
    } catch (err) {
      console.error('Error fetching participants:', err);
    }
  };

  const sendMessage = async (e) => {
    e.preventDefault();
    
    if (!messageText.trim()) return;
    
    try {
      const messageData = {
        roomId: id,
        content: messageText,
        senderId: currentUser.id
      };
      
      // Send via REST API
      await chatService.sendMessage(messageData);
      
      // Clear input
      setMessageText('');
    } catch (err) {
      console.error('Error sending message:', err);
      alert('Failed to send message. Please try again.');
    }
  };

  if (loading) {
    return <div className="loading-indicator">Loading chat room...</div>;
  }

  if (error) {
    return (
      <div className="error-container">
        <p>{error}</p>
        <button className="btn btn-primary" onClick={() => navigate('/chats')}>
          Back to Chat Rooms
        </button>
      </div>
    );
  }

  return (
    <div className="chat-room-container">
      <div className="chat-room-header">
        <h1>{roomDetails?.name}</h1>
        <p>{roomDetails?.description}</p>
      </div>

      <div className="chat-layout">
        <div className="chat-messages">
          {messages.length === 0 ? (
            <div className="empty-chat">
              <p>No messages yet. Be the first to send a message!</p>
            </div>
          ) : (
            <>
              {messages.map((message) => (
                <div
                  key={message.id}
                  className={`message ${message.senderId === currentUser.id ? 'message-own' : ''}`}
                >
                  <div className="message-header">
                    <span className="message-sender">
                      {message.senderName || 'Unknown user'}
                    </span>
                    <span className="message-time">
                      {new Date(message.timestamp).toLocaleTimeString()}
                    </span>
                  </div>
                  <div className="message-content">{message.content}</div>
                </div>
              ))}
              <div ref={messagesEndRef} />
            </>
          )}
        </div>

        <div className="chat-sidebar">
          <h3>Participants ({participants.length})</h3>
          <ul className="participants-list">
            {participants.map((user) => (
              <li key={user.id} className="participant">
                <span className={`status-indicator ${user.online ? 'online' : 'offline'}`}></span>
                <span className="participant-name">{user.name}</span>
              </li>
            ))}
          </ul>
        </div>
      </div>

      <div className="chat-input-container">
        <form onSubmit={sendMessage}>
          <input
            type="text"
            placeholder="Type your message..."
            value={messageText}
            onChange={(e) => setMessageText(e.target.value)}
            className="chat-input"
          />
          <button type="submit" className="btn btn-primary send-button">
            Send
          </button>
        </form>
      </div>
    </div>
  );
}

export default ChatRoom; 