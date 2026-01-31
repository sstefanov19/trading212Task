import axios from 'axios';

const API_BASE = 'http://localhost:8080/api/v1';

const api = axios.create({
  baseURL: API_BASE,
});

// Add token to requests
api.interceptors.request.use((config) => {
  const token = localStorage.getItem('token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

export const auth = {
  login: (username, password) =>
    api.post('/auth/login', { username, password }),
  register: (username, email, password) =>
    api.post('/auth/register', { username, email, password }),
};

export const trading = {
  start: (strategy) =>
    api.post('/trading/start', { strategy }),
  stop: () =>
    api.post('/trading/stop'),
  pause: () =>
    api.post('/trading/pause'),
  resume: () =>
    api.post('/trading/resume'),
  reset: () =>
    api.post('/trading/reset'),
  status: () =>
    api.get('/trading/status'),
  getAll: () =>
    api.get('/trading')
};

export const backtest = {
  run: (initialBalance, strategy, period) =>
    api.post(`/backtest/run?initialBalance=${initialBalance}&strategy=${strategy}&period=${period}`),
};

export const portfolio = {
  get: () =>
    api.get('/portfolio'),
};


export default api;
