import api from '@/services/Axios'

export const employeeService = {
  async getEmployees(page = 0, size = 10, search = '', filters = {}) {
    const response = await api.get('/employees', {
      params: {
        page,
        size,
        search,
        department: filters.department || '',
        jobTitle: filters.jobTitle || '',
      },
    })

    return response.data
  },

  async getFilters() {
    const response = await api.get('/employees/filters')

    return response.data
  },

  async getEmployee(id) {
    const response = await api.get(`/employees/${id}`)
    return response.data
  },

  async createEmployee(data) {
    const response = await api.post('/employees', data)
    return response.data
  },

  async updateEmployee(id, data) {
    const response = await api.put(`/employees/${id}`, data)
    return response.data
  },

  async deleteEmployee(id) {
    const response = await api.delete(`/employees/${id}`)
    return response.data
  },

  async getPhoto(id) {
    const response = await api.get(`/employees/${id}/photo`)

    return response.data.data.url
  },

  async uploadPhoto(id, file) {
    const formData = new FormData()

    formData.append('file', file)

    const response = await api.post(`/employees/${id}/photo`, formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    })

    return response.data
  },

  async deletePhoto(id) {
    const response = await api.delete(`/employees/${id}/photo`)
    return response.data
  },

  async getCv(id) {
    const response = await api.get(`/employees/${id}/cv`, {
      responseType: 'blob',
    })

    return response.data
  },

  async uploadCv(id, file) {
    const formData = new FormData()

    formData.append('file', file)

    const response = await api.post(`/employees/${id}/cv`, formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    })

    return response.data
  },

  async downloadCv(id) {
    const response = await api.get(`/employees/${id}/cv/download`, {
      responseType: 'blob',
    })

    return response.data
  },

  async deleteCv(id) {
    const response = await api.delete(`/employees/${id}/cv`)
    return response.data
  },

  async getContract(id) {
    const response = await api.get(`/employees/${id}/contract`, {
      responseType: 'blob',
    })

    return response.data
  },
}
