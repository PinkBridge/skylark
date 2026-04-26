<template>
  <div class="app-switch">
    <el-dropdown :disabled="apps.length === 0" @command="handleCommand">
      <span class="el-dropdown-link">
        <el-icon class="app-icon"><Grid /></el-icon>
        {{ currentLabel }}
      </span>
      <template #dropdown>
        <el-dropdown-menu>
          <el-dropdown-item
            v-for="app in apps"
            :key="app.clientId"
            :command="app.clientId"
            :disabled="app.clientId === currentClientId"
          >
            {{ app.name || app.clientId }}
          </el-dropdown-item>
        </el-dropdown-menu>
      </template>
    </el-dropdown>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Grid } from '@element-plus/icons-vue'
import { getAppList } from '@/views/apps/AppApi'

const apps = ref([])

const currentClientId = String(
  process.env.VUE_APP_CLIENT_ID || process.env.VUE_APP_MENU_APP || 'permission-web'
).trim()

const currentLabel = computed(() => {
  const hit = apps.value.find((a) => a && a.clientId === currentClientId)
  return hit?.name || currentClientId || 'Apps'
})

function buildTargetHomeUrl(app) {
  const port = app?.port
  const protocol = window.location.protocol || 'http:'
  const hostname = window.location.hostname
  if (!hostname) return null

  if (typeof port === 'number' && port > 0) {
    return `${protocol}//${hostname}:${port}/home`
  }

  const raw = app?.webServerRedirectUri ? String(app.webServerRedirectUri) : ''
  const first = raw.split(',').map((s) => s.trim()).filter(Boolean)[0]
  if (first) {
    try {
      const u = new URL(first)
      return `${u.origin}/home`
    } catch (e) {
      // ignore
    }
  }
  return null
}

function handleCommand(clientId) {
  const target = apps.value.find((a) => a && a.clientId === clientId)
  if (!target) return
  const url = buildTargetHomeUrl(target)
  if (!url) {
    ElMessage.warning('Target app is not available')
    return
  }
  window.location.href = url
}

async function loadApps() {
  try {
    const list = await getAppList()
    apps.value = Array.isArray(list) ? list.filter((a) => a && a.open !== false) : []
  } catch (e) {
    // ignore
  }
}

onMounted(() => {
  loadApps()
})
</script>

<style scoped>
.app-switch {
  display: flex;
  align-items: center;
  justify-content: center;
}

.app-icon {
  margin-right: 6px;
  vertical-align: middle;
}

.el-dropdown-link {
  display: flex;
  align-items: center;
  cursor: pointer;
  border: none;
}
</style>

