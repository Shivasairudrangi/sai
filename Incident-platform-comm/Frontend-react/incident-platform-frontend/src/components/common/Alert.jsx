import React, { useState, useEffect } from 'react';

function Alert({ type = 'info', message, onClose, autoClose = true, autoCloseTime = 5000 }) {
  const [visible, setVisible] = useState(true);

  useEffect(() => {
    if (autoClose && message) {
      const timer = setTimeout(() => {
        setVisible(false);
        if (onClose) onClose();
      }, autoCloseTime);

      return () => clearTimeout(timer);
    }
  }, [autoClose, autoCloseTime, message, onClose]);

  if (!message || !visible) {
    return null;
  }

  const alertClass = {
    success: 'alert-success',
    error: 'alert-error',
    warning: 'alert-warning',
    info: 'alert-info'
  }[type];

  const handleClose = () => {
    setVisible(false);
    if (onClose) onClose();
  };

  return (
    <div className={`alert ${alertClass}`} role="alert">
      <div className="alert-content">{message}</div>
      <button
        type="button"
        className="alert-close"
        aria-label="Close"
        onClick={handleClose}
      >
        &times;
      </button>
    </div>
  );
}

export default Alert; 