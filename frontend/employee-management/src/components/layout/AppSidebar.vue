<script setup>
defineProps({ collapsed: { type: Boolean, default: false } })
</script>

<template>
  <aside class="sidebar" :class="{ collapsed }" aria-label="Main navigation">
    <nav class="sidebar-nav">
      <div v-if="!collapsed" class="sidebar-section-label">Workspace</div>
      <RouterLink to="/dashboard" class="sidebar-link" title="Overview" aria-label="Overview">
        <span class="sidebar-icon"><i class="bi bi-speedometer2"></i></span><span v-if="!collapsed">Overview</span>
      </RouterLink>
      <RouterLink to="/employees" class="sidebar-link" title="Employees" aria-label="Employees">
        <span class="sidebar-icon"><i class="bi bi-person-vcard"></i></span><span v-if="!collapsed">Employees</span>
      </RouterLink>
      <RouterLink to="/users" class="sidebar-link" title="Users" aria-label="Users">
        <span class="sidebar-icon"><i class="bi bi-people"></i></span><span v-if="!collapsed">Users</span>
      </RouterLink>
    </nav>
    <div class="sidebar-footer" :class="{ centered: collapsed }"><i class="bi bi-shield-check"></i><span v-if="!collapsed">Secure workspace</span></div>
  </aside>
</template>

<style scoped>
.sidebar { position: fixed; top: 64px; left: 0; z-index: 1020; display: flex; width: 248px; height: calc(100vh - 64px); flex-direction: column; border-right: 0; background: var(--sidebar-bg); transition: width .18s ease, background-color .18s ease; }.sidebar.collapsed { width: 72px; }.sidebar-nav { display: flex; flex-direction: column; gap: 5px; padding: 28px 14px 20px; }.sidebar-section-label { padding: .25rem .75rem .7rem; color: var(--sidebar-text-muted); font-size: .65rem; font-weight: 750; letter-spacing: .11em; text-transform: uppercase; }.sidebar-link { display: flex; min-height: 44px; align-items: center; gap: 12px; padding: 10px 12px; border-radius: 5px; color: var(--sidebar-text); font-size: .86rem; font-weight: 600; text-decoration: none; transition: background .16s ease, color .16s ease, box-shadow .16s ease; }.sidebar-link:hover { background: var(--sidebar-hover); color: #fff; }.sidebar-link.router-link-exact-active { box-shadow: inset 3px 0 var(--sidebar-accent); background: var(--sidebar-active-bg); color: var(--sidebar-active-text); font-weight: 650; }.sidebar-icon { width: 24px; flex-shrink: 0; color: var(--sidebar-icon); font-size: 1.05rem; text-align: center; transition: color .16s ease; }.sidebar-link.router-link-exact-active .sidebar-icon { color: var(--sidebar-accent-icon); }.sidebar-footer { display: flex; align-items: center; gap: .55rem; margin: auto 14px 16px; padding: .9rem .75rem; border-top: 1px solid var(--sidebar-border); color: var(--sidebar-footer); font-size: .72rem; }.sidebar-footer i { color: var(--sidebar-accent-icon); font-size: .9rem; }.sidebar-footer.centered { justify-content: center; padding-inline: 0; }
@media (max-width: 767.98px) { .sidebar, .sidebar.collapsed { width: 100vw; height: calc(100vh - 64px); transform: translateX(-100%); transition: transform .22s ease; overflow-y: auto; }.sidebar:not(.collapsed) { transform: translateX(0); }.sidebar-nav { padding: 1.25rem 1rem; }.sidebar-link { padding: .8rem 1rem; }.sidebar-footer { margin: auto 1rem 1rem; }.sidebar-footer.centered { justify-content: flex-start; padding: .75rem; }.sidebar.collapsed .sidebar-footer span { display: inline; } }
</style>
