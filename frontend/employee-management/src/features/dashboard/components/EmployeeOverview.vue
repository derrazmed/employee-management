<template>
  <section class="dashboard-panel">
    <header class="dashboard-panel-header">
      <div class="dashboard-panel-heading">
        <span class="dashboard-panel-icon"><i class="bi bi-diagram-3"></i></span>
        <h2 class="dashboard-panel-title">Employees by department</h2>
      </div>

      <span v-if="!loading && !error && total" class="panel-meta">
        {{ total.toLocaleString() }} total
      </span>
    </header>

    <div class="dashboard-panel-body">
      <div v-if="loading" class="dept-list" aria-hidden="true">
        <div v-for="n in 4" :key="n" class="dept-row">
          <span class="skeleton dept-skeleton-text"></span>
          <span class="skeleton dept-skeleton-bar"></span>
        </div>
      </div>

      <div v-else-if="error" class="dashboard-error">
        <span>Unable to load department overview.</span>
        <button type="button" class="dashboard-retry" @click="$emit('retry')">Retry</button>
      </div>

      <div v-else-if="!departments.length" class="panel-empty">
        <i class="bi bi-buildings"></i>
        <p>No department data yet.</p>
      </div>

      <ul v-else class="dept-list">
        <li v-for="dept in departments" :key="dept.name" class="dept-row">
          <div class="dept-row-head">
            <span class="dept-name" :title="dept.name">{{ dept.name }}</span>
            <span class="dept-count">{{ dept.count }}</span>
          </div>

          <div class="dept-track">
            <span class="dept-fill" :style="{ width: barWidth(dept.count) }"></span>
          </div>
        </li>
      </ul>
    </div>
  </section>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  departments: { type: Array, default: () => [] },
  total: { type: Number, default: 0 },
  loading: { type: Boolean, default: false },
  error: { type: Boolean, default: false },
})

defineEmits(['retry'])

const maxCount = computed(() =>
  props.departments.reduce((max, dept) => Math.max(max, dept.count), 0),
)

const barWidth = (count) => {
  if (!maxCount.value) {
    return '0%'
  }

  const percentage = Math.round((count / maxCount.value) * 100)

  return `${Math.max(percentage, 4)}%`
}
</script>

<style scoped>
.dept-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
  margin: 0;
  padding: 0;
  list-style: none;
}

.dept-row {
  display: flex;
  min-width: 0;
  flex-direction: column;
  gap: 7px;
}

.dept-row-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
}

.dept-name {
  min-width: 0;
  overflow: hidden;
  color: var(--color-text-body);
  font-size: 0.82rem;
  font-weight: 650;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.dept-count {
  flex: 0 0 auto;
  color: var(--color-text-secondary);
  font-size: 0.78rem;
  font-weight: 750;
}

.dept-track {
  height: 8px;
  overflow: hidden;
  border-radius: 4px;
  background: var(--color-hover-strong);
}

.dept-fill {
  display: block;
  height: 100%;
  border-radius: 4px;
  background: var(--color-primary);
  transition: width 0.3s ease;
}

.dept-skeleton-text {
  width: 42%;
  height: 0.75rem;
}

.dept-skeleton-bar {
  width: 100%;
  height: 8px;
}
</style>
