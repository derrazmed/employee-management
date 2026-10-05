<script setup>
import { ref, watch } from 'vue'
import { userService } from '@/features/users/services/userService'

const props = defineProps({
  show: {
    type: Boolean,
    default: false,
  },

  userId: {
    type: Number,
    default: null,
  },
})

const emit = defineEmits(['close'])

const user = ref(null)
const loading = ref(false)
const error = ref('')

const fetchUser = async () => {
  if (!props.userId) {
    return
  }

  loading.value = true
  error.value = ''
  user.value = null

  try {
    const response = await userService.getUser(props.userId)

    user.value = response.data
  } catch (err) {
    console.error('Failed to fetch user:', err)

    error.value = err.response?.data?.message || 'Failed to load user.'
  } finally {
    loading.value = false
  }
}

watch(
  () => props.show,
  (show) => {
    if (show) {
      fetchUser()
    }
  },
)
</script>

<template>
  <div v-if="show" class="modal-backdrop-custom">
    <div class="modal-dialog-custom">
      <div class="card">
        <!-- Header -->
        <div class="card-header d-flex justify-content-between align-items-center">
          <h5 class="mb-0">User Details</h5>

          <button type="button" class="btn-close" @click="emit('close')"></button>
        </div>

        <!-- Body -->
        <div class="card-body">
          <!-- Loading -->
          <div v-if="loading" class="text-center py-4">
            <div class="spinner-border text-primary" role="status">
              <span class="visually-hidden"> Loading... </span>
            </div>
          </div>

          <!-- Error -->
          <div v-else-if="error" class="alert alert-danger mb-0">
            {{ error }}
          </div>

          <!-- User -->
          <div v-else-if="user" class="user-profile">
            <div class="user-profile-hero">
              <span class="user-profile-avatar">{{ user.name?.charAt(0) || 'U' }}</span>
              <div><h6>{{ user.name }}</h6><p>{{ user.email }}</p></div>
            </div>
            <div class="user-profile-grid">
            <div class="mb-3">
              <label class="text-muted small"> Name </label>

              <div class="fw-semibold">
                {{ user.name }}
              </div>
            </div>

            <div class="mb-3">
              <label class="text-muted small"> Email </label>

              <div>
                {{ user.email }}
              </div>
            </div>

            <div class="mb-3">
              <label class="text-muted small"> User Type </label>

              <div>
                <span class="badge text-bg-secondary">
                  {{ user.userType }}
                </span>
              </div>
            </div>

            <div class="mb-3">
              <label class="text-muted small"> Status </label>

              <div>
                <span
                  class="badge"
                  :class="user.status === 'ACTIVE' ? 'text-bg-success' : 'text-bg-secondary'"
                >
                  {{ user.status }}
                </span>
              </div>
            </div>

            <div class="user-permissions-block">
              <label class="text-muted small"> Permissions </label>

              <div class="mt-1">
                <span
                  v-for="permission in user.permissions"
                  :key="permission"
                  class="badge text-bg-light border me-1"
                >
                  {{ permission }}
                </span>
              </div>
            </div>
            </div>
          </div>
        </div>

        <!-- Footer -->
        <div class="card-footer text-end">
          <button type="button" class="btn btn-secondary" @click="emit('close')">Close</button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.user-profile-hero { display: flex; align-items: center; gap: .85rem; margin-bottom: 1.5rem; padding-bottom: 1.25rem; border-bottom: 1px solid var(--app-border); }.user-profile-avatar { display: inline-flex; width: 44px; height: 44px; align-items: center; justify-content: center; border-radius: 6px; background: #eaf1fb; color: var(--app-primary); font-weight: 700; }.user-profile-hero h6 { margin: 0; color: var(--app-text); font-size: 1rem; font-weight: 700; }.user-profile-hero p { margin: .12rem 0 0; color: var(--app-text-muted); font-size: .8rem; }.user-profile-grid { display: grid; grid-template-columns: 1fr 1fr; gap: .25rem 1.5rem; }.user-profile-grid label { display: block; margin-bottom: .25rem; color: #667085; font-size: .7rem; font-weight: 700; letter-spacing: .06em; text-transform: uppercase; }.user-permissions-block { grid-column: 1 / -1; padding-top: .9rem; border-top: 1px solid var(--app-border); } @media (max-width: 575.98px) { .user-profile-grid { grid-template-columns: 1fr; } }
</style>
