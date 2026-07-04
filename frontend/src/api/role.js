import request from './request'

export function getRoleList() {
  return request.get('/roles')
}

export function getRoleDetail(id) {
  return request.get(`/roles/${id}`)
}

export function createRole(data) {
  return request.post('/roles', data)
}

export function updateRole(id, data) {
  return request.put(`/roles/${id}`, data)
}

export function deleteRole(id) {
  return request.delete(`/roles/${id}`)
}

export function getRolePermissions(id) {
  return request.get(`/roles/${id}/permissions`)
}

export function assignRolePermissions(id, permissionIds) {
  return request.put(`/roles/${id}/permissions`, permissionIds)
}

export function getPermissionTree() {
  return request.get('/permissions/tree')
}
