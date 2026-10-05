import api from '@/services/Axios'

export const userService = {
  async getUsers(page = 0, size = 10, search = '') {
    const response = await api.get('/users', {
      params: {
        page,
        size,
        search,
      },
    })

    return response.data
  },

  async getUser(id) {
    const response = await api.get(`/users/${id}`)

    return response.data
  },

  async createUser(data) {
    const response = await api.post('/users', data)

    return response.data
  },

  async updateUser(id, data) {
    const response = await api.put(`/users/${id}`, data)

    return response.data
  },

  async deleteUser(id) {
    const response = await api.delete(`/users/${id}`)

    return response.data
  },

  async updateStatus(id, status) {
    const response = await api.put(`/users/${id}/status`, {
      status,
    })

    return response.data
  },

  async updatePermissions(id, permissions) {
    const response = await api.put(`/users/${id}/permissions`, {
      permissions,
    })

    return response.data
  },
}
