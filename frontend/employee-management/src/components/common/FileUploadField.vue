<script setup>
import { computed, ref } from 'vue'

const props = defineProps({
  modelValue: { type: [File, Object, null], default: null },
  label: { type: String, required: true },
  icon: { type: String, required: true },
  accept: { type: String, default: '' },
  hint: { type: String, default: '' },
  existingFile: { type: Object, default: null },
  disabled: { type: Boolean, default: false },
  error: { type: String, default: '' },
})

const emit = defineEmits(['update:modelValue', 'remove'])

const fileInputRef = ref(null)
const previewUrl = ref(null)

const isFileSelected = computed(() => {
  if (!props.modelValue) return false
  if (props.modelValue instanceof File) return true
  return props.modelValue.size !== undefined && props.modelValue.name !== undefined
})

const displayFile = computed(() => {
  if (props.modelValue instanceof File) {
    return props.modelValue
  }
  if (props.modelValue && typeof props.modelValue === 'object' && props.modelValue.name) {
    return props.modelValue
  }
  return null
})

const existingFileName = computed(() => {
  if (props.existingFile?.photoObjectName || props.existingFile?.cvObjectName) {
    return props.existingFile.photoObjectName || props.existingFile.cvObjectName
  }
  return null
})

const hasExistingFile = computed(() => {
  return Boolean(props.existingFile?.photoObjectName || props.existingFile?.cvObjectName)
})

const isPreviewable = computed(() => {
  const file = displayFile.value
  return file && file.type && file.type.startsWith('image/')
})

const fileSize = computed(() => {
  const file = displayFile.value
  if (!file?.size) return ''
  const bytes = file.size
  if (bytes === 0) return '0 Bytes'
  const k = 1024
  const sizes = ['Bytes', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return parseFloat((bytes / Math.pow(k, i)).toFixed(1)) + ' ' + sizes[i]
})

const removeFile = (event) => {
  event.stopPropagation()
  if (previewUrl.value) {
    URL.revokeObjectURL(previewUrl.value)
    previewUrl.value = null
  }
  emit('update:modelValue', null)
  emit('remove')
}

const handleFileChange = (event) => {
  const file = event.target.files[0] || null
  if (previewUrl.value) {
    URL.revokeObjectURL(previewUrl.value)
    previewUrl.value = null
  }
  if (file) {
    emit('update:modelValue', file)
    if (file.type.startsWith('image/')) {
      previewUrl.value = URL.createObjectURL(file)
    }
  } else {
    emit('update:modelValue', null)
  }
}

const triggerFilePicker = () => {
  if (!props.disabled) {
    fileInputRef.value?.click()
  }
}

const handleKeyDown = (event) => {
  if (event.key === 'Enter' || event.key === ' ') {
    event.preventDefault()
    triggerFilePicker()
  }
}

const handleDragOver = (event) => {
  event.preventDefault()
  event.stopPropagation()
}

const handleDragLeave = (event) => {
  event.preventDefault()
  event.stopPropagation()
}

const handleDrop = (event) => {
  event.preventDefault()
  event.stopPropagation()
  if (!props.disabled) {
    const file = event.dataTransfer.files[0] || null
    if (file) {
      handleFileChange({ target: { files: [file] } })
    }
  }
}
</script>

<template>
  <div class="file-upload-field">
    <label :for="inputId" class="form-label">
      {{ label }}
    </label>

    <div
      class="file-upload-trigger"
      :class="{
        'has-file': isFileSelected,
        'has-existing': !isFileSelected && hasExistingFile,
        'is-dragging': false,
        'has-error': Boolean(error),
        disabled: disabled,
      }"
      @click="triggerFilePicker"
      @keydown="handleKeyDown"
      @dragover="handleDragOver"
      @dragleave="handleDragLeave"
      @drop="handleDrop"
      tabindex="0"
      role="button"
      :aria-label="`Upload ${label}`"
      :aria-describedby="hint ? `${inputId}-hint` : undefined"
    >
      <input
        ref="fileInputRef"
        :id="inputId"
        type="file"
        class="file-upload-input"
        :accept="accept"
        :disabled="disabled"
        @change="handleFileChange"
        @click.stop
        aria-hidden="true"
        tabindex="-1"
      />

      <div v-if="isFileSelected" class="file-upload-selected">
        <div class="file-upload-preview" v-if="isPreviewable">
          <img :src="previewUrl" :alt="`Preview of ${displayFile.name}`" />
        </div>
        <div v-else class="file-upload-icon" aria-hidden="true">
          <i :class="icon"></i>
        </div>

        <div class="file-upload-info">
          <span class="file-upload-name">{{ displayFile.name }}</span>
          <span v-if="fileSize" class="file-upload-size">{{ fileSize }}</span>
        </div>

        <button
          type="button"
          class="file-upload-remove"
          @click="removeFile"
          :aria-label="`Remove selected ${label.toLowerCase()}`"
          :disabled="disabled"
        >
          <i class="bi bi-x"></i>
        </button>
      </div>

      <div v-else class="file-upload-empty">
        <div class="file-upload-icon" aria-hidden="true">
          <i :class="icon"></i>
        </div>
        <div class="file-upload-text">
          <span class="file-upload-primary">Upload {{ label.toLowerCase() }}</span>
          <span v-if="hint" class="file-upload-hint" :id="`${inputId}-hint`">{{ hint }}</span>
        </div>
      </div>

      <div v-if="!isFileSelected && hasExistingFile" class="file-upload-existing">
        <div class="file-upload-icon" aria-hidden="true">
          <i :class="icon"></i>
        </div>
        <div class="file-upload-info">
          <span class="file-upload-name">Existing file ({{ existingFileName }})</span>
          <span class="file-upload-replace">Click to replace</span>
        </div>
      </div>
    </div>

    <div v-if="error" class="file-upload-error" role="alert">
      {{ error }}
    </div>

    <div v-else-if="hint && !error" class="form-text" :id="`${inputId}-hint`">
      {{ hint }}
    </div>
  </div>
</template>

<style scoped>
.file-upload-field {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.file-upload-field label.form-label {
  font-size: 0.8125rem;
  font-weight: 500;
  color: var(--color-text);
}

.file-upload-trigger {
  position: relative;
  display: flex;
  align-items: center;
  gap: 10px;
  min-height: 44px;
  padding: 0 12px;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-sm);
  background: var(--color-surface);
  color: var(--color-text);
  cursor: pointer;
  transition:
    border-color 0.15s ease,
    background-color 0.15s ease;
}

.file-upload-trigger:hover:not(.disabled) {
  border-color: var(--color-border-strong);
  background: var(--color-hover);
}

.file-upload-trigger:focus-visible {
  outline: 2px solid var(--color-primary-text);
  outline-offset: 2px;
}

.file-upload-trigger.disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.file-upload-trigger.has-error {
  border-color: var(--color-danger-border);
  background: var(--color-danger-soft);
  color: var(--color-danger-text);
}

.file-upload-input {
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;
  opacity: 0;
  cursor: pointer;
  z-index: 1;
}

.file-upload-empty,
.file-upload-selected,
.file-upload-existing {
  display: flex;
  align-items: center;
  gap: 10px;
  width: 100%;
  min-width: 0;
}

.file-upload-icon {
  flex: 0 0 auto;
  display: inline-flex;
  width: 36px;
  height: 36px;
  align-items: center;
  justify-content: center;
  border-radius: var(--radius-sm);
  background: var(--color-primary-soft);
  color: var(--color-primary-text);
  font-size: 1rem;
}

.file-upload-selected .file-upload-icon {
  background: var(--color-success-soft);
  color: var(--color-success-text);
}

.file-upload-existing .file-upload-icon {
  background: var(--color-info-soft);
  color: var(--color-info-text);
}

.file-upload-icon i {
  font-size: 1rem;
}

.file-upload-preview {
  flex: 0 0 auto;
  width: 36px;
  height: 36px;
  border-radius: var(--radius-sm);
  overflow: hidden;
  background: var(--color-surface-muted);
}

.file-upload-preview img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.file-upload-text {
  flex: 1 1 auto;
  display: flex;
  flex-direction: column;
  gap: 2px;
  min-width: 0;
}

.file-upload-primary {
  font-size: 0.875rem;
  font-weight: 500;
  color: var(--color-text);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.file-upload-hint {
  font-size: 0.75rem;
  color: var(--color-text-tertiary);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.file-upload-info {
  flex: 1 1 auto;
  display: flex;
  flex-direction: column;
  gap: 2px;
  min-width: 0;
  overflow: hidden;
}

.file-upload-name {
  font-size: 0.8125rem;
  font-weight: 500;
  color: var(--color-text);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.file-upload-size,
.file-upload-replace {
  font-size: 0.75rem;
  color: var(--color-text-tertiary);
}

.file-upload-replace {
  font-weight: 500;
  color: var(--color-primary-text);
}

.file-upload-remove {
  flex: 0 0 auto;
  display: inline-flex;
  width: 28px;
  height: 28px;
  align-items: center;
  justify-content: center;
  border: 0;
  border-radius: var(--radius-sm);
  background: transparent;
  color: var(--color-text-tertiary);
  cursor: pointer;
  transition:
    background-color 0.15s ease,
    color 0.15s ease;
}

.file-upload-remove:hover:not(:disabled) {
  background: var(--color-hover-strong);
  color: var(--color-danger-text);
}

.file-upload-remove:focus-visible {
  outline: 2px solid var(--color-primary-text);
  outline-offset: 2px;
}

.file-upload-remove:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.file-upload-remove i {
  font-size: 0.75rem;
}

.file-upload-error {
  font-size: 0.75rem;
  color: var(--color-danger-text);
  padding: 2px 4px;
}

@media (max-width: 767.98px) {
  .file-upload-trigger {
    min-height: 48px;
  }
}
</style>
