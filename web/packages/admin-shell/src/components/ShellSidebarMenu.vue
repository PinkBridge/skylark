<template>
  <el-menu
    :key="menuKey"
    class="el-menu-vertical"
    :default-active="activeMenu"
    background-color="#001529"
    text-color="#fff"
    active-text-color="#ffd04b"
    :default-openeds="defaultOpeneds"
    @select="onMenuSelect"
  >
    <template v-for="group in menuItems" :key="group.id">
      <el-sub-menu :index="String(group.id)">
        <template #title>
          <component v-if="group.icon" :is="Icons[group.icon]" class="el-icon" />
          <span>{{ group.name }}</span>
        </template>
        <el-menu-item
          v-for="item in getVisibleChildren(group.children)"
          :key="item.id"
          :index="resolveMenuIndex(item)"
        >
          <component v-if="item.icon" :is="Icons[item.icon]" class="el-icon" />
          <span>{{ item.name }}</span>
        </el-menu-item>
      </el-sub-menu>
    </template>
  </el-menu>
</template>

<script setup>
import { ref, onMounted, watch, nextTick, inject } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { ElMessage } from 'element-plus'
import * as Icons from '@element-plus/icons-vue'
import { ADMIN_SHELL_CONFIG } from '../symbols'
import {
  setMenuTreeForPermLabels,
  persistPermissionLabelsFromMenuTree
} from '../utils/menuPermission'

const shell = inject(ADMIN_SHELL_CONFIG)
const route = useRoute()
const router = useRouter()
const { locale, t } = useI18n()

const activeMenu = ref(route.path)
const menuItems = ref([])
const loadedFromServer = ref(false)
const defaultOpeneds = ref([])
const menuKey = ref(0)

const resolveMenuIndex = (item) => {
  if (item?.path) return String(item.path)
  return item?.id ? String(item.id) : ''
}

const isVisibleMenu = (item) => {
  if (!item) return false
  const hidden = item.hidden === true || item.hidden === 1
  return item.type === 'menu' && !hidden
}

const getVisibleChildren = (children) => {
  if (!Array.isArray(children)) return []
  return children.filter(isVisibleMenu)
}

const normalizeMenuTree = (nodes) => {
  if (!Array.isArray(nodes)) return []
  const out = []
  for (const n of nodes) {
    if (!isVisibleMenu(n)) continue
    const normalizedChildren = normalizeMenuTree(n.children)
    out.push({
      ...n,
      children: normalizedChildren
    })
  }
  return out
}

const filterTopLevelGroups = (nodes) => {
  const normalized = normalizeMenuTree(nodes)
  return normalized.filter((n) => Array.isArray(n.children) && n.children.length > 0)
}

const homePath = shell.homePath || '/home'

/** Backend menu paths (e.g. /perm/users) often have no matching route in thin shells like iot-web. */
const isRoutablePath = (path) => {
  if (!path || typeof path !== 'string' || !path.startsWith('/')) return false
  return router.resolve({ path }).matched.length > 0
}

const onMenuSelect = (index) => {
  if (!index || typeof index !== 'string') return
  if (!index.startsWith('/')) return
  if (isRoutablePath(index)) {
    router.push(index)
    activeMenu.value = index
    return
  }
  ElMessage.info(t('MenuRouteNotInApp'))
  activeMenu.value = homePath
  menuKey.value++
}

const selectFirstChild = () => {
  if (route.path === homePath || route.path === `${homePath}/`) {
    if (!menuItems.value.length) return
    const firstGroup = menuItems.value.find(
      (g) => Array.isArray(getVisibleChildren(g?.children)) && getVisibleChildren(g.children).length > 0
    )
    const firstChild = firstGroup ? getVisibleChildren(firstGroup.children)?.[0] : null
    const targetPath = firstChild?.path ? String(firstChild.path) : homePath
    if (!isRoutablePath(targetPath)) {
      activeMenu.value = homePath
      return
    }
    router.replace(targetPath)
    activeMenu.value = targetPath
  } else {
    activeMenu.value = route.path
  }
}

const fetchMenuTree = async () => {
  if (loadedFromServer.value) return
  try {
    const data = await shell.getMyMenuTree()
    if (data) {
      setMenuTreeForPermLabels(data)
      persistPermissionLabelsFromMenuTree(data)
      const groups = filterTopLevelGroups(data)
      defaultOpeneds.value = groups.map((item) => String(item.id))
      menuItems.value = groups
      loadedFromServer.value = true
      nextTick(() => {
        menuKey.value++
        selectFirstChild()
      })
    }
  } catch (error) {
    console.error('Failed to get menu:', error)
    ElMessage.warning(error?.message || 'Failed to get menu')
  }
}

const tryFetchWithToken = () => {
  const token = shell.getAccessToken()
  if (token) {
    fetchMenuTree()
  }
}

onMounted(() => {
  tryFetchWithToken()
  const checkTokenInterval = setInterval(() => {
    if (!loadedFromServer.value && shell.getAccessToken()) {
      fetchMenuTree()
      clearInterval(checkTokenInterval)
    }
  }, 100)
  setTimeout(() => clearInterval(checkTokenInterval), 5000)
})

watch(
  () => route.path,
  (newPath) => {
    activeMenu.value = newPath
  }
)

watch(
  () => locale.value,
  async () => {
    if (!shell.getAccessToken()) return
    try {
      const data = await shell.getMyMenuTree()
      if (data) {
        setMenuTreeForPermLabels(data)
        persistPermissionLabelsFromMenuTree(data)
        const groups = filterTopLevelGroups(data)
        defaultOpeneds.value = groups.map((item) => String(item.id))
        menuItems.value = groups
        nextTick(() => {
          menuKey.value++
          if (route.path === homePath || route.path === `${homePath}/`) {
            selectFirstChild()
          } else {
            activeMenu.value = route.path
          }
        })
      }
    } catch (e) {
      console.error('Failed to refresh menu for locale:', e)
    }
  }
)
</script>

<style scoped>
.el-menu-vertical {
  border-right: none;
}

.el-menu-vertical :deep(.el-menu-item.is-active) {
  background-color: #409eff;
  color: #fff;
}

.el-icon {
  margin-right: 8px;
  width: 1em;
  height: 1em;
}
</style>
