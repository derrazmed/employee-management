import api, { refreshAccessToken } from '@/services/Axios.js'

export const authService = {
  async login(data) {
    const response = await api.post('/auth/login', data)

    return response.data
  },

  async register(data) {
    const response = await api.post('/auth/register', data)

    return response.data
  },

  async refresh() {
    const response = await refreshAccessToken()

    return response.data
  },

  async logout() {
    const response = await api.post('/auth/logout')

    return response.data
  },

  async requestPasswordReset(email) {
    const response = await api.post('/auth/password-reset/request', { email })

    return response.data
  },

  async verifyPasswordResetCode(email, code) {
    const response = await api.post('/auth/password-reset/verify', {
      email,
      code,
    })

    return response.data
  },

  async resetPassword(email, code, newPassword) {
    const response = await api.post('/auth/password-reset/complete', {
      email,
      code,
      newPassword,
    })

    return response.data
  },
}
