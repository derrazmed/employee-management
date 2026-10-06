<template>
  <article class="stat-card">
    <div class="stat-card-top">
      <span class="stat-card-label">{{ label }}</span>
      <span class="stat-card-icon"><i :class="icon"></i></span>
    </div>

    <div class="stat-card-value">
      <span v-if="loading" class="skeleton stat-skeleton"></span>

      <span v-else-if="error" class="stat-card-fallback">&mdash;</span>

      <template v-else>
        <span class="stat-card-number">{{ formattedValue }}</span>
        <span v-if="caption" class="stat-card-caption">{{ caption }}</span>
      </template>
    </div>

    <div v-if="error && !loading" class="stat-card-error">
      <span>Couldn&rsquo;t load this stat</span>
      <button type="button" class="stat-retry" @click="$emit('retry')">Retry</button>
    </div>
  </article>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  label: { type: String, required: true },
  value: { type: [Number, String], default: 0 },
  icon: { type: String, required: true },
  caption: { type: String, default: '' },
  loading: { type: Boolean, default: false },
  error: { type: Boolean, default: false },
})

defineEmits(['retry'])

const formattedValue = computed(() =>
  typeof props.value === 'number' ? props.value.toLocaleString() : props.value,
)
</script>

<style scoped>
.stat-card {
  display: flex;
  min-width: 0;
  flex-direction: column;
  gap: 8px;
  padding: 16px 18px;
  border: 1px solid var(--color-border);
  border-radius: 8px;
  background: var(--color-surface);
}

.stat-card-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
}

.stat-card-label {
  min-width: 0;
  overflow: hidden;
  color: var(--color-text-secondary);
  font-size: 0.7rem;
  font-weight: 750;
  letter-spacing: 0.07em;
  text-overflow: ellipsis;
  text-transform: uppercase;
  white-space: nowrap;
}

.stat-card-icon {
  display: inline-flex;
  width: 30px;
  height: 30px;
  flex: 0 0 auto;
  align-items: center;
  justify-content: center;
  border-radius: 6px;
  background: var(--color-primary-soft);
  color: var(--color-primary-text);
  font-size: 0.95rem;
}

.stat-card-value {
  display: flex;
  min-height: 2.6rem;
  flex-direction: column;
  justify-content: center;
  gap: 3px;
}

.stat-card-number {
  color: var(--color-text);
  font-size: 1.75rem;
  font-weight: 750;
  letter-spacing: -0.03em;
  line-height: 1.15;
}

.stat-card-fallback {
  color: var(--color-text-tertiary);
  font-size: 1.75rem;
  font-weight: 750;
  line-height: 1.15;
}

.stat-card-caption {
  color: var(--color-text-secondary);
  font-size: 0.73rem;
  font-weight: 600;
}

.stat-skeleton {
  width: 78px;
  height: 2rem;
}

.stat-card-error {
  display: flex;
  align-items: center;
  gap: 8px;
  color: var(--color-danger-text);
  font-size: 0.73rem;
  font-weight: 650;
}

.stat-retry {
  padding: 0;
  border: 0;
  background: none;
  color: var(--color-primary-text);
  cursor: pointer;
  font-size: 0.73rem;
  font-weight: 750;
}
</style>
