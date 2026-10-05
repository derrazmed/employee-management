<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { authService } from '@/features/auth/services/authService'
import PasswordInput from '@/components/common/PasswordInput.vue'

const router = useRouter()

const step = ref(1)

const email = ref('')
const code = ref('')
const newPassword = ref('')
const confirmPassword = ref('')

const loading = ref(false)
const errorMessage = ref('')
const successMessage = ref('')

const requestCode = async () => {
  errorMessage.value = ''
  successMessage.value = ''

  if (!email.value) {
    errorMessage.value = 'Please enter your email address.'
    return
  }

  loading.value = true

  try {
    await authService.requestPasswordReset(email.value)

    successMessage.value =
      'If an account exists with this email, a password reset code has been sent.'

    step.value = 2
  } catch (error) {
    errorMessage.value =
      error.response?.data?.message || 'Unable to process your request. Please try again.'
  } finally {
    loading.value = false
  }
}

const verifyCode = async () => {
  errorMessage.value = ''
  successMessage.value = ''

  if (!/^\d{6}$/.test(code.value)) {
    errorMessage.value = 'Please enter the 6-digit verification code.'
    return
  }

  loading.value = true

  try {
    await authService.verifyPasswordResetCode(email.value, code.value)

    successMessage.value = 'Verification code is valid.'

    step.value = 3
  } catch (error) {
    errorMessage.value = error.response?.data?.message || 'Invalid or expired verification code.'
  } finally {
    loading.value = false
  }
}

const resetPassword = async () => {
  errorMessage.value = ''
  successMessage.value = ''

  if (newPassword.value.length < 8) {
    errorMessage.value = 'Password must contain at least 8 characters.'
    return
  }

  if (newPassword.value !== confirmPassword.value) {
    errorMessage.value = 'Passwords do not match.'
    return
  }

  loading.value = true

  try {
    await authService.resetPassword(email.value, code.value, newPassword.value)

    successMessage.value = 'Your password has been reset successfully.'

    setTimeout(() => {
      router.push('/login')
    }, 1500)
  } catch (error) {
    errorMessage.value =
      error.response?.data?.message || 'Unable to reset your password. Please try again.'
  } finally {
    loading.value = false
  }
}

const goBack = () => {
  if (step.value === 1) {
    router.push('/login')
    return
  }

  step.value--
  errorMessage.value = ''
  successMessage.value = ''
}
</script>

<template>
  <div class="forgot-password-page">
    <div class="forgot-password-card">
      <!-- Header -->
      <div class="text-center mb-4">
        <h1 class="page-title">Reset your password</h1>

        <p class="page-subtitle">
          {{
            step === 1
              ? 'Enter your email address to receive a reset code.'
              : step === 2
                ? 'Enter the 6-digit code sent to your email.'
                : 'Create a new password for your account.'
          }}
        </p>
      </div>

      <!-- Progress -->
      <div class="reset-progress mb-4">
        <div class="progress-step" :class="{ active: step >= 1 }">
          <span>1</span>
          <small>Email</small>
        </div>

        <div class="progress-line"></div>

        <div class="progress-step" :class="{ active: step >= 2 }">
          <span>2</span>
          <small>Verify</small>
        </div>

        <div class="progress-line"></div>

        <div class="progress-step" :class="{ active: step >= 3 }">
          <span>3</span>
          <small>Reset</small>
        </div>
      </div>

      <!-- Error -->
      <div v-if="errorMessage" class="alert alert-danger">
        {{ errorMessage }}
      </div>

      <!-- Success -->
      <div v-if="successMessage" class="alert alert-success">
        {{ successMessage }}
      </div>

      <!-- STEP 1 -->
      <form v-if="step === 1" @submit.prevent="requestCode">
        <div class="mb-3">
          <label for="email" class="form-label"> Email address </label>

          <input
            id="email"
            v-model="email"
            type="email"
            class="form-control"
            placeholder="Enter your email"
            autocomplete="email"
            required
          />
        </div>

        <button type="submit" class="btn btn-primary w-100" :disabled="loading">
          {{ loading ? 'Sending...' : 'Send reset code' }}
        </button>
      </form>

      <!-- STEP 2 -->
      <form v-else-if="step === 2" @submit.prevent="verifyCode">
        <div class="mb-3">
          <label for="code" class="form-label"> Verification code </label>

          <input
            id="code"
            v-model="code"
            type="text"
            class="form-control text-center verification-code"
            placeholder="000000"
            maxlength="6"
            inputmode="numeric"
            autocomplete="one-time-code"
            required
          />

          <div class="form-text">Enter the 6-digit code sent to your email.</div>
        </div>

        <button type="submit" class="btn btn-primary w-100" :disabled="loading">
          {{ loading ? 'Verifying...' : 'Verify code' }}
        </button>
      </form>

      <!-- STEP 3 -->
      <form v-else @submit.prevent="resetPassword">
        <div class="mb-3">
          <label for="newPassword" class="form-label"> New password </label>
          <PasswordInput
            id="newPassword"
            v-model="newPassword"
            placeholder="Enter your new password"
            autocomplete-mode="new-password"
            required
          />
        </div>

        <div class="mb-3">
          <label for="confirmPassword" class="form-label"> Confirm password </label>
          <PasswordInput
            id="confirmPassword"
            v-model="confirmPassword"
            placeholder="Confirm your new password"
            autocomplete-mode="new-password"
            required
          />
        </div>

        <button type="submit" class="btn btn-primary w-100" :disabled="loading">
          {{ loading ? 'Resetting...' : 'Reset password' }}
        </button>
      </form>

      <!-- Back -->
      <div class="text-center mt-4">
        <button type="button" class="btn btn-link back-link" @click="goBack">
          ←
          {{ step === 1 ? 'Back to login' : 'Back' }}
        </button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.forgot-password-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 2rem 1rem;
  background: #f8f9fa;
}

.forgot-password-card {
  width: 100%;
  max-width: 440px;
  padding: 2rem;
  background: #ffffff;
  border: 1px solid #dee2e6;
  border-radius: 8px;
}

.page-title {
  font-size: 1.5rem;
  font-weight: 600;
  margin-bottom: 0.5rem;
  color: #212529;
}

.page-subtitle {
  margin: 0;
  color: #6c757d;
  font-size: 0.9rem;
}

.reset-progress {
  display: flex;
  align-items: center;
  justify-content: center;
}

.progress-step {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.25rem;
  color: #adb5bd;
}

.progress-step span {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 1px solid #ced4da;
  border-radius: 50%;
  font-size: 0.85rem;
}

.progress-step small {
  font-size: 0.7rem;
}

.progress-step.active {
  color: #0d6efd;
}

.progress-step.active span {
  color: #ffffff;
  background: #0d6efd;
  border-color: #0d6efd;
}

.progress-line {
  width: 50px;
  height: 1px;
  margin: 0 0.5rem 1rem;
  background: #dee2e6;
}

.verification-code {
  font-size: 1.25rem;
  letter-spacing: 0.35rem;
}

.back-link {
  color: #6c757d;
  text-decoration: none;
}

.back-link:hover {
  color: #0d6efd;
}
</style>
