<template>
  <section class="dashboard-panel">
    <header class="dashboard-panel-header">
      <div class="dashboard-panel-heading">
        <span class="dashboard-panel-icon"><i class="bi bi-clock-history"></i></span>
        <h2 class="dashboard-panel-title">Recent activity</h2>
      </div>

      <span v-if="!loading && !error && activities.length" class="panel-meta">Last {{ activities.length }}</span>
    </header>

    <div class="dashboard-panel-body">
      <ul v-if="loading" class="activity-list" aria-hidden="true">
        <li v-for="n in 4" :key="n" class="activity-row">
          <span class="skeleton activity-skeleton-icon"></span>

          <div class="activity-body">
            <span class="skeleton activity-skeleton-title"></span>
            <span class="skeleton activity-skeleton-subject"></span>
          </div>

          <span class="skeleton activity-skeleton-time"></span>
        </li>
      </ul>

      <div v-else-if="error" class="dashboard-error">
        <span>Unable to load recent activity.</span>
        <button type="button" class="dashboard-retry" @click="$emit('retry')">Retry</button>
      </div>

      <div v-else-if="!activities.length" class="panel-empty">
        <i class="bi bi-clock-history"></i>
        <p>No recent activity yet.</p>
      </div>

      <ul v-else class="activity-list">
        <li v-for="item in activities" :key="item.id" class="activity-row">
          <span class="activity-icon"><i :class="item.icon"></i></span>

          <div class="activity-body">
            <p class="activity-title">{{ item.title }}</p>
            <p class="activity-subject" :title="item.subject">{{ item.subject }}</p>
          </div>

          <time class="activity-time" :datetime="item.datetime">{{ item.time }}</time>
        </li>
      </ul>
    </div>
  </section>
</template>

<script setup>
defineProps({
  activities: { type: Array, default: () => [] },
  loading: { type: Boolean, default: false },
  error: { type: Boolean, default: false },
})

defineEmits(['retry'])
</script>

<style scoped>
.activity-list {
  display: flex;
  flex-direction: column;
  margin: 0;
  padding: 0;
  list-style: none;
}

.activity-row {
  display: flex;
  min-width: 0;
  align-items: center;
  gap: 12px;
  padding: 11px 0;
  border-bottom: 1px solid var(--color-border-soft);
}

.activity-row:first-child {
  padding-top: 2px;
}

.activity-row:last-child {
  padding-bottom: 2px;
  border-bottom: 0;
}

.activity-icon {
  display: inline-flex;
  width: 28px;
  height: 28px;
  flex: 0 0 auto;
  align-items: center;
  justify-content: center;
  border-radius: 6px;
  background: var(--color-primary-soft);
  color: var(--color-primary-text);
  font-size: 0.85rem;
}

.activity-body {
  display: flex;
  min-width: 0;
  flex: 1;
  flex-direction: column;
  gap: 3px;
}

.activity-title {
  margin: 0;
  color: var(--color-text);
  font-size: 0.82rem;
  font-weight: 700;
}

.activity-subject {
  margin: 0;
  overflow: hidden;
  color: var(--color-text-secondary);
  font-size: 0.78rem;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.activity-time {
  flex: 0 0 auto;
  color: var(--color-text-tertiary);
  font-size: 0.7rem;
  font-weight: 650;
  white-space: nowrap;
}

.activity-skeleton-icon {
  width: 28px;
  height: 28px;
  flex: 0 0 auto;
  border-radius: 6px;
}

.activity-skeleton-title {
  width: 110px;
  height: 0.7rem;
}

.activity-skeleton-subject {
  width: 75px;
  height: 0.65rem;
}

.activity-skeleton-time {
  width: 44px;
  height: 0.6rem;
  flex: 0 0 auto;
}
</style>
