import SockJS from 'sockjs-client';
import Stomp from 'stompjs';
import { API_BASE_URL } from '../utils/constants';

class WebSocketService {
  constructor(endpoint) {
    this.endpoint = endpoint;
    this.stompClient = null;
    this.connected = false;
    this.subscriptions = new Map();
    this.reconnectAttempts = 0;
    this.maxReconnectAttempts = 5;
    this.reconnectDelay = 2000;
    this.connectCallbacks = [];
    this.errorCallbacks = [];
    this.disconnectCallbacks = [];
  }

  connect(clientId, onConnected = () => {}) {
    if (this.connected) {
      console.log(`Already connected to ${this.endpoint} websocket`);
      onConnected();
      return;
    }

    // Store callback to execute after successful connection
    this.connectCallbacks.push(onConnected);

    // Create a unique subscription ID
    const connectionId = `${clientId}-${Date.now()}`;

    // Initialize SockJS connection to the endpoint
    const socket = new SockJS(`${API_BASE_URL}/${this.endpoint}`);
    this.stompClient = Stomp.over(socket);
    
    // Disable debug logs in production
    if (process.env.NODE_ENV === 'production') {
      this.stompClient.debug = null;
    }

    this.stompClient.connect(
      {
        connectionId,
        clientId
      },
      this.onConnected.bind(this),
      this.onError.bind(this)
    );

    // Set up automatic reconnection on window focus if disconnected
    window.addEventListener('focus', this.handleWindowFocus.bind(this));
  }

  onConnected() {
    this.connected = true;
    this.reconnectAttempts = 0;
    console.log(`Connected to ${this.endpoint} websocket`);
    
    // Execute all connect callbacks
    this.connectCallbacks.forEach(callback => callback());
    this.connectCallbacks = [];
    
    // Resubscribe to previous topics if any
    this.resubscribe();
  }

  resubscribe() {
    if (!this.connected || !this.stompClient) return;
    
    // Resubscribe to all previous subscriptions
    this.subscriptions.forEach((callback, destination) => {
      console.log(`Resubscribing to ${destination}`);
      this.subscribe(destination, callback);
    });
  }

  onError(error) {
    console.error(`Error connecting to ${this.endpoint} websocket:`, error);
    this.connected = false;
    
    // Execute all error callbacks
    this.errorCallbacks.forEach(callback => callback(error));
    
    // Attempt to reconnect
    this.attemptReconnect();
  }

  attemptReconnect() {
    if (this.reconnectAttempts >= this.maxReconnectAttempts) {
      console.error(`Max reconnect attempts reached for ${this.endpoint} websocket`);
      return;
    }
    
    this.reconnectAttempts++;
    const delay = this.reconnectDelay * this.reconnectAttempts;
    
    console.log(`Attempting to reconnect to ${this.endpoint} websocket in ${delay}ms...`);
    
    setTimeout(() => {
      if (!this.connected) {
        this.connect(`reconnect-${Date.now()}`);
      }
    }, delay);
  }

  handleWindowFocus() {
    if (!this.connected && this.stompClient) {
      console.log('Window focused, checking connection...');
      this.connect(`window-focus-${Date.now()}`);
    }
  }

  subscribe(destination, callback) {
    if (!this.connected) {
      console.warn(`Not connected to ${this.endpoint} websocket. Queueing subscription to ${destination}`);
      // Store subscription for later when connection is established
      this.subscriptions.set(destination, callback);
      return null;
    }
    
    console.log(`Subscribing to ${destination}`);
    
    // Subscribe to the destination
    const subscription = this.stompClient.subscribe(destination, message => {
      try {
        const payload = JSON.parse(message.body);
        callback(payload);
      } catch (error) {
        console.error(`Error parsing message from ${destination}:`, error);
        callback(message.body);
      }
    });
    
    // Store subscription for reconnection
    this.subscriptions.set(destination, callback);
    
    return subscription;
  }

  unsubscribe(destination) {
    if (!this.stompClient || !this.connected) {
      console.warn(`Not connected to ${this.endpoint} websocket. Cannot unsubscribe from ${destination}`);
      return;
    }
    
    console.log(`Unsubscribing from ${destination}`);
    this.subscriptions.delete(destination);
    
    // Find the subscription object with this destination
    const subscriptionId = Object.keys(this.stompClient.subscriptions).find(
      id => this.stompClient.subscriptions[id].destination === destination
    );
    
    if (subscriptionId) {
      this.stompClient.unsubscribe(subscriptionId);
    }
  }

  send(destination, message) {
    if (!this.stompClient || !this.connected) {
      console.error(`Not connected to ${this.endpoint} websocket. Cannot send message to ${destination}`);
      return false;
    }
    
    console.log(`Sending message to ${destination}:`, message);
    this.stompClient.send(
      destination,
      {},
      typeof message === 'string' ? message : JSON.stringify(message)
    );
    
    return true;
  }

  disconnect() {
    if (this.stompClient) {
      console.log(`Disconnecting from ${this.endpoint} websocket`);
      
      // Execute all disconnect callbacks
      this.disconnectCallbacks.forEach(callback => callback());
      
      this.stompClient.disconnect(() => {
        this.connected = false;
        console.log(`Disconnected from ${this.endpoint} websocket`);
      });
    }
    
    window.removeEventListener('focus', this.handleWindowFocus);
    this.connected = false;
    this.stompClient = null;
  }

  onDisconnect(callback) {
    this.disconnectCallbacks.push(callback);
  }

  onConnect(callback) {
    if (this.connected) {
      callback();
    } else {
      this.connectCallbacks.push(callback);
    }
  }

  onError(callback) {
    this.errorCallbacks.push(callback);
  }
}

// Create instances for different WebSocket endpoints
export const incidentWebSocketService = new WebSocketService('ws-incident');
export const chatWebSocketService = new WebSocketService('ws-chat');
export const notificationWebSocketService = new WebSocketService('ws-notification');

export default {
  incidentWebSocketService,
  chatWebSocketService,
  notificationWebSocketService
}; 