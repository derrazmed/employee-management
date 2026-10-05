<script setup>
import { ref } from 'vue'

defineOptions({ inheritAttrs: false })

defineProps({
  id: {
    type: String,
    required: true,
  },
  modelValue: {
    type: String,
    default: '',
  },
  placeholder: {
    type: String,
    default: '',
  },
  autocompleteMode: {
    type: String,
    default: 'current-password',
  },
  required: {
    type: Boolean,
    default: false,
  },
  disabled: {
    type: Boolean,
    default: false,
  },
})

const emit = defineEmits(['update:modelValue'])
const showPassword = ref(false)

const updateValue = (event) => {
  emit('update:modelValue', event.target.value)
}
</script>

<template>
  <div class="password-input-wrapper">
    <input
      v-bind="$attrs"
      :id="id"
      :value="modelValue"
      :type="showPassword ? 'text' : 'password'"
      :placeholder="placeholder"
      :autocomplete="autocompleteMode"
      :required="required"
      :disabled="disabled"
      class="form-control password-input"
      @input="updateValue"
    />

    <button
      type="button"
      class="password-visibility-toggle"
      :aria-label="showPassword ? 'Hide password' : 'Show password'"
      :title="showPassword ? 'Hide password' : 'Show password'"
      :aria-pressed="showPassword"
      :disabled="disabled"
      @click="showPassword = !showPassword"
    >
      <i :class="showPassword ? 'bi bi-eye-slash' : 'bi bi-eye'" aria-hidden="true"></i>
    </button>
  </div>
</template>

<style scoped>
.password-input-wrapper {
  position: relative;
  width: 100%;
}

.password-input {
  padding-right: 2.75rem;
}

.password-visibility-toggle {
  position: absolute;
  top: 50%;
  right: 0;
  display: flex;
  width: 40px;
  height: 100%;
  align-items: center;
  justify-content: center;
  padding: 0;
  transform: translateY(-50%);
  border: 0;
  border-radius: 5px;
  background: transparent;
  color: #687385;
  cursor: pointer;
  transition: color 140ms ease;
}

.password-visibility-toggle:hover:not(:disabled) {
  color: #344054;
}

.password-visibility-toggle:focus-visible {
  outline: 2px solid rgba(36, 87, 166, 0.5);
  outline-offset: -3px;
}

.password-visibility-toggle:disabled {
  cursor: not-allowed;
  opacity: 0.55;
}

.password-visibility-toggle i {
  font-size: 0.95rem;
  line-height: 1;
}
</style>
