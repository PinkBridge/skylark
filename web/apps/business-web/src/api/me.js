import http from '@/api/http'

// business-web must request menus scoped to itself by default.
const MENU_APP =
  process.env.VUE_APP_MENU_APP ||
  process.env.VUE_APP_CLIENT_ID ||
  'business-web'

export function getMyMenuTree() {
  return http.get('/permission/menus/me/tree', { app: MENU_APP })
}

export function getMyInfo() {
  return http.get('/permission/users/me')
}

export function changePassword(form) {
  return http.put('/permission/users/me/password', {
    oldPassword: form.oldPassword,
    newPassword: form.newPassword
  })
}

export function updateMyProfile(data) {
  return http.put('/permission/users/me', data)
}
