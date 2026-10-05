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
  align-items: flex-start;

  padding-top: 60px;

  background-color: #f8f9fa;
}

.auth-card {
  width: 100%;
  max-width: 400px;

  padding: 30px;

  background-color: #ffffff;

  border: 1px solid #dee2e6;
  border-radius: 8px;

  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}
</style>
