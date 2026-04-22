<template>
  <div class="apps-container">
    <div class="apps-content">
      <div class="header">
        <h1 class="title">{{ t('AppsPageTitle') }}</h1>
        <div class="sub">{{ t('AppsPageSubtitle') }}</div>
      </div>

      <el-skeleton v-if="loading" :rows="6" animated />

      <el-empty v-else-if="!apps.length" :description="t('AppsPageEmpty')" />

      <el-row v-else :gutter="16" class="grid">
        <el-col v-for="app in apps" :key="app.clientId" :xs="24" :sm="12" :md="8" :lg="6">
          <el-card shadow="hover" class="app-card" @click="openApp(app)">
            <div class="card-body">
              <div class="card-title">{{ resolveAppName(app) }}</div>
              <div class="card-sub">{{ app.clientId }}</div>
              <div class="card-actions">
                <el-button type="primary" size="small" @click.stop="openApp(app)">
                  {{ t('AppsPageEnter') }}
                </el-button>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<script setup name="AppsPage">
import { ref, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { ElMessage } from 'element-plus'
import { getAppList } from '@/views/apps/AppApi'

const { t } = useI18n()

const loading = ref(false)
const apps = ref([])

function normalizeApps(response) {
  const rows = Array.isArray(response) ? response : (response?.records || response?.data || [])
  return Array.isArray(rows) ? rows : []
}

function resolveAppName(app) {
  const v = app?.name || app?.clientId
  return v != null && String(v).trim() ? String(v).trim() : '-'
}

function toAppEntryUrl(app) {
  const port = app?.port
  if (!port) return ''
  if (typeof window === 'undefined' || !window.location) return ''
  const protocol = window.location.protocol || 'http:'
  const hostname = window.location.hostname
  if (!hostname) return ''
  return `${protocol}//${hostname}:${port}/home`
}

function openApp(app) {
  const target = toAppEntryUrl(app)
  if (!target) {
    ElMessage.warning(t('AppsPageOpenFailed'))
    return
  }
  window.location.href = target
}

async function refresh() {
  loading.value = true
  try {
    const response = await getAppList()
    apps.value = normalizeApps(response)
  } catch (e) {
    apps.value = []
    ElMessage.error(e?.message || 'Load failed')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  refresh()
})
</script>

<style scoped>
.apps-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #667eea 100%);
  padding: 24px;
}

.apps-content {
  max-width: 1200px;
  margin: 0 auto;
}

.header {
  color: #fff;
  margin-bottom: 16px;
}

.title {
  margin: 0;
  font-size: 28px;
  font-weight: 700;
}

.sub {
  margin-top: 6px;
  opacity: 0.9;
}

.grid {
  margin-top: 12px;
}

.app-card {
  cursor: pointer;
  margin-bottom: 16px;
  border-radius: 10px;
}

.card-body {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.card-title {
  font-size: 16px;
  font-weight: 600;
}

.card-sub {
  color: rgba(0, 0, 0, 0.55);
  font-size: 12px;
}

.card-actions {
  margin-top: 8px;
}
</style>

