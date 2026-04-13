/**
 * Permission directive
 * Usage: v-permission="'perm:user:create'" or v-permission="['perm:user:create', 'perm:user:edit']"
 */
import { hasPermission } from '../utils/permission'

/**
 * Remove element from DOM
 */
function removeElement(el) {
  if (el && el.parentNode) {
    el.parentNode.removeChild(el)
  }
}

/**
 * Check permission and remove element if not authorized
 */
async function checkPermission(el, value) {
  if (!value) {
    removeElement(el)
    return
  }

  try {
    const hasPerm = await hasPermission(value)
    if (!hasPerm) {
      removeElement(el)
    }
  } catch (error) {
    console.error('Permission check failed:', error)
    // On error, hide the element for security
    removeElement(el)
  }
}

/**
 * Permission directive
 */
const permission = {
  mounted(el, binding) {
    checkPermission(el, binding.value)
  },
  updated(el, binding) {
    checkPermission(el, binding.value)
  }
}

export default permission

