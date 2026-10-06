<script setup>
import { computed, nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { userService } from '@/features/users/services/userService'
import { humanizeLabel, pluralize } from '@/utils/format'
import ConfirmActionModal from '@/components/common/ConfirmActionModal.vue'
import AppPagination from '@/components/common/AppPagination.vue'
import { ESCAPE_PRIORITY, useEscapeDismiss } from '@/composables/useEscapeDismiss'

defineProps({
  isSuperAdmin: {
    type: Boolean,
    default: false,
  },
})

const emit = defineEmits(['create', 'edit', 'view', 'delete', 'permissions', 'toggle-status'])

const users = ref([])

const loading = ref(false)
const error = ref('')

const search = ref('')

const currentPage = ref(0)
const pageSize = ref(10)

const totalPages = ref(0)
const totalElements = ref(0)

const hasAccess = ref(true)
const showDeleteModal = ref(false)
const userToDelete = ref(null)
const deleting = ref(false)
const deleteError = ref('')

const fetchUsers = async () => {
  loading.value = true
  error.value = ''
  hasAccess.value = true

  try {
    const response = await userService.getUsers(currentPage.value, pageSize.value, search.value)

    const pageData = response.data

    users.value = pageData.content
    totalPages.value = pageData.totalPages
    totalElements.value = pageData.totalElements
  } catch (err) {
    console.error('Failed to fetch users:', err)

    error.value = err.response?.data?.message || 'Failed to load users.'

    if (err.response?.status === 403) {
      hasAccess.value = false
    }
  } finally {
    loading.value = false
  }
}

defineExpose({
  fetchUsers,
})

const goToPage = (page) => {
  if (page < 0 || page >= totalPages.value) {
    return
  }

  currentPage.value = page
  fetchUsers()
}

const changePageSize = () => {
  currentPage.value = 0
  fetchUsers()
}

let searchTimeout = null

watch(search, () => {
  clearTimeout(searchTimeout)

  searchTimeout = setTimeout(() => {
    currentPage.value = 0
    fetchUsers()
  }, 400)
})

const hasSearchQuery = computed(() => Boolean(search.value.trim()))

const summaryCounts = computed(() => {
  if (users.value.length !== totalElements.value) {
    return null
  }

  return {
    total: totalElements.value,
    active: users.value.filter((user) => user.status === 'ACTIVE').length,
    admins: users.value.filter((user) => user.userType === 'SUPER_ADMIN').length,
  }
})

const permissionLabel = (user) => pluralize(user.permissions?.length || 0, 'permission')

/*
 * Single piece of state for the user action menu. null → no menu open,
 * otherwise the id of the user whose menu is open. Only one dropdown can
 * ever be visible, and the menu itself lives in a fixed layer so no
 * table/card overflow context can clip it.
 */
const openUserMenuId = ref(null)
const menuTriggerEl = ref(null)
const anchorRect = ref(null)
const menuOpensUpward = ref(false)
const openMenuItemsEl = ref(null)

const menuUser = computed(() => users.value.find((user) => user.id === openUserMenuId.value) || null)

const menuStyle = computed(() => {
  const rect = anchorRect.value

  if (!rect) {
    return { display: 'none' }
  }

  const right = Math.max(8, Math.round(window.innerWidth - rect.right))

  return menuOpensUpward.value
    ? { right: `${right}px`, bottom: `${Math.round(window.innerHeight - rect.top) + 6}px` }
    : { right: `${right}px`, top: `${Math.round(rect.bottom) + 6}px` }
})

const toggleUserMenu = (user, event) => {
  event.stopPropagation()

  if (openUserMenuId.value === user.id) {
    openUserMenuId.value = null
    return
  }

  menuTriggerEl.value = event.currentTarget
  openUserMenuId.value = user.id
}

const refreshAnchorRect = () => {
  const el = menuTriggerEl.value

  if (!el || !el.isConnected || el.offsetParent === null) {
    openUserMenuId.value = null
    return
  }

  anchorRect.value = el.getBoundingClientRect()
}

/*
 * The menu normally opens below its trigger. When there is not enough room
 * near the bottom of the viewport, it flips above the trigger instead.
 */
const updateMenuPlacement = () => {
  const el = openMenuItemsEl.value
  const rect = anchorRect.value

  if (!el || !rect) {
    menuOpensUpward.value = false
    return
  }

  const menuHeight = el.getBoundingClientRect().height
  const spaceBelow = window.innerHeight - rect.bottom
  const spaceAbove = rect.top
  const viewportMargin = 16

  menuOpensUpward.value = spaceBelow < menuHeight + viewportMargin && spaceAbove > spaceBelow
}

const focusFirstMenuItem = () => {
  openMenuItemsEl.value?.querySelector('[role="menuitem"]')?.focus()
}

const onMenuKeydown = (event) => {
  if (!['ArrowDown', 'ArrowUp', 'Home', 'End'].includes(event.key)) {
    return
  }

  const items = Array.from(event.currentTarget.querySelectorAll('[role="menuitem"]'))

  if (items.length === 0) {
    return
  }

  event.preventDefault()

  const current = items.indexOf(document.activeElement)

  if (event.key === 'Home') {
    items[0].focus()
    return
  }

  if (event.key === 'End') {
    items[items.length - 1].focus()
    return
  }

  if (event.key === 'ArrowDown') {
    items[(current + 1) % items.length].focus()
    return
  }

  items[(current <= 0 ? items.length : current) - 1].focus()
}

useEscapeDismiss(
  () => openUserMenuId.value !== null,
  () => {
    const trigger = menuTriggerEl.value
    openUserMenuId.value = null
    if (trigger instanceof HTMLElement) {
      trigger.focus()
    }
  },
  ESCAPE_PRIORITY.DROPDOWN,
)

watch(openUserMenuId, (id) => {
  if (id === null) {
    menuOpensUpward.value = false
    anchorRect.value = null
    menuTriggerEl.value = null
  } else {
    menuOpensUpward.value = false
    refreshAnchorRect()
    nextTick(() => {
      focusFirstMenuItem()
      refreshAnchorRect()
      updateMenuPlacement()
    })
  }
})

watch([users, loading], () => {
  openUserMenuId.value = null
})

const runMenuAction = (event, action) => {
  event.stopPropagation()

  try {
    action()
  } finally {
    openUserMenuId.value = null
  }
}

const openUserDetails = (user) => {
  if (openUserMenuId.value !== null) {
    openUserMenuId.value = null
    return
  }

  emit('view', user.id)
}

const viewUser = (user) => {
  openUserMenuId.value = null
  emit('view', user.id)
}

const managePermissions = (user) => {
  openUserMenuId.value = null
  emit('permissions', user)
}

const openDeleteConfirmation = (user) => {
  userToDelete.value = user
  deleteError.value = ''
  showDeleteModal.value = true
}

const closeDeleteConfirmation = () => {
  if (deleting.value) {
    return
  }

  showDeleteModal.value = false
  userToDelete.value = null
  deleteError.value = ''
}

const deleteUser = async () => {
  const user = userToDelete.value

  if (!user || deleting.value) {
    return
  }

  deleting.value = true
  deleteError.value = ''

  try {
    await userService.deleteUser(user.id)
    showDeleteModal.value = false
    userToDelete.value = null
    await fetchUsers()
  } catch (err) {
    console.error('Failed to delete user:', err)
    deleteError.value = err.response?.data?.message || 'Failed to delete user.'
  } finally {
    deleting.value = false
  }
}

onBeforeUnmount(() => {
  clearTimeout(searchTimeout)
})

onMounted(() => {
  fetchUsers()
})
</script>

<template>
  <section class="directory-workspace user-directory">
    <!-- Toolbar -->
    <div class="directory-toolbar table-toolbar user-table-toolbar">
      <div v-if="isSuperAdmin" class="input-group table-search user-search">
        <span class="input-group-text">
          <i class="bi bi-search"></i>
        </span>

        <input
          v-model="search"
          type="text"
          class="form-control"
          placeholder="Search users..."
          aria-label="Search users"
        />
      </div>

      <p v-if="totalElements > 0" class="directory-summary">
        <template v-if="summaryCounts">
          <strong>{{ pluralize(summaryCounts.total, 'user') }}</strong>
          <span class="directory-summary-sep">·</span>
          {{ summaryCounts.active }} active
          <span class="directory-summary-sep">·</span>
          {{ pluralize(summaryCounts.admins, 'administrator') }}
        </template>

        <template v-else>
          <strong>{{ pluralize(totalElements, 'user') }}</strong>
        </template>
      </p>
    </div>

    <!-- Error -->
    <div v-if="error" class="alert alert-danger" role="alert">
      {{ error }}
    </div>

    <!-- Loading -->
    <div v-if="loading" class="directory-loading">
      <div class="spinner-border text-primary mb-2" role="status">
        <span class="visually-hidden"> Loading... </span>
      </div>
      <div class="text-muted small">Loading users...</div>
    </div>

    <!-- Directory -->
    <div v-else-if="hasAccess" class="directory-panel user-table-panel">
      <!-- Empty -->
      <div v-if="users.length === 0" class="users-empty">
        <span class="users-empty-icon"><i class="bi bi-people"></i></span>

        <h6>{{ hasSearchQuery ? 'No users found' : 'No users yet' }}</h6>

        <p>
          {{
            hasSearchQuery
              ? 'Try changing your search term to find what you are looking for.'
              : 'Create your first application user to begin managing access.'
          }}
        </p>

        <button
          v-if="!hasSearchQuery && isSuperAdmin"
          type="button"
          class="btn btn-primary"
          @click="emit('create')"
        >
          <i class="bi bi-plus-lg"></i>
          <span>Add User</span>
        </button>
      </div>

      <template v-else>
        <!-- Desktop directory -->
        <div class="table-responsive desktop-user-table">
          <table class="directory-table table table-hover align-middle mb-0 user-data-table">
            <colgroup>
              <col class="user-col-name" />
              <col class="user-col-email" />
              <col class="user-col-role" />
              <col class="user-col-permissions" />
              <col class="user-col-status" />
              <col class="user-col-actions" />
            </colgroup>

            <thead>
              <tr>
                <th scope="col">Name</th>
                <th scope="col">Email</th>
                <th scope="col">User Type</th>
                <th scope="col">Permissions</th>
                <th scope="col">Status</th>
                <th scope="col" class="text-end">Actions</th>
              </tr>
            </thead>

            <tbody>
              <tr
                v-for="user in users"
                :key="user.id"
                class="user-row"
                :class="{ 'menu-open': openUserMenuId === user.id }"
                @click="openUserDetails(user)"
              >
                <td class="user-cell-name">{{ user.name }}</td>

                <td class="user-cell-email">{{ user.email }}</td>

                <td>
                  <span
                    class="role-chip"
                    :class="{ 'role-chip-admin': user.userType === 'SUPER_ADMIN' }"
                  >
                    {{ humanizeLabel(user.userType) }}
                  </span>
                </td>

                <td>
                  <button
                    type="button"
                    class="permission-summary permission-summary-link"
                    :aria-label="`Manage permissions for ${user.name}`"
                    title="Manage permissions"
                    @click.stop="managePermissions(user)"
                  >
                    <i class="bi bi-shield-check"></i>
                    <span>{{ permissionLabel(user) }}</span>
                  </button>
                </td>

                <td>
                  <span
                    class="user-status"
                    :class="{ 'is-active': user.status === 'ACTIVE' }"
                  >
                    {{ humanizeLabel(user.status) }}
                  </span>
                </td>

                <td class="text-end">
                  <div class="user-row-actions">
                    <button
                      type="button"
                      class="user-view-btn"
                      :aria-label="`View ${user.name}`"
                      title="View user"
                      @click.stop="viewUser(user)"
                    >
                      <span>View</span>
                    </button>

                    <button
                      type="button"
                      class="user-menu-trigger"
                      aria-haspopup="menu"
                      :aria-expanded="openUserMenuId === user.id"
                      :aria-label="`User actions for ${user.name}`"
                      title="More actions"
                      @click.stop="toggleUserMenu(user, $event)"
                    >
                      <i class="bi bi-three-dots"></i>
                    </button>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>

        <!-- Mobile directory -->
        <div class="mobile-user-list" role="list" aria-label="Users">
          <article
            v-for="user in users"
            :key="user.id"
            class="mobile-user-row"
            :class="{ 'menu-open': openUserMenuId === user.id }"
            role="listitem"
            @click="openUserDetails(user)"
          >
            <div class="mobile-user-identity">
              <div class="mobile-user-info">
                <div class="mobile-user-name">{{ user.name }}</div>
                <div class="mobile-user-email">{{ user.email }}</div>
              </div>

              <button
                type="button"
                class="mobile-user-menu-trigger"
                aria-haspopup="menu"
                :aria-expanded="openUserMenuId === user.id"
                :aria-label="`User actions for ${user.name}`"
                title="More actions"
                @click.stop="toggleUserMenu(user, $event)"
              >
                <i class="bi bi-three-dots"></i>
              </button>
            </div>

            <div class="mobile-user-details">
              <div class="mobile-user-meta">
                <span
                  class="role-chip"
                  :class="{ 'role-chip-admin': user.userType === 'SUPER_ADMIN' }"
                >
                  {{ humanizeLabel(user.userType) }}
                </span>

                <span class="user-status" :class="{ 'is-active': user.status === 'ACTIVE' }">
                  {{ humanizeLabel(user.status) }}
                </span>
              </div>

              <button
                type="button"
                class="permission-summary permission-summary-link mobile-user-permissions"
                :aria-label="`Manage permissions for ${user.name}`"
                title="Manage permissions"
                @click.stop="managePermissions(user)"
              >
                <i class="bi bi-shield-check"></i>
                <span>{{ permissionLabel(user) }}</span>
              </button>
            </div>
          </article>
        </div>
      </template>
    </div>

    <!-- Shared user action menu (fixed layer) -->
    <div
      v-if="menuUser"
      id="user-action-menu"
      ref="openMenuItemsEl"
      class="user-action-menu"
      :style="menuStyle"
      role="menu"
      :aria-label="`Actions for ${menuUser.name}`"
      @click.stop
      @keydown="onMenuKeydown"
    >
      <button
        type="button"
        class="user-menu-item user-menu-item-view"
        role="menuitem"
        @click="runMenuAction($event, () => viewUser(menuUser))"
      >
        <i class="bi bi-eye"></i>
        <span>View</span>
      </button>

      <button
        type="button"
        class="user-menu-item"
        role="menuitem"
        @click="runMenuAction($event, () => emit('edit', menuUser))"
      >
        <i class="bi bi-pencil"></i>
        <span>Edit</span>
      </button>

      <button
        type="button"
        class="user-menu-item"
        role="menuitem"
        @click="runMenuAction($event, () => emit('permissions', menuUser))"
      >
        <i class="bi bi-shield-lock"></i>
        <span>Manage permissions</span>
      </button>

      <button
        type="button"
        class="user-menu-item"
        role="menuitem"
        @click="runMenuAction($event, () => emit('toggle-status', menuUser))"
      >
        <i :class="menuUser.status === 'ACTIVE' ? 'bi bi-person-slash' : 'bi bi-person-check'"></i>
        <span>{{ menuUser.status === 'ACTIVE' ? 'Disable user' : 'Enable user' }}</span>
      </button>

      <button
        type="button"
        class="user-menu-item user-menu-item-danger"
        role="menuitem"
        @click="runMenuAction($event, () => openDeleteConfirmation(menuUser))"
      >
        <i class="bi bi-trash"></i>
        <span>Delete</span>
      </button>
    </div>

    <AppPagination
      :current-page="currentPage"
      :page-size="pageSize"
      :total-pages="totalPages"
      :total-elements="totalElements"
      noun="users"
      @page-change="goToPage"
      @page-size-change="(size) => { pageSize = size; changePageSize() }"
    />

    <ConfirmActionModal
      :show="showDeleteModal"
      title="Delete user?"
      message-prefix="Are you sure you want to delete"
      :subject="userToDelete?.name || ''"
      warning="This action cannot be undone."
      confirm-label="Delete User"
      loading-label="Deleting..."
      :loading="deleting"
      :error="deleteError"
      @close="closeDeleteConfirmation"
      @confirm="deleteUser"
    />
  </section>
</template>

<style scoped>
.mobile-user-list {
  display: none;
}

.directory-workspace { display: grid; gap: .75rem; }
.directory-toolbar { display: flex; flex-wrap: wrap; align-items: center; gap: 12px 16px; padding: 0 0 .75rem; border: 0; background: transparent; }
.directory-panel { overflow: hidden; border: 1px solid var(--color-border); background: var(--color-surface); }
.directory-loading { padding: 3.5rem 1rem; border: 1px solid var(--color-border); background: var(--color-surface); text-align: center; }
.directory-table thead th { padding: .7rem 1rem; }
.directory-table tbody td { padding: 10px 16px; }

.directory-summary {
  margin: 0 0 0 auto;
  color: var(--color-text-secondary);
  font-size: .76rem;
}

.directory-summary:first-child {
  margin-left: 0;
}

.directory-summary strong {
  color: var(--color-text);
  font-weight: 700;
}

.directory-summary-sep {
  margin: 0 .35rem;
  color: var(--color-text-tertiary);
}

.permission-summary {
  display: inline-flex;
  align-items: center;
  gap: .35rem;
  color: var(--color-text-secondary);
  font-size: .8rem;
}

.permission-summary i {
  color: var(--color-primary-text);
}

.permission-summary-link {
  padding: 5px 8px;
  border: 0;
  border-radius: var(--radius-sm);
  background: transparent;
  font-weight: 650;
  cursor: pointer;
  transition:
    background .15s ease,
    color .15s ease;
}

.permission-summary-link:hover {
  background: var(--color-primary-soft);
  color: var(--color-primary-text);
}

.permission-summary-link:hover i {
  color: var(--color-primary-text);
}

.user-row {
  cursor: pointer;
}

.user-row.menu-open {
  background: var(--color-primary-soft);
}

.user-cell-name {
  color: var(--color-text);
  font-size: .95rem;
  font-weight: 700;
}

.user-cell-email {
  color: var(--color-text-secondary);
  font-size: .82rem;
  font-weight: 400;
  overflow-wrap: anywhere;
}

.user-row-actions {
  display: inline-flex;
  align-items: center;
  justify-content: flex-end;
  gap: 4px;
}

.user-view-btn,
.user-menu-trigger,
.mobile-user-menu-trigger {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border-radius: var(--radius-sm);
  cursor: pointer;
  transition:
    background .15s ease,
    color .15s ease,
    border-color .15s ease;
}

.user-view-btn {
  gap: 6px;
  height: 36px;
  padding: 0 10px;
  border: 1px solid var(--color-border);
  background: var(--color-surface);
  color: var(--color-text-body);
  font-size: .76rem;
  font-weight: 700;
  white-space: nowrap;
}

.user-view-btn:hover {
  border-color: var(--color-primary-border);
  background: var(--color-surface-muted);
  color: var(--color-primary-text);
}

.user-menu-trigger {
  width: 36px;
  height: 36px;
  padding: 0;
  border: 1px solid transparent;
  background: transparent;
  color: var(--color-text-secondary);
  font-size: 1.05rem;
}

.user-menu-trigger:hover {
  background: var(--color-hover-strong);
  color: var(--color-text-body);
}

.user-view-btn:focus-visible,
.user-menu-trigger:focus-visible,
.mobile-user-menu-trigger:focus-visible,
.permission-summary-link:focus-visible {
  outline: 2px solid var(--color-primary-text);
  outline-offset: 2px;
}

.user-action-menu {
  position: fixed;
  z-index: 1040;
  display: flex;
  width: max-content;
  max-width: min(240px, calc(100vw - 24px));
  flex-direction: column;
  padding: .3rem;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-sm);
  background: var(--color-surface);
  box-shadow: var(--shadow-menu);
}

.user-menu-item {
  display: flex;
  min-height: 42px;
  align-items: center;
  gap: .6rem;
  padding: 0 .7rem;
  border: 0;
  border-radius: 4px;
  background: transparent;
  color: var(--color-text-body);
  font-size: .85rem;
  font-weight: 600;
  text-align: left;
  white-space: nowrap;
  cursor: pointer;
}

.user-menu-item:hover {
  background: var(--color-hover-strong);
}

.user-menu-item:focus-visible {
  background: var(--color-hover-strong);
  outline: 2px solid var(--color-primary-text);
  outline-offset: -2px;
}

.user-menu-item i {
  width: 1rem;
  color: var(--color-text-secondary);
  font-size: .95rem;
  text-align: center;
}

.user-menu-item-danger,
.user-menu-item-danger i {
  color: var(--color-danger-text);
}

.user-menu-item-view {
  display: none;
}

.users-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 3.25rem 1.5rem;
  text-align: center;
}

.users-empty-icon {
  display: inline-flex;
  width: 52px;
  height: 52px;
  align-items: center;
  justify-content: center;
  margin-bottom: .7rem;
  border-radius: 50%;
  background: var(--color-primary-soft);
  color: var(--color-primary-text);
  font-size: 1.3rem;
}

.users-empty h6 {
  margin: 0;
  color: var(--color-text);
  font-size: 1rem;
  font-weight: 700;
}

.users-empty p {
  max-width: 44ch;
  margin: .35rem 0 0;
  color: var(--color-text-secondary);
  font-size: .85rem;
  line-height: 1.5;
}

.users-empty .btn {
  margin-top: 1rem;
}

@media (min-width: 768px) {
  .user-table-panel .table-responsive { overflow-x: auto; }
  .user-table-panel .directory-table { min-width: 920px; table-layout: fixed; }
  .user-table-panel .directory-table tbody tr { transition: background .15s ease; }
  .user-table-panel .directory-table tbody tr:hover { background: var(--color-hover); }
  .user-table-panel .directory-table tbody tr:last-child td { border-bottom: 0; }
  .user-table-panel .directory-table td:first-child { padding-left: 20px; }
  .user-col-name { width: 25%; }
  .user-col-email { width: 24%; }
  .user-col-role { width: 14%; }
  .user-col-permissions { width: 14%; }
  .user-col-status { width: 9%; }
  .user-col-actions { width: 14%; }
}

@media (max-width: 767.98px) {
  .directory-toolbar {
    gap: 10px;
  }

  .directory-summary {
    margin-left: 0;
  }

  .directory-toolbar { border-radius: 0; }

  .user-search {
    width: 100%;
  }

  /*
   * The panel would otherwise clip content that spills out of a row.
   * The action menu lives in a fixed layer, so only the panel itself
   * needs to stay open for the mobile list borders.
   */
  .user-table-panel {
    overflow: visible;
  }

  .desktop-user-table {
    display: none;
  }

  .mobile-user-list {
    display: block;
    width: 100%;
  }

  .user-menu-item-view {
    display: flex;
  }

  .mobile-user-row {
    position: relative;
    z-index: 1;
    display: flex;
    flex-direction: column;
    gap: 14px;
    padding: 18px 16px;
    border-bottom: 1px solid var(--color-border);
    background: var(--color-surface);
    cursor: pointer;
    transition: background .15s ease;
  }

  .mobile-user-row.menu-open {
    z-index: 100;
    background: var(--color-primary-soft);
  }

  .mobile-user-row:last-of-type {
    border-bottom: 0;
  }

  .mobile-user-row:hover {
    background: var(--color-hover);
  }

  .mobile-user-row:active {
    background: var(--color-surface-muted);
  }

  .mobile-user-identity {
    display: flex;
    align-items: flex-start;
    gap: 12px;
    min-width: 0;
  }

  .mobile-user-info {
    flex: 1 1 auto;
    min-width: 0;
    padding-top: 1px;
  }

  .mobile-user-name {
    color: var(--color-text);
    font-size: 1rem;
    font-weight: 700;
    line-height: 1.3;
    overflow-wrap: anywhere;
  }

  .mobile-user-email {
    margin-top: 3px;
    color: var(--color-text-secondary);
    font-size: 0.8125rem;
    line-height: 1.4;
    overflow-wrap: anywhere;
  }

  .mobile-user-details {
    display: flex;
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
    min-width: 0;
  }

  .mobile-user-meta {
    display: flex;
    flex-wrap: wrap;
    align-items: center;
    gap: 0.45rem;
  }

  .mobile-user-permissions {
    margin-left: -8px;
  }

  .mobile-user-menu-trigger {
    flex: 0 0 auto;
    width: 42px;
    height: 42px;
    padding: 0;
    border: 1px solid transparent;
    background: transparent;
    color: var(--color-text-secondary);
    font-size: 1.1rem;
  }

  .mobile-user-menu-trigger:hover {
    background: var(--color-hover-strong);
    color: var(--color-text-body);
  }
}
</style>
