import { defineStore } from 'pinia'
import { ref } from 'vue'
import { notificationService } from '../services/notificationService'
import { notificationSocket } from '../services/notificationSocket'

export const useNotificationStore = defineStore('notifications', () => {
  const notifications = ref([])
  const unreadCount = ref(0)
  const loading = ref(false)

  let pollingInterval = null

  const fetchNotifications = async () => {
    try {
      loading.value = true

      const response = await notificationService.getNotifications()

      notifications.value = response.data || []

      unreadCount.value = notifications.value.filter((notification) => !notification.read).length
    } catch (error) {
      console.error('Failed to fetch notifications:', error)
    } finally {
      loading.value = false
    }
  }

  const fetchUnreadCount = async () => {
    try {
      const response = await notificationService.getUnreadCount()

      unreadCount.value = response.data || 0
    } catch (error) {
      console.error('Failed to fetch notification count:', error)
    }
  }

  const handleRealtimeNotification = (notification) => {
    notifications.value.unshift(notification)

    unreadCount.value++
  }

  const markAsRead = async (id) => {
    const notification = notifications.value.find((item) => item.id === id)

    if (!notification || notification.read) {
      return
    }

    notification.read = true
    unreadCount.value = Math.max(0, unreadCount.value - 1)

    try {
      await notificationService.markAsRead(id)
    } catch (error) {
      notification.read = false
      unreadCount.value++
      console.error('Failed to mark notification as read:', error)
    }
  }

  const markAllAsRead = async () => {
    try {
      await notificationService.markAllAsRead()

      notifications.value.forEach((notification) => {
        notification.read = true
      })

      unreadCount.value = 0
    } catch (error) {
      console.error('Failed to mark all notifications as read:', error)
    }
  }

  const deleteNotification = async (id) => {
    try {
      await notificationService.deleteNotification(id)

      const notification = notifications.value.find((item) => item.id === id)

      notifications.value = notifications.value.filter((item) => item.id !== id)

      if (notification && !notification.read && unreadCount.value > 0) {
        unreadCount.value--
      }
    } catch (error) {
      console.error('Failed to delete notification:', error)
    }
  }

  const startPolling = () => {
    if (pollingInterval) {
      return
    }

    void fetchUnreadCount()

    pollingInterval = setInterval(() => {
      void fetchUnreadCount()
    }, 15000)
  }

  const stopPolling = () => {
    if (!pollingInterval) {
      return
    }

    clearInterval(pollingInterval)
    pollingInterval = null
  }

  const reset = () => {
    notifications.value = []
    unreadCount.value = 0
    stopPolling()
  }

  const startWebSocket = async () => {
    await fetchUnreadCount()

    notificationSocket.connect((notification) => {
      notifications.value.unshift(notification)

      if (!notification.read) {
        unreadCount.value++
      }
    })
  }

  const stopWebSocket = () => {
    notificationSocket.disconnect()
  }

  return {
    notifications,
    unreadCount,
    loading,
    fetchNotifications,
    fetchUnreadCount,
    handleRealtimeNotification,
    markAsRead,
    markAllAsRead,
    deleteNotification,
    startPolling,
    stopPolling,
    startWebSocket,
    stopWebSocket,
    reset,
  }
})
