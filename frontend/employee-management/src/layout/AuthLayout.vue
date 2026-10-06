<template>
  <div class="auth-page">
    <div class="auth-card">
      <slot />
    </div>
  </div>
</template>

<style scoped>
.auth-page {
  min-height: 100vh;

  display: flex;
  justify-content: center;
  align-items: center;
  padding: 2rem 1rem;
  background: var(--app-background);
}

.auth-card {
  width: 100%;
  max-width: 400px;

  padding: 2rem;

  background-color: var(--color-surface);

  border: 1px solid var(--app-border);
  border-radius: var(--app-radius);
  box-shadow: 0 8px 24px rgba(31, 41, 55, 0.06);
}
</style>
