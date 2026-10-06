<script setup>
import { ref, watch } from 'vue'
import { employeeService } from '../services/employeeService'
import { ESCAPE_PRIORITY, useEscapeDismiss } from '@/composables/useEscapeDismiss'

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

const closeModal = () => {
  if (fileLoading.value || cvAction.value) {
    return
  }

  employeeDetails.value = null
  error.value = ''
  fileError.value = ''

  emit('close')
}

useEscapeDismiss(
  () => props.show,
  closeModal,
  ESCAPE_PRIORITY.MODAL,
)

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
            <div class="employee-profile-hero">
              <!-- Photo -->
              <div class="employee-profile-photo">
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
              <div class="employee-profile-title">
                <h4 class="mb-1">
                  {{ employeeDetails.firstName }}
                  {{ employeeDetails.lastName }}
                </h4>

                <div class="employee-profile-role">
                  {{ employeeDetails.jobTitle || 'Employee' }}
                </div>

                <div class="employee-profile-department">
                  {{ employeeDetails.department || '—' }}
                </div>
              </div>
            </div>

            <!-- Personal information -->
            <section class="profile-section">
              <h6>Personal Information</h6>

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
            </section>

            <!-- Employment information -->
            <section class="profile-section">
              <h6>Employment Information</h6>

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
            </section>

            <!-- Documents -->
            <section class="profile-section profile-documents">
              <h6>Documents</h6>

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
            </section>
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
.modal-dialog { width: min(620px, 100%); height: 100%; max-height: 100%; margin: 0 0 0 auto; transform: none !important; }.modal-content { min-height: 100%; border-radius: 0; }.modal-header { padding: 20px 24px; }.modal-body { padding: 26px 24px; }.modal-footer { padding: 16px 24px; }
.employee-photo {
  width: 100px;
  height: 100px;
  object-fit: cover;
  border-radius: 50%;
  border: 3px solid var(--color-border);
}

.employee-photo-placeholder {
  width: 100px;
  height: 100px;
  border-radius: 50%;
  border: 3px solid var(--color-border);
  background-color: var(--color-surface-muted);

  display: flex;
  align-items: center;
  justify-content: center;
}

.employee-profile-hero { display: flex; align-items: center; gap: 1.25rem; padding: 0 0 1.5rem; border-bottom: 1px solid var(--app-border); }.employee-profile-photo { flex: 0 0 auto; }.employee-profile-title h4 { color: var(--app-text); font-size: 1.25rem; font-weight: 700; }.employee-profile-role { color: var(--color-text-body); font-weight: 600; }.employee-profile-department { margin-top: .18rem; color: var(--app-text-muted); font-size: .84rem; }.profile-section { margin-top: 1.5rem; }.profile-section h6 { margin: 0 0 1rem; color: var(--color-text-secondary); font-size: .72rem; font-weight: 700; letter-spacing: .07em; text-transform: uppercase; }.profile-section .text-muted.small { margin-bottom: .22rem; font-size: .72rem; text-transform: uppercase; letter-spacing: .035em; }.profile-section .fw-semibold { color: var(--app-text); font-size: .9rem; }.profile-documents { padding-top: 1.5rem; border-top: 1px solid var(--app-border); }
@media (max-width: 575.98px) { .modal-dialog { width:100%; }.employee-profile-hero { align-items: flex-start; gap: .85rem; }.employee-photo, .employee-photo-placeholder { width: 68px; height: 68px; }.employee-profile-title h4 { font-size: 1.05rem; } }
</style>
