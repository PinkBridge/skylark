<template>
  <el-card shadow="always">
    <div class="buttons-block">
      <el-button type="default" size="default" :icon="Refresh" @click="loadData">{{
        t('RefreshButtonLabel')
      }}</el-button>
    </div>
    <el-table v-loading="loading" :data="tableData" style="width: 100%" stripe border show-overflow-tooltip>
      <el-table-column prop="configKey" :label="t('PlatformConfigKeyLabel')" min-width="220" />
      <el-table-column prop="valueType" :label="t('PlatformConfigTypeLabel')" width="100" />
      <el-table-column prop="description" :label="t('PlatformConfigDescriptionLabel')" min-width="280" />
      <el-table-column prop="configValue" :label="t('PlatformConfigValueLabel')" min-width="120">
        <template #default="{ row }">
          <span v-if="isBoolRow(row)">{{ parseBool(row.configValue) ? t('Active') : t('Disabled') }}</span>
          <span v-else>{{ row.configValue }}</span>
        </template>
      </el-table-column>
      <el-table-column :label="t('OperationsLabel')" width="160" fixed="right" :show-overflow-tooltip="false">
        <template #default="{ row }">
          <el-switch
            v-if="isBoolRow(row) && canEdit"
            v-model="boolStates[row.configKey]"
            @change="(v) => onBoolChange(row, v)"
          />
          <span v-else>-</span>
        </template>
      </el-table-column>
    </el-table>
  </el-card>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { Refresh } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { listPlatformConfigs, updatePlatformConfig } from '@/views/platformconfig/PlatformConfigApi'
import { getMyMenuTree } from '@/api/me'
import { setMenuTreeForPermLabels } from '@/utils/menuPermLabelNames'
import { hasPermission, persistPermissionLabelsFromMenuTree } from '@/utils/permission'

const { t } = useI18n()
const loading = ref(false)
const tableData = ref([])
const boolStates = ref({})
const canEdit = ref(false)

function isBoolRow(row) {
  return row?.valueType && String(row.valueType).toUpperCase() === 'BOOL'
}

function parseBool(v) {
  if (v === true || v === 1) return true
  const s = String(v ?? '').trim().toLowerCase()
  return s === 'true' || s === '1' || s === 'yes' || s === 'on'
}

function syncBoolStates(rows) {
  const next = {}
  for (const row of rows || []) {
    if (isBoolRow(row)) {
      next[row.configKey] = parseBool(row.configValue)
    }
  }
  boolStates.value = next
}

async function loadData() {
  loading.value = true
  try {
    const data = await listPlatformConfigs()
    tableData.value = Array.isArray(data) ? data : []
    syncBoolStates(tableData.value)
  } catch (e) {
    console.error('platform config list failed:', e)
  } finally {
    loading.value = false
  }
}

async function onBoolChange(row, newVal) {
  const key = row.configKey
  const strVal = newVal ? 'true' : 'false'
  try {
    await updatePlatformConfig(key, strVal)
    row.configValue = strVal
    ElMessage.success(t('UpdateSuccess'))
  } catch (e) {
    boolStates.value[key] = !newVal
    ElMessage.error(t('UpdateFailed'))
  }
}

onMounted(async () => {
  try {
    const tree = await getMyMenuTree()
    setMenuTreeForPermLabels(tree)
    persistPermissionLabelsFromMenuTree(tree)
  } catch (e) {
    console.warn('menu tree preload for permissions failed:', e)
  }
  canEdit.value = await hasPermission('system.platform.edit')
  loadData()
})
</script>

<style scoped>
.buttons-block {
  margin-bottom: 12px;
}
</style>
