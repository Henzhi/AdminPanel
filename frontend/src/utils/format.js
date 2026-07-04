/**
 * 格式化工具函数
 */

/**
 * 格式化日期时间
 * @param {string|number|Date} value 日期值
 * @param {string} format 格式 (默认 YYYY-MM-DD HH:mm:ss)
 * @returns {string} 格式化后的字符串
 */
export function formatDateTime(value, format = 'YYYY-MM-DD HH:mm:ss') {
  if (!value) return '-'
  const date = new Date(value)
  if (isNaN(date.getTime())) return '-'

  const pad = (n) => String(n).padStart(2, '0')
  const map = {
    YYYY: date.getFullYear(),
    MM: pad(date.getMonth() + 1),
    DD: pad(date.getDate()),
    HH: pad(date.getHours()),
    mm: pad(date.getMinutes()),
    ss: pad(date.getSeconds()),
  }

  let result = format
  Object.keys(map).forEach((key) => {
    result = result.replace(key, map[key])
  })
  return result
}

/**
 * 格式化日期
 */
export function formatDate(value) {
  return formatDateTime(value, 'YYYY-MM-DD')
}

/**
 * 格式化文件大小
 * @param {number} bytes 字节数
 * @returns {string} 格式化后的大小
 */
export function formatFileSize(bytes) {
  if (!bytes || bytes === 0) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB', 'TB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
}

/**
 * 截断文本
 */
export function truncate(text, length = 50) {
  if (!text) return ''
  return text.length > length ? text.substring(0, length) + '...' : text
}

/**
 * 安全的 JSON 解析
 */
export function safeJsonParse(str, defaultValue = null) {
  if (!str) return defaultValue
  try {
    return typeof str === 'string' ? JSON.parse(str) : str
  } catch (e) {
    return defaultValue
  }
}

/**
 * 下载 Blob 文件
 * @param {Blob} blob 文件数据
 * @param {string} filename 文件名
 */
export function downloadBlob(blob, filename) {
  const url = window.URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = filename
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
  window.URL.revokeObjectURL(url)
}

/**
 * 获取操作类型标签样式
 */
export function getOperationTypeTag(type) {
  const map = {
    CREATE: 'success',
    UPDATE: 'warning',
    DELETE: 'danger',
    QUERY: 'info',
    EXPORT: 'info',
    IMPORT: 'success',
    LOGIN: 'primary',
    LOGOUT: 'info',
  }
  return map[type] || 'info'
}

/**
 * 获取日志类型标签样式
 */
export function getLogTypeTag(type) {
  const map = {
    OPERATION: 'primary',
    SECURITY: 'danger',
    SYSTEM: 'info',
  }
  return map[type] || 'info'
}

/**
 * 获取备份状态标签
 */
export function getBackupStatusTag(status) {
  const map = {
    SUCCESS: 'success',
    FAILED: 'danger',
    IN_PROGRESS: 'warning',
    RESTORED: 'primary',
  }
  return map[status] || 'info'
}

/**
 * 获取管理员状态标签
 */
export function getAdminStatusTag(status) {
  if (status === 1 || status === true || status === 'NORMAL') return 'success'
  if (status === 0 || status === false || status === 'DISABLED') return 'danger'
  return 'info'
}

/**
 * 获取管理员状态文本
 */
export function getAdminStatusText(status) {
  if (status === 1 || status === true || status === 'NORMAL') return '正常'
  if (status === 0 || status === false || status === 'DISABLED') return '禁用'
  return '未知'
}
