import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { authService } from '../services/authService'
import { sessionManager } from '@/services/sessionManager'
import { registerAuthFailureHandler } from '@/services/Axios'

export const useAuthStore = defineStore('auth', () => {
  const router = useRouter()
  let storedUser = null

  try {
    const serializedUser = localStorage.getItem('user')
    storedUser = serializedUser ? JSON.parse(serializedUser) : null
  } catch {
    localStorage.removeItem('user')
  }

  const user = ref(storedUser && typeof storedUser === 'object' ? storedUser : null)
  const sessionWarningVisible = ref(false)
  let logoutPromise = null

  const isAuthenticated = computed(() => !!user.value)

  const login = async (email, password) => {
    const response = await authService.login({
      email,
      password,
    })

    const data = response.data

    sessionManager.stop()
    sessionWarningVisible.value = false

    user.value = {
      userId: data.userId,
      name: data.name,
      email: data.email,
      userType: data.userType,
      permissions: data.permissions || [],
    }

    localStorage.setItem('user', JSON.stringify(user.value))

    sessionManager.start({
      onWarning: handleSessionWarning,
      onLogout: handleSessionLogout,
    })

    return response
  }

  const handleSessionWarning = (visible) => {
    sessionWarningVisible.value = visible
  }

  const handleSessionLogout = async () => {
    sessionWarningVisible.value = false

    await logout()
  }

  const stayLoggedIn = async () => {
    try {
      await sessionManager.stayLoggedIn()

      sessionWarningVisible.value = false
    } catch (error) {
      console.error('Failed to refresh session:', error)
    }
  }

  const logout = () => {
    if (logoutPromise) {
      return logoutPromise
    }

    sessionManager.stop()
    user.value = null
    localStorage.removeItem('user')
    sessionWarningVisible.value = false

    logoutPromise = (async () => {
      try {
        await authService.logout()
      } catch (error) {
        console.error('Logout request failed:', error)
      } finally {
        sessionWarningVisible.value = false

        try {
          if (router.currentRoute.value.name !== 'login') {
            await router.push('/login')
          }
        } finally {
          logoutPromise = null
        }
      }
    })()

    return logoutPromise
  }

  const hasPermission = (permission) => {
    return Array.isArray(user.value?.permissions) && user.value.permissions.includes(permission)
  }

  registerAuthFailureHandler(logout)

  const initializeSession = () => {
    if (!user.value) {
      return
    }

    sessionManager.start({
      onWarning: handleSessionWarning,
      onLogout: handleSessionLogout,
    })
  }

  return {
    user,
    isAuthenticated,
    sessionWarningVisible,
    initializeSession,
    login,
    logout,
    stayLoggedIn,
    hasPermission,
  }
})
