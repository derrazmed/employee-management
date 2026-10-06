import { defineStore } from 'pinia'
import { ref } from 'vue'
import { notificationService } from '../services/notificationService'
import { notificationSocket } from '../services/notificationSocket'

export const useNotificationStore = defineStore('notifications', () => {
  const notifications = ref([])
  const unreadCount = ref(0)
  const loading = ref(false)
  const error = ref(false)

  let pollingInterval = null

  const fetchNotifications = async () => {
    try {
      loading.value = true
      error.value = false

      const response = await notificationService.getNotifications()

      notifications.value = response.data || []

      unreadCount.value = notifications.value.filter((notification) => !notification.read).length
    } catch (err) {
      error.value = true
      console.error('Failed to fetch notifications:', err)
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

  /*
   * Single entry point for adding a realtime notification to the list.
   *
   * Notifications are identified by id: an already known id is merged into the
   * existing object instead of being inserted again, so we never create
   * duplicates and never replace a full notification with a partial payload.
   * Only a genuinely new, unread notification increases the unread count.
   */
  const addNotification = (notification) => {
    if (!notification?.id) {
      return
    }

    const existingIndex = notifications.value.findIndex((item) => item.id === notification.id)

    if (existingIndex !== -1) {
      Object.assign(notifications.value[existingIndex], notification)
      return
    }

    notifications.value.unshift(notification)

    if (!notification.read) {
      unreadCount.value++
    }
  }

  const handleRealtimeNotification = (notification) => {
    addNotification(notification)
  }

  /*
   * Marks a notification as read.
   *
   * Only the `read` flag changes — every other field (message, details, actor,
   * action, entityType, entityId, createdAt, ...) stays exactly as it is, so
   * the list row and the already-open details modal keep the full content.
   */
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
    error.value = false
    stopPolling()
  }

  const startWebSocket = async () => {
    await fetchUnreadCount()

    notificationSocket.connect((notification) => {
      addNotification(notification)
    })
  }

  const stopWebSocket = () => {
    notificationSocket.disconnect()
  }

  return {
    notifications,
    unreadCount,
    loading,
    error,
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
