export function formatBytes(bytes: number | undefined) {
  if (bytes === undefined || bytes === null) return '-'
  if (bytes === 0) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB', 'TB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  const value = bytes / Math.pow(k, i)
  return `${value.toFixed(2)} ${sizes[i]}`
}

export function calculateUsedQuotaPercentage(totalQuota: number, usedQuota: number) {
  const total = totalQuota || 0
  const used = usedQuota || 0
  return total > 0 ? (used / total) * 100 : 0
}
