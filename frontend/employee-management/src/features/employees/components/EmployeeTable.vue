<script setup>
import { onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { employeeService } from '../services/employeeService'
import { useAuthStore } from '@/features/auth/stores/authStore'
import ConfirmActionModal from '@/components/common/ConfirmActionModal.vue'

const emit = defineEmits(['view', 'edit'])
const authStore = useAuthStore()

const employees = ref([])
const loading = ref(false)
const error = ref('')
const search = ref('')
const currentPage = ref(0)
const pageSize = ref(10)
const totalPages = ref(0)
const totalElements = ref(0)
const showDeleteModal = ref(false)
const employeeToDelete = ref(null)
const deleting = ref(false)
const deleteError = ref('')

let searchTimeout = null

const fetchEmployees = async () => {
  if (!authStore.hasPermission('READ')) {
    employees.value = []
    totalElements.value = 0
    totalPages.value = 0
    return
  }

  loading.value = true
  error.value = ''

  try {
    const response = await employeeService.getEmployees(
      currentPage.value,
      pageSize.value,
      search.value,
    )
    const data = response.data

    if (Array.isArray(data)) {
      employees.value = data
      totalElements.value = data.length
      totalPages.value = data.length > 0 ? 1 : 0
    } else {
      employees.value = data?.content || []
      totalElements.value = data?.totalElements || 0
      totalPages.value = data?.totalPages || 0
    }
  } catch (err) {
    console.error('Failed to fetch employees:', err)
    error.value = err.response?.data?.message || 'Failed to load employees.'
    employees.value = []
  } finally {
    loading.value = false
  }
}

const goToPage = (page) => {
  if (page < 0 || page >= totalPages.value || page === currentPage.value) {
    return
  }

  currentPage.value = page
  fetchEmployees()
}

const changePageSize = () => {
  currentPage.value = 0
  fetchEmployees()
}

watch(search, () => {
  clearTimeout(searchTimeout)
  searchTimeout = setTimeout(() => {
    currentPage.value = 0
    fetchEmployees()
  }, 400)
})

const openDeleteConfirmation = (employee) => {
  employeeToDelete.value = employee
  deleteError.value = ''
  showDeleteModal.value = true
}

const closeDeleteConfirmation = () => {
  if (deleting.value) {
    return
  }

  showDeleteModal.value = false
  employeeToDelete.value = null
  deleteError.value = ''
}

const deleteEmployee = async () => {
  const employee = employeeToDelete.value

  if (!employee || deleting.value) {
    return
  }

  deleting.value = true
  deleteError.value = ''

  try {
    await employeeService.deleteEmployee(employee.id)
    showDeleteModal.value = false
    employeeToDelete.value = null
    await fetchEmployees()
  } catch (err) {
    console.error('Failed to delete employee:', err)
    deleteError.value = err.response?.data?.message || 'Failed to delete employee.'
  } finally {
    deleting.value = false
  }
}

defineExpose({ fetchEmployees })

onBeforeUnmount(() => {
  clearTimeout(searchTimeout)
})

onMounted(() => {
  fetchEmployees()
})
</script>

<template>
  <section class="directory-workspace">
    <div class="directory-toolbar table-toolbar employee-table-toolbar">
      <div class="input-group table-search employee-search">
        <span class="input-group-text">
          <i class="bi bi-search"></i>
        </span>
        <input
          v-model="search"
          type="text"
          class="form-control"
          placeholder="Search employees..."
          aria-label="Search employees"
          :disabled="loading"
        />
        <button
          v-if="search"
          type="button"
          class="btn btn-outline-secondary"
          aria-label="Clear employee search"
          @click="search = ''"
        >
          <i class="bi bi-x-lg"></i>
        </button>
      </div>

      <div class="employee-page-size-control d-flex align-items-center gap-2">
        <span class="text-muted small">Show</span>
        <select
          v-model.number="pageSize"
          class="form-select table-page-size"
          aria-label="Employees per page"
          :disabled="loading"
          @change="changePageSize"
        >
          <option :value="5">5</option>
          <option :value="10">10</option>
          <option :value="20">20</option>
          <option :value="50">50</option>
        </select>
        <span class="text-muted small">employees</span>
      </div>
    </div>

    <div v-if="loading" class="directory-loading">
      <div class="card-body py-5 text-center">
        <div class="spinner-border text-primary mb-3" role="status">
          <span class="visually-hidden">Loading...</span>
        </div>
        <div class="text-muted">Loading employees...</div>
      </div>
    </div>

    <div v-else-if="error" class="alert alert-danger" role="alert">
      <div class="d-flex align-items-center">
        <i class="bi bi-exclamation-triangle-fill me-2"></i>
        <div>{{ error }}</div>
      </div>
    </div>

    <div v-else class="directory-panel employee-panel">
      <div class="desktop-employee-table">
        <div class="table-responsive">
          <table class="directory-table table table-hover align-middle mb-0 employee-data-table">
            <thead>
              <tr>
                <th class="ps-3">Photo</th>
                <th>Full Name</th>
                <th>Email</th>
                <th>Job Title</th>
                <th>Department</th>
                <th>Hire Date</th>
                <th class="text-end pe-3">Actions</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="employee in employees" :key="employee.id">
                <td class="ps-3">
                  <img
                    v-if="employee.photoObjectName"
                    :src="`/api/employees/${employee.id}/photo`"
                    :alt="`${employee.firstName} ${employee.lastName}`"
                    class="employee-avatar"
                  />
                  <div v-else class="employee-avatar-placeholder">
                    <i class="bi bi-person"></i>
                  </div>
                </td>
                <td>
                  <div class="fw-semibold">{{ employee.firstName }} {{ employee.lastName }}</div>
                </td>
                <td>{{ employee.email || '—' }}</td>
                <td>{{ employee.jobTitle || '—' }}</td>
                <td>{{ employee.department || '—' }}</td>
                <td>{{ employee.hireDate || '—' }}</td>
                <td class="text-end pe-3">
                  <div
                    class="table-action-group"
                    role="group"
                    :aria-label="`Actions for ${employee.firstName} ${employee.lastName}`"
                  >
                    <button
                      v-if="authStore.hasPermission('READ')"
                      type="button"
                      class="btn btn-sm btn-outline-primary"
                      :aria-label="`View ${employee.firstName} ${employee.lastName}`"
                      title="View employee"
                      @click="emit('view', employee)"
                    >
                      <i class="bi bi-eye"></i>
                    </button>
                    <button
                      v-if="authStore.hasPermission('UPDATE')"
                      type="button"
                      class="btn btn-sm btn-outline-secondary"
                      :aria-label="`Edit ${employee.firstName} ${employee.lastName}`"
                      title="Edit employee"
                      @click="emit('edit', employee)"
                    >
                      <i class="bi bi-pencil"></i>
                    </button>
                    <button
                      v-if="authStore.hasPermission('DELETE')"
                      type="button"
                      class="btn btn-sm btn-outline-danger"
                      :aria-label="`Delete ${employee.firstName} ${employee.lastName}`"
                      title="Delete employee"
                      @click="openDeleteConfirmation(employee)"
                    >
                      <i class="bi bi-trash"></i>
                    </button>
                  </div>
                </td>
              </tr>
              <tr v-if="employees.length === 0">
                <td colspan="7" class="empty-state">
                  <i class="bi bi-people fs-1 text-muted d-block mb-3"></i>
                  <h6 class="mb-1">No employees found</h6>
                  <p class="text-muted mb-0">
                    {{
                      search
                        ? 'No employees match your search.'
                        : 'There are no employees to display.'
                    }}
                  </p>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <div class="mobile-employee-list" role="list" aria-label="Employees">
        <article
          v-for="employee in employees"
          :key="employee.id"
          class="mobile-employee-row"
          role="listitem"
        >
          <img
            v-if="employee.photoObjectName"
            :src="`/api/employees/${employee.id}/photo`"
            :alt="`${employee.firstName} ${employee.lastName}`"
            class="mobile-employee-avatar"
          />
          <div v-else class="mobile-employee-avatar mobile-employee-avatar-placeholder">
            <i class="bi bi-person"></i>
          </div>

          <div class="mobile-employee-info">
            <div class="mobile-employee-name">{{ employee.firstName }} {{ employee.lastName }}</div>
            <div class="mobile-employee-email">{{ employee.email || 'No email address' }}</div>
            <div class="mobile-employee-title">
              {{ employee.jobTitle || 'Employee' }}<span v-if="employee.department"> · {{ employee.department }}</span>
            </div>
          </div>

          <div
            class="table-action-group mobile-employee-actions"
            role="group"
            :aria-label="`Actions for ${employee.firstName} ${employee.lastName}`"
          >
            <button
              v-if="authStore.hasPermission('READ')"
              type="button"
              class="btn btn-sm btn-outline-primary"
              :aria-label="`View ${employee.firstName} ${employee.lastName}`"
              title="View employee"
              @click="emit('view', employee)"
            >
              <i class="bi bi-eye"></i>
            </button>
            <button
              v-if="authStore.hasPermission('UPDATE')"
              type="button"
              class="btn btn-sm btn-outline-secondary"
              :aria-label="`Edit ${employee.firstName} ${employee.lastName}`"
              title="Edit employee"
              @click="emit('edit', employee)"
            >
              <i class="bi bi-pencil"></i>
            </button>
            <button
              v-if="authStore.hasPermission('DELETE')"
              type="button"
              class="btn btn-sm btn-outline-danger"
              :aria-label="`Delete ${employee.firstName} ${employee.lastName}`"
              title="Delete employee"
              @click="openDeleteConfirmation(employee)"
            >
              <i class="bi bi-trash"></i>
            </button>
          </div>
        </article>

        <div v-if="employees.length === 0" class="mobile-employee-empty empty-state">
          <i class="bi bi-people"></i>
          <h6 class="mb-1">No employees found</h6>
          <p class="mb-0">
            {{ search ? 'No employees match your search.' : 'There are no employees to display.' }}
          </p>
        </div>
      </div>

      <div v-if="totalPages > 0" class="table-pagination">
        <div class="d-flex justify-content-between align-items-center w-100">
          <div class="text-muted small">
            Showing {{ currentPage * pageSize + 1 }}–{{ Math.min((currentPage + 1) * pageSize, totalElements) }} of {{ totalElements }} employees
          </div>
          <nav v-if="totalPages > 1" aria-label="Employee pagination">
            <ul class="pagination pagination-sm mb-0">
              <li class="page-item" :class="{ disabled: currentPage === 0 }">
                <button
                  type="button"
                  class="page-link"
                  :disabled="currentPage === 0"
                  @click="goToPage(currentPage - 1)"
                >
                  <i class="bi bi-chevron-left"></i>
                </button>
              </li>
              <li
                v-for="page in totalPages"
                :key="page"
                class="page-item"
                :class="{ active: currentPage === page - 1 }"
              >
                <button type="button" class="page-link" @click="goToPage(page - 1)">
                  {{ page }}
                </button>
              </li>
              <li class="page-item" :class="{ disabled: currentPage === totalPages - 1 }">
                <button
                  type="button"
                  class="page-link"
                  :disabled="currentPage === totalPages - 1"
                  @click="goToPage(currentPage + 1)"
                >
                  <i class="bi bi-chevron-right"></i>
                </button>
              </li>
            </ul>
          </nav>
        </div>
      </div>
    </div>

    <ConfirmActionModal
      :show="showDeleteModal"
      title="Delete employee?"
      message-prefix="Are you sure you want to delete"
      :subject="
        employeeToDelete ? `${employeeToDelete.firstName} ${employeeToDelete.lastName}` : ''
      "
      warning="This action cannot be undone."
      confirm-label="Delete Employee"
      loading-label="Deleting..."
      :loading="deleting"
      :error="deleteError"
      @close="closeDeleteConfirmation"
      @confirm="deleteEmployee"
    />
  </section>
</template>

<style scoped>
.employee-avatar,
.employee-avatar-placeholder {
  width: 42px;
  height: 42px;
  border-radius: 50%;
}

.employee-avatar {
  object-fit: cover;
  border: 1px solid #dee2e6;
}

.employee-avatar-placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f1f3f5;
  color: #6c757d;
  border: 1px solid #dee2e6;
}

.employee-avatar-placeholder i {
  font-size: 20px;
}

.mobile-employee-list {
  display: none;
}

.directory-workspace { display: grid; gap: .75rem; }
.directory-toolbar { padding: 0 0 .75rem; border: 0; background: transparent; }
.directory-panel { overflow: hidden; border: 1px solid var(--color-border); background: var(--color-surface); }
.directory-loading { padding: 3.5rem 1rem; border: 1px solid var(--app-border); background: var(--app-surface); text-align: center; }
.directory-table thead th { padding: .72rem 1rem; }.directory-table tbody td { padding: .85rem 1rem; }

@media (min-width: 768px) {
  .employee-panel .table-responsive { overflow-x: auto; }
  .employee-panel .directory-table { min-width: 920px; }
  .employee-panel .directory-table tbody tr { transition: background .15s ease; }
  .employee-panel .directory-table tbody tr:hover { background: #f7f8fc; }
  .employee-panel .directory-table td:first-child { padding-left: 22px; }
  .employee-panel .directory-table td:last-child { padding-right: 22px; }
  .employee-avatar,.employee-avatar-placeholder { width:42px;height:42px; }
}

@media (max-width: 767.98px) {
  .desktop-employee-table {
    display: none;
  }

  .employee-table-toolbar {
    align-items: stretch;
    flex-direction: column;
  }

  .directory-toolbar { padding: 0 0 .7rem; border-radius: 0; }
  .directory-panel { border-radius: 0; }

  .employee-search {
    width: 100%;
  }

  .employee-page-size-control {
    align-self: flex-start;
  }

  .mobile-employee-list {
    display: block;
    width: 100%;
  }

  .mobile-employee-row {
    display: grid;
    grid-template-columns: 40px minmax(0, 1fr) auto;
    align-items: center;
    gap: 0.6rem;
    min-height: 68px;
    padding: 0.7rem 0.75rem;
    border-bottom: 1px solid #e9edf2;
    background: #fff;
  }

  .mobile-employee-row:last-of-type {
    border-bottom: 0;
  }

  .mobile-employee-avatar {
    width: 40px;
    height: 40px;
    border: 1px solid #dee2e6;
    border-radius: 50%;
    object-fit: cover;
  }

  .mobile-employee-avatar-placeholder {
    display: flex;
    align-items: center;
    justify-content: center;
    background: #f1f3f5;
    color: #687385;
    font-size: 1.1rem;
  }

  .mobile-employee-info {
    min-width: 0;
  }

  .mobile-employee-name {
    color: #1f2937;
    font-size: 0.84rem;
    font-weight: 650;
    line-height: 1.3;
    overflow-wrap: anywhere;
  }

  .mobile-employee-title {
    margin-top: 0.2rem;
    color: #687385;
    font-size: 0.75rem;
    line-height: 1.3;
    overflow-wrap: anywhere;
  }

  .mobile-employee-email {
    margin-top: 0.15rem;
    color: #687385;
    font-size: 0.72rem;
    line-height: 1.3;
    overflow-wrap: anywhere;
  }

  .mobile-employee-actions {
    gap: 0.15rem;
  }

  .mobile-employee-actions .btn {
    width: 28px;
    height: 28px;
    min-height: 28px;
    border-radius: 5px !important;
  }

  .mobile-employee-actions .btn i {
    font-size: 0.78rem;
  }

  .mobile-employee-empty {
    padding: 2rem 1rem;
  }

  .table-pagination > .d-flex {
    align-items: flex-start !important;
    flex-direction: column;
    gap: 0.75rem;
  }

  .table-pagination nav,
  .table-pagination .pagination {
    width: 100%;
  }

  .table-pagination .pagination {
    flex-wrap: wrap;
    gap: 0.2rem;
  }

  .table-pagination .page-link {
    display: flex;
    min-width: 32px;
    min-height: 32px;
    align-items: center;
    justify-content: center;
    padding: 0.25rem 0.4rem;
    font-size: 0.78rem;
  }
}
</style>
