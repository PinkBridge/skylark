import { ref } from 'vue'

/** permlabel → display name from last /menus/me/tree response */
export const menuPermLabelMap = ref(new Map())

function flattenToMap(nodes, map) {
  if (!Array.isArray(nodes)) return
  for (const n of nodes) {
    if (n.permlabel && n.name != null && String(n.name).length > 0) {
      map.set(n.permlabel, n.name)
    }
    if (n.children?.length) {
      flattenToMap(n.children, map)
    }
  }
}

export function setMenuTreeForPermLabels(tree) {
  const m = new Map()
  flattenToMap(Array.isArray(tree) ? tree : [], m)
  menuPermLabelMap.value = m
}

export function collectPermissionLabelsFromTree(tree) {
  let result = []
  if (!Array.isArray(tree)) return result
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
