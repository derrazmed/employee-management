<template>
  <section class="dashboard-panel">
    <header class="dashboard-panel-header">
      <div class="dashboard-panel-heading">
        <span class="dashboard-panel-icon"><i class="bi bi-plus-lg"></i></span>
        <h2 class="dashboard-panel-title">Quick actions</h2>
      </div>
    </header>

    <div class="dashboard-panel-body">
      <div class="qa-grid">
        <div v-if="canCreateEmployee" class="qa-item">
          <button
            type="button"
            class="btn btn-primary qa-btn"
            @click="$emit('create-employee')"
          >
            <i class="bi bi-person-plus"></i>
            <span>Add Employee</span>
          </button>

          <p class="qa-hint">Creates a new employee record.</p>
        </div>

        <div v-if="canCreateUser" class="qa-item">
          <button
            type="button"
            class="btn btn-outline-primary qa-btn"
            @click="$emit('create-user')"
          >
            <i class="bi bi-plus-lg"></i>
            <span>Add User</span>
          </button>

          <p class="qa-hint">Creates a new user account.</p>
        </div>
      </div>
    </div>
  </section>
</template>

<script setup>
defineProps({
  canCreateEmployee: { type: Boolean, default: false },
  canCreateUser: { type: Boolean, default: false },
})

defineEmits(['create-employee', 'create-user'])
</script>

<style scoped>
.qa-grid {
  display: grid;
  gap: 14px;
  grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
}

.qa-item {
  display: flex;
  min-width: 0;
  flex-direction: column;
  gap: 7px;
}

.qa-btn {
  width: 100%;
}

.qa-hint {
  margin: 0;
  color: var(--color-text-secondary);
  font-size: 0.73rem;
}
</style>
