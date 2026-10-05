<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { authService } from '@/features/auth/services/authService'
import PasswordInput from '@/components/common/PasswordInput.vue'

const name = ref('')
const email = ref('')
const password = ref('')
const confirmPassword = ref('')

const router = useRouter()

const register = async () => {
  if (password.value !== confirmPassword.value) {
    console.log('Passwords do not match')
    return
  }

  try {
    const response = await authService.register({
      name: name.value,
      email: email.value,
      password: password.value,
    })

    console.log('Registration successful:', response)
    await router.push('/login')
  } catch (error) {
    console.error('Registration failed:', error)
  }
}
</script>

<template>
  <div class="auth-page">
    <div class="auth-card">
      <h2 class="text-center mb-4">Register</h2>

      <form @submit.prevent="register">
        <div class="mb-3">
          <label for="name" class="form-label"> Name </label>

          <input
            id="name"
            v-model="name"
            type="text"
            class="form-control"
            placeholder="Enter your name"
            required
          />
        </div>

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
            autocomplete-mode="new-password"
            required
          />
        </div>

        <div class="mb-3">
          <label for="confirmPassword" class="form-label"> Confirm Password </label>
          <PasswordInput
            id="confirmPassword"
            v-model="confirmPassword"
            placeholder="Confirm your password"
            autocomplete-mode="new-password"
            required
          />
        </div>

        <button type="submit" class="btn btn-primary w-100">Register</button>
      </form>

      <div class="text-center mt-3">
        Already have an account?

        <RouterLink to="/login"> Login </RouterLink>
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
