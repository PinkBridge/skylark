import { createRouter, createWebHistory } from 'vue-router'
import { isAuthenticated } from '@/utils/auth'
import { getAuthorizationUrl } from '@/api/oauth'
import { ShellWelcome, ShellHomeLayout } from '@skylark/admin-shell'
import { syncBrowserTabTitle } from '@skylark/tenant-client'

const routes = [
  { path: '/', redirect: '/home' },
  {
    path: '/welcome',
    name: 'Welcome',
    component: ShellWelcome,
    meta: { requiresAuth: false }
  },
  {
    path: '/home',
    name: 'Home',
    component: ShellHomeLayout,
    meta: { requiresAuth: false },
    children: [
      {
        path: '',
        name: 'Dashboard',
        component: () => import('@/views/Dashboard.vue'),
        meta: { requiresAuth: true }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  syncBrowserTabTitle()
  if (to.query.code) {
    next()
    return
  }
  const requiresAuth = to.matched.some((r) => r.meta.requiresAuth)
  if (requiresAuth && !isAuthenticated()) {
    window.location.href = getAuthorizationUrl()
    return
  }
  next()
})

export default router
