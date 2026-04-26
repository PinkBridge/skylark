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
import { ref, computed, inject, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Grid } from '@element-plus/icons-vue'
import { ADMIN_SHELL_CONFIG } from '../symbols'

const shell = inject(ADMIN_SHELL_CONFIG)

const apps = ref([])
const currentClientId = String(
  process.env.VUE_APP_CLIENT_ID || process.env.VUE_APP_MENU_APP || ''
).trim()

const currentLabel = computed(() => {
  const hit = apps.value.find((a) => a && a.clientId === currentClientId)
  return hit?.name || currentClientId || 'Apps'
})

function resolveApiBase() {
  const v = process.env.VUE_APP_API_BASE_URL
  return (v ? String(v) : '/api').replace(/\/$/, '')
}

function buildTargetHomeUrl(app) {
  const port = app?.port
  const protocol = window.location.protocol || 'http:'
  const hostname = window.location.hostname
  if (!hostname) return null

  if (typeof port === 'number' && port > 0) {
    return `${protocol}//${hostname}:${port}/home`
  }

  // Fallback: try parsing the first redirect URI origin.
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

async function loadApps() {
  try {
    const apiBase = resolveApiBase()
    const token = shell?.getAccessToken ? shell.getAccessToken() : ''
    const tenantId = shell?.getTenant ? shell.getTenant()?.id : null
    const headers = { 'Content-Type': 'application/json' }
    if (token) headers.Authorization = `Bearer ${token}`
    if (tenantId != null) headers['X-Tenant-Id'] = tenantId

    const resp = await fetch(`${apiBase}/permission/apps`, { headers })
    if (!resp.ok) {
      return
    }
    const data = await resp.json()
    const list = data && data.code === 200 ? (data.data || []) : []
    apps.value = Array.isArray(list) ? list.filter((a) => a && a.open !== false) : []
  } catch (e) {
    // ignore (header tool should not block the app)
  }
}

function handleCommand(clientId) {
  const target = apps.value.find((a) => a && a.clientId === clientId)
  if (!target) return
  const url = buildTargetHomeUrl(target)
  if (!url) {
    ElMessage.warning('Target app is not available')
    return
  }
  if (url === window.location.href || url === `${window.location.origin}/home`) {
    return
  }
  window.location.href = url
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
  color: rgba(0, 0, 0, 0.85);
  border: none;
}
</style>
