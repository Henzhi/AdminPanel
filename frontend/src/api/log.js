import request from './request'

export function getLogList(params) {
  return request.get('/logs', { params })
}

export function getOperationLogs(params) {
  return request.get('/logs/operation', { params })
}

export function getSecurityLogs(params) {
  return request.get('/logs/security', { params })
}

export function getSystemLogs(params) {
  return request.get('/logs/system', { params })
}

export function getLogDetail(id) {
  return request.get(`/logs/${id}`)
}

export function exportLogs(params) {
  return request.get('/logs/export', {
    params,
    responseType: 'blob',
  })
}

export function deleteLog(id) {
  return request.delete(`/logs/${id}`)
}
