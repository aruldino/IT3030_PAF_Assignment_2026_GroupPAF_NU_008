import React, { useEffect, useState } from 'react';
import { useAuth } from '../context/AuthContext';
import { useNavigate } from 'react-router-dom';
import { authService } from '../services/authService';

/**
 * Shown to users who are waiting for admin approval.
 * Member 4 responsibility.
 */
function PendingApprovalPage() {
  const { user, login, logout } = useAuth();
  const navigate = useNavigate();
  const [checking, setChecking] = useState(false);

  // Auto check every 10 seconds if approved
  useEffect(() => {
    const interval = setInterval(async () => {
      try {
        const response = await authService.getCurrentUser();
        const updatedUser = response.data;
        if (updatedUser.status === 'ACTIVE') {
          // Update context with new status
          login({ ...updatedUser, token: user?.token });
          navigate('/');
        }
      } catch (err) {
        if (err.response?.status === 404 || err.response?.status === 403) {
        logout();
        navigate('/login');
      }
      }
    }, 10000); // check every 10 seconds

    return () => clearInterval(interval);
  }, []);

  async function handleCheckNow() {
    setChecking(true);
    try {
      const response = await authService.getCurrentUser();
      const updatedUser = response.data;
      if (updatedUser.status === 'ACTIVE') {
        login({ ...updatedUser, token: user?.token });
        navigate('/');
      } else {
        alert('Still pending. Please wait for admin approval.');
      }
    } catch (err) {
      alert('Error checking status. Please try again.');
    } finally {
      setChecking(false);
    }
  }

  function handleLogout() {
    logout();
    navigate('/login');
  }

  return (
    <div style={{
      minHeight: '100vh',
      backgroundColor: '#f8fafc',
      display: 'flex',
      flexDirection: 'column',
      alignItems: 'center',
      justifyContent: 'center',
      padding: '2rem'
    }}>
      {/* Main Card */}
      <div style={{
        backgroundColor: 'white',
        borderRadius: '16px',
        padding: '3rem',
        maxWidth: '480px',
        width: '100%',
        boxShadow: '0 4px 24px rgba(0,0,0,0.08)',
        textAlign: 'center'
      }}>
        {/* Icon */}
        <div style={{ fontSize: '4rem', marginBottom: '1rem' }}>⏳</div>

        <h2 style={{ color: '#1a73e8', marginBottom: '0.5rem', fontSize: '1.5rem' }}>
          Account Pending Approval
        </h2>

        <p style={{ color: '#666', lineHeight: '1.6', marginBottom: '1.5rem' }}>
          Your account has been created successfully. Please wait for an
          administrator to approve your access to Smart Campus Hub.
        </p>

        {/* User Info */}
        {user && (
          <div style={{
            backgroundColor: '#f0f4ff',
            borderRadius: '10px',
            padding: '1rem',
            marginBottom: '1.5rem',
            display: 'flex',
            alignItems: 'center',
            gap: '12px'
          }}>
            {user.profilePicture ? (
              <img
                src={user.profilePicture}
                alt="profile"
                style={{ width: '44px', height: '44px', borderRadius: '50%' }}
              />
            ) : (
              <div style={{
                width: '44px', height: '44px', borderRadius: '50%',
                backgroundColor: '#3182ce', display: 'flex',
                alignItems: 'center', justifyContent: 'center',
                color: 'white', fontWeight: '600', fontSize: '1.2rem'
              }}>
                {user.name?.charAt(0) || '?'}
              </div>
            )}
            <div style={{ textAlign: 'left' }}>
              <div style={{ fontWeight: '600', fontSize: '0.95rem' }}>{user.name}</div>
              <div style={{ color: '#888', fontSize: '0.85rem' }}>{user.email}</div>
            </div>
          </div>
        )}

        {/* Steps */}
        <div style={{
          backgroundColor: '#fffbeb',
          border: '1px solid #fcd34d',
          borderRadius: '10px',
          padding: '1rem',
          marginBottom: '1.5rem',
          textAlign: 'left'
        }}>
          <p style={{ fontWeight: '600', color: '#92400e', marginBottom: '0.5rem', fontSize: '0.9rem' }}>
            ⚠️ What happens next?
          </p>
          <ol style={{ color: '#78350f', fontSize: '0.85rem', paddingLeft: '1.2rem', margin: 0, lineHeight: '1.8' }}>
            <li>An administrator will review your account</li>
            <li>This page checks automatically every 10 seconds</li>
            <li>You'll be redirected automatically once approved</li>
          </ol>
        </div>

        {/* Auto checking indicator */}
        <div style={{
          fontSize: '0.8rem',
          color: '#888',
          marginBottom: '1rem'
        }}>
          🔄 Checking approval status automatically...
        </div>

        {/* Check Now Button */}
        <button
          onClick={handleCheckNow}
          disabled={checking}
          style={{
            width: '100%',
            padding: '12px',
            backgroundColor: '#1a73e8',
            color: 'white',
            border: 'none',
            borderRadius: '8px',
            cursor: checking ? 'not-allowed' : 'pointer',
            fontWeight: '600',
            fontSize: '0.95rem',
            marginBottom: '0.75rem',
            opacity: checking ? 0.7 : 1
          }}
        >
          {checking ? 'Checking...' : '🔍 Check Now'}
        </button>

        <button
          onClick={handleLogout}
          style={{
            width: '100%',
            padding: '12px',
            backgroundColor: '#f1f3f4',
            color: '#333',
            border: 'none',
            borderRadius: '8px',
            cursor: 'pointer',
            fontWeight: '600',
            fontSize: '0.95rem'
          }}
        >
          Sign Out
        </button>
      </div>

      {/* Blurred dashboard preview */}
      <div style={{
        marginTop: '2rem',
        width: '100%',
        maxWidth: '800px',
        position: 'relative'
      }}>
        <div style={{
          filter: 'blur(6px)',
          opacity: 0.4,
          pointerEvents: 'none',
          backgroundColor: 'white',
          borderRadius: '12px',
          padding: '1.5rem',
          boxShadow: '0 2px 8px rgba(0,0,0,0.08)'
        }}>
          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(4, 1fr)', gap: '1rem' }}>
            {['🏛️ Resources', '📅 Bookings', '🔧 Tickets', '🔔 Notifications'].map(item => (
              <div key={item} style={{
                backgroundColor: '#f8fafc',
                borderRadius: '8px',
                padding: '1.5rem',
                textAlign: 'center',
                fontSize: '0.9rem',
                color: '#666'
              }}>
                {item}
              </div>
            ))}
          </div>
        </div>

        {/* Lock overlay */}
        <div style={{
          position: 'absolute',
          top: '50%',
          left: '50%',
          transform: 'translate(-50%, -50%)',
          fontSize: '2rem'
        }}>
          🔒
        </div>
      </div>
    </div>
  );
}

export default PendingApprovalPage;