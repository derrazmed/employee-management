<template>
  <article class="employee-card" :class="{ 'menu-open': menuOpen }">
    <div class="employee-card-identity">
      <EmployeeAvatar :employee="employee" size="lg" />

      <h3 class="employee-card-name">{{ fullName }}</h3>
      <p class="employee-card-role">{{ employee.jobTitle || 'Employee' }}</p>
      <p v-if="employee.department" class="employee-card-department" :title="employee.department">
        {{ employee.department }}
      </p>
    </div>

    <p class="employee-card-email" :title="employee.email">
      {{ employee.email || 'No email address' }}
    </p>

    <div class="employee-card-actions">
      <button
        v-if="authStore.hasPermission('READ')"
        type="button"
        class="btn btn-sm btn-outline-primary employee-card-view"
        @click="emit('view', employee)"
      >
        <i class="bi bi-eye"></i>
        <span>View</span>
      </button>

      <button
        v-if="authStore.hasPermission('UPDATE') || authStore.hasPermission('DELETE')"
        type="button"
        class="employee-menu-trigger employee-card-menu"
        aria-haspopup="menu"
        :aria-expanded="menuOpen"
        :aria-label="`Actions for ${fullName}`"
        title="Employee actions"
        @click="emit('toggle-menu', employee, $event)"
      >
        <i class="bi bi-three-dots-vertical"></i>
      </button>
    </div>
  </article>
</template>

<script setup>
import { computed } from 'vue'
import EmployeeAvatar from './EmployeeAvatar.vue'
import { useAuthStore } from '@/features/auth/stores/authStore'

const props = defineProps({
  employee: { type: Object, required: true },
  menuOpen: { type: Boolean, default: false },
})

const emit = defineEmits(['view', 'toggle-menu'])

const authStore = useAuthStore()

const fullName = computed(
  () => `${props.employee.firstName || ''} ${props.employee.lastName || ''}`.trim() || 'Employee',
)
</script>

<style scoped>
.employee-card {
  display: flex;
  min-width: 0;
  flex-direction: column;
  gap: 14px;
  padding: 20px 16px 16px;
  border: 1px solid var(--color-border);
  border-radius: 8px;
  background: var(--color-surface);
  transition:
    background 0.15s ease,
    border-color 0.15s ease;
}

.employee-card:hover {
  border-color: var(--color-border-strong);
  background: var(--color-hover);
}

.employee-card.menu-open {
  border-color: var(--color-primary-border);
  background: var(--color-primary-soft);
}

.employee-card-identity {
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
}

.employee-card-name {
  margin: 12px 0 0;
  overflow-wrap: anywhere;
  color: var(--color-text);
  font-size: 0.95rem;
  font-weight: 750;
  letter-spacing: -0.01em;
  line-height: 1.25;
}

.employee-card-role {
  margin: 4px 0 0;
  overflow-wrap: anywhere;
  color: var(--color-text-body);
  font-size: 0.8rem;
  font-weight: 500;
  line-height: 1.35;
}

.employee-card-department {
  margin: 3px 0 0;
  overflow: hidden;
  max-width: 100%;
  color: var(--color-text-secondary);
  font-size: 0.76rem;
  font-weight: 650;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.employee-card-email {
  margin: 0;
  overflow: hidden;
  color: var(--color-text-secondary);
  font-size: 0.78rem;
  text-align: center;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.employee-card-actions {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  margin-top: auto;
  padding-top: 12px;
  border-top: 1px solid var(--color-border-soft);
}

.employee-card-view {
  min-height: 34px;
  gap: 0.4rem;
  padding-inline: 12px;
  font-size: 0.78rem;
}

.employee-card-menu {
  margin-left: auto;
}
</style>
