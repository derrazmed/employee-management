<script setup>
import { ref, watch } from 'vue'
import { employeeService } from '../services/employeeService'

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

const emit = defineEmits(['close'])

const employeeDetails = ref(null)

const loading = ref(false)
const error = ref('')

const fileLoading = ref(false)
const fileError = ref('')

const cvAction = ref('')

const loadEmployee = async () => {
  if (!props.employee?.id) {
    return
  }

  loading.value = true
  error.value = ''

  try {
    const response = await employeeService.getEmployee(props.employee.id)

    employeeDetails.value = response.data
  } catch (err) {
    console.error('Failed to load employee:', err)

    error.value = err.response?.data?.message || 'Failed to load employee.'
  } finally {
    loading.value = false
  }
}

const viewCv = async () => {
  if (!employeeDetails.value?.cvObjectName || cvAction.value) {
    return
  }

  fileError.value = ''

  const previewWindow = window.open('about:blank', '_blank')

  if (!previewWindow) {
    fileError.value = 'Allow pop-ups to view the employee CV.'
    return
  }

  previewWindow.opener = null
  cvAction.value = 'view'

  try {
    const blob = await employeeService.getCv(employeeDetails.value.id)

    if (blob.type !== 'application/pdf') {
      throw new Error('The CV response was not a PDF.')
    }

    const url = URL.createObjectURL(blob)
    previewWindow.location.replace(url)

    setTimeout(() => {
      URL.revokeObjectURL(url)
    }, 60000)
  } catch (err) {
    console.error('Failed to view employee CV:', err)
    previewWindow.close()
    fileError.value = err.message || 'Failed to view employee CV.'
  } finally {
    cvAction.value = ''
  }
}

const downloadCv = async () => {
  if (!employeeDetails.value?.cvObjectName || cvAction.value) {
    return
  }

  cvAction.value = 'download'
  fileError.value = ''

  try {
    const employee = employeeDetails.value
    const blob = await employeeService.downloadCv(employee.id)

    if (blob.type !== 'application/pdf') {
      throw new Error('The CV response was not a PDF.')
    }

    const url = URL.createObjectURL(blob)
    const employeeName =
      [employee.firstName, employee.lastName].filter(Boolean).join('_') || `employee-${employee.id}`

    const link = document.createElement('a')

    link.href = url
    link.download = `${employeeName}_CV.pdf`

    document.body.appendChild(link)
    link.click()
    link.remove()

    setTimeout(() => {
      URL.revokeObjectURL(url)
    }, 1000)
  } catch (err) {
    console.error('Failed to download employee CV:', err)
    fileError.value = err.message || 'Failed to download employee CV.'
  } finally {
    cvAction.value = ''
  }
}

const downloadContract = async () => {
  if (!employeeDetails.value?.id) {
    return
  }

  fileLoading.value = true
  fileError.value = ''

  try {
    const blob = await employeeService.getContract(employeeDetails.value.id)

    const url = URL.createObjectURL(blob)

    const link = document.createElement('a')
    link.href = url
    link.download = `${employeeDetails.value.firstName}_${employeeDetails.value.lastName}_Contract`
    document.body.appendChild(link)
    link.click()
    link.remove()

    URL.revokeObjectURL(url)
  } catch (err) {
    console.error('Failed to download contract:', err)

    fileError.value = err.response?.data?.message || 'Contract is not available.'
  } finally {
    fileLoading.value = false
  }
}

const closeModal = () => {
  if (fileLoading.value || cvAction.value) {
    return
  }

  employeeDetails.value = null
  error.value = ''
  fileError.value = ''

  emit('close')
}

watch(
  () => props.show,
  (show) => {
    if (show) {
      loadEmployee()
    }
  },
)

watch(
  () => props.employee,
  (employee) => {
    if (props.show && employee) {
      loadEmployee()
    }
  },
)
</script>

<template>
  <div v-if="show" class="modal fade show d-block" tabindex="-1" role="dialog" aria-modal="true">
    <div class="modal-dialog modal-lg modal-dialog-centered modal-dialog-scrollable">
      <div class="modal-content">
        <!-- Header -->
        <div class="modal-header">
          <h5 class="modal-title">
            <i class="bi bi-person-vcard me-2"></i>
            Employee Details
          </h5>

          <button
            type="button"
            class="btn-close"
            :disabled="fileLoading"
            @click="closeModal"
          ></button>
        </div>

        <!-- Body -->
        <div class="modal-body">
          <!-- Loading -->
          <div v-if="loading" class="d-flex justify-content-center py-5">
            <div class="spinner-border text-primary" role="status">
              <span class="visually-hidden"> Loading... </span>
            </div>
          </div>

          <!-- Error -->
          <div v-else-if="error" class="alert alert-danger">
            {{ error }}
          </div>

          <!-- Details -->
          <div v-else-if="employeeDetails">
            <!-- Employee header -->
            <div class="d-flex align-items-center mb-4">
              <!-- Photo -->
              <div class="me-4">
                <img
                  v-if="employeeDetails.photoObjectName"
                  :src="`/api/employees/${employeeDetails.id}/photo`"
                  :alt="`${employeeDetails.firstName} ${employeeDetails.lastName}`"
                  class="employee-photo"
                />

                <div v-else class="employee-photo-placeholder">
                  <i class="bi bi-person fs-1 text-muted"></i>
                </div>
              </div>

              <!-- Name -->
              <div>
                <h4 class="mb-1">
                  {{ employeeDetails.firstName }}
                  {{ employeeDetails.lastName }}
                </h4>

                <div class="text-muted">
                  {{ employeeDetails.jobTitle || 'Employee' }}
                </div>

                <div class="text-muted small">
                  {{ employeeDetails.department || '—' }}
                </div>
              </div>
            </div>

            <!-- Personal information -->
            <div class="mb-4">
              <h6 class="fw-bold border-bottom pb-2 mb-3">Personal Information</h6>

              <div class="row g-3">
                <div class="col-md-6">
                  <div class="text-muted small">First Name</div>

                  <div class="fw-semibold">
                    {{ employeeDetails.firstName || '—' }}
                  </div>
                </div>

                <div class="col-md-6">
                  <div class="text-muted small">Last Name</div>

                  <div class="fw-semibold">
                    {{ employeeDetails.lastName || '—' }}
                  </div>
                </div>

                <div class="col-md-6">
                  <div class="text-muted small">Email</div>

                  <div class="fw-semibold">
                    {{ employeeDetails.email || '—' }}
                  </div>
                </div>

                <div class="col-md-6">
                  <div class="text-muted small">Phone Number</div>

                  <div class="fw-semibold">
                    {{ employeeDetails.phoneNumber || '—' }}
                  </div>
                </div>
              </div>
            </div>

            <!-- Employment information -->
            <div class="mb-4">
              <h6 class="fw-bold border-bottom pb-2 mb-3">Employment Information</h6>

              <div class="row g-3">
                <div class="col-md-6">
                  <div class="text-muted small">Job Title</div>

                  <div class="fw-semibold">
                    {{ employeeDetails.jobTitle || '—' }}
                  </div>
                </div>

                <div class="col-md-6">
                  <div class="text-muted small">Department</div>

                  <div class="fw-semibold">
                    {{ employeeDetails.department || '—' }}
                  </div>
                </div>

                <div class="col-md-6">
                  <div class="text-muted small">Hire Date</div>

                  <div class="fw-semibold">
                    {{ employeeDetails.hireDate || '—' }}
                  </div>
                </div>

                <div class="col-md-6">
                  <div class="text-muted small">Salary</div>

                  <div class="fw-semibold">
                    <span v-if="employeeDetails.salary != null">
                      {{ employeeDetails.salary.toLocaleString() }}
                    </span>

                    <span v-else> — </span>
                  </div>
                </div>
              </div>
            </div>

            <!-- Documents -->
            <div>
              <h6 class="fw-bold border-bottom pb-2 mb-3">Documents</h6>

              <div v-if="fileError" class="alert alert-danger">
                {{ fileError }}
              </div>

              <div class="d-flex gap-2 flex-wrap">
                <!-- View CV -->
                <button
                  v-if="employeeDetails.cvObjectName"
                  type="button"
                  class="btn btn-outline-primary"
                  :disabled="Boolean(cvAction)"
                  @click="viewCv"
                >
                  <span
                    v-if="cvAction === 'view'"
                    class="spinner-border spinner-border-sm me-2"
                  ></span>

                  <i v-else class="bi bi-eye me-2"></i>

                  View CV
                </button>

                <!-- Download CV -->
                <button
                  v-if="employeeDetails.cvObjectName"
                  type="button"
                  class="btn btn-outline-danger"
                  :disabled="Boolean(cvAction)"
                  @click="downloadCv"
                >
                  <span
                    v-if="cvAction === 'download'"
                    class="spinner-border spinner-border-sm me-2"
                  ></span>

                  <i v-else class="bi bi-download me-2"></i>
                  Download CV
                </button>

                <!-- No CV -->
                <span v-if="!employeeDetails.cvObjectName" class="text-muted">
                  <i class="bi bi-file-earmark-x me-2"></i>
                  No CV uploaded
                </span>

                <!-- Contract -->
                <button
                  type="button"
                  class="btn btn-outline-secondary"
                  :disabled="fileLoading"
                  @click="downloadContract"
                >
                  <span v-if="fileLoading" class="spinner-border spinner-border-sm me-2"></span>

                  <i v-else class="bi bi-file-earmark-text me-2"></i>

                  Download Contract
                </button>
              </div>
            </div>
          </div>
        </div>

        <!-- Footer -->
        <div class="modal-footer">
          <button
            type="button"
            class="btn btn-secondary"
            :disabled="fileLoading"
            @click="closeModal"
          >
            Close
          </button>
        </div>
      </div>
    </div>
  </div>

  <!-- Backdrop -->
  <div v-if="show" class="modal-backdrop fade show"></div>
</template>

<style scoped>
.employee-photo {
  width: 100px;
  height: 100px;
  object-fit: cover;
  border-radius: 50%;
  border: 3px solid #dee2e6;
}

.employee-photo-placeholder {
  width: 100px;
  height: 100px;
  border-radius: 50%;
  border: 3px solid #dee2e6;
  background-color: #f8f9fa;

  display: flex;
  align-items: center;
  justify-content: center;
}
</style>
