<script setup>
import { nextTick, ref, watch } from 'vue'
import { ESCAPE_PRIORITY, useEscapeDismiss } from '@/composables/useEscapeDismiss'

const props = defineProps({
  show: {
    type: Boolean,
    default: false,
  },
  title: {
    type: String,
    default: 'Confirm action',
  },
  messagePrefix: {
    type: String,
    default: 'Are you sure you want to continue with',
  },
  subject: {
    type: String,
    default: '',
  },
  warning: {
    type: String,
    default: 'This action cannot be undone.',
  },
  confirmLabel: {
    type: String,
    default: 'Confirm',
  },
  loadingLabel: {
    type: String,
    default: 'Working...',
  },
  loading: {
    type: Boolean,
    default: false,
  },
  error: {
    type: String,
    default: '',
  },
})

const emit = defineEmits(['close', 'confirm'])
const cancelButton = ref(null)
const dialogElement = ref(null)
let previouslyFocused = null

watch(
  () => props.show,
  async (show) => {
    if (show) {
      previouslyFocused = document.activeElement
      await nextTick()
      cancelButton.value?.focus()
      return
    }

    await nextTick()
    previouslyFocused?.focus?.()
    previouslyFocused = null
  },
)

const close = () => {
  if (!props.loading) {
    emit('close')
  }
}

useEscapeDismiss(
  () => props.show,
  close,
  ESCAPE_PRIORITY.MODAL,
)

const handleKeydown = (event) => {
  if (event.key !== 'Tab') {
    return
  }

  const buttons = dialogElement.value?.querySelectorAll('button:not(:disabled)')

  if (!buttons?.length) {
    event.preventDefault()
    return
  }

  const firstButton = buttons[0]
  const lastButton = buttons[buttons.length - 1]

  if (event.shiftKey && document.activeElement === firstButton) {
    event.preventDefault()
    lastButton.focus()
  } else if (!event.shiftKey && document.activeElement === lastButton) {
    event.preventDefault()
    firstButton.focus()
  }
}
</script>

<template>
  <Teleport to="body">
    <div
      v-if="show"
      class="modal-backdrop-custom confirm-action-backdrop"
      tabindex="-1"
      @click.self="close"
      @keydown="handleKeydown"
    >
      <section
        ref="dialogElement"
        class="modal-dialog-custom confirm-action-dialog"
        tabindex="-1"
        role="dialog"
        aria-modal="true"
        aria-labelledby="confirm-action-title"
        aria-describedby="confirm-action-description confirm-action-warning"
      >
        <div class="card confirm-action-card">
          <header class="card-header confirm-action-header">
            <span class="confirm-action-icon" aria-hidden="true">
              <i class="bi bi-trash3"></i>
            </span>
            <h2 id="confirm-action-title" class="mb-0">{{ title }}</h2>
          </header>

          <div class="card-body confirm-action-body">
            <p id="confirm-action-description" class="confirm-action-message">
              {{ messagePrefix }} <strong>{{ subject }}</strong
              >?
            </p>
            <p id="confirm-action-warning" class="confirm-action-warning mb-0">
              {{ warning }}
            </p>
            <div v-if="error" class="alert alert-danger mb-0 mt-3" role="alert">
              {{ error }}
            </div>
          </div>

          <footer class="card-footer confirm-action-footer">
            <button
              ref="cancelButton"
              type="button"
              class="btn btn-outline-secondary"
              :disabled="loading"
              @click="close"
            >
              Cancel
            </button>
            <button
              type="button"
              class="btn btn-danger"
              :disabled="loading"
              @click="emit('confirm')"
            >
              <span
                v-if="loading"
                class="spinner-border spinner-border-sm me-2"
                aria-hidden="true"
              ></span>
              {{ loading ? loadingLabel : confirmLabel }}
            </button>
          </footer>
        </div>
      </section>
    </div>
  </Teleport>
</template>

<style scoped>
.confirm-action-backdrop {
  z-index: 1065;
}

.confirm-action-dialog {
  width: min(100%, 440px);
  max-height: calc(100vh - 2rem);
}

.confirm-action-card {
  width: 100%;
}

.confirm-action-header {
  display: flex;
  align-items: center;
  gap: 0.85rem;
}

.confirm-action-header h2 {
  color: var(--color-text);
  font-size: 1.05rem;
  font-weight: 650;
}

.confirm-action-icon {
  display: inline-flex;
  width: 38px;
  height: 38px;
  flex: 0 0 38px;
  align-items: center;
  justify-content: center;
  border: 1px solid var(--color-danger-border);
  border-radius: 50%;
  background: var(--color-danger-soft);
  color: var(--color-danger-text);
}

.confirm-action-body {
  display: grid;
  gap: 0.55rem;
}

.confirm-action-message {
  margin: 0;
  color: var(--color-text-body);
  line-height: 1.5;
  overflow-wrap: anywhere;
}

.confirm-action-message strong {
  color: var(--color-text);
  font-weight: 650;
}

.confirm-action-warning {
  color: var(--color-text-secondary);
  font-size: 0.82rem;
}

.confirm-action-footer {
  display: flex;
  justify-content: flex-end;
  gap: 0.6rem;
}

.confirm-action-footer .btn {
  min-width: 92px;
}

@media (max-width: 575.98px) {
  .confirm-action-footer .btn {
    min-width: 0;
  }
}
</style>
