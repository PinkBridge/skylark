/**
 * Permission utility functions
 */
import { getMyMenuTree } from '@/api/me'
import { setMenuTreeForPermLabels } from '@/utils/menuPermLabelNames'

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

/**
 * 与侧栏菜单树一致，写入本地权限缓存（含按钮类 permlabel），供 v-permission / hasPermission 使用。
 */
export function persistPermissionLabelsFromMenuTree(tree) {
  const labels = collectPermissionLabelsFromTree(tree)
  localStorage.setItem('permissions', JSON.stringify(labels))
}

/**
 * Check if user has a specific permission
 * @param {string|string[]} permission - Permission label(s) to check
 * @returns {boolean} - True if user has the permission
 */
export async function hasPermission(permission) {
  if (!permission) {
    return false
  }

  // 从缓存中获取权限
  let permissions = null
  try {
    const cached = localStorage.getItem('permissions')
    if (cached) {
      permissions = JSON.parse(cached)
    }
  } catch (error) {
    console.error('Failed to parse permissions from cache:', error)
    permissions = null
  }

  if (!permissions || !Array.isArray(permissions) || permissions.length === 0) {
    // 从后端获取菜单树
    try {
      const menuTree = await getMyMenuTree()
      setMenuTreeForPermLabels(menuTree)
      permissions = collectPermissionLabelsFromTree(menuTree)
      // 存储到缓存中
      localStorage.setItem('permissions', JSON.stringify(permissions))
    } catch (error) {
      console.error('Failed to get menu tree:', error)
      return false
    }
  }

  return permissions.includes(permission)
}
