import api from './api';

/**
 * Auth service — Member 4 responsibility.
 * Calls /api/auth endpoints.
 */

export const authService = {
  getCurrentUser: () => api.get('/auth/me'),
  logout: () => api.post('/auth/logout'),
};
