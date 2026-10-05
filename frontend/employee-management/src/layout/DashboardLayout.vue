<script setup>
import { ref } from 'vue'
import AppNavbar from '@/components/layout/AppNavbar.vue'
import AppSidebar from '@/components/layout/AppSidebar.vue'

const sidebarCollapsed = ref(false)

const toggleSidebar = () => {
  sidebarCollapsed.value = !sidebarCollapsed.value
}
</script>

<template>
  <div class="dashboard-layout">
    <AppNavbar @toggle-sidebar="toggleSidebar" />

    <AppSidebar :collapsed="sidebarCollapsed" />

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
  padding: 28px 32px;

  min-height: calc(100vh - 64px);

  transition: margin-left 0.18s ease;
}

.main-content.sidebar-collapsed {
  margin-left: 72px;
}

.main-content-inner {
  width: min(100%, 1560px);
  margin-inline: auto;
}

@media (max-width: 767.98px) {
  .main-content,
  .main-content.sidebar-collapsed {
    width: 100%;
    max-width: 100%;
    margin-left: 0 !important;
    padding: 22px 16px;
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
