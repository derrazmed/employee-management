<template>
  <div class="employee-grid">
    <template v-if="loading">
      <div v-for="n in 8" :key="n" class="sk-card" aria-hidden="true">
        <div class="sk-card-identity">
          <span class="skeleton sk-avatar-lg"></span>
          <span class="skeleton sk-line sk-line-lg sk-mt"></span>
          <span class="skeleton sk-line sk-line-md sk-mt-sm"></span>
          <span class="skeleton sk-line sk-line-sm sk-mt-sm"></span>
        </div>

        <span class="skeleton sk-line sk-line-md"></span>

        <div class="sk-card-actions">
          <span class="skeleton sk-btn"></span>
          <span class="skeleton sk-dot"></span>
        </div>
      </div>
    </template>

    <template v-else>
      <EmployeeCard
        v-for="employee in employees"
        :key="employee.id"
        :employee="employee"
        :menu-open="openMenuId === employee.id"
        @view="emit('view', $event)"
        @toggle-menu="(cardEmployee, event) => emit('toggle-menu', cardEmployee, event)"
      />
    </template>
  </div>
</template>

<script setup>
import EmployeeCard from './EmployeeCard.vue'

defineProps({
  employees: { type: Array, default: () => [] },
  loading: { type: Boolean, default: false },
  openMenuId: { type: [Number, String], default: null },
})

const emit = defineEmits(['view', 'toggle-menu', 'close-menu'])
</script>

<style scoped>
.employee-grid {
  display: grid;
  gap: 14px;
  padding: 14px;
  grid-template-columns: repeat(4, minmax(0, 1fr));
}

.sk-card {
  display: flex;
  min-width: 0;
  flex-direction: column;
  gap: 14px;
  padding: 20px 16px 16px;
  border: 1px solid var(--color-border);
  border-radius: 8px;
  background: var(--color-surface);
  pointer-events: none;
}

.sk-card-identity {
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
}

.sk-card-actions {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  margin-top: auto;
  padding-top: 12px;
  border-top: 1px solid var(--color-border-soft);
}

.sk-avatar-lg {
  display: block;
  width: 76px;
  height: 76px;
  border-radius: 50%;
}

.sk-line {
  display: block;
  height: 0.7rem;
  border-radius: 4px;
}

.sk-line-sm {
  width: 46%;
}

.sk-line-md {
  width: 68%;
}

.sk-line-lg {
  width: 86%;
}

.sk-mt {
  margin-top: 12px;
}

.sk-mt-sm {
  margin-top: 7px;
}

.sk-btn {
  display: block;
  width: 76px;
  height: 34px;
  border-radius: var(--radius-sm);
}

.sk-dot {
  display: block;
  width: 30px;
  height: 30px;
  border-radius: 6px;
}

@media (max-width: 1399.98px) {
  .employee-grid {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }
}

@media (max-width: 1199.98px) {
  .employee-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 575.98px) {
  .employee-grid {
    grid-template-columns: minmax(0, 1fr);
    padding: 12px;
  }
}
</style>
