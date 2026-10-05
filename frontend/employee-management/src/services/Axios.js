import axios from 'axios'

const api = axios.create({
  baseURL: '/api',
  withCredentials: true,
  headers: {
    'Content-Type': 'application/json',
  },
})

let refreshPromise = null
let authFailureHandler = null

export const registerAuthFailureHandler = (handler) => {
  authFailureHandler = handler
}

const isAuthEndpoint = (url = '') => {
  const path = url.split('?')[0]

  return ['/auth/login', '/auth/refresh', '/auth/logout'].some((endpoint) =>
    path.endsWith(endpoint),
  )
}

export const refreshAccessToken = () => {
  if (!refreshPromise) {
    refreshPromise = api
      .post('/auth/refresh')
      .catch(async (error) => {
        try {
          if (authFailureHandler) {
            await authFailureHandler()
          } else {
            localStorage.removeItem('user')
            window.location.assign('/login')
          }
        } catch (logoutError) {
          console.error('Failed to clear the local session:', logoutError)
        }

        throw error
      })
      .finally(() => {
        refreshPromise = null
      })
  }

  return refreshPromise
}

api.interceptors.response.use(
  (response) => {
    return response
  },

  async (error) => {
    const originalRequest = error.config

    if (
      error.response?.status !== 401 ||
      !originalRequest ||
      originalRequest._retry ||
      isAuthEndpoint(originalRequest.url)
    ) {
      throw error
    }

    originalRequest._retry = true

    await refreshAccessToken()
    return api(originalRequest)
  },
)

export default api
