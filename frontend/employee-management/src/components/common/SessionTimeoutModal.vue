<script setup>
import { onUnmounted, ref, watch } from 'vue'

const props = defineProps({
  visible: {
    type: Boolean,
    default: false,
  },
})

const emit = defineEmits(['stay-logged-in', 'logout'])

const remainingSeconds = ref(30)

let countdownTimer = null

const startCountdown = () => {
  clearInterval(countdownTimer)

  remainingSeconds.value = 30

  countdownTimer = setInterval(() => {
    if (remainingSeconds.value > 0) {
      remainingSeconds.value--
    }
  }, 1000)
}

watch(
  () => props.visible,
  (visible) => {
    if (visible) {
      startCountdown()
    } else {
      clearInterval(countdownTimer)
    }
  },
)

const stayLoggedIn = () => {
  clearInterval(countdownTimer)
  emit('stay-logged-in')
}

const logout = () => {
  clearInterval(countdownTimer)
  emit('logout')
}

onUnmounted(() => {
  clearInterval(countdownTimer)
})
</script>

<template>
  <div v-if="visible" class="session-timeout-backdrop">
    <div class="session-modal">
      <div class="session-icon">
        <i class="bi bi-clock-history"></i>
      </div>

      <h4 class="mb-3">Are you still there?</h4>

      <p class="text-muted mb-4">
        You have been inactive for 90 seconds. Your session will be logged out in
        <strong>{{ remainingSeconds }}</strong>
        seconds.
      </p>

      <div class="d-flex gap-2 justify-content-center">
        <button type="button" class="btn btn-outline-secondary" @click="logout">Logout</button>

        <button type="button" class="btn btn-primary" @click="stayLoggedIn">Stay logged in</button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.session-modal {
  width: 100%;
  max-width: 440px;
  padding: 1.5rem;
  border: 1px solid #e1e5eb;
  background: #fff;
  border-radius: 7px;

  text-align: center;

  box-shadow: 0 0.8rem 2rem rgba(24, 32, 40, 0.13);
}

.session-icon {
  display: inline-flex;
  width: 42px;
  height: 42px;
  align-items: center;
  justify-content: center;
  margin-bottom: 0.75rem;
  border-radius: 50%;
  background: #f1f4f8;
  color: #526075;
  font-size: 1rem;
}
</style>
