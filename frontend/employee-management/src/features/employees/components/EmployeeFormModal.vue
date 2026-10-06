<script setup>
import { computed, ref, watch } from 'vue'
import { employeeService } from '../services/employeeService'
import { useAuthStore } from '@/features/auth/stores/authStore'
import { ESCAPE_PRIORITY, useEscapeDismiss } from '@/composables/useEscapeDismiss'
import FileUploadField from '@/components/common/FileUploadField.vue'

const authStore = useAuthStore()

const props = defineProps({
  show: {
    type: Boolean,
    default: false,
  },

  employee: {
    type: Object,
    default: null,
  },
})

const emit = defineEmits(['close', 'saved'])

const loading = ref(false)
const error = ref('')

const form = ref({
  firstName: '',
  lastName: '',
  email: '',
  phoneNumber: '',
  jobTitle: '',
  department: '',
  hireDate: '',
  salary: '',
})

const photoFile = ref(null)
const cvFile = ref(null)

const isEdit = computed(() => {
  return !!props.employee
})

const canManageEmployee = computed(() => {
  return authStore.hasPermission(isEdit.value ? 'UPDATE' : 'CREATE')
})

const canUploadFiles = computed(() => authStore.hasPermission('UPDATE'))

const modalTitle = computed(() => {
  return isEdit.value ? 'Edit Employee' : 'Create Employee'
})

const closeModal = () => {
  if (loading.value) {
    return
  }

  emit('close')
}

useEscapeDismiss(() => props.show && canManageEmployee.value, closeModal, ESCAPE_PRIORITY.MODAL)

const resetForm = () => {
  form.value = {
    firstName: '',
    lastName: '',
    email: '',
    phoneNumber: '',
    jobTitle: '',
    department: '',
    hireDate: '',
    salary: '',
  }

  photoFile.value = null
  cvFile.value = null
  error.value = ''
}

const populateForm = () => {
  if (!props.employee) {
    resetForm()
    return
  }

  form.value = {
    firstName: props.employee.firstName || '',
    lastName: props.employee.lastName || '',
    email: props.employee.email || '',
    phoneNumber: props.employee.phoneNumber || '',
    jobTitle: props.employee.jobTitle || '',
    department: props.employee.department || '',
    hireDate: props.employee.hireDate || '',
    salary: props.employee.salary ?? '',
  }

  photoFile.value = null
  cvFile.value = null
  error.value = ''
}

watch(
  () => props.show,
  (show) => {
    if (show && canManageEmployee.value) {
      populateForm()
    }
  },
)

watch(
  () => props.employee,
  () => {
    if (props.show && canManageEmployee.value) {
      populateForm()
    }
  },
)

const removePhoto = () => {
  photoFile.value = null
}

const removeCv = () => {
  cvFile.value = null
}

const submitForm = async () => {
  if (!canManageEmployee.value) {
    return
  }

  error.value = ''

  // Basic validation
  if (
    !form.value.firstName ||
    !form.value.lastName ||
    !form.value.email ||
    !form.value.jobTitle ||
    !form.value.department ||
    !form.value.hireDate ||
    form.value.salary === ''
  ) {
    error.value = 'Please fill in all required fields.'
    return
  }

  loading.value = true

  try {
    let response

    const employeeData = {
      firstName: form.value.firstName,
      lastName: form.value.lastName,
      email: form.value.email,
      phoneNumber: form.value.phoneNumber,
      jobTitle: form.value.jobTitle,
      department: form.value.department,
      hireDate: form.value.hireDate,
      salary: Number(form.value.salary),
    }

    if (isEdit.value) {
      response = await employeeService.updateEmployee(props.employee.id, employeeData)
    } else {
      response = await employeeService.createEmployee(employeeData)
    }

    /*
     * The employee must exist before we can upload
     * the photo/CV because those endpoints require
     * /employees/{id}/...
     */
    const savedEmployee = response.data

    const employeeId = savedEmployee?.id || props.employee?.id

    // Upload photo if selected
    if (photoFile.value && employeeId && canUploadFiles.value) {
      await employeeService.uploadPhoto(employeeId, photoFile.value)
    }

    // Upload CV if selected
    if (cvFile.value && employeeId && canUploadFiles.value) {
      await employeeService.uploadCv(employeeId, cvFile.value)
    }

    emit('saved')
    emit('close')

    resetForm()
  } catch (err) {
    console.error('Failed to save employee:', err)

    error.value = err.response?.data?.message || 'Failed to save employee.'
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div
    v-if="show && canManageEmployee"
    class="modal fade show d-block"
    tabindex="-1"
    role="dialog"
    aria-modal="true"
  >
    <div class="modal-dialog modal-lg modal-dialog-centered modal-dialog-scrollable">
      <div class="modal-content">
        <!-- Header -->
        <div class="modal-header">
          <h5 class="modal-title">
            <i class="bi bi-person-plus me-2"></i>

            {{ modalTitle }}
          </h5>

          <button type="button" class="btn-close" :disabled="loading" @click="closeModal"></button>
        </div>

        <!-- Body -->
        <div class="modal-body">
          <!-- Error -->
          <div v-if="error" class="alert alert-danger" role="alert">
            {{ error }}
          </div>

          <form @submit.prevent="submitForm">
            <!-- Personal Information -->
            <h6 class="fw-bold mb-3">Personal Information</h6>

            <div class="row g-3">
              <!-- First Name -->
              <div class="col-md-6">
                <label for="firstName" class="form-label">
                  First Name
                  <span class="text-danger">*</span>
                </label>

                <input
                  id="firstName"
                  v-model="form.firstName"
                  type="text"
                  class="form-control"
                  placeholder="Enter first name"
                  required
                  :disabled="loading"
                />
              </div>

              <!-- Last Name -->
              <div class="col-md-6">
                <label for="lastName" class="form-label">
                  Last Name
                  <span class="text-danger">*</span>
                </label>

                <input
                  id="lastName"
                  v-model="form.lastName"
                  type="text"
                  class="form-control"
                  placeholder="Enter last name"
                  required
                  :disabled="loading"
                />
              </div>

              <!-- Email -->
              <div class="col-md-6">
                <label for="email" class="form-label">
                  Email
                  <span class="text-danger">*</span>
                </label>

                <input
                  id="email"
                  v-model="form.email"
                  type="email"
                  class="form-control"
                  placeholder="Enter email"
                  required
                  :disabled="loading"
                />
              </div>

              <!-- Phone -->
              <div class="col-md-6">
                <label for="phoneNumber" class="form-label"> Phone Number </label>

                <input
                  id="phoneNumber"
                  v-model="form.phoneNumber"
                  type="tel"
                  class="form-control"
                  placeholder="+212..."
                  :disabled="loading"
                />
              </div>
            </div>

            <hr class="my-4" />

            <!-- Employment Information -->
            <h6 class="fw-bold mb-3">Employment Information</h6>

            <div class="row g-3">
              <!-- Job Title -->
              <div class="col-md-6">
                <label for="jobTitle" class="form-label">
                  Job Title
                  <span class="text-danger">*</span>
                </label>

                <input
                  id="jobTitle"
                  v-model="form.jobTitle"
                  type="text"
                  class="form-control"
                  placeholder="e.g. Software Engineer"
                  required
                  :disabled="loading"
                />
              </div>

              <!-- Department -->
              <div class="col-md-6">
                <label for="department" class="form-label">
                  Department
                  <span class="text-danger">*</span>
                </label>

                <input
                  id="department"
                  v-model="form.department"
                  type="text"
                  class="form-control"
                  placeholder="e.g. IT"
                  required
                  :disabled="loading"
                />
              </div>

              <!-- Hire Date -->
              <div class="col-md-6">
                <label for="hireDate" class="form-label">
                  Hire Date
                  <span class="text-danger">*</span>
                </label>

                <input
                  id="hireDate"
                  v-model="form.hireDate"
                  type="date"
                  class="form-control"
                  required
                  :disabled="loading"
                />
              </div>

              <!-- Salary -->
              <div class="col-md-6">
                <label for="salary" class="form-label">
                  Salary
                  <span class="text-danger">*</span>
                </label>

                <input
                  id="salary"
                  v-model="form.salary"
                  type="number"
                  class="form-control"
                  min="0"
                  step="0.01"
                  placeholder="12000.00"
                  required
                  :disabled="loading"
                />
              </div>
            </div>

            <hr class="my-4" />

            <!-- Documents -->
            <h6 class="fw-bold mb-3">Documents</h6>

            <div class="row g-3">
              <!-- Photo -->
              <div v-if="canUploadFiles" class="col-md-6">
                <FileUploadField
                  v-model="photoFile"
                  label="Employee Photo"
                  icon="bi-image"
                  accept="image/*"
                  hint="JPG, PNG, WEBP"
                  :existing-file="props.employee"
                  :disabled="loading"
                  @remove="removePhoto"
                />
              </div>

              <!-- CV -->
              <div v-if="canUploadFiles" class="col-md-6">
                <FileUploadField
                  v-model="cvFile"
                  label="CV"
                  icon="bi-file-earmark-text"
                  accept=".pdf,.doc,.docx"
                  hint="PDF, DOC, DOCX"
                  :existing-file="props.employee"
                  :disabled="loading"
                  @remove="removeCv"
                />
              </div>
            </div>
          </form>
        </div>

        <!-- Footer -->
        <div class="modal-footer">
          <button type="button" class="btn btn-secondary" :disabled="loading" @click="closeModal">
            Cancel
          </button>

          <button
            type="button"
            class="btn btn-primary"
            :disabled="loading || !canManageEmployee"
            @click="submitForm"
          >
            <span v-if="loading" class="spinner-border spinner-border-sm me-2"></span>

            <i v-else class="bi bi-check-lg me-2"></i>

            {{ isEdit ? 'Update Employee' : 'Create Employee' }}
          </button>
        </div>
      </div>
    </div>
  </div>

  <!-- Modal backdrop -->
  <div v-if="show && canManageEmployee" class="modal-backdrop fade show"></div>
</template>

<style scoped>
/* Modal header */
.modal-title i {
  color: var(--color-primary-text);
}

/* Section headings */
.modal-body h6 {
  margin-top: 0.15rem;
  margin-bottom: 1rem;
  color: var(--color-text-secondary);
  font-size: 0.6875rem;
  font-weight: 700;
  letter-spacing: 0.07em;
  text-transform: uppercase;
}

/* Section dividers */
.modal-body hr {
  margin-block: 1.5rem;
  border-color: var(--app-border);
  opacity: 1;
}

/* Form labels */
.modal-body .form-label {
  font-size: 0.8125rem;
  font-weight: 500;
  color: var(--color-text);
  margin-bottom: 0.5rem;
}

.modal-body .form-label .text-danger {
  font-size: 0.75rem;
  font-weight: 600;
}

/* Form inputs - unified height and styling */
.modal-body .form-control {
  height: 40px;
  min-height: 40px;
  padding: 0 12px;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-sm);
  background: var(--color-surface);
  color: var(--color-text);
  font-size: 0.875rem;
  font-weight: 400;
  transition:
    border-color 0.15s ease,
    background-color 0.15s ease,
    box-shadow 0.15s ease;
}

.modal-body .form-control::placeholder {
  color: var(--color-text-tertiary);
  opacity: 1;
}

.modal-body .form-control:hover:not(:disabled):not([readonly]) {
  border-color: var(--color-border-strong);
  background: var(--color-hover);
}

.modal-body .form-control:focus {
  border-color: var(--color-primary-border);
  box-shadow: 0 0 0 3px var(--color-primary-soft);
  outline: none;
}

.modal-body .form-control:disabled,
.modal-body .form-control[readonly] {
  background: var(--color-surface-muted);
  color: var(--color-text-secondary);
  cursor: not-allowed;
  opacity: 0.8;
}

/* Date input specific styling to match other inputs */
.modal-body input[type='date'].form-control {
  padding-inline-end: 36px;
  background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='16' height='16' viewBox='0 0 24 24' fill='none' stroke='%236b7280' stroke-width='2' stroke-linecap='round' stroke-linejoin='round'%3E%3Crect x='3' y='4' width='18' height='18' rx='2' ry='2'%3E%3C/rect%3E%3Cline x1='16' y1='2' x2='16' y2='6'%3E%3C/line%3E%3Cline x1='8' y1='2' x2='8' y2='6'%3E%3C/line%3E%3Cline x1='3' y1='10' x2='21' y2='10'%3E%3C/line%3E%3C/svg%3E");
  background-repeat: no-repeat;
  background-position: right 10px center;
  background-size: 16px;
  cursor: pointer;
}

/* Number input (salary) */
.modal-body input[type='number'].form-control {
  text-align: left;
}

/* Error state for form controls */
.modal-body .form-control.is-invalid,
.modal-body .was-validated .form-control:invalid {
  border-color: var(--color-danger-border);
  background-image: none;
}

.modal-body .form-control.is-invalid:focus,
.modal-body .was-validated .form-control:invalid:focus {
  box-shadow: 0 0 0 3px var(--color-danger-soft);
}

/* Validation error messages */
.modal-body .invalid-feedback,
.modal-body .form-text.text-danger {
  display: block;
  margin-top: 0.375rem;
  font-size: 0.75rem;
  color: var(--color-danger-text);
}

/* Helper text */
.modal-body .form-text {
  margin-top: 0.375rem;
  font-size: 0.75rem;
  color: var(--color-text-tertiary);
}

/* Align FileUploadField trigger with form-control height */
.modal-body :deep(.file-upload-trigger) {
  min-height: 40px;
  height: 40px;
}

/* Modal footer */
.modal-footer {
  justify-content: flex-end;
  gap: 0.5rem;
  padding-top: 1rem;
  padding-bottom: 1rem;
  border-top: 1px solid var(--app-border);
}

.modal-footer .btn {
  height: 40px;
  min-width: 112px;
  padding: 0 16px;
  border-radius: var(--radius-sm);
  font-size: 0.8125rem;
  font-weight: 600;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
}

.modal-footer .btn .spinner-border-sm {
  width: 1rem;
  height: 1rem;
}

.modal-footer .btn-secondary {
  border-color: var(--color-border);
  background: var(--color-surface);
  color: var(--color-text);
}

.modal-footer .btn-secondary:hover:not(:disabled) {
  border-color: var(--color-border-strong);
  background: var(--color-hover);
  color: var(--color-text);
}

.modal-footer .btn-primary {
  background: var(--color-primary);
  border-color: var(--color-primary);
  color: #fff;
}

.modal-footer .btn-primary:hover:not(:disabled) {
  background: var(--color-primary-hover);
  border-color: var(--color-primary-hover);
}

.modal-footer .btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.modal-footer .btn:focus-visible {
  outline: 2px solid var(--color-primary-text);
  outline-offset: 2px;
}

/* Modal content spacing */
.modal-body {
  padding: 1.5rem;
}

/* Row spacing */
.modal-body .row.g-3 > [class*='col-'] {
  padding-bottom: 0.5rem;
}

@media (max-width: 575.98px) {
  .modal-body {
    padding: 1rem;
  }

  .modal-body .row.g-3 > [class*='col-'] {
    padding-bottom: 0;
  }
}
</style>
