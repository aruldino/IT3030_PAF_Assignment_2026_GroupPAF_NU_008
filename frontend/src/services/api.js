import axios from 'axios';

const api = axios.create({
  baseURL: '/api',
  headers: {
    'Content-Type': 'application/json',
  },
});

api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

api.interceptors.response.use(
  (response) => response,
  (error) => {
    const status = error.response?.status;
    const data = error.response?.data;

    if (status === 401) {
      // User deleted — clear everything and go to login
      localStorage.removeItem('user');
      localStorage.removeItem('token');
      window.location.href = '/login';
    }

    if (status === 403) {
      const currentPath = window.location.pathname;
      if (data?.status === 'PENDING' && currentPath !== '/pending-approval') {
        // Don't clear localStorage — keep user info for the page
        window.location.href = '/pending-approval';
      } else if (data?.status === 'SUSPENDED' && currentPath !== '/access-denied') {
        // Don't clear localStorage — keep user info for the page
        window.location.href = '/access-denied';
      }
    }

    return Promise.reject(error);
  }
);

export default api;