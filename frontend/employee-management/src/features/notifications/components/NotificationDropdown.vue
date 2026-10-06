<script setup>
import { computed, onMounted } from 'vue'
import { useNotificationStore } from '../stores/notificationStore'
import { formatRelativeTime } from '@/utils/format'
import { ESCAPE_PRIORITY, useEscapeDismiss } from '@/composables/useEscapeDismiss'

const notificationStore = useNotificationStore()

const emit = defineEmits(['close', 'open-details'])

const notifications = computed(() => notificationStore.notifications)

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

useEscapeDismiss(
  () => true,
  () => emit('close'),
  ESCAPE_PRIORITY.DROPDOWN,
)
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
          type="button"
          class="notification-open notification-content"
          :aria-label="`Open details for notification ${notification.id}`"
          @click="handleNotificationClick(notification)"
        >
          <span class="notification-message">
            <strong>{{ notification.actorName }}</strong>
            {{ notification.message }}
          </span>

          <small class="text-muted">
            {{ formatRelativeTime(notification.createdAt) }}
          </small>
        </button>

        <button
          v-if="!notification.read"
          type="button"
          class="notification-action"
          title="Mark as read"
          aria-label="Mark notification as read"
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
  background: var(--color-elevated);
  border: 1px solid var(--app-border);
  border-radius: var(--app-radius);
  box-shadow: var(--shadow-menu);
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
  border-bottom: 1px solid var(--color-border-soft);
  background: var(--color-elevated);
  text-align: left;
}

.notification-item:hover {
  background: var(--color-surface-muted);
}

.notification-item.notification-unread {
  border-left-color: var(--color-primary-text);
  background: var(--color-hover-strong);
}

.notification-open:focus-visible {
  outline: 2px solid var(--color-primary-text);
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
  background: var(--color-primary-soft);
  color: var(--color-primary-text);
}

.notification-content {
  flex: 1;
  min-width: 0;
}

.notification-message {
  color: var(--color-icon);
  font-size: 0.82rem;
  line-height: 1.4;
  margin-bottom: 4px;
}

.notification-message strong {
  color: var(--color-text);
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
  color: var(--color-text-tertiary);
}

.notification-delete-button:hover {
  border-color: var(--color-danger-border);
  background: var(--color-danger-soft);
  color: var(--color-danger-text);
}

.notification-delete-button:focus-visible {
  outline: 2px solid var(--color-primary-text);
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
  color: var(--color-text-secondary);
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
  color: var(--color-text-secondary);
  padding: 0;
  border-radius: 4px;
  cursor: pointer;
}

.notification-action:hover {
  color: var(--color-primary-text);
  background: var(--color-hover-strong);
}

.notification-action.delete:hover {
  color: var(--color-danger-text);
}
</style>
