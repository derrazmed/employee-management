import { createRouter, createWebHistory } from 'vue-router'

import LoginView from '@/views/auth/LoginView.vue'
import RegisterView from '@/views/auth/RegisterView.vue'
import ForgotPasswordView from '@/views/auth/ForgotPasswordView.vue'
import DashboardLayout from '@/layout/DashboardLayout.vue'
import DashboardView from '@/views/dashboard/DashboardView.vue'
import UsersView from '@/views/users/UsersView.vue'
import EmployeesView from '@/views/employees/EmployeesView.vue'

const router = createRouter({
  history: createWebHistory(),

  routes: [
    // Public pages
    {
      path: '/login',
      name: 'login',
      component: LoginView,
      meta: {
        guest: true,
      },
    },

    {
      path: '/register',
      name: 'register',
      component: RegisterView,
      meta: {
        guest: true,
      },
    },

    {
      path: '/forgot-password',
      name: 'forgot-password',
      component: ForgotPasswordView,
      meta: {
        guest: true,
      },
    },

    {
      path: '/',
      component: DashboardLayout,
      meta: {
        requiresAuth: true,
      },

      children: [
        {
          path: '',
          redirect: '/dashboard',
        },

        {
          path: 'dashboard',
          name: 'dashboard',
          component: DashboardView,
        },

        {
          path: 'users',
          name: 'users',
          component: UsersView,
          meta: {
            requiresSuperAdmin: true,
          },
        },

        {
          path: 'employees',
          name: 'employees',
          component: EmployeesView,
        },
      ],
    },
  ],
})

router.beforeEach((to) => {
  const storedUser = localStorage.getItem('user')

  const user = storedUser ? JSON.parse(storedUser) : null

  const isAuthenticated = !!user

  if (to.meta.requiresAuth && !isAuthenticated) {
    return {
      name: 'login',
    }
  }

  if (to.meta.guest && isAuthenticated) {
    return {
      path: '/dashboard',
    }
  }

  if (to.meta.requiresSuperAdmin && user?.userType !== 'SUPER_ADMIN') {
    return {
      path: '/employees',
      query: {
        error: 'Only Super Administrators can manage users',
      },
    }
  }

  return true
})

export default router
