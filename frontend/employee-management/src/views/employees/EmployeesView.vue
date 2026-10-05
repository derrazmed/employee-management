<script setup>
import { computed, ref, watch } from 'vue'

import EmployeeTable from '@/features/employees/components/EmployeeTable.vue'
import EmployeeFormModal from '@/features/employees/components/EmployeeFormModal.vue'
import EmployeeDetailsModal from '@/features/employees/components/EmployeeDetailsModal.vue'
import { useAuthStore } from '@/features/auth/stores/authStore'
import { useRoute, useRouter } from 'vue-router'

const authStore = useAuthStore()
const canReadEmployees = computed(() => authStore.hasPermission('READ'))
const employeeTable = ref(null)

const showFormModal = ref(false)
const showDetailsModal = ref(false)

const selectedEmployee = ref(null)

const route = useRoute()
const router = useRouter()

const accessError = ref('')

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

const openCreateModal = () => {
  if (!authStore.hasPermission('CREATE')) {
    return
  }

  selectedEmployee.value = null
  showFormModal.value = true
}

/*
 * Open Edit Employee modal
 */
const openEditModal = (employee) => {
  if (!authStore.hasPermission('UPDATE')) {
    return
  }

  selectedEmployee.value = employee
  showFormModal.value = true
}

/*
 * Open Employee Details modal
 */
const openDetailsModal = (employee) => {
  if (!authStore.hasPermission('READ')) {
    return
  }

  selectedEmployee.value = employee
  showDetailsModal.value = true
}

/*
 * Close Form modal
 */
const closeFormModal = () => {
  showFormModal.value = false
  selectedEmployee.value = null
}

/*
 * Close Details modal
 */
const closeDetailsModal = () => {
  showDetailsModal.value = false
  selectedEmployee.value = null
}

/*
 * Called after create/update succeeds.
 *
 * Refresh the table so the new/updated
 * employee appears immediately.
 */
const handleEmployeeSaved = async () => {
  await employeeTable.value?.fetchEmployees()
}
</script>

<template>
  <div class="container-fluid py-4">
    <!-- Page header -->
    <div class="page-heading">
      <div>
        <h1>Employees</h1>

        <p>Manage employee records and information.</p>
      </div>

      <!-- Add Employee -->
      <button
        v-if="authStore.hasPermission('CREATE')"
        type="button"
        class="btn btn-primary"
        @click="openCreateModal"
      >
        <i class="bi bi-person-plus me-2"></i>
        Add Employee
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

    <!-- Employee table -->
    <EmployeeTable
      v-if="canReadEmployees"
      ref="employeeTable"
      @view="openDetailsModal"
      @edit="openEditModal"
    />

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
  </div>
</template>
