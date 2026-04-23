import React from 'react';

/**
 * LoadingSpinner — displayed while async data is being fetched.
 * Used by ProtectedRoute and any page with async data loading.
 */
function LoadingSpinner() {
  return (
    <div className="spinner-container">
      <div className="spinner" role="status" aria-label="Loading..." />
    </div>
  );
}

export default LoadingSpinner;
