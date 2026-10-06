<script setup>
import { computed, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'

import UserTable from '@/features/users/components/UserTable.vue'
import UserFormModal from '@/features/users/components/UserFormModal.vue'
import UserPermissionsModal from '@/features/users/components/UserPermissionsModal.vue'

import { userService } from '@/features/users/services/userService'
import UserDetailsModal from '@/features/users/components/UserDetailsModal.vue'
import { useAuthStore } from '@/features/auth/stores/authStore'

const authStore = useAuthStore()
const route = useRoute()
const router = useRouter()

const showUserDetails = ref(false)
const selectedUserId = ref(null)

const openView = (userId) => {
  selectedUserId.value = userId
  showUserDetails.value = true
}

const closeView = () => {
  showUserDetails.value = false
  selectedUserId.value = null
}

const showUserForm = ref(false)
const selectedUser = ref(null)
const userTable = ref(null)
const showPermissions = ref(false)

const openPermissions = (user) => {
  selectedUser.value = user
  showPermissions.value = true
}

const closePermissions = async () => {
  showPermissions.value = false
  selectedUser.value = null

  await userTable.value?.fetchUsers()
}

const openCreate = () => {
  selectedUser.value = null
  showUserForm.value = true
}

watch(
  () => route.query.create,
  (value) => {
    if (value) {
      openCreate()

      router.replace({
        path: '/users',
        query: {},
      })
    }
  },
  { immediate: true },
)

const openEdit = (user) => {
  selectedUser.value = user
  showUserForm.value = true
}

const closeForm = () => {
  showUserForm.value = false
  selectedUser.value = null
}

const saveUser = async (data) => {
  try {
    if (selectedUser.value) {
      await userService.updateUser(selectedUser.value.id, data)
    } else {
      await userService.createUser(data)
    }

    closeForm()

    await userTable.value.fetchUsers()
  } catch (error) {
    console.error('Failed to save user:', error)
  }
}

const isSuperAdmin = computed(() => {
  return authStore.user?.userType === 'SUPER_ADMIN'
})

const toggleUserStatus = async (user) => {
  try {
    const newStatus = user.status === 'ACTIVE' ? 'INACTIVE' : 'ACTIVE'

    await userService.updateStatus(user.id, newStatus)

    await userTable.value?.fetchUsers()
  } catch (err) {
    console.error('Failed to update user status:', err)
  }
}
</script>

<template>
  <div class="container-fluid app-page">
    <div class="page-heading">
      <div>
        <span class="page-kicker">Access control</span>
        <h1>Users</h1>

        <p>Manage accounts, access levels, and permissions.</p>
      </div>

      <button v-if="isSuperAdmin" class="btn btn-primary" @click="openCreate">
        <i class="bi bi-plus-lg"></i>
        <span>Add User</span>
      </button>
    </div>

    <UserTable
      :is-super-admin="isSuperAdmin"
      ref="userTable"
      @create="openCreate"
      @edit="openEdit"
      @permissions="openPermissions"
      @view="openView"
      @toggle-status="toggleUserStatus"
    />

    <UserFormModal
      :show="showUserForm"
      :user="selectedUser"
      @close="closeForm"
      @submit="saveUser"
    />

    <UserPermissionsModal
      :show="showPermissions"
      :user="selectedUser"
      @close="closePermissions"
      @saved="closePermissions"
    />

    <UserDetailsModal :show="showUserDetails" :user-id="selectedUserId" @close="closeView" />
  </div>
</template>
