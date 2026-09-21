import axios from 'axios';

const API_BASE = import.meta.env.VITE_API_BASE || 'http://localhost:8080/api';

const api = axios.create({
  baseURL: API_BASE,
  headers: { 'Content-Type': 'application/json' },
});

// Attach JWT token on every request
api.interceptors.request.use((config) => {
  const token = localStorage.getItem('token');
  if (token) {
    config.headers['Authorization'] = `Bearer ${token}`;
  }
  return config;
});

// Auto-logout on 401 (token expired mid-session)
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      // Clear all auth data and redirect to login
      ['token', 'username', 'email', 'role', 'tokenExpiry'].forEach((k) =>
        localStorage.removeItem(k)
      );
      delete api.defaults.headers.common['Authorization'];
      window.location.href = '/';
    }
    return Promise.reject(error);
  }
);

// ─── Medicines ────────────────────────────────────────────────────────────────
export const getMedicines = (params = {}) => api.get('/medicines', { params });
export const getMedicineById = (id) => api.get(`/medicines/${id}`);
export const createMedicine = (data) => api.post('/medicines', data);
export const updateMedicine = (id, data) => api.put(`/medicines/${id}`, data);
export const deleteMedicine = (id) => api.delete(`/medicines/${id}`);
export const adjustStock = (data) => api.post('/medicines/adjust-stock', data);
export const getLowStockMedicines = (threshold = 10) =>
  api.get('/medicines/low-stock', { params: { threshold } });
export const getExpiringMedicines = (days = 90) =>
  api.get('/medicines/expiring', { params: { days } });
export const getExpiredMedicines = () =>
  api.get('/medicines/expired');
export const getMedicinesBySupplier = (supplierId) =>
  api.get(`/medicines/supplier/${supplierId}`);
export const getDashboardStats = (lowStockThreshold = 10, expiryDays = 90) =>
  api.get('/medicines/stats', { params: { lowStockThreshold, expiryDays } });

// ─── Categories ───────────────────────────────────────────────────────────────
export const getCategories = () => api.get('/categories');
export const getCategoryById = (id) => api.get(`/categories/${id}`);
export const createCategory = (data) => api.post('/categories', data);
export const updateCategory = (id, data) => api.put(`/categories/${id}`, data);
export const deleteCategory = (id) => api.delete(`/categories/${id}`);

// ─── Suppliers ────────────────────────────────────────────────────────────────
export const getSuppliers = () => api.get('/suppliers');
export const getSupplierById = (id) => api.get(`/suppliers/${id}`);
export const createSupplier = (data) => api.post('/suppliers', data);
export const updateSupplier = (id, data) => api.put(`/suppliers/${id}`, data);
export const deleteSupplier = (id) => api.delete(`/suppliers/${id}`);

// ─── Notifications ────────────────────────────────────────────────────────────
export const getNotifications = () => api.get('/notifications');
export const getUnreadCount = () => api.get('/notifications/unread-count');
export const markNotificationRead = (id) => api.put(`/notifications/${id}/read`);
export const markAllNotificationsRead = () => api.put('/notifications/read-all');
export const dismissNotification = (id) => api.delete(`/notifications/${id}`);
export const triggerNotificationScan = () => api.post('/notifications/trigger-scan');

// ─── Analytics ────────────────────────────────────────────────────────────────
export const getAnalytics = () => api.get('/analytics');

// ─── Stock Logs ───────────────────────────────────────────────────────────────
export const getStockLogs = (page = 0, size = 20) =>
  api.get('/stock-logs', { params: { page, size } });

// ─── Purchase Orders ──────────────────────────────────────────────────────────
export const getPurchaseOrders = (page = 0, size = 10) =>
  api.get('/purchase-orders', { params: { page, size } });
export const getPurchaseOrderById = (id) => api.get(`/purchase-orders/${id}`);
export const createPurchaseOrder = (data) => api.post('/purchase-orders', data);
export const updatePurchaseOrderStatus = (id, status) =>
  api.put(`/purchase-orders/${id}/status`, null, { params: { status } });
export const deletePurchaseOrder = (id) => api.delete(`/purchase-orders/${id}`);

// ─── Users ────────────────────────────────────────────────────────────────────
export const getUsers = () => api.get('/users');
export const getUserById = (id) => api.get(`/users/${id}`);
export const updateUser = (id, data) => api.put(`/users/${id}`, data);
export const deleteUser = (id) => api.delete(`/users/${id}`);
export const getMe = () => api.get('/users/me');
export const changePassword = (data) => api.put('/users/me/change-password', data);

// ─── Activity Logs (Admin) ────────────────────────────────────────────────────
export const getActivityLogs = (page = 0, size = 20) =>
  api.get('/activity-logs', { params: { page, size } });
export const getRecentActivityLogs = () => api.get('/activity-logs/recent');
export const getActivityLogsByUser = (username, page = 0, size = 20) =>
  api.get(`/activity-logs/user/${username}`, { params: { page, size } });
export const getActivityLogsByEntity = (entityType, page = 0, size = 20) =>
  api.get(`/activity-logs/entity/${entityType}`, { params: { page, size } });
export const getUserActivitySummary = (days = 7) =>
  api.get('/activity-logs/summary', { params: { days } });

// ─── Supplier Analytics ───────────────────────────────────────────────────────
export const getAllSupplierPerformance = () => api.get('/supplier-analytics');
export const getSupplierPerformance = (id) => api.get(`/supplier-analytics/${id}`);

export default api;
