import { createRouter, createWebHistory } from 'vue-router'
import { isAuthenticated } from '../utils/auth'
import { getAuthorizationUrl } from '../api/oauth'
import i18n from '@/i18n/index'
import { getPlatformInitState } from '@/api/init'

// route configuration
const routes = [
  {
    path: '/',
    redirect: '/home'
  },
  {
    path: '/init',
    name: 'PlatformInit',
    component: () => import('@/views/PlatformInit.vue'),
    meta: {
      title: 'Initialize',
      requiresAuth: false
    }
  },
  {
    path: '/welcome',
    name: 'Welcome',
    component: () => import('@/views/Welcome.vue'),
    meta: {
      title: 'Welcome',
      requiresAuth: false
    }
  },
  {
    path: '/home',
    component: () => import('@/views/Home.vue'),
    name: 'Home',
    meta: {
      title: 'Home',
      requiresAuth: false // Need to handle OAuth callback here, relax restriction
    },
    children: [
      {
        path: '/perm/users',
        name: 'UserDataList',
        component: () => import('@/views/users/UserDataList.vue'),
        meta: {
          title: 'Users',
          requiresAuth: true
        }
      },
      {
        path: '/perm/roles',
        name: 'RoleDataList',
        component: () => import('@/views/roles/RoleDataList.vue'),
        meta: {
          title: 'Roles',
          requiresAuth: true
        }
      },
      {
        path: '/system/apis',
        name: 'ApiDataList',
        component: () => import('@/views/apis/ApiDataList.vue'),
        meta: {
          title: 'Apis',
          requiresAuth: true
        }
      },
      {
        path: '/system/whitelist',
        name: 'WhitelistDataList',
        component: () => import('@/views/whitelist/WhitelistDataList.vue'),
        meta: {
          title: 'Whitelists',
          requiresAuth: true
        }
      },
      {
        path: '/system/data-domains',
        name: 'DataDomainDataList',
        component: () => import('@/views/datadomains/DataDomainDataList.vue'),
        meta: {
          title: 'DataDomains',
          titleI18nKey: 'DataDomainManagementTitle',
          requiresAuth: true
        }
      },
      {
        path: '/system/menus',
        name: 'MenuDataList',
        component: () => import('@/views/menus/MenuDataList.vue'),
        meta: {
          title: 'Menus',
          requiresAuth: true
        }
      },
      {
        path: '/system/apps',
        name: 'AppDataList',
        component: () => import('@/views/apps/AppDataList.vue'),
        meta: {
          title: 'Apps',
          requiresAuth: true
        }
      },
      {
        path: '/system/tenant-profile',
        name: 'TenantProfile',
        component: () => import('@/views/tenants/TenantProfile.vue'),
        meta: {
          title: 'TenantProfile',
          requiresAuth: true
        }
      },
      {
        path: '/system/platform-config',
        name: 'PlatformConfigDataList',
        component: () => import('@/views/platformconfig/PlatformConfigDataList.vue'),
        meta: {
          titleI18nKey: 'SystemSettingTitle',
          requiresAuth: true
        }
      },
      {
        path: '/perm/orgs',
        name: 'OrgDataList',
        component: () => import('@/views/orgs/OrgDataList.vue'),
        meta: {
          title: 'Orgs',
          requiresAuth: true
        }
      },
      {
        path: '/perm/tenants',
        name: 'TenantDataList',
        component: () => import('@/views/tenants/TenantDataList.vue'),
        meta: {
          title: 'Tenants',
          requiresAuth: true
        }
      },
      {
        path: '/perm/resources',
        name: 'ResourceDataList',
        component: () => import('@/views/resources/ResourceDataList.vue'),
        meta: {
          title: 'Resources',
          requiresAuth: true
        }
      },
      {
        path: '/logger/loging',
        name: 'LoginLogDataList',
        component: () => import('@/views/loginlogs/LoginLogDataList.vue'),
        meta: {
          title: 'LoginLogs',
          requiresAuth: true
        }
      },
      {
        path: '/logger/operation-logs',
        name: 'OperationLogDataList',
        component: () => import('@/views/operationlogs/OperationLogDataList.vue'),
        meta: {
          title: 'OperationLogs',
          requiresAuth: true
        }
      }
    ]
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/views/NotFound.vue'),
    meta: {
      title: 'NotFound'
    }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

let initStateCache = null
let initStateCacheAt = 0
const INIT_STATE_TTL_MS = 5000

// route guard - control access permission
router.beforeEach(async (to, from, next) => {
  const titleKey = to.meta.titleI18nKey
  document.title = titleKey ? i18n.global.t(titleKey) : (to.meta.title || 'Permission App')

  const authenticated = isAuthenticated()
  const requiresAuth = to.matched.some(record => record.meta.requiresAuth)

  // If it's an OAuth callback (with code), allow entering Home first to handle it
  if (to.query.code) {
    next()
    return
  }

  // Platform initialization guard (anonymous).
  // If not initialized, force to /init (except itself).
  try {
    const now = Date.now()
    if (!initStateCache || (now - initStateCacheAt) > INIT_STATE_TTL_MS) {
      initStateCache = await getPlatformInitState()
      initStateCacheAt = now
    }
    if (initStateCache && initStateCache.initialized === false && to.path !== '/init') {
      next({ name: 'PlatformInit' })
      return
    }
    if (initStateCache && initStateCache.initialized === true && to.path === '/init') {
      next({ name: 'Welcome' })
      return
    }
  } catch (e) {
    // If init-state query fails, continue to avoid blocking routing.
  }

  if (requiresAuth && !authenticated) {
    const authUrl = getAuthorizationUrl()
    window.location.href = authUrl
    return
  }

  next()
})

export default router