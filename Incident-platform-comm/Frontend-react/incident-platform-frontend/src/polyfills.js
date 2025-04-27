// SockJS polyfills
window.global = window;

// Also add these common Node.js globals that might be needed
window.process = window.process || { env: {} };

// Don't use require as it's not available in ESM
window.Buffer = window.Buffer || {}; 