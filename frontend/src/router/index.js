import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'

const Layout = () => import('@/views/layout/Index.vue')

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/Index.vue'),
    meta: { title: '登录' },
  },
  {
    path: '/',
    component: Layout,
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/dashboard/Index.vue'),
        meta: { title: '首页', icon: 'Odometer' },
      },
    ],
  },
  {
    path: '/artifact',
    component: Layout,
    redirect: '/artifact/list',
    meta: { title: '文物管理', icon: 'Picture' },
    children: [
      {
        path: 'list',
        name: 'ArtifactList',
        component: () => import('@/views/artifact/List.vue'),
        meta: { title: '文物列表', icon: 'List' },
      },
    ],
  },
  {
    path: '/knowledge',
    component: Layout,
    redirect: '/knowledge/list',
    meta: { title: '知识图谱', icon: 'Share' },
    children: [
      {
        path: 'list',
        name: 'KnowledgeList',
        component: () => import('@/views/knowledge/List.vue'),
        meta: { title: '三元组管理', icon: 'Connection' },
      },
    ],
  },
  {
    path: '/backup',
    component: Layout,
    redirect: '/backup/list',
    meta: { title: '备份管理', icon: 'FolderOpened' },
    children: [
      {
        path: 'list',
        name: 'BackupList',
        component: () => import('@/views/backup/List.vue'),
        meta: { title: '备份记录', icon: 'Files' },
      },
    ],
  },
  {
    path: '/log',
    component: Layout,
    redirect: '/log/operation',
    meta: { title: '日志管理', icon: 'Document' },
    children: [
      {
        path: 'operation',
        name: 'LogOperation',
        component: () => import('@/views/log/Operation.vue'),
        meta: { title: '操作日志', icon: 'EditPen' },
      },
      {
        path: 'security',
        name: 'LogSecurity',
        component: () => import('@/views/log/Security.vue'),
        meta: { title: '安全日志', icon: 'Lock' },
      },
      {
        path: 'system',
        name: 'LogSystem',
        component: () => import('@/views/log/System.vue'),
        meta: { title: '系统日志', icon: 'Monitor' },
      },
    ],
  },
  {
    path: '/system',
    component: Layout,
    redirect: '/system/admin',
    meta: { title: '系统管理', icon: 'Setting' },
    children: [
      {
        path: 'admin',
        name: 'SystemAdmin',
        component: () => import('@/views/system/Admin.vue'),
        meta: { title: '管理员管理', icon: 'User' },
      },
      {
        path: 'user',
        name: 'SystemUser',
        component: () => import('@/views/system/User.vue'),
        meta: { title: '普通用户管理', icon: 'UserFilled' },
      },
      {
        path: 'role',
        name: 'SystemRole',
        component: () => import('@/views/system/Role.vue'),
        meta: { title: '角色管理', icon: 'Avatar' },
      },
    ],
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/views/error/404.vue'),
    meta: { title: '404' },
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

// Route guard
router.beforeEach(async (to, from, next) => {
  document.title = to.meta.title ? `${to.meta.title} - 文化遗产后台管理` : '文化遗产后台管理'
  const userStore = useUserStore()

  if (to.path === '/login') {
    if (userStore.isLogin) {
      next('/dashboard')
    } else {
      next()
    }
    return
  }

  if (!userStore.isLogin) {
    next('/login')
    return
  }

  // Fetch menus if not loaded
  if (userStore.menus.length === 0) {
    try {
      await userStore.fetchMenus()
    } catch (e) {
      // Menu fetch failed, continue anyway
    }
  }

  next()
})

export default router
