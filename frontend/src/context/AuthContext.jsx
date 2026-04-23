import React, { createContext, useContext, useState, useEffect } from 'react';
import api from '../services/api';

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    async function initAuth() {
      try {
        const savedUser = localStorage.getItem('user');
        const token = localStorage.getItem('token');

        if (savedUser && token) {
          try {
            const response = await api.get('/auth/me');
            const freshUser = response.data;
            const updatedUser = { ...JSON.parse(savedUser), ...freshUser, token };
            setUser(updatedUser);
            localStorage.setItem('user', JSON.stringify(updatedUser));

            // Redirect based on status on page load — only if not already there
            const currentPath = window.location.pathname;
            if (freshUser.status === 'SUSPENDED' && currentPath !== '/access-denied') {
              window.location.href = '/access-denied';
              return;
            }
            if (freshUser.status === 'PENDING' && currentPath !== '/pending-approval') {
              window.location.href = '/pending-approval';
              return;
            }
          } catch (err) {
            if (err.response?.status === 401) {
              setUser(null);
              localStorage.removeItem('user');
              localStorage.removeItem('token');
            } else {
              setUser(JSON.parse(savedUser));
            }
          }
        }
      } catch (err) {
        console.error('Failed to init auth', err);
      } finally {
        setLoading(false);
      }
    }

    initAuth();
  }, []);

  // Background check every 10 seconds
  useEffect(() => {
    if (!user) return;

    const interval = setInterval(async () => {
      try {
        const response = await api.get('/auth/me');
        const updatedUser = response.data;

        const currentPath = window.location.pathname;

        // Redirect based on status — only if not already on correct page
        if (updatedUser.status === 'SUSPENDED' && currentPath !== '/access-denied') {
          window.location.href = '/access-denied';
          return;
        }
        if (updatedUser.status === 'PENDING' && currentPath !== '/pending-approval') {
          window.location.href = '/pending-approval';
          return;
        }

        // Update context if anything changed
        if (updatedUser.status !== user.status) {
          setUser(prev => ({ ...prev, ...updatedUser }));
          localStorage.setItem('user', JSON.stringify({ ...user, ...updatedUser }));
        }
      } catch (err) {
        if (err.response?.status === 401) {
          setUser(null);
          localStorage.removeItem('user');
          localStorage.removeItem('token');
          window.location.href = '/login';
        }
      }
    }, 10000);

    return () => clearInterval(interval);
  }, [user]);

  function login(userData) {
    setUser(userData);
    localStorage.setItem('user', JSON.stringify(userData));
    if (userData.token) {
      localStorage.setItem('token', userData.token);
    }
  }

  function logout() {
    setUser(null);
    localStorage.removeItem('user');
    localStorage.removeItem('token');
  }

  function isAdmin() {
    return user?.role === 'ADMIN';
  }

  const value = { user, loading, login, logout, isAdmin };

  return (
    <AuthContext.Provider value={value}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
}

export default AuthContext;