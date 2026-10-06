<script setup>
import { useAuthStore } from '@/features/auth/stores/authStore'
import NotificationBell from '@/features/notifications/components/NotificationBell.vue'
import { useTheme } from '@/composables/useTheme'

const authStore = useAuthStore()
const emit = defineEmits(['toggle-sidebar'])
defineProps({ navigationOpen: { type: Boolean, default: true } })
const logout = () => authStore.logout()

const { theme, toggleTheme } = useTheme()
</script>

<template>
  <header class="app-header">
    <div class="app-header-inner">
      <button class="btn sidebar-toggle" type="button" aria-label="Toggle navigation" :aria-expanded="navigationOpen" @click="emit('toggle-sidebar')"><i class="bi bi-list"></i></button>
      <RouterLink to="/employees" class="app-brand">
        <span class="brand-mark"><i class="bi bi-person-vcard-fill"></i></span>
        <span class="brand-copy"><strong>Employee Management</strong><small>PEOPLE OPERATIONS</small></span>
      </RouterLink>
      <div class="app-header-actions">
        <button
          class="btn theme-toggle"
          type="button"
          :aria-label="theme === 'dark' ? 'Switch to light mode' : 'Switch to dark mode'"
          :title="theme === 'dark' ? 'Switch to light mode' : 'Switch to dark mode'"
          @click="toggleTheme"
        >
          <i :key="theme" :class="theme === 'dark' ? 'bi bi-sun' : 'bi bi-moon'"></i>
        </button>
        <NotificationBell v-if="authStore.user?.userType === 'SUPER_ADMIN'" />
        <div class="navbar-profile d-none d-md-flex">
          <span class="navbar-profile-icon">{{ authStore.user?.name?.charAt(0) || 'U' }}</span>
          <span class="navbar-user"><strong class="text-truncate">{{ authStore.user?.name }}</strong><small>{{ authStore.user?.userType === 'SUPER_ADMIN' ? 'Administrator' : 'Team member' }}</small></span>
        </div>
        <button class="btn btn-outline-secondary btn-sm navbar-logout" type="button" @click="logout"><i class="bi bi-box-arrow-right"></i><span class="d-none d-sm-inline">Logout</span><span class="visually-hidden d-sm-none">Logout</span></button>
      </div>
    </div>
  </header>
</template>

<style scoped>
.app-header { position: sticky; top: 0; z-index: 1030; height: 64px; border-bottom: 1px solid var(--app-border); background: var(--app-surface); }.app-header-inner { display: flex; height: 100%; align-items: center; padding: 0 24px; }.sidebar-toggle { width: 38px; height: 38px; min-height: 38px; padding: 0; border: 1px solid transparent; color: var(--color-icon); font-size: 1.25rem; }.sidebar-toggle:hover { border-color: var(--app-border); background: var(--color-hover-strong); }.app-brand { display: inline-flex; align-items: center; gap: .65rem; margin: 0 auto 0 .5rem; color: var(--app-text); text-decoration: none; }.brand-mark { display: inline-flex; width: 34px; height: 34px; align-items: center; justify-content: center; border: 1px solid var(--color-border); border-radius: 6px; background: var(--color-primary-soft); color: var(--color-primary-text); font-size: 1rem; }.brand-copy { display: flex; flex-direction: column; gap: .04rem; }.brand-copy strong { font-size: .93rem; font-weight: 700; }.brand-copy small { color: var(--color-text-tertiary); font-size: .56rem; font-weight: 700; letter-spacing: .09em; }.app-header-actions { display: flex; align-items: center; gap: .6rem; }.theme-toggle { width: 38px; height: 38px; min-height: 38px; padding: 0; border: 1px solid transparent; color: var(--color-icon); font-size: 1.05rem; }.theme-toggle:hover { border-color: var(--app-border); background: var(--color-hover-strong); color: var(--color-primary-text); }.theme-toggle i { display: inline-flex; animation: theme-swap .18s ease; }.navbar-profile { min-height: 40px; align-items: center; gap: .6rem; padding-left: 1rem; border-left: 1px solid var(--color-border-soft); }.navbar-profile-icon { display: inline-flex; width: 28px; height: 28px; align-items: center; justify-content: center; border-radius: 6px; background: var(--color-primary-soft); color: var(--color-primary-text); font-size: .76rem; font-weight: 700; }.navbar-user { display: flex; max-width: 180px; flex-direction: column; color: var(--color-text-body); font-size: .78rem; line-height: 1.2; }.navbar-user strong { font-weight: 650; }.navbar-user small { color: var(--app-text-muted); font-size: .68rem; }.navbar-logout { gap: .35rem; } @keyframes theme-swap { from { opacity: 0; transform: rotate(-45deg); } to { opacity: 1; transform: none; } } @media (max-width: 575.98px) { .app-header-inner { padding-inline: 12px; }.brand-copy small { display: none; }.brand-copy strong { font-size: .82rem; }.app-brand { gap: .5rem; }.brand-mark { width: 32px; height: 32px; } }
</style>
