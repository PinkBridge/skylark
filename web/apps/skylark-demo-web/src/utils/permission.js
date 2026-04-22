import { createHasPermission } from '@skylark/authz-vue'
import { getMyMenuTree } from '@/api/me'

export const hasPermission = createHasPermission({
  fetchMenuTree: getMyMenuTree
})
