<script setup>
import { onBeforeUnmount, onMounted, ref } from 'vue'
import { useNotificationStore } from '../stores/notificationStore'
import NotificationDropdown from './NotificationDropdown.vue'
import NotificationDetailsModal from './NotificationDetailsModal.vue'

const showDetails = ref(false)
const selectedNotification = ref(null)

const notificationStore = useNotificationStore()

const isOpen = ref(false)

const toggleDropdown = () => {
  isOpen.value = !isOpen.value
}

const closeDropdown = () => {
  isOpen.value = false
}

const handleOutsideClick = (event) => {
  if (!event.target.closest('.notification-wrapper')) {
    closeDropdown()
  }
}

onMounted(() => {
  document.addEventListener('click', handleOutsideClick)

  notificationStore.startWebSocket()
})

onBeforeUnmount(() => {
  notificationStore.stopWebSocket()
  document.removeEventListener('click', handleOutsideClick)
})

const openNotificationDetails = async (notification) => {
  selectedNotification.value = notification
  showDetails.value = true
  closeDropdown()

  if (!notification.read) {
    await notificationStore.markAsRead(notification.id)
  }
}

const closeNotificationDetails = () => {
  showDetails.value = false
  selectedNotification.value = null
}
</script>

<template>
  <div class="notification-wrapper">
    <button
      type="button"
      class="notification-button"
      aria-label="Notifications"
      aria-haspopup="true"
      :aria-expanded="isOpen"
      @click.stop="toggleDropdown"
    >
      <i class="bi bi-bell"></i>

      <span v-if="notificationStore.unreadCount > 0" class="notification-badge">
        {{ notificationStore.unreadCount > 99 ? '99+' : notificationStore.unreadCount }}
      </span>
    </button>

    <NotificationDropdown
      v-if="isOpen"
      @open-details="openNotificationDetails"
      @close="closeDropdown"
    />
  </div>

  <NotificationDetailsModal
    :show="showDetails"
    :notification="selectedNotification"
    @close="closeNotificationDetails"
  />
</template>

<style scoped>
.notification-wrapper {
  position: relative;
}

.notification-button {
  position: relative;
  width: 38px;
  height: 38px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 1px solid var(--color-border);
  border-radius: 7px;
  background: var(--color-surface);
  color: var(--color-text-body);
  font-size: 16px;
  cursor: pointer;
  transition:
    background-color 0.15s ease,
    border-color 0.15s ease;
}

.notification-button:hover {
  border-color: var(--color-border-strong);
  background: var(--color-surface-muted);
}

.notification-badge {
  position: absolute;
  top: -5px;
  right: -5px;
  min-width: 16px;
  height: 16px;
  padding: 0 3px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 1px solid var(--color-surface);
  border-radius: 8px;
  background: var(--color-danger);
  color: #fff;
  font-size: 10px;
  font-weight: 600;
  line-height: 1;
}
</style>
