<script setup>
defineProps({
  collapsed: {
    type: Boolean,
    default: false,
  },
})
</script>

<template>
  <aside class="sidebar" :class="{ collapsed }">
    <nav class="sidebar-nav">
      <div v-if="!collapsed" class="sidebar-section-label">Workspace</div>

      <RouterLink to="/users" class="sidebar-link" title="Users" aria-label="Users">
        <span class="sidebar-icon"><i class="bi bi-people"></i></span>

        <span v-if="!collapsed"> Users </span>
      </RouterLink>

      <RouterLink to="/employees" class="sidebar-link" title="Employees" aria-label="Employees">
        <span class="sidebar-icon"><i class="bi bi-person-vcard"></i></span>

        <span v-if="!collapsed"> Employees </span>
      </RouterLink>
    </nav>
  </aside>
</template>

<style scoped>
.sidebar {
  position: fixed;
  top: 64px;
  left: 0;

  width: 248px;
  height: calc(100vh - 64px);

  background: #fff;
  border-right: 1px solid #e1e5eb;

  transition: width 0.18s ease;

  z-index: 1020;
}

.sidebar.collapsed {
  width: 72px;
}

.sidebar-nav {
  display: flex;
  flex-direction: column;
  gap: 3px;

  padding: 18px 12px 20px;
}

.sidebar-section-label {
  padding: 0.25rem 0.65rem 0.55rem;
  color: #8a94a4;
  font-size: 0.66rem;
  font-weight: 700;
  letter-spacing: 0.09em;
  text-transform: uppercase;
}

.sidebar-link {
  display: flex;
  align-items: center;
  gap: 12px;

  min-height: 42px;
  padding: 10px 11px;

  color: #495057;
  text-decoration: none;

  border-radius: 6px;
  font-size: 0.86rem;
  font-weight: 500;
}

.sidebar-link:hover {
  background-color: #f4f6f8;
  color: #19427f;
}

.sidebar-link.router-link-exact-active {
  background-color: #edf3fc;
  color: #19427f;
  font-weight: 600;
  box-shadow: inset 2px 0 #2457a6;
}

.sidebar-icon {
  width: 24px;
  flex-shrink: 0;

  text-align: center;
  font-size: 1.05rem;
}

@media (max-width: 767.98px) {
  .sidebar,
  .sidebar.collapsed {
    position: fixed;
    top: 64px;
    left: 0;
    width: 100vw;
    height: calc(100vh - 64px);

    z-index: 1050;

    transform: translateX(-100%);
    transition: transform 0.25s ease;

    overflow-y: auto;
  }

  /* Sidebar open */
  .sidebar:not(.collapsed) {
    transform: translateX(0);
  }

  .sidebar-nav {
    padding: 1rem;
  }

  .sidebar-link {
    justify-content: flex-start;
    padding: 0.75rem 1rem;
  }

  .sidebar-link > span:not(.sidebar-icon) {
    display: inline;
  }

  .sidebar-section-label {
    display: block;
  }

  .main-content {
    margin-left: 0 !important;
  }
}
</style>
