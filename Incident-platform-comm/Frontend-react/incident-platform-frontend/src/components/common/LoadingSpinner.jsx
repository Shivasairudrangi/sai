import React from 'react';

function LoadingSpinner({ size = 'medium', text = 'Loading...' }) {
  const sizeClass = {
    small: 'spinner-sm',
    medium: 'spinner-md',
    large: 'spinner-lg'
  }[size];

  return (
    <div className={`loading-spinner-container ${sizeClass}`}>
      <div className="spinner-border" role="status">
        <span className="sr-only">{text}</span>
      </div>
      {text && <p className="spinner-text">{text}</p>}
    </div>
  );
}

export default LoadingSpinner; 