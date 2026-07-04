import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { login as loginApi, logout as logoutApi, getAdminInfo, getMenuTree, getCaptcha } from '@/api/auth'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('admin_token') || '')
  const adminInfo = ref(JSON.parse(localStorage.getItem('admin_info') || 'null'))
  const menus = ref([])
  const permissions = ref([])
  const roles = ref([])

  const isLogin = computed(() => !!token.value)
  const username = computed(() => adminInfo.value?.username || '')
  const realName = computed(() => adminInfo.value?.realName || '')
  const avatar = computed(() => adminInfo.value?.avatar || '')

  async function login(loginForm) {
    const res = await loginApi(loginForm)
    token.value = res.data.token
    adminInfo.value = {
      id: res.data.adminId,
      username: res.data.username,
      realName: res.data.realName,
      avatar: res.data.avatar,
    }
    permissions.value = res.data.permissions || []
    roles.value = res.data.roles || []
    localStorage.setItem('admin_token', res.data.token)
    localStorage.setItem('admin_info', JSON.stringify(adminInfo.value))
    return res
  }

  async function fetchAdminInfo() {
    const res = await getAdminInfo()
    adminInfo.value = res.data
    localStorage.setItem('admin_info', JSON.stringify(res.data))
    return res.data
  }

  async function fetchMenus() {
    const res = await getMenuTree()
    menus.value = res.data || []
    return res.data
  }

  function logout() {
    return logoutApi().finally(() => {
      token.value = ''
      adminInfo.value = null
      menus.value = []
      permissions.value = []
      roles.value = []
      localStorage.removeItem('admin_token')
      localStorage.removeItem('admin_info')
    })
  }

  function hasPermission(perm) {
    if (!perm) return true
    if (roles.value.includes('SUPER_ADMIN')) return true
    return permissions.value.includes(perm)
  }

  function hasRole(role) {
    return roles.value.includes(role)
  }

  return {
    token,
    adminInfo,
    menus,
    permissions,
    roles,
    isLogin,
    username,
    realName,
    avatar,
    login,
    fetchAdminInfo,
    fetchMenus,
    logout,
    hasPermission,
    hasRole,
  }
})
