<script setup>
import { ref, watch } from 'vue'
import PasswordInput from '@/components/common/PasswordInput.vue'
import FilterDropdown from '@/components/common/FilterDropdown.vue'
import { ESCAPE_PRIORITY, useEscapeDismiss } from '@/composables/useEscapeDismiss'

const props = defineProps({
  show: {
    type: Boolean,
    default: false,
  },

  user: {
    type: Object,
    default: null,
  },
})

const emit = defineEmits(['close', 'submit'])

const name = ref('')
const email = ref('')
const password = ref('')
const userType = ref('NORMAL_USER')

const isEditing = ref(false)

const userTypeOptions = ['NORMAL_USER', 'SUPER_ADMIN']

const userTypeLabelFn = (value) => {
  const labels = {
    NORMAL_USER: 'Normal User',
    SUPER_ADMIN: 'Super Administrator',
  }
  return labels[value] || value
}

const closeModal = () => {
  emit('close')
}

useEscapeDismiss(() => props.show, closeModal, ESCAPE_PRIORITY.MODAL)

watch(
  () => props.user,
  (user) => {
    if (user) {
      isEditing.value = true

      name.value = user.name
      email.value = user.email
      userType.value = user.userType

      password.value = ''
    } else {
      isEditing.value = false

      name.value = ''
      email.value = ''
      password.value = ''
      userType.value = 'NORMAL_USER'
    }
  },
  { immediate: true },
)

const submit = () => {
  const data = {
    name: name.value,
    email: email.value,
    userType: userType.value,
  }

  if (!isEditing.value) {
    data.password = password.value
  }

  emit('submit', data)
}
</script>

<template>
  <div v-if="show" class="modal fade show d-block" tabindex="-1" role="dialog" aria-modal="true">
    <div class="modal-dialog modal-dialog-centered modal-dialog-scrollable">
      <div class="modal-content">
        <!-- Header -->
        <div class="modal-header">
          <h5 class="modal-title">
            <i :class="isEditing ? 'bi bi-person' : 'bi bi-person-plus'" class="me-2"></i>

            {{ isEditing ? 'Update User' : 'Create User' }}
          </h5>

          <button type="button" class="btn-close" @click="emit('close')"></button>
        </div>

        <!-- Body -->
        <div class="modal-body">
          <form @submit.prevent="submit">
            <!-- User Information -->
            <h6 class="fw-bold mb-3">User Information</h6>

            <div class="row g-3">
              <!-- Name -->
              <div class="col-12">
                <label for="name" class="form-label">
                  Name
                  <span class="text-danger">*</span>
                </label>

                <input
                  id="name"
                  v-model="name"
                  type="text"
                  class="form-control"
                  placeholder="Enter name"
                  required
                />
              </div>

              <!-- Email -->
              <div class="col-12">
                <label for="email" class="form-label">
                  Email
                  <span class="text-danger">*</span>
                </label>

                <input
                  id="email"
                  v-model="email"
                  type="email"
                  class="form-control"
                  placeholder="Enter email"
                  required
                />
              </div>

              <!-- Password -->
              <div v-if="!isEditing" class="col-12">
                <label for="password" class="form-label">
                  Password
                  <span class="text-danger">*</span>
                </label>

                <PasswordInput
                  id="password"
                  v-model="password"
                  placeholder="Enter a temporary password"
                  autocomplete-mode="new-password"
                  required
                />
              </div>

              <!-- User Type -->
              <div class="col-12">
                <label for="userType" class="form-label">
                  User Type
                  <span class="text-danger">*</span>
                </label>

                <FilterDropdown
                  v-model="userType"
                  :options="userTypeOptions"
                  placeholder="Select user type"
                  :option-label="userTypeLabelFn"
                  :allow-empty="false"
                />
              </div>
            </div>
          </form>
        </div>

        <!-- Footer -->
        <div class="modal-footer">
          <button type="button" class="btn btn-secondary" @click="emit('close')">Cancel</button>

          <button type="button" class="btn btn-primary" @click="submit">
            <i class="bi bi-check-lg me-2"></i>

            {{ isEditing ? 'Update' : 'Create' }}
          </button>
        </div>
      </div>
    </div>
  </div>

  <!-- Modal backdrop -->
  <div v-if="show" class="modal-backdrop fade show"></div>
</template>

<style scoped>
/* Modal header */
.modal-title i {
  color: var(--color-primary-text);
}

/* Section headings */
.modal-body h6 {
  margin-top: 0.15rem;
  margin-bottom: 1rem;
  color: var(--color-text-secondary);
  font-size: 0.6875rem;
  font-weight: 700;
  letter-spacing: 0.07em;
  text-transform: uppercase;
}

/* Form labels */
.modal-body .form-label {
  font-size: 0.8125rem;
  font-weight: 500;
  color: var(--color-text);
  margin-bottom: 0.5rem;
}

.modal-body .form-label .text-danger {
  font-size: 0.75rem;
  font-weight: 600;
}

/* Form inputs - unified height and styling */
.modal-body .form-control {
  height: 40px;
  min-height: 40px;
  padding: 0 12px;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-sm);
  background: var(--color-surface);
  color: var(--color-text);
  font-size: 0.875rem;
  font-weight: 400;
  transition:
    border-color 0.15s ease,
    background-color 0.15s ease,
    box-shadow 0.15s ease;
}

.modal-body .form-control::placeholder {
  color: var(--color-text-tertiary);
  opacity: 1;
}

.modal-body .form-control:hover:not(:disabled):not([readonly]) {
  border-color: var(--color-border-strong);
  background: var(--color-hover);
}

.modal-body .form-control:focus {
  border-color: var(--color-primary-border);
  box-shadow: 0 0 0 3px var(--color-primary-soft);
  outline: none;
}

.modal-body .form-control:disabled,
.modal-body .form-control[readonly] {
  background: var(--color-surface-muted);
  color: var(--color-text-secondary);
  cursor: not-allowed;
  opacity: 0.8;
}

/* Align PasswordInput with form-control height */
.modal-body :deep(.password-input-wrapper) {
  height: 40px;
  min-height: 40px;
}

.modal-body :deep(.password-input) {
  height: 40px;
  min-height: 40px;
  padding-right: 2.75rem;
}

/* Align FilterDropdown trigger with form-control height */
.modal-body :deep(.file-upload-trigger),
.modal-body :deep(.filter-trigger) {
  min-height: 40px;
  height: 40px;
  width: 100%;
}

.modal-body :deep(.filter-dropdown) {
  width: 100%;
}

/* Modal footer */
.modal-footer {
  justify-content: flex-end;
  gap: 0.5rem;
  padding-top: 1rem;
  padding-bottom: 1rem;
  border-top: 1px solid var(--app-border);
}

.modal-footer .btn {
  height: 40px;
  min-width: 112px;
  padding: 0 16px;
  border-radius: var(--radius-sm);
  font-size: 0.8125rem;
  font-weight: 600;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
}

.modal-footer .btn-secondary {
  border-color: var(--color-border);
  background: var(--color-surface);
  color: var(--color-text);
}

.modal-footer .btn-secondary:hover:not(:disabled) {
  border-color: var(--color-border-strong);
  background: var(--color-hover);
  color: var(--color-text);
}

.modal-footer .btn-primary {
  background: var(--color-primary);
  border-color: var(--color-primary);
  color: #fff;
}

.modal-footer .btn-primary:hover:not(:disabled) {
  background: var(--color-primary-hover);
  border-color: var(--color-primary-hover);
}

.modal-footer .btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.modal-footer .btn:focus-visible {
  outline: 2px solid var(--color-primary-text);
  outline-offset: 2px;
}

/* Modal content spacing */
.modal-body {
  padding: 1.5rem;
}

/* Row spacing */
.modal-body .row.g-3 > [class*='col-'] {
  padding-bottom: 0.5rem;
}

@media (max-width: 575.98px) {
  .modal-body {
    padding: 1rem;
  }

  .modal-body .row.g-3 > [class*='col-'] {
    padding-bottom: 0;
  }
}
</style>
