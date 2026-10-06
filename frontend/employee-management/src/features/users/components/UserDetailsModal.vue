<script setup>
import { computed, ref, watch } from 'vue'
import { userService } from '@/features/users/services/userService'
import { humanizeLabel, pluralize } from '@/utils/format'
import { ESCAPE_PRIORITY, useEscapeDismiss } from '@/composables/useEscapeDismiss'

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

const isAdmin = computed(() => user.value?.userType === 'SUPER_ADMIN')

const permissionList = computed(() =>
  Array.isArray(user.value?.permissions) ? user.value.permissions : [],
)

const permissionCountLabel = computed(() =>
  pluralize(permissionList.value.length, 'permission'),
)

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

const closeModal = () => {
  emit('close')
}

useEscapeDismiss(
  () => props.show,
  closeModal,
  ESCAPE_PRIORITY.MODAL,
)

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
  <div v-if="show" class="modal fade show d-block" tabindex="-1" role="dialog" aria-modal="true">
    <div class="modal-dialog modal-dialog-centered modal-dialog-scrollable">
      <div class="modal-content">
        <!-- Header -->
        <div class="modal-header">
          <h5 class="modal-title">
            <i class="bi bi-person-vcard me-2"></i>
            User Details
          </h5>

          <button type="button" class="btn-close" @click="emit('close')"></button>
        </div>

        <!-- Body -->
        <div class="modal-body">
          <!-- Loading -->
          <div v-if="loading" class="d-flex justify-content-center py-5">
            <div class="spinner-border text-primary" role="status">
              <span class="visually-hidden"> Loading... </span>
            </div>
          </div>

          <!-- Error -->
          <div v-else-if="error" class="alert alert-danger mb-0">
            {{ error }}
          </div>

          <!-- User -->
          <div v-else-if="user">
            <!-- Identity -->
            <div class="user-profile-hero">
              <span class="user-profile-avatar">{{ user.name?.charAt(0) || 'U' }}</span>

              <div class="user-profile-title">
                <h4 class="mb-1">{{ user.name }}</h4>

                <div class="user-profile-meta">
                  <span class="role-chip" :class="{ 'role-chip-admin': isAdmin }">
                    {{ humanizeLabel(user.userType) }}
                  </span>

                  <span
                    class="user-status"
                    :class="{ 'is-active': user.status === 'ACTIVE' }"
                  >
                    {{ user.status === 'ACTIVE' ? 'Active' : 'Inactive' }}
                  </span>
                </div>
              </div>
            </div>

            <!-- Account -->
            <section class="profile-section">
              <h6>Account</h6>

              <div class="row g-3">
                <div class="col-md-6">
                  <div class="text-muted small">Email</div>

                  <div class="fw-semibold user-email">{{ user.email || '—' }}</div>
                </div>

                <div class="col-md-6">
                  <div class="text-muted small">User Type</div>

                  <div class="fw-semibold">{{ humanizeLabel(user.userType) || '—' }}</div>
                </div>

                <div class="col-md-6">
                  <div class="text-muted small">Status</div>

                  <div class="fw-semibold">
                    {{ user.status === 'ACTIVE' ? 'Active' : 'Inactive' }}
                  </div>
                </div>

                <div class="col-md-6">
                  <div class="text-muted small">Permissions</div>

                  <div class="fw-semibold">{{ permissionCountLabel }}</div>
                </div>
              </div>
            </section>

            <!-- Permissions -->
            <section class="profile-section">
              <h6>Permissions</h6>

              <div v-if="permissionList.length" class="user-permissions-list">
                <span v-for="permission in permissionList" :key="permission" class="permission-chip">
                  {{ humanizeLabel(permission) }}
                </span>
              </div>

              <div v-else class="text-muted">No permissions assigned.</div>
            </section>
          </div>
        </div>

        <!-- Footer -->
        <div class="modal-footer">
          <button type="button" class="btn btn-secondary" @click="emit('close')">Close</button>
        </div>
      </div>
    </div>
  </div>

  <!-- Backdrop -->
  <div v-if="show" class="modal-backdrop fade show"></div>
</template>

<style scoped>
.modal-dialog { width: min(560px, 100%); }
.user-profile-hero { display: flex; align-items: center; gap: 1rem; padding-bottom: 1.4rem; border-bottom: 1px solid var(--app-border); }
.user-profile-avatar { display: inline-flex; width: 52px; height: 52px; flex: 0 0 auto; align-items: center; justify-content: center; border-radius: 50%; background: var(--color-primary-soft); color: var(--color-primary-text); font-size: 1.2rem; font-weight: 700; text-transform: uppercase; }
.user-profile-title h4 { color: var(--app-text); font-size: 1.15rem; font-weight: 700; }
.user-profile-meta { display: flex; flex-wrap: wrap; align-items: center; gap: .6rem; }
.user-email { overflow-wrap: anywhere; }
.profile-section { margin-top: 1.5rem; }
.profile-section h6 { margin: 0 0 1rem; color: var(--color-text-secondary); font-size: .72rem; font-weight: 700; letter-spacing: .07em; text-transform: uppercase; }
.profile-section .text-muted.small { margin-bottom: .22rem; font-size: .72rem; text-transform: uppercase; letter-spacing: .035em; }
.profile-section .fw-semibold { color: var(--app-text); font-size: .9rem; }
.user-permissions-list { display: flex; flex-wrap: wrap; gap: .5rem; }
@media (max-width: 575.98px) { .user-profile-hero { align-items: flex-start; gap: .8rem; } .user-profile-avatar { width: 44px; height: 44px; font-size: 1.05rem; } .user-profile-title h4 { font-size: 1.05rem; } }
</style>
