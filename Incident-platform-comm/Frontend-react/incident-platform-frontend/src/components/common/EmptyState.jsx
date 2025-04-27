import React from 'react';

function EmptyState({ 
  title = 'No data found', 
  message = 'There are no items to display.', 
  icon = '📭',
  actionText,
  onAction
}) {
  return (
    <div className="empty-state">
      <div className="empty-state-icon">{icon}</div>
      <h3 className="empty-state-title">{title}</h3>
      <p className="empty-state-message">{message}</p>
      {actionText && onAction && (
        <button 
          className="btn btn-primary empty-state-action" 
          onClick={onAction}
        >
          {actionText}
        </button>
      )}
    </div>
  );
}

export default EmptyState; 