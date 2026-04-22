import router from '@/router'
import {
  getAuthorizationUrl,
  exchangeCodeForToken,
  checkToken,
  logout as oauthLogout
} from '@/api/oauth'
import {
  saveUserInfo,
  clearTokens,
  getAccessToken,
  getUserInfo,
  isAuthenticated
} from '@/utils/auth'
import { getTenant, getTenantForCurrentLocation } from '@/utils/tenant'
import { getMyMenuTree, getMyInfo, changePassword, updateMyProfile } from '@/api/me'

/**
 * Injected into @skylark/admin-shell (permission-app–aligned shell).
 */
export function createAdminShellConfig() {
  return {
    router,
    routeNames: { welcome: 'Welcome', home: 'Home' },
    homePath: '/home',
    showRegister: false,
    getTenantForCurrentLocation,
    getTenant,
    getAuthorizationUrl,
    exchangeCodeForToken,
    checkToken,
    logout: oauthLogout,
    saveUserInfo,
    clearTokens,
    getAccessToken,
    getUserInfo,
    isAuthenticated,
    getMyMenuTree,
    getMyInfo,
    changePassword,
    updateMyProfile
  }
}
