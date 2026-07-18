import api from './api'

export const authService = {
  signup: (data) => api.post('/auth/signup', data),
  login: (data) => api.post('/auth/login', data),
  logout: (refreshToken) => api.post('/auth/logout', { refreshToken }),
  forgotPassword: (email) => api.post('/auth/forgot-password', { email }),
  resetPassword: (data) => api.post('/auth/reset-password', data),
  verifyEmail: (email, otp) => api.post('/auth/verify-email', { email, otp }),
  refreshToken: (refreshToken) => api.post('/auth/refresh', { refreshToken }),
  getMe: () => api.get('/auth/me'),
}

export const cropService = {
  recommend: (data) => api.post('/crops/recommend', data),
  getHistory: (page = 0, size = 10) => api.get(`/crops/history?page=${page}&size=${size}`),
  getById: (id) => api.get(`/crops/history/${id}`),
}

export const weatherService = {
  getByCoords: (lat, lon) => api.get(`/weather?lat=${lat}&lon=${lon}`),
  getByCity: (city) => api.get(`/weather/city?city=${encodeURIComponent(city)}`),
}

export const diseaseService = {
  detect: (formData) => api.post('/disease/detect', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
    timeout: 60000
  }),
  getHistory: (page = 0, size = 10) => api.get(`/disease/history?page=${page}&size=${size}`),
  markResolved: (id) => api.patch(`/disease/${id}/resolve`),
}

export const marketService = {
  getPrices: (commodity, state, district) => {
    const params = new URLSearchParams()
    if (commodity) params.append('commodity', commodity)
    if (state) params.append('state', state)
    if (district) params.append('district', district)
    return api.get(`/market/prices?${params}`)
  },
  getTrend: (commodity, state, days = 30) =>
    api.get(`/market/trend?commodity=${commodity}&state=${state}&days=${days}`),
  getCommodities: () => api.get('/market/commodities'),
  getStates: () => api.get('/market/states'),
  getSellAdvice: (commodity, state) =>
    api.get(`/market/advice?commodity=${commodity}&state=${state}`),
}

export const schemeService = {
  getAll: (page = 0, size = 12) => api.get(`/schemes?page=${page}&size=${size}`),
  filter: (params) => api.get('/schemes/filter', { params }),
  search: (q, page = 0) => api.get(`/schemes/search?q=${encodeURIComponent(q)}&page=${page}`),
  getById: (id) => api.get(`/schemes/${id}`),
  create: (data) => api.post('/schemes', data),
  update: (id, data) => api.put(`/schemes/${id}`, data),
  delete: (id) => api.delete(`/schemes/${id}`),
  toggleBookmark: (id) => api.post(`/schemes/${id}/bookmark`),
  getBookmarks: (page = 0) => api.get(`/schemes/bookmarks?page=${page}`),
}

export const chatService = {
  sendMessage: (data) => api.post('/chat/message', data),
  getSessions: () => api.get('/chat/sessions'),
  getSessionHistory: (sessionId) => api.get(`/chat/sessions/${sessionId}`),
  deleteSession: (sessionId) => api.delete(`/chat/sessions/${sessionId}`),
}

export const notificationService = {
  getAll: (page = 0) => api.get(`/notifications?page=${page}`),
  getUnreadCount: () => api.get('/notifications/unread-count'),
  markRead: (id) => api.patch(`/notifications/${id}/read`),
  markAllRead: () => api.patch('/notifications/read-all'),
  broadcast: (data) => api.post('/notifications/broadcast', data),
}

export const profileService = {
  getProfile: () => api.get('/profile'),
  updateProfile: (formData) => api.put('/profile', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  }),
  getDashboard: () => api.get('/profile/dashboard'),
}
