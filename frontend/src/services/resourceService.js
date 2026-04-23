import api from './api';

/**
 * Resource service — Member 1 responsibility.
 * Calls /api/resources endpoints.
 */

export const resourceService = {
  getAll: () => api.get('/resources'),
  getById: (id) => api.get(`/resources/${id}`),
  create: (data) => api.post('/resources', data),
  update: (id, data) => api.put(`/resources/${id}`, data),
  delete: (id) => api.delete(`/resources/${id}`),
};
