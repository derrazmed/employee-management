<template>
  <div class="container-fluid app-page">
    <!-- Page header -->
    <div class="page-heading">
      <div>
        <h1>Employees</h1>

        <p>Manage your organization's workforce.</p>
      </div>

      <!-- Add Employee -->
      <button
        v-if="canCreateEmployee"
        type="button"
        class="btn btn-primary"
        @click="openCreateModal"
      >
        <i class="bi bi-person-plus"></i>
        <span>Add Employee</span>
      </button>
    </div>

    <div v-if="accessError" class="alert alert-danger d-flex align-items-center mb-3" role="alert">
      <i class="bi bi-exclamation-triangle-fill me-2"></i>

      <span>{{ accessError }}</span>

      <button
        type="button"
        class="btn-close ms-auto"
        aria-label="Close"
        @click="accessError = ''"
      ></button>
    </div>

    <!-- Employee directory -->
    <section v-if="canReadEmployees" class="employee-directory">
      <EmployeeDirectoryToolbar
        :search="search"
        :department="department"
        :job-title="jobTitle"
        :departments="departmentOptions"
        :job-titles="jobTitleOptions"
        :view-mode="viewMode"
        @update:search="search = $event"
        @update:department="onDepartmentChange"
        @update:job-title="onJobTitleChange"
        @set-view="setViewMode"
      />

      <div v-if="hasLoaded && !error" class="directory-meta">
        <p class="directory-summary">
          <strong>{{ pluralize(totalElements, 'employee') }}</strong>

          <template v-if="hasActiveFilters">
            <span class="directory-summary-sep">·</span>
            matching current filters
          </template>
        </p>

        <button
          v-if="hasActiveFilters"
          type="button"
          class="clear-filters-btn"
          @click="clearFilters"
        >
          <i class="bi bi-funnel"></i>
          <span>Clear filters</span>
        </button>
      </div>

      <!-- Error -->
      <div v-if="error" class="employee-directory-error" role="alert">
        <span class="employee-error-icon"><i class="bi bi-exclamation-triangle"></i></span>

        <div class="employee-error-text">
          <h2>Unable to load employees.</h2>
          <p>Please try again.</p>
        </div>

        <button type="button" class="btn btn-outline-danger btn-sm" @click="fetchEmployees">
          <i class="bi bi-arrow-clockwise"></i>
          <span>Retry</span>
        </button>
      </div>

      <!-- Directory -->
      <div v-else class="directory-panel employee-panel">
        <div v-if="employees.length === 0 && !loading" class="employee-empty">
          <span class="employee-empty-icon"><i class="bi bi-people"></i></span>

          <h2>{{ hasActiveFilters ? 'No employees found' : 'No employees yet' }}</h2>

          <p>
            {{
              hasActiveFilters
                ? 'Try adjusting your search or filters to find what you are looking for.'
                : 'Add your first employee to start building your directory.'
            }}
          </p>

          <button
            v-if="hasActiveFilters"
            type="button"
            class="btn btn-outline-primary"
            @click="clearFilters"
          >
            <i class="bi bi-funnel"></i>
            <span>Clear filters</span>
          </button>

          <button
            v-else-if="canCreateEmployee"
            type="button"
            class="btn btn-primary"
            @click="openCreateModal"
          >
            <i class="bi bi-person-plus"></i>
            <span>Add employee</span>
          </button>
        </div>

        <template v-else>
          <EmployeeListView
            v-if="viewMode === 'list'"
            :employees="employees"
            :loading="loading"
            :open-menu-id="openEmployeeMenuId"
            @view="openDetailsModal"
            @toggle-menu="toggleEmployeeMenu"
            @close-menu="closeEmployeeMenu"
          />

          <EmployeeGridView
            v-else
            :employees="employees"
            :loading="loading"
            :open-menu-id="openEmployeeMenuId"
            @view="openDetailsModal"
            @toggle-menu="toggleEmployeeMenu"
            @close-menu="closeEmployeeMenu"
          />
        </template>
      </div>

      <!-- Shared employee action menu (fixed layer) -->
      <div
        v-if="menuEmployee"
        id="employee-action-menu"
        ref="openMenuItemsEl"
        class="employee-action-menu"
        :style="menuStyle"
        role="menu"
        :aria-label="`Actions for ${menuEmployeeName}`"
        @click.stop
        @keydown="onMenuKeydown"
      >
        <button
          v-if="viewMode === 'list'"
          type="button"
          class="employee-menu-item employee-menu-item-view"
          role="menuitem"
          @click="runMenuAction($event, () => openDetailsModal(menuEmployee))"
        >
          <i class="bi bi-eye"></i>
          <span>View</span>
        </button>

        <button
          v-if="canUpdateEmployee"
          type="button"
          class="employee-menu-item"
          role="menuitem"
          @click="runMenuAction($event, () => openEditModal(menuEmployee))"
        >
          <i class="bi bi-pencil"></i>
          <span>Edit</span>
        </button>

        <button
          v-if="canDeleteEmployee"
          type="button"
          class="employee-menu-item employee-menu-item-danger"
          role="menuitem"
          @click="runMenuAction($event, () => openDeleteConfirmation(menuEmployee))"
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
        noun="employees"
        @page-change="goToPage"
        @page-size-change="
          (size) => {
            pageSize = size
            changePageSize()
          }
        "
      />
    </section>

    <div v-else class="alert alert-warning" role="alert">
      You do not have permission to view employee records.
    </div>

    <!-- Create / Edit modal -->
    <EmployeeFormModal
      :show="showFormModal"
      :employee="selectedEmployee"
      @close="closeFormModal"
      @saved="handleEmployeeSaved"
    />

    <!-- Details modal -->
    <EmployeeDetailsModal
      :show="showDetailsModal"
      :employee="selectedEmployee"
      @close="closeDetailsModal"
    />

    <!-- Delete confirmation -->
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
  </div>
</template>

<script setup>
import { computed, nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'

import EmployeeDirectoryToolbar from '@/features/employees/components/EmployeeDirectoryToolbar.vue'
import EmployeeListView from '@/features/employees/components/EmployeeListView.vue'
import EmployeeGridView from '@/features/employees/components/EmployeeGridView.vue'
import EmployeeFormModal from '@/features/employees/components/EmployeeFormModal.vue'
import EmployeeDetailsModal from '@/features/employees/components/EmployeeDetailsModal.vue'
import ConfirmActionModal from '@/components/common/ConfirmActionModal.vue'
import AppPagination from '@/components/common/AppPagination.vue'
import { useAuthStore } from '@/features/auth/stores/authStore'
import { employeeService } from '@/features/employees/services/employeeService'
import { pluralize } from '@/utils/format'
import { ESCAPE_PRIORITY, useEscapeDismiss } from '@/composables/useEscapeDismiss'

const VIEW_MODE_STORAGE_KEY = 'employee-view-mode'
const SEARCH_DEBOUNCE_MS = 400

const authStore = useAuthStore()
const route = useRoute()
const router = useRouter()

const canReadEmployees = computed(() => authStore.hasPermission('READ'))
const canCreateEmployee = computed(() => authStore.hasPermission('CREATE'))
const canUpdateEmployee = computed(() => authStore.hasPermission('UPDATE'))
const canDeleteEmployee = computed(() => authStore.hasPermission('DELETE'))

const employees = ref([])
const loading = ref(false)
const error = ref('')
const hasLoaded = ref(false)

const search = ref('')
const department = ref('')
const jobTitle = ref('')

const departmentOptions = ref([])
const jobTitleOptions = ref([])

const currentPage = ref(0)
const pageSize = ref(10)
const totalPages = ref(0)
const totalElements = ref(0)

const showFormModal = ref(false)
const showDetailsModal = ref(false)
const selectedEmployee = ref(null)

const showDeleteModal = ref(false)
const employeeToDelete = ref(null)
const deleting = ref(false)
const deleteError = ref('')

const accessError = ref('')

const storedViewMode = localStorage.getItem(VIEW_MODE_STORAGE_KEY)
const viewMode = ref(storedViewMode === 'grid' ? 'grid' : 'list')

let searchTimeout = null
let fetchSequence = 0
let ignoreNextSearchWatch = false

const hasActiveFilters = computed(
  () => Boolean(search.value.trim()) || department.value !== '' || jobTitle.value !== '',
)

const fetchEmployees = async () => {
  if (!canReadEmployees.value) {
    employees.value = []
    totalElements.value = 0
    totalPages.value = 0
    return
  }

  const sequence = ++fetchSequence

  loading.value = true
  error.value = ''

  try {
    const response = await employeeService.getEmployees(
      currentPage.value,
      pageSize.value,
      search.value,
      {
        department: department.value,
        jobTitle: jobTitle.value,
      },
    )

    if (sequence !== fetchSequence) {
      return
    }

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

    hasLoaded.value = true
  } catch (err) {
    if (sequence !== fetchSequence) {
      return
    }

    console.error('Failed to fetch employees:', err)

    error.value = 'Unable to load employees.'
    employees.value = []
    totalElements.value = 0
    totalPages.value = 0
    hasLoaded.value = true
  } finally {
    if (sequence === fetchSequence) {
      loading.value = false
    }
  }
}

const loadFilterOptions = async () => {
  if (!canReadEmployees.value) {
    return
  }

  try {
    const response = await employeeService.getFilters()

    departmentOptions.value = response.data?.departments || []
    jobTitleOptions.value = response.data?.jobTitles || []
  } catch (err) {
    console.error('Failed to load employee filter options:', err)
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
  if (ignoreNextSearchWatch) {
    ignoreNextSearchWatch = false
    return
  }

  clearTimeout(searchTimeout)

  searchTimeout = setTimeout(() => {
    currentPage.value = 0
    fetchEmployees()
  }, SEARCH_DEBOUNCE_MS)
})

const onDepartmentChange = (value) => {
  clearTimeout(searchTimeout)
  department.value = value
  currentPage.value = 0
  fetchEmployees()
}

const onJobTitleChange = (value) => {
  clearTimeout(searchTimeout)
  jobTitle.value = value
  currentPage.value = 0
  fetchEmployees()
}

const clearFilters = () => {
  clearTimeout(searchTimeout)

  ignoreNextSearchWatch = search.value !== ''

  search.value = ''
  department.value = ''
  jobTitle.value = ''
  currentPage.value = 0

  fetchEmployees()
}

const setViewMode = (mode) => {
  if (mode !== 'list' && mode !== 'grid') {
    return
  }

  openEmployeeMenuId.value = null
  viewMode.value = mode
}

watch(viewMode, (mode) => {
  localStorage.setItem(VIEW_MODE_STORAGE_KEY, mode)
})

/*
 * Single piece of state for the employee action menu. null → no menu open,
 * otherwise the id of the employee whose menu is open. Only one dropdown can
 * ever be visible, and the menu itself lives in a fixed layer so no
 * table/card overflow context can clip it.
 */
const openEmployeeMenuId = ref(null)
const menuTriggerEl = ref(null)
const anchorRect = ref(null)
const menuOpensUpward = ref(false)
const openMenuItemsEl = ref(null)

const menuEmployee = computed(
  () => employees.value.find((employee) => employee.id === openEmployeeMenuId.value) || null,
)

const menuEmployeeName = computed(() => {
  const employee = menuEmployee.value

  if (!employee) {
    return ''
  }

  return `${employee.firstName || ''} ${employee.lastName || ''}`.trim() || 'Employee'
})

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

const toggleEmployeeMenu = (employee, event) => {
  event.stopPropagation()

  if (openEmployeeMenuId.value === employee.id) {
    openEmployeeMenuId.value = null
    return
  }

  menuTriggerEl.value = event.currentTarget
  openEmployeeMenuId.value = employee.id
}

const closeEmployeeMenu = () => {
  openEmployeeMenuId.value = null
}

const refreshAnchorRect = () => {
  const el = menuTriggerEl.value

  if (!el || !el.isConnected || el.offsetParent === null) {
    openEmployeeMenuId.value = null
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
  () => openEmployeeMenuId.value !== null,
  () => {
    const trigger = menuTriggerEl.value
    openEmployeeMenuId.value = null
    if (trigger instanceof HTMLElement) {
      trigger.focus()
    }
  },
  ESCAPE_PRIORITY.DROPDOWN,
)

watch(openEmployeeMenuId, (id) => {
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

watch([employees, loading], () => {
  openEmployeeMenuId.value = null
})

const runMenuAction = (event, action) => {
  event.stopPropagation()

  try {
    action()
  } finally {
    openEmployeeMenuId.value = null
  }
}

const openCreateModal = () => {
  if (!canCreateEmployee.value) {
    return
  }

  openEmployeeMenuId.value = null
  selectedEmployee.value = null
  showFormModal.value = true
}

const openEditModal = (employee) => {
  if (!canUpdateEmployee.value) {
    return
  }

  openEmployeeMenuId.value = null
  selectedEmployee.value = employee
  showFormModal.value = true
}

const openDetailsModal = (employee) => {
  if (!canReadEmployees.value) {
    return
  }

  openEmployeeMenuId.value = null
  selectedEmployee.value = employee
  showDetailsModal.value = true
}

const closeFormModal = () => {
  showFormModal.value = false
  selectedEmployee.value = null
}

const closeDetailsModal = () => {
  showDetailsModal.value = false
  selectedEmployee.value = null
}

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
    loadFilterOptions()
  } catch (err) {
    console.error('Failed to delete employee:', err)
    deleteError.value = err.response?.data?.message || 'Failed to delete employee.'
  } finally {
    deleting.value = false
  }
}

const handleEmployeeSaved = async () => {
  await fetchEmployees()
  loadFilterOptions()
}

watch(
  () => route.query.error,
  (message) => {
    if (message) {
      accessError.value = message

      router.replace({
        path: '/employees',
        query: {},
      })
    }
  },
  { immediate: true },
)

watch(
  () => route.query.create,
  (value) => {
    if (value) {
      openCreateModal()

      router.replace({
        path: '/employees',
        query: {},
      })
    }
  },
  { immediate: true },
)

onMounted(() => {
  fetchEmployees()
  loadFilterOptions()
})

onBeforeUnmount(() => {
  clearTimeout(searchTimeout)
})
</script>

<style scoped>
.employee-directory {
  display: grid;
  gap: 0.75rem;
}

.directory-meta {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  justify-content: space-between;
  gap: 8px 16px;
}

.directory-summary {
  margin: 0;
  color: var(--color-text-secondary);
  font-size: 0.76rem;
}

.directory-summary strong {
  color: var(--color-text);
  font-weight: 700;
}

.directory-summary-sep {
  margin: 0 0.35rem;
  color: var(--color-text-tertiary);
}

.clear-filters-btn {
  display: inline-flex;
  align-items: center;
  gap: 0.4rem;
  padding: 0;
  border: 0;
  background: transparent;
  color: var(--color-primary-text);
  font-size: 0.78rem;
  font-weight: 750;
  cursor: pointer;
}

.clear-filters-btn:hover {
  text-decoration: underline;
}

.clear-filters-btn:focus-visible {
  outline: 2px solid var(--color-primary-text);
  outline-offset: 2px;
}

.directory-panel {
  overflow: hidden;
  border: 1px solid var(--color-border);
  background: var(--color-surface);
}

.employee-directory-error {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 14px;
  padding: 18px 20px;
  border: 1px solid var(--color-danger-border);
  border-radius: 8px;
  background: var(--color-danger-soft);
}

.employee-error-icon {
  display: inline-flex;
  width: 42px;
  height: 42px;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  background: var(--color-surface);
  color: var(--color-danger-text);
  font-size: 1.15rem;
}

.employee-error-text {
  flex: 1 1 220px;
  min-width: 0;
}

.employee-error-text h2 {
  margin: 0;
  color: var(--color-danger-text);
  font-size: 0.95rem;
  font-weight: 750;
}

.employee-error-text p {
  margin: 2px 0 0;
  color: var(--color-danger-text);
  font-size: 0.83rem;
}

.employee-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 3.25rem 1.5rem;
  text-align: center;
}

.employee-empty-icon {
  display: inline-flex;
  width: 52px;
  height: 52px;
  align-items: center;
  justify-content: center;
  margin-bottom: 0.7rem;
  border-radius: 50%;
  background: var(--color-primary-soft);
  color: var(--color-primary-text);
  font-size: 1.3rem;
}

.employee-empty h2 {
  margin: 0;
  color: var(--color-text);
  font-size: 1rem;
  font-weight: 750;
}

.employee-empty p {
  max-width: 44ch;
  margin: 0.35rem 0 0;
  color: var(--color-text-secondary);
  font-size: 0.85rem;
  line-height: 1.5;
}

.employee-empty .btn {
  margin-top: 1rem;
}

.employee-action-menu {
  position: fixed;
  z-index: 1040;
  display: flex;
  width: max-content;
  max-width: min(240px, calc(100vw - 24px));
  flex-direction: column;
  padding: 0.3rem;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-sm);
  background: var(--color-surface);
  box-shadow: var(--shadow-menu);
}

.employee-menu-item {
  display: flex;
  min-height: 42px;
  align-items: center;
  gap: 0.6rem;
  padding: 0 0.7rem;
  border: 0;
  border-radius: 4px;
  background: transparent;
  color: var(--color-text-body);
  font-size: 0.85rem;
  font-weight: 600;
  text-align: left;
  white-space: nowrap;
  cursor: pointer;
}

.employee-menu-item:hover {
  background: var(--color-hover-strong);
}

.employee-menu-item:focus-visible {
  background: var(--color-hover-strong);
  outline: 2px solid var(--color-primary-text);
  outline-offset: -2px;
}

.employee-menu-item i {
  width: 1rem;
  color: var(--color-text-secondary);
  font-size: 0.95rem;
  text-align: center;
}

.employee-menu-item-danger,
.employee-menu-item-danger i {
  color: var(--color-danger-text);
}

@media (max-width: 767.98px) {
  .directory-panel {
    overflow: visible;
    border-radius: 0;
  }
}
</style>
