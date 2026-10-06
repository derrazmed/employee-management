<template>
  <div class="employee-avatar" :class="`employee-avatar-${size}`">
    <img
      v-if="employee.photoObjectName && !photoFailed"
      :src="`/api/employees/${employee.id}/photo`"
      alt=""
      class="employee-avatar-image"
      @error="photoFailed = true"
    />

    <span v-else class="employee-avatar-initials" aria-hidden="true">{{ initials }}</span>
  </div>
</template>

<script setup>
import { computed, ref, watch } from 'vue'

const props = defineProps({
  employee: { type: Object, required: true },
  size: { type: String, default: 'md' },
})

const photoFailed = ref(false)

watch(
  () => props.employee.id,
  () => {
    photoFailed.value = false
  },
)

const initials = computed(() => {
  const first = String(props.employee.firstName || '').trim()
  const last = String(props.employee.lastName || '').trim()
  const letters = `${first.charAt(0)}${last.charAt(0)}`

  return (letters || first.charAt(0) || '?').toUpperCase()
})
</script>

<style scoped>
.employee-avatar {
  position: relative;
  display: inline-flex;
  flex: 0 0 auto;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  border: 1px solid var(--color-border);
  border-radius: 50%;
  background: var(--color-primary-soft);
  color: var(--color-primary-text);
}

.employee-avatar-sm {
  width: 44px;
  height: 44px;
}

.employee-avatar-md {
  width: 40px;
  height: 40px;
}

.employee-avatar-lg {
  width: 76px;
  height: 76px;
}

.employee-avatar-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.employee-avatar-initials {
  font-weight: 750;
  letter-spacing: 0.02em;
}

.employee-avatar-sm .employee-avatar-initials {
  font-size: 0.85rem;
}

.employee-avatar-md .employee-avatar-initials {
  font-size: 0.8rem;
}

.employee-avatar-lg .employee-avatar-initials {
  font-size: 1.4rem;
}
</style>
