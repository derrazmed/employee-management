<script setup>
import { computed, nextTick, onBeforeUnmount, ref, watch } from 'vue'
import { ESCAPE_PRIORITY, useEscapeDismiss } from '@/composables/useEscapeDismiss'

const props = defineProps({
  modelValue: { type: String, default: '' },
  label: { type: String, default: '' },
  icon: { type: String, default: '' },
  options: { type: Array, default: () => [] },
  placeholder: { type: String, default: '' },
  searchable: { type: Boolean, default: false },
  optionLabel: { type: Function, default: null },
  allowEmpty: { type: Boolean, default: true },
})

const emit = defineEmits(['update:modelValue'])

const isOpen = ref(false)
const triggerRef = ref(null)
const menuRef = ref(null)
const searchQuery = ref('')
const highlightedIndex = ref(-1)
const anchorRect = ref(null)
const menuOpensUpward = ref(false)

const displayOptions = computed(() => {
  if (!props.searchable || !searchQuery.value.trim()) {
    return props.options
  }
  const query = searchQuery.value.toLowerCase().trim()
  return props.options.filter((opt) => opt.toLowerCase().includes(query))
})

const selectedOption = computed(() => {
  if (!props.modelValue) return null
  return props.options.find((opt) => opt === props.modelValue) || null
})

const selectedLabel = computed(() => {
  return selectedOption.value || props.placeholder || `All ${props.label.toLowerCase()}`
})

const optionDisplayLabel = (option) => {
  if (props.optionLabel) {
    return props.optionLabel(option)
  }
  if (option === '') {
    return props.placeholder || `All ${props.label.toLowerCase()}`
  }
  return option
}

const ariaLabel = computed(() => (props.label ? `Filter by ${props.label}` : 'Select option'))
const dropdownAriaLabel = computed(() => (props.label ? `${props.label} options` : 'Options'))

const isActive = computed(() => Boolean(props.modelValue))

const open = () => {
  isOpen.value = true
  highlightedIndex.value = displayOptions.value.findIndex((opt) => opt === props.modelValue)
  if (highlightedIndex.value === -1) highlightedIndex.value = 0
  updatePosition()
}

const close = () => {
  isOpen.value = false
  searchQuery.value = ''
  highlightedIndex.value = -1
  anchorRect.value = null
}

const toggle = () => {
  if (isOpen.value) {
    close()
  } else {
    open()
  }
}

const selectOption = (option) => {
  emit('update:modelValue', option)
  close()
}

const handleKeydown = (event) => {
  if (!isOpen.value) {
    if (['Enter', 'Space', 'ArrowDown'].includes(event.key)) {
      event.preventDefault()
      open()
    }
    return
  }

  const opts = displayOptions.value
  if (!opts.length) return

  switch (event.key) {
    case 'ArrowDown':
      event.preventDefault()
      highlightedIndex.value = (highlightedIndex.value + 1) % opts.length
      scrollToHighlighted()
      break
    case 'ArrowUp':
      event.preventDefault()
      highlightedIndex.value = (highlightedIndex.value - 1 + opts.length) % opts.length
      scrollToHighlighted()
      break
    case 'Enter':
    case ' ':
      event.preventDefault()
      if (highlightedIndex.value >= 0) {
        selectOption(opts[highlightedIndex.value])
      }
      break
    case 'Escape':
      event.preventDefault()
      close()
      break
    case 'Tab':
      close()
      break
  }
}

const scrollToHighlighted = () => {
  nextTick(() => {
    const item = menuRef.value?.querySelector(`[data-index="${highlightedIndex.value}"]`)
    item?.scrollIntoView({ block: 'nearest' })
  })
}

const updatePosition = () => {
  const el = triggerRef.value
  if (!el) return

  anchorRect.value = el.getBoundingClientRect()

  nextTick(() => {
    const menu = menuRef.value
    if (!menu || !anchorRect.value) return

    const menuHeight = menu.getBoundingClientRect().height
    const spaceBelow = window.innerHeight - anchorRect.value.bottom
    const spaceAbove = anchorRect.value.top
    const viewportMargin = 16

    menuOpensUpward.value = spaceBelow < menuHeight + viewportMargin && spaceAbove > spaceBelow
  })
}

const menuStyle = computed(() => {
  if (!anchorRect.value) {
    return { display: 'none' }
  }

  const right = Math.max(8, Math.round(window.innerWidth - anchorRect.value.right))

  return menuOpensUpward.value
    ? {
        right: `${right}px`,
        bottom: `${Math.round(window.innerHeight - anchorRect.value.top) + 6}px`,
      }
    : {
        right: `${right}px`,
        top: `${Math.round(anchorRect.value.bottom) + 6}px`,
      }
})

const handleClickOutside = (event) => {
  if (triggerRef.value && !triggerRef.value.contains(event.target)) {
    if (menuRef.value && !menuRef.value.contains(event.target)) {
      close()
    }
  }
}

useEscapeDismiss(() => isOpen.value, close, ESCAPE_PRIORITY.DROPDOWN)

let clickListenerAttached = false

watch(isOpen, (open) => {
  if (open) {
    nextTick(() => {
      if (!clickListenerAttached) {
        document.addEventListener('click', handleClickOutside)
        clickListenerAttached = true
      }
    })
  } else {
    if (clickListenerAttached) {
      document.removeEventListener('click', handleClickOutside)
      clickListenerAttached = false
    }
  }
})

onBeforeUnmount(() => {
  if (clickListenerAttached) {
    document.removeEventListener('click', handleClickOutside)
  }
})

watch(
  () => props.modelValue,
  () => {
    if (isOpen.value) {
      highlightedIndex.value = displayOptions.value.findIndex((opt) => opt === props.modelValue)
      if (highlightedIndex.value === -1) highlightedIndex.value = 0
    }
  },
)
</script>

<template>
  <div class="filter-dropdown" ref="triggerRef">
    <button
      type="button"
      class="filter-trigger"
      :class="{ active: isActive, open: isOpen }"
      @click="toggle"
      @keydown="handleKeydown"
      aria-haspopup="listbox"
      :aria-expanded="isOpen"
      :aria-label="ariaLabel"
      :title="selectedLabel"
    >
      <span v-if="icon" class="filter-trigger-icon" aria-hidden="true">
        <i :class="icon"></i>
      </span>
      <span class="filter-trigger-text">
        <span v-if="label && !isActive" class="filter-trigger-label">{{ label }}</span>
        <span :class="{ 'is-placeholder': !isActive }">{{ selectedLabel }}</span>
      </span>
      <span class="filter-trigger-chevron" aria-hidden="true">
        <i class="bi bi-chevron-down" :class="{ rotated: isOpen }"></i>
      </span>
    </button>

    <Teleport to="body">
      <div
        v-if="isOpen"
        class="filter-dropdown-menu"
        ref="menuRef"
        role="listbox"
        :aria-label="dropdownAriaLabel"
        tabindex="-1"
        :style="menuStyle"
        @click.stop
      >
        <div v-if="searchable" class="filter-dropdown-search">
          <input
            type="text"
            class="form-control form-control-sm"
            placeholder="Search..."
            v-model="searchQuery"
            aria-label="Search options"
            @click.stop
            @keydown.stop
            autoFocus
          />
        </div>

        <div class="filter-dropdown-options" role="presentation">
          <button
            v-if="props.allowEmpty"
            type="button"
            class="filter-dropdown-option"
            :class="{ selected: !props.modelValue, highlighted: highlightedIndex === 0 }"
            :data-index="0"
            role="option"
            :aria-selected="!props.modelValue"
            @click="selectOption('')"
            @mouseenter="highlightedIndex = 0"
          >
            <span class="filter-option-text">{{ optionDisplayLabel('') }}</span>
            <span v-if="!props.modelValue" class="filter-option-check" aria-hidden="true">
              <i class="bi bi-check"></i>
            </span>
          </button>

          <button
            v-for="(option, index) in displayOptions"
            :key="option"
            type="button"
            class="filter-dropdown-option"
            :class="{
              selected: option === props.modelValue,
              highlighted: highlightedIndex === index + (props.allowEmpty ? 1 : 0),
            }"
            :data-index="index + (props.allowEmpty ? 1 : 0)"
            role="option"
            :aria-selected="option === props.modelValue"
            @click="selectOption(option)"
            @mouseenter="highlightedIndex = index + (props.allowEmpty ? 1 : 0)"
          >
            <span class="filter-option-text">{{ optionDisplayLabel(option) }}</span>
            <span v-if="option === props.modelValue" class="filter-option-check" aria-hidden="true">
              <i class="bi bi-check"></i>
            </span>
          </button>

          <div v-if="displayOptions.length === 0" class="filter-dropdown-empty">
            No matching options
          </div>
        </div>
      </div>
    </Teleport>
  </div>
</template>

<style scoped>
.filter-dropdown {
  position: relative;
  display: inline-flex;
}

.filter-trigger {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  min-height: 38px;
  padding: 0 12px;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-sm);
  background: var(--color-surface);
  color: var(--color-text);
  font-size: 0.8125rem;
  font-weight: 500;
  white-space: nowrap;
  cursor: pointer;
  transition:
    border-color 0.15s ease,
    background-color 0.15s ease,
    box-shadow 0.15s ease;
}

.filter-trigger:hover {
  border-color: var(--color-border-strong);
  background: var(--color-hover);
}

.filter-trigger:focus-visible {
  outline: 2px solid var(--color-primary-text);
  outline-offset: 2px;
}

.filter-trigger.active {
  border-color: var(--color-primary-border);
  background: var(--color-primary-soft);
  color: var(--color-primary-text);
}

.filter-trigger.active .filter-trigger-icon i {
  color: var(--color-primary-text);
}

.filter-trigger.open {
  border-color: var(--color-primary-border);
  box-shadow: 0 0 0 3px var(--color-primary-soft);
}

.filter-trigger-icon {
  flex: 0 0 auto;
  display: inline-flex;
  width: 16px;
  height: 16px;
  align-items: center;
  justify-content: center;
  color: var(--color-text-tertiary);
  font-size: 0.75rem;
}

.filter-trigger-text {
  flex: 1 1 auto;
  display: inline-flex;
  align-items: center;
  gap: 4px;
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
}

.filter-trigger-separator {
  color: var(--color-text-tertiary);
}

.filter-trigger-text .is-placeholder {
  color: var(--color-text-tertiary);
}

.filter-trigger-chevron {
  flex: 0 0 auto;
  display: inline-flex;
  width: 16px;
  height: 16px;
  align-items: center;
  justify-content: center;
  color: var(--color-text-tertiary);
  font-size: 0.625rem;
  transition: transform 0.15s ease;
}

.filter-trigger-chevron.rotated {
  transform: rotate(180deg);
}

.filter-dropdown-menu {
  position: fixed;
  z-index: 1070;
  display: flex;
  flex-direction: column;
  width: max-content;
  max-width: min(320px, calc(100vw - 24px));
  min-width: 200px;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-sm);
  background: var(--color-elevated);
  box-shadow: var(--shadow-menu);
  overflow: hidden;
  animation: filterDropdownFadeIn 0.12s ease;
}

@keyframes filterDropdownFadeIn {
  from {
    opacity: 0;
    transform: translateY(-4px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.filter-dropdown-search {
  padding: 8px 10px;
  border-bottom: 1px solid var(--color-border-soft);
  background: var(--color-elevated);
}

.filter-dropdown-search .form-control {
  padding: 6px 10px;
  font-size: 0.8125rem;
  border-radius: 4px;
}

.filter-dropdown-options {
  max-height: 280px;
  overflow-y: auto;
  overflow-x: hidden;
  padding: 4px 0;
}

.filter-dropdown-option {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
  min-height: 36px;
  padding: 0 12px;
  border: 0;
  background: transparent;
  color: var(--color-text);
  font-size: 0.8125rem;
  font-weight: 500;
  text-align: left;
  cursor: pointer;
  transition: background-color 0.1s ease;
}

.filter-dropdown-option:hover,
.filter-dropdown-option.highlighted {
  background: var(--color-hover-strong);
}

.filter-dropdown-option.selected {
  background: var(--color-primary-soft);
  color: var(--color-primary-text);
  font-weight: 600;
}

.filter-dropdown-option.selected .filter-option-check {
  display: inline-flex;
}

.filter-option-text {
  flex: 1 1 auto;
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.filter-option-check {
  display: none;
  flex: 0 0 auto;
  width: 16px;
  height: 16px;
  margin-left: 8px;
  align-items: center;
  justify-content: center;
  color: var(--color-primary-text);
  font-size: 0.75rem;
}

.filter-dropdown-empty {
  padding: 16px 12px;
  color: var(--color-text-tertiary);
  font-size: 0.8125rem;
  text-align: center;
}

@media (max-width: 767.98px) {
  .filter-dropdown-menu {
    max-width: calc(100vw - 24px);
    min-width: calc(100vw - 24px);
  }
}
</style>
