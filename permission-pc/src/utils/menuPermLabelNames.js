import { ref } from 'vue'
import i18n from '@/i18n/index'

/** permlabel → name，来自最近一次 /menus/me/tree（与侧栏同源，name 随请求头 X-Language 由后端解析） */
export const menuPermLabelMap = ref(new Map())

function flattenToMap(nodes, map) {
  if (!Array.isArray(nodes)) {
    return
  }
  for (const n of nodes) {
    if (n.permlabel && n.name != null && String(n.name).length > 0) {
      map.set(n.permlabel, n.name)
    }
    if (n.children?.length) {
      flattenToMap(n.children, map)
    }
  }
}

/**
 * 菜单树加载后调用（Menu.vue、permission.js），与侧栏使用同一批节点。
 */
export function setMenuTreeForPermLabels(tree) {
  const m = new Map()
  flattenToMap(Array.isArray(tree) ? tree : [], m)
  menuPermLabelMap.value = m
}

/**
 * 按钮文案：优先菜单树中该 permlabel 的 name（多语言与侧栏一致），否则 i18n key，否则 permlabel。
 */
export function permLabelName(permlabel, fallbackI18nKey) {
  if (!permlabel) {
    return fallbackI18nKey ? i18n.global.t(fallbackI18nKey) : ''
  }
  const fromMenu = menuPermLabelMap.value.get(permlabel)
  if (fromMenu) {
    return fromMenu
  }
  if (fallbackI18nKey) {
    return i18n.global.t(fallbackI18nKey)
  }
  return permlabel
}
