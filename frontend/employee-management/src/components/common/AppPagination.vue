<script setup>
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'

const props = defineProps({
  currentPage: { type: Number, required: true },
  pageSize: { type: Number, required: true },
  totalPages: { type: Number, required: true },
  totalElements: { type: Number, required: true },
  pageSizes: { type: Array, default: () => [5, 10, 20, 50] },
  noun: { type: String, default: 'results' },
})

const emit = defineEmits(['page-change', 'page-size-change'])

const isMobile = ref(false)
const isNarrowMobile = ref(false)

const updateViewport = () => {
  isMobile.value = window.matchMedia('(max-width: 575.98px)').matches
  isNarrowMobile.value = window.matchMedia('(max-width: 380px)').matches
}

onMounted(() => {
  updateViewport()
  window.addEventListener('resize', updateViewport)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', updateViewport)
})

const pages = computed(() => {
  const total = props.totalPages
  const current = props.currentPage + 1
  const windowSize = isNarrowMobile.value ? 1 : isMobile.value ? 3 : 5
  const smallPageCount = isMobile.value ? 6 : 7
  if (total <= smallPageCount) return Array.from({ length: total }, (_, index) => index + 1)

  let start
  let end
  if (current <= Math.ceil(windowSize / 2) + 1) {
    start = 2
    end = Math.min(total - 1, windowSize)
  } else if (current > total - Math.ceil(windowSize / 2) - 1) {
    start = Math.max(2, total - windowSize + 1)
    end = total - 1
  } else {
    start = current - Math.floor(windowSize / 2)
    end = current + Math.floor(windowSize / 2)
  }

  const result = [1]
  if (start > 2) result.push(start === 3 ? 2 : 'start-ellipsis')
  for (let page = start; page <= end; page += 1) result.push(page)
  if (end < total - 1) result.push(end === total - 2 ? total - 1 : 'end-ellipsis')
  result.push(total)
  return result
})

const rangeStart = computed(() =>
  props.totalElements ? props.currentPage * props.pageSize + 1 : 0,
)
const rangeEnd = computed(() =>
  Math.min((props.currentPage + 1) * props.pageSize, props.totalElements),
)

const setPage = (page) => {
  if (page >= 0 && page < props.totalPages && page !== props.currentPage) emit('page-change', page)
}
</script>

<template>
  <section
    v-if="totalPages > 0"
    class="app-pagination"
    :class="{ 'is-single-page': totalPages === 1 }"
    :aria-label="`${noun} pagination`"
  >
    <p class="pagination-summary">
      {{ rangeStart.toLocaleString() }}–{{ rangeEnd.toLocaleString() }} of
      {{ totalElements.toLocaleString() }} {{ noun }}
    </p>

    <nav v-if="totalPages > 1" class="pagination-nav" :aria-label="`${noun} pages`">
      <button
        class="pagination-nav-button"
        type="button"
        :disabled="currentPage === 0"
        aria-label="Previous page"
        @click="setPage(currentPage - 1)"
      >
        <i class="bi bi-chevron-left"></i><span>Back</span>
      </button>
      <template v-for="page in pages" :key="page">
        <span v-if="typeof page === 'string'" class="pagination-ellipsis" aria-hidden="true"
          >…</span
        >
        <button
          v-else
          class="pagination-number"
          :class="{ active: currentPage === page - 1 }"
          type="button"
          :aria-label="`Go to page ${page}`"
          :aria-current="currentPage === page - 1 ? 'page' : undefined"
          @click="setPage(page - 1)"
        >
          {{ page }}
        </button>
      </template>
      <button
        class="pagination-nav-button"
        type="button"
        :disabled="currentPage === totalPages - 1"
        aria-label="Next page"
        @click="setPage(currentPage + 1)"
      >
        <span>Next</span><i class="bi bi-chevron-right"></i>
      </button>
    </nav>

    <div class="pagination-controls">
      <label>
        <span>Results per page</span>
        <select
          :value="pageSize"
          aria-label="Results per page"
          @change="emit('page-size-change', Number($event.target.value))"
        >
          <option v-for="size in pageSizes" :key="size" :value="size">{{ size }}</option>
        </select>
      </label>
    </div>
  </section>
</template>

<style scoped>
.app-pagination {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 12px 10px;
}

.pagination-summary {
  flex: 1 0 100%;
  margin: 0;
  color: var(--color-text-secondary);
  font-size: 0.8rem;
  font-weight: 600;
  line-height: 1.4;
}

.app-pagination.is-single-page .pagination-summary {
  flex: 0 1 auto;
  margin-right: auto;
}

.pagination-nav {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 6px;
}

.pagination-nav-button,
.pagination-number {
  display: inline-flex;
  min-width: 40px;
  height: 40px;
  align-items: center;
  justify-content: center;
  gap: 6px;
  padding: 0 10px;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-sm);
  background: var(--color-surface);
  color: var(--color-text-body);
  font-size: 0.78rem;
  font-weight: 700;
  cursor: pointer;
  transition:
    background 0.15s,
    border-color 0.15s,
    color 0.15s;
}

.pagination-nav-button {
  min-width: 78px;
}

.pagination-nav-button:hover:not(:disabled),
.pagination-number:hover:not(.active) {
  border-color: var(--color-primary-border);
  background: var(--color-surface-muted);
  color: var(--color-primary-text);
}

.pagination-nav-button:focus-visible,
.pagination-number:focus-visible,
.pagination-controls select:focus-visible {
  outline: 2px solid var(--color-primary-text);
  outline-offset: 2px;
}

.pagination-number.active {
  border-color: var(--color-primary-text);
  background: var(--color-primary);
  color: #fff;
}

.pagination-nav-button:disabled {
  cursor: not-allowed;
  opacity: 0.45;
}

.pagination-ellipsis {
  display: inline-flex;
  width: 26px;
  align-items: center;
  justify-content: center;
  color: var(--color-text-secondary);
  font-weight: 700;
}

.pagination-controls {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-left: auto;
}

.pagination-controls label {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  margin: 0;
  color: var(--color-text-secondary);
  font-size: 0.78rem;
  font-weight: 650;
  white-space: nowrap;
}

.pagination-controls select {
  width: auto;
  min-width: 64px;
  height: 38px;
  padding: 0 26px 0 9px;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-sm);
  background: var(--color-surface);
  color: var(--color-text);
  font-size: 0.8rem;
  font-weight: 700;
  cursor: pointer;
}

.pagination-controls select:hover:not(:disabled) {
  border-color: var(--color-primary-border);
}

@media (max-width: 575.98px) {
  .pagination-nav {
    flex: 1 0 100%;
    justify-content: space-between;
    gap: 4px;
  }
  .pagination-nav-button {
    min-width: 60px;
    padding: 0 9px;
  }
  .pagination-number {
    min-width: 34px;
    padding: 0 6px;
  }
  .pagination-controls {
    flex: 1 0 100%;
    justify-content: flex-start;
    margin-top: 2px;
    margin-left: 0;
  }
  .pagination-controls select {
    height: 40px;
  }
  .pagination-ellipsis {
    width: 18px;
  }
  .pagination-nav-button span {
    font-size: 0.74rem;
  }
}

@media (max-width: 380px) {
  .pagination-nav {
    gap: 3px;
  }
  .pagination-nav-button {
    min-width: 54px;
    padding: 0 7px;
  }
  .pagination-number {
    min-width: 32px;
    font-size: 0.76rem;
  }
  .pagination-ellipsis {
    width: 14px;
  }
}
</style>
