import { nextTick, onBeforeUnmount, watch } from 'vue'

export const ESCAPE_PRIORITY = Object.freeze({
  MODAL: 300,
  DROPDOWN: 200,
  OVERLAY: 100,
})

const layers = []

const restoreFocus = (layer) => {
  if (layers.includes(layer)) {
    return
  }

  const active = document.activeElement
  const focusLost =
    !active ||
    active === document.body ||
    active === document.documentElement ||
    !active.isConnected

  if (
    focusLost &&
    layer.savedFocus instanceof HTMLElement &&
    layer.savedFocus !== document.body &&
    layer.savedFocus.isConnected
  ) {
    layer.savedFocus.focus()
  }
}

const handleWindowKeydown = (event) => {
  if (event.key !== 'Escape' || layers.length === 0) {
    return
  }

  let topmost = layers[0]

  for (let i = 1; i < layers.length; i += 1) {
    if (layers[i].priority >= topmost.priority) {
      topmost = layers[i]
    }
  }

  event.preventDefault()
  event.stopPropagation()

  topmost.handler(event)

  nextTick(() => {
    restoreFocus(topmost)
  })
}

const attachListener = () => {
  window.addEventListener('keydown', handleWindowKeydown)
}

const detachListener = () => {
  window.removeEventListener('keydown', handleWindowKeydown)
}

export const useEscapeDismiss = (isActive, handler, priority = ESCAPE_PRIORITY.OVERLAY) => {
  let layer = null

  const activate = () => {
    if (layer) {
      return
    }

    layer = {
      priority,
      handler,
      savedFocus: document.activeElement instanceof HTMLElement ? document.activeElement : null,
    }

    layers.push(layer)
    attachListener()
  }

  const release = () => {
    if (!layer) {
      return
    }

    const index = layers.indexOf(layer)

    if (index !== -1) {
      layers.splice(index, 1)
    }

    layer = null

    if (layers.length === 0) {
      detachListener()
    }
  }

  watch(
    isActive,
    (active) => {
      if (active) {
        activate()
      } else {
        release()
      }
    },
    { immediate: true },
  )

  onBeforeUnmount(release)
}
