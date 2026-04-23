import api from './api';

/**
 * Ticket service — Member 3 responsibility.
 * Calls /api/tickets endpoints.
 */

export const ticketService = {
  create: (data) => api.post('/tickets', data),
  getAll: () => api.get('/tickets'),
  getMyTickets: () => api.get('/tickets/my'),
  getById: (id) => api.get(`/tickets/${id}`),
  updateStatus: (id, status) => api.patch(`/tickets/${id}/status`, status),
  assign: (id, technicianId) =>
    api.put(`/tickets/${id}/assign`, null, { params: { technicianId } }),
  addComment: (id, content) => api.post(`/tickets/${id}/comments`, content),
  getComments: (id) => api.get(`/tickets/${id}/comments`),
  delete: (id) => api.delete(`/tickets/${id}`),
};
