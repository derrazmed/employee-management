<script setup>
import { useAuthStore } from '@/features/auth/stores/authStore'
import NotificationBell from '@/features/notifications/components/NotificationBell.vue'

const authStore = useAuthStore()

const emit = defineEmits(['toggle-sidebar'])

const logout = () => {
  authStore.logout()
}
</script>

<template>
  <nav class="navbar navbar-light bg-white border-bottom">
    <div class="container-fluid px-3 px-lg-4">
      <!-- Sidebar toggle -->
      <button
        class="btn sidebar-toggle"
        type="button"
        @click="emit('toggle-sidebar')"
        aria-label="Toggle sidebar"
      >
        <i class="bi bi-list"></i>
      </button>

      <!-- Application name -->
      <RouterLink to="/" class="navbar-brand ms-2 me-auto">
        <span class="brand-mark"><i class="bi bi-person-vcard-fill"></i></span>
        <span class="brand-copy">
          <span>Employee Management</span>
          <small>PEOPLE OPERATIONS</small>
        </span>
      </RouterLink>

      <div class="d-flex align-items-center gap-2 ms-auto">
        <NotificationBell v-if="authStore.user?.userType === 'SUPER_ADMIN'" />

        <div class="navbar-profile d-none d-sm-flex align-items-center gap-2">
          <span class="navbar-profile-icon"><i class="bi bi-person-fill"></i></span>
          <span class="navbar-user text-truncate">{{ authStore.user?.name }}</span>
        </div>

        <button
          class="btn btn-outline-secondary btn-sm navbar-logout"
          type="button"
          @click="logout"
        >
          <i class="bi bi-box-arrow-right me-sm-1"></i>
          <span class="d-none d-sm-inline">Logout</span>
          <span class="visually-hidden d-sm-none">Logout</span>
        </button>
      </div>
    </div>
  </nav>
</template>

<style scoped>
.sidebar-toggle {
  display: inline-flex;
  width: 38px;
  height: 38px;
  align-items: center;
  justify-content: center;
  padding: 0;
  border: 1px solid transparent;
  color: #4b5563;
  font-size: 1.25rem;
}

.sidebar-toggle:hover {
  border-color: #e1e5eb;
  background-color: #f4f6f8;
}

.navbar-brand {
  display: inline-flex;
  align-items: center;
  gap: 0.65rem;
  letter-spacing: 0;
}

.brand-mark {
  display: inline-flex;
  width: 34px;
  height: 34px;
  align-items: center;
  justify-content: center;
  border: 1px solid #d7e1f0;
  border-radius: 7px;
  background: #f1f5fb;
  color: #2457a6;
  font-size: 1rem;
}

.brand-copy {
  display: flex;
  flex-direction: column;
  gap: 0.08rem;
}

.brand-copy small {
  color: #778295;
  font-size: 0.56rem;
  font-weight: 700;
  letter-spacing: 0.09em;
}

.navbar-user {
  max-width: 190px;
  color: #394456;
  font-size: 0.82rem;
  font-weight: 600;
}

.navbar-profile {
  min-height: 38px;
  padding-inline: 0.7rem;
  border-left: 1px solid #e5e8ed;
}

.navbar-profile-icon {
  display: inline-flex;
  width: 28px;
  height: 28px;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  background: #eef1f5;
  color: #526075;
}

.navbar-logout {
  min-width: 38px;
}

@media (max-width: 575.98px) {
  .brand-copy small {
    display: none;
  }

  .navbar-brand {
    font-size: 0.88rem;
  }
}
</style>
