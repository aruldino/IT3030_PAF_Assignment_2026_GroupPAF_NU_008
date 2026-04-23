import api from './api';

/**
 * Booking service — Member 2 responsibility.
 * Calls /api/bookings endpoints.
 */

export const bookingService = {
  create: (data) => api.post('/bookings', data),
  getMyBookings: () => api.get('/bookings/my'),
  getAllBookings: () => api.get('/bookings'),
  getById: (id) => api.get(`/bookings/${id}`),
  approve: (id, data) => api.put(`/bookings/${id}/approve`, data),
  reject: (id, data) => api.put(`/bookings/${id}/reject`, data),
  cancel: (id) => api.patch(`/bookings/${id}/cancel`),
  delete: (id) => api.delete(`/bookings/${id}`),
  getQRCode: (id) => api.get(`/bookings/${id}/qr`),
  checkIn: (token) => api.post('/bookings/check-in', null, { params: { token } }),
};
