<template>
  <div class="directory-toolbar">
    <div class="toolbar-filters">
      <div class="input-group directory-search">
        <span class="input-group-text">
          <i class="bi bi-search"></i>
        </span>

        <input
          :value="search"
          type="text"
          class="form-control"
          placeholder="Search employees..."
          aria-label="Search employees"
          @input="emit('update:search', $event.target.value)"
        />

        <button
          v-if="search"
          type="button"
          class="btn btn-outline-secondary"
          aria-label="Clear employee search"
          title="Clear search"
          @click="emit('update:search', '')"
        >
          <i class="bi bi-x-lg"></i>
        </button>
      </div>

      <FilterDropdown
        :model-value="department"
        :label="'Department'"
        :icon="'bi-diagram-3'"
        :options="departments"
        placeholder="All departments"
        @update:model-value="emit('update:department', $event)"
      />

      <FilterDropdown
        :model-value="jobTitle"
        :label="'Job title'"
        :icon="'bi-briefcase'"
        :options="jobTitles"
        placeholder="All job titles"
        :searchable="true"
        @update:model-value="emit('update:jobTitle', $event)"
      />
    </div>

    <div class="view-toggle" role="group" aria-label="Directory view mode">
      <button
        type="button"
        class="view-toggle-btn"
        :class="{ active: viewMode === 'list' }"
        aria-label="List view"
        title="List view"
        :aria-pressed="viewMode === 'list'"
        @click="emit('set-view', 'list')"
      >
        <i class="bi bi-list"></i>
      </button>

      <button
        type="button"
        class="view-toggle-btn"
        :class="{ active: viewMode === 'grid' }"
        aria-label="Grid view"
        title="Grid view"
        :aria-pressed="viewMode === 'grid'"
        @click="emit('set-view', 'grid')"
      >
        <i class="bi bi-grid-3x3-gap"></i>
      </button>
    </div>
  </div>
</template>

<script setup>
import FilterDropdown from '@/components/common/FilterDropdown.vue'

defineProps({
  search: { type: String, default: '' },
  department: { type: String, default: '' },
  jobTitle: { type: String, default: '' },
  departments: { type: Array, default: () => [] },
  jobTitles: { type: Array, default: () => [] },
  viewMode: { type: String, default: 'list' },
})

const emit = defineEmits(['update:search', 'update:department', 'update:jobTitle', 'set-view'])
</script>

<style scoped>
.directory-toolbar {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 10px 14px;
}

.toolbar-filters {
  display: flex;
  flex: 1;
  flex-wrap: wrap;
  gap: 10px;
  min-width: 0;
}

.directory-search {
  flex: 1 1 260px;
  max-width: 420px;
  min-width: 0;
}

.directory-search .form-control {
  min-width: 0;
}

.view-toggle {
  display: inline-flex;
  gap: 2px;
  margin-left: auto;
  padding: 3px;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-sm);
  background: var(--color-surface);
}

.view-toggle-btn {
  display: inline-flex;
  width: 36px;
  height: 32px;
  align-items: center;
  justify-content: center;
  padding: 0;
  border: 0;
  border-radius: 4px;
  background: transparent;
  color: var(--color-text-secondary);
  font-size: 1rem;
  cursor: pointer;
}

.view-toggle-btn:hover {
  background: var(--color-hover-strong);
  color: var(--color-text-body);
}

.view-toggle-btn.active {
  background: var(--color-primary);
  color: #fff;
}

.view-toggle-btn:focus-visible {
  outline: 2px solid var(--color-primary-text);
  outline-offset: 1px;
}

@media (max-width: 767.98px) {
  .directory-toolbar {
    align-items: stretch;
    flex-direction: column;
  }

  .toolbar-filters {
    width: 100%;
  }

  .directory-search {
    flex: 1 1 100%;
    max-width: none;
  }

  .view-toggle {
    align-self: flex-end;
    margin-left: 0;
  }
}
</style>
