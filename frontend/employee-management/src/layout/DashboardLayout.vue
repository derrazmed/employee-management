<script setup>
import { onBeforeUnmount, onMounted, ref } from 'vue'
import AppNavbar from '@/components/layout/AppNavbar.vue'
import AppSidebar from '@/components/layout/AppSidebar.vue'

const sidebarCollapsed = ref(false)

const syncNavigation = () => {
  if (window.innerWidth < 768) sidebarCollapsed.value = true
}

const toggleSidebar = () => {
  sidebarCollapsed.value = !sidebarCollapsed.value
}

onMounted(() => {
  syncNavigation()
  window.addEventListener('resize', syncNavigation)
})

onBeforeUnmount(() => window.removeEventListener('resize', syncNavigation))
</script>

<template>
  <div class="dashboard-layout">
    <AppNavbar :navigation-open="!sidebarCollapsed" @toggle-sidebar="toggleSidebar" />

    <AppSidebar :collapsed="sidebarCollapsed" />

    <button
      v-if="!sidebarCollapsed"
      type="button"
      class="mobile-nav-scrim"
      aria-label="Close navigation"
      @click="sidebarCollapsed = true"
    ></button>

    <main class="main-content" :class="{ 'sidebar-collapsed': sidebarCollapsed }">
      <div class="main-content-inner">
        <RouterView />
      </div>
    </main>
  </div>
</template>

<style scoped>
.main-content {
  margin-left: 248px;
  padding: 32px 40px 48px;

  min-height: calc(100vh - 64px);

  transition: margin-left 0.18s ease;
}

.main-content.sidebar-collapsed {
  margin-left: 72px;
}

.main-content-inner {
  width: min(100%, 1240px);
  margin-inline: auto;
}

.mobile-nav-scrim { display: none; }

@media (max-width: 767.98px) {
  .mobile-nav-scrim { position: fixed; inset: 64px 0 0; z-index: 1010; display: block; width: 100%; padding: 0; border: 0; background: var(--nav-scrim); }
  .main-content,
  .main-content.sidebar-collapsed {
    width: 100%;
    max-width: 100%;
    margin-left: 0 !important;
    padding: 24px 16px 32px;
    box-sizing: border-box;
  }

  .main-content-inner {
    width: 100%;
    max-width: none;
    margin: 0;
  }

  .main-content-inner :deep(.container-fluid) {
    width: 100%;
    max-width: none;
    padding-right: 0 !important;
    padding-left: 0 !important;
  }
}

@media (max-width: 575.98px) {
  .main-content,
  .main-content.sidebar-collapsed {
    padding: 18px 16px;
  }
}
</style>
