<script setup>
import { computed, ref, watch } from 'vue'
import { employeeService } from '../services/employeeService'
import { useAuthStore } from '@/features/auth/stores/authStore'

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

const handlePhotoChange = (event) => {
  photoFile.value = event.target.files[0] || null
}

const handleCvChange = (event) => {
  cvFile.value = event.target.files[0] || null
}

const closeModal = () => {
  if (loading.value) {
    return
  }

  emit('close')
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
                <label for="photo" class="form-label"> Employee Photo </label>

                <input
                  id="photo"
                  type="file"
                  class="form-control"
                  accept="image/*"
                  :disabled="loading"
                  @change="handlePhotoChange"
                />

                <div class="form-text">JPG, PNG, WEBP, etc.</div>
              </div>

              <!-- CV -->
              <div v-if="canUploadFiles" class="col-md-6">
                <label for="cv" class="form-label"> CV </label>

                <input
                  id="cv"
                  type="file"
                  class="form-control"
                  accept=".pdf,.doc,.docx"
                  :disabled="loading"
                  @change="handleCvChange"
                />

                <div class="form-text">PDF, DOC, DOCX</div>
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
.modal-title i { color: var(--app-primary); }.modal-body h6 { margin-top: .15rem; color: #526075; font-size: .72rem; font-weight: 700 !important; letter-spacing: .07em; text-transform: uppercase; }.modal-body hr { margin-block: 1.75rem !important; border-color: var(--app-border); opacity: 1; }.modal-footer { justify-content: flex-end; gap: .5rem; }.modal-footer .btn { min-width: 112px; }
</style>
