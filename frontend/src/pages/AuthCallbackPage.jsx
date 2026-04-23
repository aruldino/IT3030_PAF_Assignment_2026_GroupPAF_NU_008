import React, { useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { authService } from '../services/authService';

/**
 * Handles OAuth2 callback — Member 4 responsibility.
 */
function AuthCallbackPage() {
  const navigate = useNavigate();
  const { login } = useAuth();

  useEffect(() => {
    async function handleCallback() {
      try {
        const params = new URLSearchParams(window.location.search);
        const token = params.get('token');

        if (!token) {
          navigate('/login');
          return;
        }

        // Save token first
        localStorage.setItem('token', token);

        // Fetch user profile — get REAL status from DB
        const response = await authService.getCurrentUser();
        const user = response.data;

        // Save to auth context
        login({ ...user, token });

        // Redirect based on ACTUAL status from DB
        if (user.status === 'PENDING') {
          navigate('/pending-approval');
        } else if (user.status === 'SUSPENDED') {
          navigate('/access-denied');
        } else {
          navigate('/');
        }
      } catch (error) {
        console.error('Auth callback failed:', error);
        navigate('/login');
      }
    }

    handleCallback();
  }, []);

  return (
    <div style={{
      display: 'flex',
      justifyContent: 'center',
      alignItems: 'center',
      height: '100vh',
      fontSize: '1.2rem',
      color: '#555'
    }}>
      Signing you in...
    </div>
  );
}

export default AuthCallbackPage;