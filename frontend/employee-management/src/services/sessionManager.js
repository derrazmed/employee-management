import { authService } from '@/features/auth/services/authService'

const INACTIVITY_LIMIT = 90 * 1000
const WARNING_DURATION = 30 * 1000
const PROACTIVE_REFRESH_INTERVAL = 90 * 1000

let inactivityTimer = null
let warningTimer = null
let proactiveRefreshTimer = null

let onWarning = null
let onLogout = null

let isSessionActive = false
let isWarningVisible = false
let refreshPromise = null

let lastActivity = 0
let lastRefreshAt = 0

const activityEvents = [
  'mousemove',
  'mousedown',
  'keydown',
  'scroll',
  'touchstart',
  'pointerdown',
  'click',
]

const clearWarning = () => {
  if (!isWarningVisible) {
    return
  }

  isWarningVisible = false
  clearTimeout(warningTimer)
  warningTimer = null
  onWarning?.(false)
}

const handleActivity = () => {
  if (!isSessionActive || isWarningVisible) {
    return
  }

  lastActivity = Date.now()

  resetInactivityTimer()

  if (lastActivity - lastRefreshAt >= PROACTIVE_REFRESH_INTERVAL) {
    refreshSession().catch(() => {})
  }
}

const resetInactivityTimer = () => {
  if (!isSessionActive || isWarningVisible) {
    return
  }

  clearTimeout(inactivityTimer)

  inactivityTimer = setTimeout(() => {
    showInactivityWarning()
  }, INACTIVITY_LIMIT)
}

const showInactivityWarning = () => {
  if (!isSessionActive || isWarningVisible) {
    return
  }

  isWarningVisible = true
  clearTimeout(proactiveRefreshTimer)
  proactiveRefreshTimer = null

  onWarning?.(true)

  clearTimeout(warningTimer)

  warningTimer = setTimeout(() => {
    void handleAutomaticLogout()
  }, WARNING_DURATION)
}

const handleAutomaticLogout = async () => {
  if (!isSessionActive) {
    return
  }

  const logout = onLogout
  stop()

  await logout?.()
}

const scheduleProactiveRefresh = () => {
  clearTimeout(proactiveRefreshTimer)

  if (!isSessionActive || isWarningVisible) {
    proactiveRefreshTimer = null
    return
  }

  proactiveRefreshTimer = setTimeout(() => {
    if (Date.now() - lastActivity < INACTIVITY_LIMIT) {
      refreshSession().catch(() => {})
    }
  }, PROACTIVE_REFRESH_INTERVAL)
}

const refreshSession = async (force = false) => {
  if (!isSessionActive) {
    return false
  }

  if (!force && (isWarningVisible || Date.now() - lastActivity >= INACTIVITY_LIMIT)) {
    return false
  }

  if (refreshPromise) {
    return refreshPromise
  }

  refreshPromise = (async () => {
    try {
      await authService.refresh()

      if (isSessionActive) {
        lastRefreshAt = Date.now()
        scheduleProactiveRefresh()
      }

      return true
    } catch (error) {
      console.error('Session refresh failed:', error)

      await handleAutomaticLogout()
      throw error
    } finally {
      refreshPromise = null
    }
  })()

  return refreshPromise
}

const start = (callbacks = {}) => {
  onWarning = callbacks.onWarning || onWarning
  onLogout = callbacks.onLogout || onLogout

  if (isSessionActive) {
    return
  }

  isSessionActive = true
  lastActivity = Date.now()
  lastRefreshAt = Date.now()

  activityEvents.forEach((event) => {
    window.addEventListener(event, handleActivity, { passive: true })
  })

  resetInactivityTimer()
  scheduleProactiveRefresh()
}

const stop = () => {
  const wasWarningVisible = isWarningVisible
  isSessionActive = false
  isWarningVisible = false

  clearTimeout(inactivityTimer)
  clearTimeout(warningTimer)
  clearTimeout(proactiveRefreshTimer)

  inactivityTimer = null
  warningTimer = null
  proactiveRefreshTimer = null

  activityEvents.forEach((event) => {
    window.removeEventListener(event, handleActivity)
  })

  if (wasWarningVisible) {
    onWarning?.(false)
  }

  onWarning = null
  onLogout = null
}

const stayLoggedIn = async () => {
  if (!isSessionActive) {
    return
  }

  clearWarning()
  lastActivity = Date.now()
  resetInactivityTimer()

  await refreshSession(true)
}

export const sessionManager = {
  start,
  stop,
  stayLoggedIn,
  refreshSession,
}
