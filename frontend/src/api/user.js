import request from './request'

export function getUserList(params) {
  return request.get('/users', { params })
}

export function getUserDetail(id) {
  return request.get(`/users/${id}`)
}

export function createUser(data) {
  return request.post('/users', data)
}

export function updateUser(id, data) {
  return request.put(`/users/${id}`, data)
}

export function deleteUser(id) {
  return request.delete(`/users/${id}`)
}

export function resetUserPassword(id, data) {
  return request.put(`/users/${id}/password`, data)
}

export function toggleUserStatus(id) {
  return request.put(`/users/${id}/status`)
}
