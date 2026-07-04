import request from './request'

export function getBackupList(params) {
  return request.get('/backups', { params })
}

export function getBackupDetail(id) {
  return request.get(`/backups/${id}`)
}

export function createBackup(data) {
  return request.post('/backups', data)
}

export function restoreBackup(id, data) {
  return request.post(`/backups/${id}/restore`, data)
}

export function downloadBackup(id) {
  return request.get(`/backups/${id}/download`, {
    responseType: 'blob',
  })
}

export function getBackupStatus() {
  return request.get('/backups/status')
}
