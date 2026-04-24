import { getMyMenuTree } from '@/api/me'
import { getAccessToken } from '@/utils/auth'

export function collectPermissionLabelsFromTree(tree) {
  let result = []
  if (!Array.isArray(tree)) {
    return result
  }
  for (const node of tree) {
    if (node.permlabel) {
      result.push(node.permlabel)
    }
    if (Array.isArray(node.children) && node.children.length > 0) {
      result = result.concat(collectPermissionLabelsFromTree(node.children))
    }
  }
  return result
}

export function persistPermissionLabelsFromMenuTree(tree) {
  const labels = collectPermissionLabelsFromTree(tree)
  localStorage.setItem('permissions', JSON.stringify(labels))
}

/**
 * Async permission check aligned with permission-pc.
 * - Uses localStorage cache first
 * - Falls back to /menus/me/tree to rebuild cache
 */
export async function hasPermission(permission) {
  if (!permission) {
    return false
  }
  // OAuth callback: callers should wait for a token before invoking; see permission directive.
  if (!getAccessToken()) {
    return false
  }

  let permissions = null
  try {
    const cached = localStorage.getItem('permissions')
    if (cached) {
      permissions = JSON.parse(cached)
    }
  } catch (e) {
    permissions = null
  }

  if (!permissions || !Array.isArray(permissions) || permissions.length === 0) {
    const menuTree = await getMyMenuTree()
    permissions = collectPermissionLabelsFromTree(menuTree)
    localStorage.setItem('permissions', JSON.stringify(permissions))
  }

  if (Array.isArray(permission)) {
    return permission.every((p) => permissions.includes(p))
  }
  return permissions.includes(permission)
}
