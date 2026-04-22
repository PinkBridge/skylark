import http from '@/api/http'
import { MENU_APP_CODE } from '@/views/menus/MenuApi'

const APP_NAME = process.env.APP_NAME || 'permission'
const USER_PREFIX = "/" + APP_NAME + "/users"

/**
 * Get user information
 */
export function getMyInfo() {
  return http.get(`${USER_PREFIX}/me`)
}

/**
 * Change password
 */
export function changePassword(data) {
  return http.put(`${USER_PREFIX}/me/password`, data)
}

/**
 * 当前用户更新个人资料（头像、手机、邮箱、性别、详细地址）
 */
export function updateMyProfile(data) {
  return http.put(`${USER_PREFIX}/me`, data)
}

const MENU_PREFIX = "/" + APP_NAME + "/menus"

/**
 * Get current user's menu tree
 */
export function getMyMenuTree() {
  return http.get(`${MENU_PREFIX}/me/tree`, { app: MENU_APP_CODE })
}