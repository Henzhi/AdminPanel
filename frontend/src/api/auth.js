import request from './request'

export function login(data) {
  return request.post('/auth/login', data)
}

export function logout() {
  return request.post('/auth/logout')
}

export function getAdminInfo() {
  return request.get('/auth/info')
}

export function changePassword(data) {
  return request.put('/auth/password', data)
}

export function getMenuTree() {
  return request.get('/auth/menus')
}

export function getCaptcha() {
  return request.get('/auth/captcha')
}
