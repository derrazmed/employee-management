<template>
  <div class="container-fluid app-page">
    <div class="page-heading">
      <div>
        <span class="page-kicker">Overview</span>
        <h1>{{ greeting }}, {{ firstName }}</h1>

        <p>Here&rsquo;s what&rsquo;s happening in your workspace today.</p>
      </div>
    </div>

    <div v-if="statsVisible" class="dashboard-stats" :style="statsStyle">
      <DashboardStatCard
        v-if="canRead"
        label="Total employees"
        :value="totalEmployees"
        icon="bi bi-people"
        :loading="employeesLoading"
        :error="employeesError"
        @retry="loadEmployees"
      />

      <DashboardStatCard
        v-if="canRead"
        label="Departments"
        :value="departmentsCount"
        icon="bi bi-buildings"
        :loading="employeesLoading"
        :error="employeesError"
        @retry="loadEmployees"
      />

      <DashboardStatCard
        v-if="canRead"
        label="New this month"
        :value="newThisMonth"
        icon="bi bi-person-plus"
        :caption="monthLabel"
        :loading="employeesLoading"
        :error="employeesError"
        @retry="loadEmployees"
      />

      <DashboardStatCard
        v-if="isSuperAdmin"
        label="Active users"
        :value="activeUsersCount"
        icon="bi bi-person-check"
        :caption="activeUsersCaption"
        :loading="usersLoading"
        :error="usersError"
        @retry="loadUsers"
      />
    </div>

    <div v-if="hasPanels" class="dashboard-main" :class="{ single: !canRead }">
      <EmployeeOverview
        v-if="canRead"
        :departments="topDepartments"
        :total="totalEmployees"
        :loading="employeesLoading"
        :error="employeesError"
        @retry="loadEmployees"
      />

      <div class="dashboard-side">
        <RecentActivity
          v-if="isSuperAdmin"
          :activities="activityItems"
          :loading="activityLoading"
          :error="activityError"
          @retry="loadActivity"
        />

        <QuickActions
          v-if="canCreateEmployee || canCreateUser"
          :can-create-employee="canCreateEmployee"
          :can-create-user="canCreateUser"
          @create-employee="goCreateEmployee"
          @create-user="goCreateUser"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'

import DashboardStatCard from '@/features/dashboard/components/DashboardStatCard.vue'
import EmployeeOverview from '@/features/dashboard/components/EmployeeOverview.vue'
import RecentActivity from '@/features/dashboard/components/RecentActivity.vue'
import QuickActions from '@/features/dashboard/components/QuickActions.vue'
import { useAuthStore } from '@/features/auth/stores/authStore'
import { useNotificationStore } from '@/features/notifications/stores/notificationStore'
import { employeeService } from '@/features/employees/services/employeeService'
import { userService } from '@/features/users/services/userService'
import { humanizeLabel, formatRelativeTime } from '@/utils/format'

const router = useRouter()
const authStore = useAuthStore()
const notificationStore = useNotificationStore()

const employeesPage = ref(null)
const employeesLoading = ref(false)
const employeesError = ref(false)

const usersPage = ref(null)
const usersLoading = ref(false)
const usersError = ref(false)

const canRead = computed(() => authStore.hasPermission('READ'))
const canCreateEmployee = computed(() => authStore.hasPermission('CREATE'))
const isSuperAdmin = computed(() => authStore.user?.userType === 'SUPER_ADMIN')
const canCreateUser = computed(() => isSuperAdmin.value)

const loadEmployees = async () => {
  if (!canRead.value) {
    return
  }

  employeesLoading.value = true
  employeesError.value = false

  try {
    const response = await employeeService.getEmployees(0, 1000, '')
    employeesPage.value = response.data
  } catch {
    employeesError.value = true
  } finally {
    employeesLoading.value = false
  }
}

const loadUsers = async () => {
  if (!isSuperAdmin.value) {
    return
  }

  usersLoading.value = true
  usersError.value = false

  try {
    const response = await userService.getUsers(0, 1000, '')
    usersPage.value = response.data
  } catch {
    usersError.value = true
  } finally {
    usersLoading.value = false
  }
}

const loadActivity = () => {
  return notificationStore.fetchNotifications()
}

const greeting = computed(() => {
  const hour = new Date().getHours()

  if (hour < 12) {
    return 'Good morning'
  }

  if (hour < 18) {
    return 'Good afternoon'
  }

  return 'Good evening'
})

const firstName = computed(() => {
  const name = String(authStore.user?.name || '').trim()

  return name ? name.split(/\s+/)[0] : 'there'
})

const employeeContent = computed(() => {
  const content = employeesPage.value?.content

  return Array.isArray(content) ? content : []
})

const totalEmployees = computed(() => employeesPage.value?.totalElements || 0)

const departmentCounts = computed(() => {
  const counts = new Map()

  for (const employee of employeeContent.value) {
    const name = String(employee.department || '').trim() || 'Unassigned'

    counts.set(name, (counts.get(name) || 0) + 1)
  }

  return Array.from(counts, ([name, count]) => ({ name, count })).sort(
    (a, b) => b.count - a.count || a.name.localeCompare(b.name),
  )
})

const topDepartments = computed(() => departmentCounts.value.slice(0, 5))

const departmentsCount = computed(
  () => departmentCounts.value.filter((dept) => dept.name !== 'Unassigned').length,
)

const now = new Date()

const monthPrefix = `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}`

const monthLabel = now.toLocaleDateString(undefined, { month: 'long', year: 'numeric' })

const newThisMonth = computed(() =>
  employeeContent.value.filter((employee) =>
    String(employee.hireDate || '').startsWith(monthPrefix),
  ).length,
)

const totalUsers = computed(() => usersPage.value?.totalElements || 0)

const activeUsersCount = computed(() => {
  const content = usersPage.value?.content

  if (!Array.isArray(content)) {
    return 0
  }

  return content.filter((user) => user.status === 'ACTIVE').length
})

const activeUsersCaption = computed(() =>
  usersPage.value ? `of ${totalUsers.value.toLocaleString()} accounts` : '',
)

const statsVisible = computed(() => canRead.value || isSuperAdmin.value)

const statsStyle = computed(() => ({
  '--stats-cols': String((canRead.value ? 1 : 0) + (isSuperAdmin.value ? 1 : 0)),
}))

const hasPanels = computed(
  () =>
    canRead.value ||
    isSuperAdmin.value ||
    canCreateEmployee.value ||
    canCreateUser.value,
)

const actionLabels = {
  CREATE: 'created',
  UPDATE: 'updated',
  DELETE: 'deleted',
}

const actionIcons = {
  CREATE: 'bi bi-person-plus',
  UPDATE: 'bi bi-pencil',
  DELETE: 'bi bi-trash',
}

const subjectFromNotification = (notification) => {
  const message = String(notification.message || '').trim()
  const stripped = message.replace(/^(created|updated|deleted)\s+(employee|user)\s+/i, '')

  return stripped || message || String(notification.details || '').trim()
}

const activityItems = computed(() =>
  notificationStore.notifications.slice(0, 6).map((notification) => {
    const entity = humanizeLabel(notification.entityType).trim()
    const action = actionLabels[notification.action] || 'updated'

    return {
      id: notification.id,
      icon: actionIcons[notification.action] || 'bi bi-info-circle',
      title: [entity, action].filter(Boolean).join(' '),
      subject: subjectFromNotification(notification),
      time: formatRelativeTime(notification.createdAt),
      datetime: String(notification.createdAt || ''),
    }
  }),
)

const activityLoading = computed(() => notificationStore.loading)
const activityError = computed(() => notificationStore.error)

const goCreateEmployee = () => {
  router.push({ path: '/employees', query: { create: '1' } })
}

const goCreateUser = () => {
  router.push({ path: '/users', query: { create: '1' } })
}

onMounted(() => {
  loadEmployees()
  loadUsers()

  if (
    isSuperAdmin.value &&
    !notificationStore.notifications.length &&
    !notificationStore.loading
  ) {
    loadActivity()
  }
})
</script>

<style scoped>
.dashboard-stats {
  display: grid;
  gap: 16px;
  grid-template-columns: repeat(var(--stats-cols, 4), minmax(0, 1fr));
  margin-bottom: 16px;
}

.dashboard-main {
  display: grid;
  gap: 16px;
  align-items: start;
  grid-template-columns: minmax(0, 3fr) minmax(0, 2fr);
}

.dashboard-main.single {
  grid-template-columns: minmax(0, 1fr);
}

.dashboard-side {
  display: flex;
  min-width: 0;
  flex-direction: column;
  gap: 16px;
}

@media (max-width: 991.98px) {
  .dashboard-stats {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .dashboard-main {
    grid-template-columns: minmax(0, 1fr);
  }
}

@media (max-width: 575.98px) {
  .dashboard-stats {
    grid-template-columns: minmax(0, 1fr);
  }
}
</style>
