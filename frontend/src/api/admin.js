import request from './request'

export function getAdminList(params) {
  return request.get('/admins', { params })
}

export function getAdminDetail(id) {
  return request.get(`/admins/${id}`)
}

export function createAdmin(data) {
  return request.post('/admins', data)
}

export function updateAdmin(id, data) {
  return request.put(`/admins/${id}`, data)
}

export function deleteAdmin(id) {
  return request.delete(`/admins/${id}`)
}

export function resetPassword(id, data) {
  return request.put(`/admins/${id}/password`, data)
}

export function toggleAdminStatus(id) {
  return request.put(`/admins/${id}/status`)
}
