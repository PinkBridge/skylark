<template>
  <el-menu :key="menuKey" class="el-menu-vertical" :default-active="activeMenu" router background-color="#001529" 
  text-color="#fff" active-text-color="#ffd04b" :default-openeds="defaultOpeneds">
    <template v-for="group in menuItems" :key="group.id">
      <el-sub-menu :index="String(group.id)">
        <template #title>
          <component v-if="group.icon" :is="Icons[group.icon]" class="el-icon" />
          <span>{{ group.name }}</span>
        </template>
        <el-menu-item v-for="item in getVisibleChildren(group.children)" :key="item.id" :index="resolveMenuIndex(item)">
          <component v-if="item.icon" :is="Icons[item.icon]" class="el-icon" />
          <span>{{ item.name }}</span>
        </el-menu-item>
      </el-sub-menu>
    </template>
  </el-menu>
</template>

<script>
import { ref, onMounted, watch, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import * as Icons from '@element-plus/icons-vue'
import { getMyMenuTree } from '@/api/me'
import { getAccessToken } from '@/utils/auth'
import { setMenuTreeForPermLabels } from '@/utils/menuPermLabelNames'
import { persistPermissionLabelsFromMenuTree } from '@/utils/permission'
import i18n from '@/i18n/index'

export default {
  name: 'MenuComponent',
  setup() {
    const route = useRoute()
    const router = useRouter()
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

    const selectFirstChild = () => {
      // Only redirect to first child if current path is exactly /home or /home/
      // Don't redirect if user is already on a specific route like /perm/tenants
      if (route.path === '/home' || route.path === '/home/') {
        if (!menuItems.value.length) return
        const firstGroup = menuItems.value[0]
        const firstChild = getVisibleChildren(firstGroup?.children)?.[0]
        const targetPath = firstChild?.path ? String(firstChild.path) : '/home'
        router.replace(targetPath)
        activeMenu.value = targetPath
      } else {
        // Just update activeMenu to match current route
        activeMenu.value = route.path
      }
    }

    const fetchMenuTree = async () => {
      if (loadedFromServer.value) return
      try {
        const data = await getMyMenuTree()
        if (data) {
          setMenuTreeForPermLabels(data)
          persistPermissionLabelsFromMenuTree(data)
          // Set defaultOpeneds first
          defaultOpeneds.value = data.map(item => String(item.id))
          menuItems.value = data
          loadedFromServer.value = true
          // Force re-render by updating key
          nextTick(() => {
            menuKey.value++
            selectFirstChild()
          })
        }
      } catch (error) {
        console.error('Failed to get menu:', error)
        ElMessage.warning('Failed to get menu')
      }
    }

    const tryFetchWithToken = () => {
      const token = getAccessToken()
      if (token) {
        fetchMenuTree()
      }
    }

    onMounted(() => {
      tryFetchWithToken()
      // Poll for token availability if not immediately available
      const checkTokenInterval = setInterval(() => {
        if (!loadedFromServer.value && getAccessToken()) {
          fetchMenuTree()
          clearInterval(checkTokenInterval)
        }
      }, 100)
      
      // Clear interval after 5 seconds to avoid infinite polling
      setTimeout(() => {
        clearInterval(checkTokenInterval)
      }, 5000)
    })

    watch(
      () => route.path,
      (newPath) => {
        activeMenu.value = newPath
      }
    )

    watch(
      () => i18n.global.locale.value,
      async () => {
        if (!getAccessToken()) {
          return
        }
        try {
          const data = await getMyMenuTree()
          if (data) {
            setMenuTreeForPermLabels(data)
            persistPermissionLabelsFromMenuTree(data)
            defaultOpeneds.value = data.map(item => String(item.id))
            menuItems.value = data
            nextTick(() => {
              menuKey.value++
              if (route.path === '/home' || route.path === '/home/') {
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

    return {
      activeMenu,
      menuItems,
      resolveMenuIndex,
      getVisibleChildren,
      Icons,
      defaultOpeneds,
      menuKey
    }
  }
}
</script>

<style scoped>
.el-menu-vertical {
  border-right: none;
}

.el-menu-vertical :deep(.el-menu-item.is-active) {
  background-color: #409eff;
  color: #fff;
}

.el-menu-vertical :deep(.el-menu-item.is-active:hover) {
  background-color: #409eff;
}

.el-menu-vertical :deep(.el-menu-item.is-active .el-icon) {
  color: #fff;
}
</style>