<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/features/auth/stores/authStore'
import PasswordInput from '@/components/common/PasswordInput.vue'

const router = useRouter()
const authStore = useAuthStore()

const email = ref('')
const password = ref('')
const error = ref('')

const login = async () => {
  try {
    await authStore.login(email.value, password.value)

    await router.push('/')
  } catch (err) {
    console.error('Login failed:', err)
    error.value = err.response?.data?.message || 'Login failed. Please check your credentials.'
  }
}
</script>

<template>
  <div class="auth-page">
    <div class="auth-card">
      <h2 class="text-center mb-4">Login</h2>

      <form @submit.prevent="login">
        <div class="mb-3">
          <label for="email" class="form-label"> Email </label>

          <input
            id="email"
            v-model="email"
            type="email"
            class="form-control"
            placeholder="Enter your email"
            required
          />
        </div>

        <div class="mb-3">
          <label for="password" class="form-label"> Password </label>
          <PasswordInput
            id="password"
            v-model="password"
            placeholder="Enter your password"
            autocomplete-mode="current-password"
            required
          />
        </div>
        <div class="text-end mb-3">
          <RouterLink to="/forgot-password" class="forgot-password-link">
            Forgot your password?
          </RouterLink>
        </div>
        <div v-if="error" class="alert alert-danger" role="alert">
          {{ error }}
        </div>
        <button type="submit" class="btn btn-primary w-100">Login</button>
      </form>

      <div class="text-center mt-3">
        Don't have an account?

        <RouterLink to="/register"> Register </RouterLink>
      </div>
    </div>
  </div>
</template>

<style scoped>
.auth-page {
  min-height: 100vh;
  display: flex;
  justify-content: center;
  align-items: flex-start;
  padding-top: 60px;
  background-color: #f8f9fa;
}

.auth-card {
  width: 100%;
  max-width: 400px;
  padding: 30px;
  background-color: #ffffff;
  border: 1px solid #dee2e6;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}
</style>
