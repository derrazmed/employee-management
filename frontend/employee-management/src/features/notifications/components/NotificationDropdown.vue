<script setup>
import { computed, onMounted } from 'vue'
import { useNotificationStore } from '../stores/notificationStore'

const notificationStore = useNotificationStore()

const emit = defineEmits(['close', 'open-details'])

const notifications = computed(() => notificationStore.notifications)

const formatDate = (date) => {
  if (!date) {
    return ''
  }

  const notificationDate = new Date(date)
  const now = new Date()

  const difference = Math.floor((now.getTime() - notificationDate.getTime()) / 1000)

  if (difference < 60) {
    return 'Just now'
  }

  const minutes = Math.floor(difference / 60)

  if (minutes < 60) {
    return `${minutes} min ago`
  }

  const hours = Math.floor(minutes / 60)

  if (hours < 24) {
    return `${hours}h ago`
  }

  const days = Math.floor(hours / 24)

  if (days < 7) {
    return `${days}d ago`
  }

  return notificationDate.toLocaleDateString()
}

const handleNotificationClick = (notification) => {
  emit('open-details', notification)
  emit('close')
}

const handleMarkAllAsRead = async () => {
  await notificationStore.markAllAsRead()
}

const handleMarkAsRead = async (notification) => {
  await notificationStore.markAsRead(notification.id)
}

onMounted(async () => {
  await notificationStore.fetchNotifications()
})

const deleteNotification = async (notification) => {
  await notificationStore.deleteNotification(notification.id)
}
</script>

<template>
  <div class="notification-dropdown">
    <div class="notification-header">
      <div>
        <h6 class="mb-0">Notifications</h6>

        <small v-if="notificationStore.unreadCount > 0" class="text-muted">
          {{ notificationStore.unreadCount }} unread
        </small>
      </div>

      <button
        v-if="notificationStore.unreadCount > 0"
        type="button"
        class="btn btn-sm btn-link text-decoration-none"
        @click="handleMarkAllAsRead"
      >
        Mark all as read
      </button>
    </div>

    <div class="notification-list">
      <div v-if="notificationStore.loading" class="notification-empty">
        <span class="spinner-border spinner-border-sm"></span>
      </div>

      <div v-else-if="notifications.length === 0" class="notification-empty">
        <i class="bi bi-bell-slash fs-4 mb-2"></i>

        <span>No notifications</span>
      </div>

      <div
        v-for="notification in notifications"
        :key="notification.id"
        class="notification-item d-flex align-items-start"
        :class="{ 'notification-unread': !notification.read }"
      >
        <div class="notification-icon">
          <i v-if="notification.action === 'CREATE'" class="bi bi-person-plus"></i>

          <i v-else-if="notification.action === 'UPDATE'" class="bi bi-pencil"></i>

          <i v-else-if="notification.action === 'DELETE'" class="bi bi-trash"></i>

          <i v-else class="bi bi-info-circle"></i>
        </div>

        <button
          v-if="!notification.read"
          type="button"
          class="notification-open notification-content"
          @click="handleNotificationClick(notification)"
        >
          <span class="notification-message">
            <strong>{{ notification.actorName }}</strong>
            {{ notification.message }}
          </span>

          <small class="text-muted">
            {{ formatDate(notification.createdAt) }}
          </small>
        </button>

        <button
          type="button"
          class="notification-action"
          title="Mark as read"
          @click.stop="handleMarkAsRead(notification)"
        >
          <i class="bi bi-check2-circle"></i>
        </button>

        <button
          type="button"
          class="notification-delete-button"
          title="Delete notification"
          aria-label="Delete notification"
          @click.stop="deleteNotification(notification)"
        >
          <i class="bi bi-trash"></i>
        </button>

        <span v-if="!notification.read" class="unread-dot"></span>
      </div>
    </div>
  </div>
</template>

<style scoped>
.notification-dropdown {
  position: absolute;
  top: calc(100% + 10px);
  right: 0;
  width: min(380px, calc(100vw - 24px));
  background: #fff;
  border: 1px solid var(--app-border);
  border-radius: var(--app-radius);
  box-shadow: 0 8px 22px rgba(31, 41, 55, 0.14);
  overflow: hidden;
  z-index: 1050;
}

.notification-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 0.75rem;
  padding: 12px 14px;
  border-bottom: 1px solid var(--app-border);
}

.notification-header h6 {
  color: var(--app-text);
  font-size: 0.9rem;
  font-weight: 700;
}

.notification-list {
  max-height: 420px;
  overflow-y: auto;
}

.notification-item {
  position: relative;
  display: flex;
  align-items: flex-start;
  gap: 10px;
  padding: 13px 14px;
  border: 0;
  border-left: 3px solid transparent;
  border-bottom: 1px solid #f0f0f0;
  background: #fff;
  text-align: left;
}

.notification-item:hover {
  background: #f8f9fa;
}

.notification-item.notification-unread {
  border-left-color: var(--app-primary);
  background: #f4f7fb;
}

.notification-open:focus-visible {
  outline: 2px solid var(--app-primary);
  outline-offset: 2px;
}

.notification-open {
  display: flex;
  flex: 1;
  flex-direction: column;
  align-items: flex-start;
  min-width: 0;
  padding: 0;
  border: 0;
  background: transparent;
  color: inherit;
  text-align: left;
}

.notification-icon {
  flex-shrink: 0;
  width: 34px;
  height: 34px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 5px;
  background: #edf1f6;
  color: #526b8d;
}

.notification-content {
  flex: 1;
  min-width: 0;
}

.notification-message {
  color: #475467;
  font-size: 0.82rem;
  line-height: 1.4;
  margin-bottom: 4px;
}

.notification-message strong {
  color: #293447;
  font-weight: 650;
}

.notification-delete-button {
  display: inline-flex;
  width: 30px;
  height: 30px;
  flex: 0 0 30px;
  align-items: center;
  justify-content: center;
  padding: 0;
  border: 1px solid transparent;
  border-radius: 5px;
  background: transparent;
  color: #7b8493;
}

.notification-delete-button:hover {
  border-color: #f1c5c9;
  background: #fff4f4;
  color: #b02a37;
}

.notification-delete-button:focus-visible {
  outline: 2px solid var(--app-primary);
  outline-offset: 1px;
}

.unread-dot {
  width: 8px;
  height: 8px;
  flex-shrink: 0;
  margin-top: 6px;
  border-radius: 50%;
  background: var(--app-primary);
}

@media (max-width: 480px) {
  .notification-dropdown {
    right: -52px;
  }
}

.notification-empty {
  min-height: 180px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #6c757d;
}

.notification-actions {
  display: flex;
  align-items: center;
  gap: 0.35rem;
}

.notification-action {
  width: 28px;
  height: 28px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: none;
  background: transparent;
  color: #6c757d;
  padding: 0;
  border-radius: 4px;
  cursor: pointer;
}

.notification-action:hover {
  color: #0d6efd;
  background: #f1f3f5;
}

.notification-action.delete:hover {
  color: #dc3545;
}
</style>
