<script setup>
import { ref, onMounted, watch } from 'vue'
import { userService } from '@/features/users/services/userService'
import ConfirmActionModal from '@/components/common/ConfirmActionModal.vue'

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

const emit = defineEmits(['edit', 'view', 'delete', 'permissions', 'toggle-status'])

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

onMounted(() => {
  fetchUsers()
})

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

const runMobileAction = (event, action) => {
  event.currentTarget.closest('details')?.removeAttribute('open')
  action()
}

defineProps({
  isSuperAdmin: {
    type: Boolean,
    default: false,
  },
})
</script>

<template>
  <section class="directory-workspace user-directory">
    <!-- Search and page size -->
    <div v-if="isSuperAdmin" class="directory-toolbar table-toolbar user-table-toolbar">
      <div class="input-group table-search user-search">
        <span class="input-group-text">
          <i class="bi bi-search"></i>
        </span>

        <input v-model="search" type="text" class="form-control" placeholder="Search users..." />
      </div>

      <div class="table-page-size-control d-flex align-items-center gap-2">
        <span class="text-muted small">Show</span>

        <select v-model="pageSize" class="form-select table-page-size" @change="changePageSize">
          <option :value="5">5</option>
          <option :value="10">10</option>
          <option :value="20">20</option>
          <option :value="50">50</option>
        </select>

        <span class="text-muted small">users</span>
      </div>
    </div>

    <!-- Error -->
    <div v-if="error" class="alert alert-danger">
      {{ error }}
    </div>

    <!-- Loading -->
    <div v-if="loading" class="directory-loading">
      <div class="spinner-border text-primary mb-2" role="status">
        <span class="visually-hidden"> Loading... </span>
      </div>
      <div class="text-muted small">Loading users...</div>
    </div>

    <!-- Table -->
    <div v-else-if="hasAccess" class="directory-panel user-table-panel">
      <div class="table-responsive desktop-user-table">
        <table class="directory-table table table-hover align-middle mb-0 user-data-table">
          <thead>
            <tr>
              <th>Name</th>
              <th>Email</th>
              <th>User Type</th>
              <th>Permissions</th>
              <th>Status</th>
              <th class="text-end">Actions</th>
            </tr>
          </thead>

          <tbody>
            <tr v-for="user in users" :key="user.id">
              <td>
                <strong>
                  {{ user.name }}
                </strong>
              </td>

              <td>
                {{ user.email }}
              </td>

              <td>
                <span class="badge text-bg-secondary">
                  {{ user.userType }}
                </span>
              </td>

              <td>
                <span class="permission-summary">
                  <i class="bi bi-shield-check"></i>
                  {{ user.permissions?.length || 0 }} permissions
                </span>
              </td>

              <td>
                <span
                  class="badge"
                  :class="user.status === 'ACTIVE' ? 'text-bg-success' : 'text-bg-secondary'"
                >
                  {{ user.status }}
                </span>
              </td>

              <td class="text-end">
                <div class="table-action-group" role="group">
                  <button
                    class="btn btn-sm btn-outline-secondary"
                    title="View"
                    @click="emit('view', user.id)"
                  >
                    <i class="bi bi-eye"></i>
                  </button>

                  <button
                    class="btn btn-sm btn-outline-primary"
                    title="Edit"
                    @click="emit('edit', user)"
                  >
                    <i class="bi bi-pencil"></i>
                  </button>

                  <button
                    class="btn btn-sm btn-outline-warning"
                    title="Permissions"
                    @click="emit('permissions', user)"
                  >
                    <i class="bi bi-shield-lock"></i>
                  </button>

                  <button
                    class="btn btn-sm btn-outline-danger"
                    title="Delete"
                    @click="openDeleteConfirmation(user)"
                  >
                    <i class="bi bi-trash"></i>
                  </button>
                  <button
                    v-if="user.status === 'ACTIVE'"
                    class="btn btn-sm btn-outline-warning"
                    title="Disable user"
                    @click="$emit('toggle-status', user)"
                  >
                    <i class="bi bi-person-slash"></i>
                  </button>

                  <button
                    v-else
                    class="btn btn-sm btn-outline-success"
                    title="Enable user"
                    @click="$emit('toggle-status', user)"
                  >
                    <i class="bi bi-person-check"></i>
                  </button>
                </div>
              </td>
            </tr>

            <tr v-if="users.length === 0">
              <td colspan="6" class="empty-state">
                <i class="bi bi-people"></i>
                <h6 class="mb-1">No users found</h6>
                <p class="mb-0">{{ search ? 'No users match your search.' : 'There are no users to display.' }}</p>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <div class="mobile-user-list" role="list" aria-label="Users">
        <article v-for="user in users" :key="user.id" class="mobile-user-row" role="listitem">
          <div class="mobile-user-content">
            <div class="mobile-user-name">{{ user.name }}</div>
            <div class="mobile-user-email">{{ user.email }}</div>

            <div class="mobile-user-meta">
              <span class="badge text-bg-secondary">{{ user.userType }}</span>
              <span
                class="badge"
                :class="user.status === 'ACTIVE' ? 'text-bg-success' : 'text-bg-secondary'"
              >
                {{ user.status }}
              </span>
              <span class="mobile-user-permissions">
                {{ user.permissions?.length || 0 }}
                {{ user.permissions?.length === 1 ? 'permission' : 'permissions' }}
              </span>
            </div>
          </div>

          <div class="mobile-user-actions">
            <button
              type="button"
              class="btn btn-sm btn-outline-secondary mobile-user-view"
              :aria-label="`View ${user.name}`"
              title="View user"
              @click="emit('view', user.id)"
            >
              <i class="bi bi-eye"></i>
            </button>

            <details class="mobile-user-actions-menu">
              <summary :aria-label="`More actions for ${user.name}`" title="More actions">
                <i class="bi bi-three-dots"></i>
                <span class="visually-hidden">More actions</span>
              </summary>
              <div class="mobile-user-menu-items">
                <button
                  type="button"
                  class="mobile-user-menu-item"
                  @click="runMobileAction($event, () => emit('edit', user))"
                >
                  <i class="bi bi-pencil"></i>
                  Edit
                </button>
                <button
                  type="button"
                  class="mobile-user-menu-item"
                  @click="runMobileAction($event, () => emit('permissions', user))"
                >
                  <i class="bi bi-shield-lock"></i>
                  Manage permissions
                </button>
                <button
                  type="button"
                  class="mobile-user-menu-item"
                  @click="runMobileAction($event, () => emit('toggle-status', user))"
                >
                  <i
                    :class="user.status === 'ACTIVE' ? 'bi bi-person-slash' : 'bi bi-person-check'"
                  ></i>
                  {{ user.status === 'ACTIVE' ? 'Disable user' : 'Enable user' }}
                </button>
                <button
                  type="button"
                  class="mobile-user-menu-item mobile-user-delete"
                  @click="runMobileAction($event, () => openDeleteConfirmation(user))"
                >
                  <i class="bi bi-trash"></i>
                  Delete
                </button>
              </div>
            </details>
          </div>
        </article>

        <div v-if="users.length === 0" class="empty-state mobile-user-empty">
          <i class="bi bi-people"></i>
          <h6 class="mb-1">No users found</h6>
          <p class="mb-0">{{ search ? 'No users match your search.' : 'There are no users to display.' }}</p>
        </div>
      </div>
    </div>

    <!-- Pagination -->
    <div v-if="totalPages > 0" class="table-pagination user-table-pagination">
      <div class="text-muted small">Showing {{ currentPage * pageSize + 1 }}–{{ Math.min((currentPage + 1) * pageSize, totalElements) }} of {{ totalElements }} users</div>

      <nav>
        <ul class="pagination pagination-sm mb-0">
          <!-- Previous -->
          <li class="page-item" :class="{ disabled: currentPage === 0 }">
            <button
              class="page-link"
              :disabled="currentPage === 0"
              @click="goToPage(currentPage - 1)"
            >
              Previous
            </button>
          </li>

          <!-- Pages -->
          <li
            v-for="page in totalPages"
            :key="page"
            class="page-item"
            :class="{ active: currentPage === page - 1 }"
          >
            <button class="page-link" @click="goToPage(page - 1)">
              {{ page }}
            </button>
          </li>

          <!-- Next -->
          <li
            class="page-item"
            :class="{
              disabled: currentPage === totalPages - 1,
            }"
          >
            <button
              class="page-link"
              :disabled="currentPage === totalPages - 1"
              @click="goToPage(currentPage + 1)"
            >
              Next
            </button>
          </li>
        </ul>
      </nav>
    </div>

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
.table th {
  white-space: nowrap;
}

.pagination {
  margin-bottom: 0;
}

.mobile-user-list {
  display: none;
}

.directory-workspace { display: grid; gap: .75rem; }
.directory-toolbar { padding: 0 0 .75rem; border: 0; background: transparent; }
.directory-panel { overflow: hidden; border: 1px solid var(--color-border); background: var(--color-surface); }.directory-loading { padding: 3.5rem 1rem; border: 1px solid var(--color-border); background: var(--color-surface); text-align: center; }.directory-table thead th { padding: .72rem 1rem; }.directory-table tbody td { padding: .85rem 1rem; }
.permission-summary { display: inline-flex; align-items: center; gap: .35rem; color: #526075; font-size: .8rem; }.permission-summary i { color: var(--app-primary); }

@media (min-width: 768px) {
  .user-table-panel .table-responsive { overflow-x: auto; }
  .user-table-panel .directory-table { min-width: 900px; }
  .user-table-panel .directory-table tbody tr { transition: background .15s ease; }
  .user-table-panel .directory-table tbody tr:hover { background:#f7f8fc; }
  .user-table-panel .directory-table td:first-child { padding-left: 22px; }
  .user-table-panel .directory-table td:last-child { padding-right: 22px; }
}

@media (max-width: 767.98px) {
  .user-table-toolbar {
    align-items: stretch;
    flex-direction: column;
  }

  .directory-toolbar { border-radius: 0; }

  .user-search {
    width: 100%;
  }

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

  .mobile-user-row {
    position: relative;
    display: grid;
    grid-template-columns: minmax(0, 1fr) auto;
    align-items: center;
    gap: 0.65rem;
    min-height: 82px;
    padding: 0.75rem 0.8rem;
    border-bottom: 1px solid #e9edf2;
    background: #fff;
  }

  .mobile-user-row:last-of-type {
    border-bottom: 0;
  }

  .mobile-user-content {
    min-width: 0;
  }

  .mobile-user-name {
    color: #1f2937;
    font-size: 0.85rem;
    font-weight: 650;
    line-height: 1.3;
    overflow-wrap: anywhere;
  }

  .mobile-user-email {
    margin-top: 0.15rem;
    color: #687385;
    font-size: 0.72rem;
    line-height: 1.3;
    overflow-wrap: anywhere;
  }

  .mobile-user-meta {
    display: flex;
    flex-wrap: wrap;
    align-items: center;
    gap: 0.35rem;
    margin-top: 0.4rem;
  }

  .mobile-user-meta .badge {
    padding: 0.28em 0.5em;
    font-size: 0.63rem;
    font-weight: 600;
  }

  .mobile-user-permissions {
    color: #687385;
    font-size: 0.68rem;
    white-space: nowrap;
  }

  .mobile-user-actions {
    display: flex;
    align-items: center;
    gap: 0.25rem;
  }

  .mobile-user-view,
  .mobile-user-actions-menu summary {
    display: inline-flex;
    width: 34px;
    height: 34px;
    align-items: center;
    justify-content: center;
    padding: 0;
    border-radius: 5px;
  }

  .mobile-user-actions-menu {
    position: relative;
  }

  .mobile-user-actions-menu summary {
    border: 1px solid #e1e5eb;
    background: #fff;
    color: #596579;
    cursor: pointer;
    list-style: none;
  }

  .mobile-user-actions-menu summary::-webkit-details-marker {
    display: none;
  }

  .mobile-user-actions-menu summary:hover {
    background: #f6f8fb;
    color: #344054;
  }

  .mobile-user-actions-menu summary:focus-visible {
    outline: 2px solid #2457a6;
    outline-offset: 2px;
  }

  .mobile-user-menu-items {
    position: absolute;
    top: calc(100% + 0.35rem);
    right: 0;
    z-index: 10;
    display: flex;
    width: max-content;
    max-width: min(220px, calc(100vw - 110px));
    flex-direction: column;
    padding: 0.3rem;
    border: 1px solid #e1e5eb;
    border-radius: 6px;
    background: #fff;
    box-shadow: 0 6px 18px rgba(31, 41, 55, 0.13);
  }

  .mobile-user-menu-item {
    display: flex;
    align-items: center;
    gap: 0.55rem;
    padding: 0.55rem 0.65rem;
    border: 0;
    border-radius: 4px;
    background: transparent;
    color: #354052;
    font-size: 0.8rem;
    text-align: left;
    white-space: nowrap;
  }

  .mobile-user-menu-item:hover,
  .mobile-user-menu-item:focus-visible {
    background: #f4f6f8;
  }

  .mobile-user-menu-item i {
    width: 1rem;
    color: #687385;
    text-align: center;
  }

  .mobile-user-menu-item.mobile-user-delete,
  .mobile-user-menu-item.mobile-user-delete i {
    color: #b02a37;
  }

  .mobile-user-empty {
    padding: 2rem 1rem;
  }

  .user-table-pagination {
    align-items: stretch;
    flex-direction: column;
    gap: 0.7rem;
    padding: 0.75rem 0.8rem;
  }

  .user-table-pagination nav,
  .user-table-pagination .pagination {
    width: 100%;
  }

  .user-table-pagination .pagination {
    flex-wrap: wrap;
    gap: 0.2rem;
  }

  .user-table-pagination .page-link {
    display: flex;
    min-width: 34px;
    min-height: 34px;
    align-items: center;
    justify-content: center;
    padding: 0.3rem 0.5rem;
    font-size: 0.8rem;
  }
}
</style>
