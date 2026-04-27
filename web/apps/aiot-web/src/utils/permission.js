import { createHasPermission } from '@skylark/authz-vue'
import { getMyMenuTree } from '@/api/me'
import { getAccessToken } from '@/utils/auth'

export const hasPermission = createHasPermission({
  fetchMenuTree: getMyMenuTree,
  getAccessToken
})
