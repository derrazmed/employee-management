export const humanizeLabel = (value) =>
  String(value ?? '')
    .trim()
    .split('_')
    .filter(Boolean)
    .map((word) => word.charAt(0).toUpperCase() + word.slice(1).toLowerCase())
    .join(' ')

export const pluralize = (count, singular, plural = `${singular}s`) =>
  `${count} ${count === 1 ? singular : plural}`

export const formatRelativeTime = (date) => {
  if (!date) {
    return ''
  }

  const targetDate = new Date(date)
  const now = new Date()

  const difference = Math.floor((now.getTime() - targetDate.getTime()) / 1000)

  if (difference < 60) {
    return 'Just now'
  }

  const minutes = Math.floor(difference / 60)

  if (minutes < 60) {
    return `${minutes} min ago`
  }

  const hours = Math.floor(minutes / 60)

  if (hours < 24) {
    return `${hours}h ago`
  }

  const days = Math.floor(hours / 24)

  if (days < 7) {
    return `${days}d ago`
  }

  return targetDate.toLocaleDateString()
}
