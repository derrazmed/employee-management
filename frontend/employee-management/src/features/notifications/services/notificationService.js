import api from '@/services/Axios.js'

export const notificationService = {
  async getNotifications() {
    const response = await api.get('/notifications')
    return response.data
  },

  async getUnreadNotifications() {
    const response = await api.get('/notifications/unread')
    return response.data
  },

  async getUnreadCount() {
    const response = await api.get('/notifications/unread/count')
    return response.data
  },

  async markAsRead(id) {
    const response = await api.patch(`/notifications/${id}/read`)
    return response.data
  },

  async markAllAsRead() {
    const response = await api.patch('/notifications/read-all')
    return response.data
  },

  async deleteNotification(id) {
    const response = await api.delete(`/notifications/${id}`)
    return response.data
  },
}
