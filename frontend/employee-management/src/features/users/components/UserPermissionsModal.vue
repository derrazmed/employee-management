<script setup>
import { ref, watch } from 'vue'
import { userService } from '@/features/users/services/userService'

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

const emit = defineEmits(['close', 'saved'])

const permissions = ['READ', 'CREATE', 'UPDATE', 'DELETE']

const selectedPermissions = ref([])

const saving = ref(false)
const error = ref('')

watch(
  () => props.user,
  (user) => {
    if (user) {
      selectedPermissions.value = [...(user.permissions || [])]
    } else {
      selectedPermissions.value = []
    }

    error.value = ''
  },
  { immediate: true },
)

const togglePermission = (permission) => {
  if (selectedPermissions.value.includes(permission)) {
    selectedPermissions.value = selectedPermissions.value.filter((item) => item !== permission)
  } else {
    selectedPermissions.value.push(permission)
  }
}

const savePermissions = async () => {
  if (!props.user) {
    return
  }

  saving.value = true
  error.value = ''

  try {
    await userService.updatePermissions(props.user.id, selectedPermissions.value)

    emit('saved')
    emit('close')
  } catch (err) {
    console.error('Failed to update permissions:', err)

    error.value = err.response?.data?.message || 'Failed to update permissions.'
  } finally {
    saving.value = false
  }
}
</script>

<template>
  <div v-if="show" class="modal-backdrop-custom">
    <div class="modal-dialog-custom">
      <div class="card">
        <!-- Header -->
        <div class="card-header d-flex justify-content-between align-items-center">
          <div>
            <h5 class="mb-0">Manage Permissions</h5>

            <small v-if="user" class="text-muted">
              {{ user.name }}
            </small>
          </div>

          <button
            type="button"
            class="btn-close"
            :disabled="saving"
            @click="emit('close')"
          ></button>
        </div>

        <!-- Body -->
        <div class="card-body">
          <!-- Error -->
          <div v-if="error" class="alert alert-danger">
            {{ error }}
          </div>

          <p class="text-muted">Select the permissions this user should have.</p>

          <!-- Permissions -->
          <div v-for="permission in permissions" :key="permission" class="form-check mb-3">
            <input
              :id="`permission-${permission}`"
              class="form-check-input"
              type="checkbox"
              :checked="selectedPermissions.includes(permission)"
              :disabled="saving"
              @change="togglePermission(permission)"
            />

            <label :for="`permission-${permission}`" class="form-check-label">
              {{ permission }}
            </label>
          </div>
        </div>

        <!-- Footer -->
        <div class="card-footer d-flex justify-content-end gap-2">
          <button type="button" class="btn btn-secondary" :disabled="saving" @click="emit('close')">
            Cancel
          </button>

          <button type="button" class="btn btn-primary" :disabled="saving" @click="savePermissions">
            <span v-if="saving" class="spinner-border spinner-border-sm me-2"></span>

            Save
          </button>
        </div>
      </div>
    </div>
  </div>
</template>
