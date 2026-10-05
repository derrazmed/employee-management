<script setup>
import { computed } from 'vue'

const props = defineProps({
  show: {
    type: Boolean,
    default: false,
  },
  notification: {
    type: Object,
    default: null,
  },
})

const emit = defineEmits(['close'])

const formattedDate = computed(() => {
  if (!props.notification?.createdAt) {
    return '—'
  }

  const date = new Date(props.notification.createdAt)

  return Number.isNaN(date.getTime()) ? '—' : date.toLocaleString()
})

const actionLabel = computed(() => {
  if (!props.notification?.action) {
    return ''
  }

  return props.notification.action
})
</script>

<template>
  <Teleport to="body">
    <div
      v-if="show && notification"
      class="notification-modal-backdrop"
      @click.self="emit('close')"
      @keydown.esc.stop.prevent="emit('close')"
      tabindex="-1"
    >
      <dialog
        open
        class="notification-dialog"
        aria-modal="true"
        aria-labelledby="notification-details-title"
      >
        <div class="notification-dialog-surface">
          <header class="notification-dialog-header">
            <div>
              <div class="text-muted small mb-1">ACTIVITY</div>
              <h2 id="notification-details-title">Notification Details</h2>
            </div>

            <button
              type="button"
              class="btn-close"
              aria-label="Close"
              autofocus
              @click="emit('close')"
            ></button>
          </header>

          <div class="notification-dialog-body">
            <section class="notification-field notification-action">
              <h3>Action</h3>
              <div>
                <span class="badge text-bg-primary">{{ actionLabel || 'Activity' }}</span>
              </div>
            </section>

            <section class="notification-field">
              <h3>Performed by</h3>
              <p>{{ notification.actorName || '—' }}</p>
            </section>

            <section class="notification-field">
              <h3>Entity</h3>
              <p>{{ notification.entityType || '—' }}</p>
            </section>

            <section class="notification-field">
              <h3>Entity ID</h3>
              <p>{{ notification.entityId != null ? `#${notification.entityId}` : '—' }}</p>
            </section>

            <section class="notification-field">
              <h3>Description</h3>
              <p>{{ notification.message || 'No description provided.' }}</p>
            </section>

            <section v-if="notification.details" class="notification-field">
              <h3>Details</h3>
              <div class="notification-details">{{ notification.details }}</div>
            </section>

            <section class="notification-field">
              <h3>Date</h3>
              <p>{{ formattedDate }}</p>
            </section>
          </div>

          <footer class="notification-dialog-footer">
            <button type="button" class="btn btn-secondary" @click="emit('close')">Close</button>
          </footer>
        </div>
      </dialog>
    </div>
  </Teleport>
</template>

<style scoped>
.notification-modal-backdrop {
  position: fixed;
  inset: 0;
  z-index: 1080;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 1rem;
  overflow-y: auto;
  background: rgba(22, 30, 39, 0.48);
}

.notification-dialog {
  display: block;
  position: relative;
  inset: auto;
  width: min(600px, 100%);
  max-width: calc(100vw - 2rem);
  max-height: calc(100vh - 2rem);
  margin: auto;
  padding: 0;
  border: 0;
  background: transparent;
  color: inherit;
}

.notification-dialog-surface {
  display: flex;
  max-height: calc(100vh - 2rem);
  flex-direction: column;
  overflow: hidden;
  border: 1px solid #dee2e6;
  border-radius: 8px;
  background: #fff;
  box-shadow: 0 1rem 2rem rgba(24, 32, 40, 0.16);
}

.notification-dialog-header {
  display: flex;
  flex: 0 0 auto;
  align-items: center;
  justify-content: space-between;
  gap: 1rem;
  padding: 1.125rem 1.5rem;
  border-bottom: 1px solid #e5e7eb;
}

.notification-dialog-header h2 {
  margin: 0;
  color: #1f2937;
  font-size: 1.2rem;
  font-weight: 650;
}

.notification-dialog-body {
  display: flex;
  flex: 1 1 auto;
  flex-direction: column;
  gap: 1.125rem;
  min-height: 0;
  padding: 1.25rem 1.5rem;
  overflow-y: auto;
}

.notification-field {
  min-width: 0;
}

.notification-field h3 {
  margin: 0 0 0.35rem;
  color: #6b7280;
  font-size: 0.72rem;
  font-weight: 700;
  letter-spacing: 0.04em;
  text-transform: uppercase;
}

.notification-field p {
  margin: 0;
  color: #1f2937;
  line-height: 1.5;
  overflow-wrap: break-word;
}

.notification-dialog-footer {
  display: flex;
  flex: 0 0 auto;
  justify-content: flex-end;
  padding: 0.875rem 1.5rem;
  border-top: 1px solid #e5e7eb;
}

.notification-details {
  white-space: pre-wrap;
  overflow-wrap: break-word;
  word-break: normal;
  background: #f7f8fa;
  border: 1px solid #e5e7eb;
  border-radius: 6px;
  padding: 0.75rem;
  line-height: 1.5;
}

@media (max-width: 575.98px) {
  .notification-modal-backdrop {
    padding: 0.5rem;
  }

  .notification-dialog {
    max-width: calc(100vw - 1rem);
    max-height: calc(100vh - 1rem);
  }

  .notification-dialog-surface {
    max-height: calc(100vh - 1rem);
  }

  .notification-dialog-header,
  .notification-dialog-body {
    padding-inline: 1rem;
  }

  .notification-dialog-footer {
    padding-inline: 1rem;
  }
}
</style>
