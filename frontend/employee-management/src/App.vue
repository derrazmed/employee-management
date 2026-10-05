<script setup>
import { onMounted } from 'vue'
import { storeToRefs } from 'pinia'
import { useAuthStore } from '@/features/auth/stores/authStore'
import SessionTimeoutModal from '@/components/common/SessionTimeoutModal.vue'

const authStore = useAuthStore()

const { sessionWarningVisible } = storeToRefs(authStore)

onMounted(() => {
  authStore.initializeSession()
})

const handleStayLoggedIn = async () => {
  await authStore.stayLoggedIn()
}

const handleLogout = async () => {
  await authStore.logout()
}
</script>

<template>
  <RouterView />

  <SessionTimeoutModal
    :visible="sessionWarningVisible"
    @stay-logged-in="handleStayLoggedIn"
    @logout="handleLogout"
  />
</template>
