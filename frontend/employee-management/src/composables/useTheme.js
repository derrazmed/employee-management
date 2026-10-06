import { ref, watch } from 'vue'

const STORAGE_KEY = 'theme'

const readStoredTheme = () => {
  try {
    const saved = localStorage.getItem(STORAGE_KEY)
    return saved === 'light' || saved === 'dark' ? saved : null
  } catch {
    return null
  }
}

const theme = ref(
  document.documentElement.getAttribute('data-theme') === 'dark' ? 'dark' : 'light',
)

watch(theme, (value) => {
  document.documentElement.setAttribute('data-theme', value)
})

export const useTheme = () => {
  const setTheme = (value, { persist = true } = {}) => {
    if (value !== 'light' && value !== 'dark') {
      return
    }

    theme.value = value

    if (persist) {
      try {
        localStorage.setItem(STORAGE_KEY, value)
      } catch {
        /* storage unavailable; theme still applies for this session */
      }
    }
  }

  const toggleTheme = () => {
    setTheme(theme.value === 'dark' ? 'light' : 'dark')
  }

  return { theme, setTheme, toggleTheme }
}

const media = window.matchMedia('(prefers-color-scheme: dark)')

const handleSystemChange = (event) => {
  if (readStoredTheme()) {
    return
  }

  theme.value = event.matches ? 'dark' : 'light'
}

if (typeof media.addEventListener === 'function') {
  media.addEventListener('change', handleSystemChange)
} else {
  media.addListener(handleSystemChange)
}
