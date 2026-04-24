/**
 * Permission directive (async).
 * Usage: v-permission="'biz.xxx'" or v-permission="['a','b']"
 */
import { hasPermission } from '@/utils/permission'
import { getAccessToken } from '@/utils/auth'

function removeElement(el) {
  if (el && el.parentNode) {
    el.parentNode.removeChild(el)
  }
}

/** OAuth code flow: default child can mount before access_token is stored; avoid firing API without Bearer. */
async function waitForAccessToken(maxWaitMs = 12000) {
  const step = 50
  for (let t = 0; t < maxWaitMs && !getAccessToken(); t += step) {
    await new Promise((r) => setTimeout(r, step))
  }
}

async function checkPermission(el, value) {
  if (!value) {
    removeElement(el)
    return
  }
  await waitForAccessToken()
  try {
    const ok = await hasPermission(value)
    if (!ok) {
      removeElement(el)
    }
  } catch (e) {
    // Fail-closed
    removeElement(el)
  }
}

export default {
  mounted(el, binding) {
    checkPermission(el, binding.value)
  },
  updated(el, binding) {
    checkPermission(el, binding.value)
  }
}
