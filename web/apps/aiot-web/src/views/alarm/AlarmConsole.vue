<template>
  <el-card shadow="always">
    <el-tabs v-model="activeTab" @tab-change="onTabChange">
      <el-tab-pane :label="t('AlarmRulesTab')" name="rules">
        <div class="toolbar">
          <el-button v-permission="'iot.alarm.edit'" type="primary" :icon="Plus" @click="openRuleCreate">
            {{ t('NewButtonLabel') }}
          </el-button>
          <el-button :icon="Refresh" @click="loadRules">{{ t('RefreshButtonLabel') }}</el-button>
        </div>

        <el-table :data="ruleRows" stripe border empty-text=" ">
          <template #empty>
            <el-empty :description="t('AlarmRulesEmpty')" />
          </template>
          <el-table-column prop="name" :label="t('NameLabel')" min-width="180" show-overflow-tooltip />
          <el-table-column prop="deviceGroupKey" :label="t('IntegrationDeviceGroupsLabel')" min-width="180" show-overflow-tooltip />
          <el-table-column prop="sourceType" :label="t('AlarmSourceType')" width="110" />
          <el-table-column prop="severity" :label="t('AlarmSeverity')" width="110" />
          <el-table-column :label="t('AlarmTriggerMode')" width="140">
            <template #default="{ row }">
              {{ row.triggerMode }}<span v-if="row.triggerMode === 'DURATION'"> ({{ row.durationSeconds }}s)</span>
            </template>
          </el-table-column>
          <el-table-column :label="t('StatusLabel')" width="110">
            <template #default="{ row }">
              <el-tag :type="row.enabled ? 'success' : 'info'" size="small">{{ row.enabled ? t('Enabled') : t('Disabled') }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="updatedAt" :label="t('IntegrationUpdatedAt')" min-width="160" />
          <el-table-column :label="t('OperationsLabel')" width="260" fixed="right">
            <template #default="{ row }">
              <el-button link type="primary" @click="openRuleDetail(row)">{{ t('DetailLabel') }}</el-button>
              <el-button v-permission="'iot.alarm.edit'" link type="primary" @click="openRuleEdit(row)">{{ t('EditLabel') }}</el-button>
              <el-button v-permission="'iot.alarm.edit'" link type="primary" @click="confirmDeleteRule(row)">{{ t('DeleteLabel') }}</el-button>
            </template>
          </el-table-column>
        </el-table>

        <div class="pagination-wrap">
          <el-pagination
            v-model:current-page="rulePage.pageNum"
            v-model:page-size="rulePage.pageSize"
            background
            layout="total, sizes, prev, pager, next"
            :total="rulePage.total"
            :page-sizes="[10, 20, 50]"
            @current-change="loadRules"
            @size-change="loadRules"
          />
        </div>
      </el-tab-pane>

      <el-tab-pane :label="t('AlarmRecordsTab')" name="records">
        <div class="toolbar">
          <el-button :icon="Refresh" @click="loadRecords">{{ t('RefreshButtonLabel') }}</el-button>
        </div>
        <el-table :data="recordRows" stripe border empty-text=" ">
          <template #empty>
            <el-empty :description="t('AlarmRecordsEmpty')" />
          </template>
          <el-table-column prop="status" :label="t('StatusLabel')" width="110" />
          <el-table-column prop="severity" :label="t('AlarmSeverity')" width="110" />
          <el-table-column prop="deviceGroupKey" :label="t('IntegrationDeviceGroupsLabel')" min-width="180" show-overflow-tooltip />
          <el-table-column prop="productKey" :label="t('ProductKeyLabel')" min-width="140" />
          <el-table-column prop="deviceKey" :label="t('DeviceKeyLabel')" min-width="140" />
          <el-table-column prop="ruleId" :label="t('AlarmRuleId')" width="120" />
          <el-table-column prop="lastTriggeredAt" :label="t('AlarmLastTriggeredAt')" min-width="160" />
          <el-table-column :label="t('OperationsLabel')" width="120" fixed="right">
            <template #default="{ row }">
              <el-button link type="primary" @click="openRecordDetail(row)">{{ t('DetailLabel') }}</el-button>
            </template>
          </el-table-column>
        </el-table>
        <div class="pagination-wrap">
          <el-pagination
            v-model:current-page="recordPage.pageNum"
            v-model:page-size="recordPage.pageSize"
            background
            layout="total, sizes, prev, pager, next"
            :total="recordPage.total"
            :page-sizes="[10, 20, 50]"
            @current-change="loadRecords"
            @size-change="loadRecords"
          />
        </div>
      </el-tab-pane>
    </el-tabs>

    <el-dialog v-model="ruleDialog.visible" :title="ruleDialog.id ? t('EditTitle') : t('CreateTitle')" width="720px" destroy-on-close @closed="resetRuleForm">
      <el-form ref="ruleFormRef" :model="ruleForm" :rules="ruleRules" label-position="top">
        <el-form-item :label="t('IntegrationDeviceGroupsLabel')" prop="deviceGroupKey">
          <el-select v-model="ruleForm.deviceGroupKey" filterable style="width: 100%">
            <el-option v-for="g in deviceGroupOptions" :key="g.groupKey" :label="g.name ? `${g.name} (#${g.groupKey})` : `#${g.groupKey}`" :value="g.groupKey" />
          </el-select>
        </el-form-item>
        <el-form-item :label="t('NameLabel')" prop="name">
          <el-input v-model="ruleForm.name" />
        </el-form-item>
        <el-form-item :label="t('AlarmSourceType')" prop="sourceType">
          <el-select v-model="ruleForm.sourceType" style="width: 100%">
            <el-option label="PROPERTY" value="PROPERTY" />
            <el-option label="EVENT" value="EVENT" />
          </el-select>
        </el-form-item>
        <el-form-item :label="t('AlarmSeverity')" prop="severity">
          <el-select v-model="ruleForm.severity" style="width: 100%">
            <el-option label="HIGH" value="HIGH" />
            <el-option label="MEDIUM" value="MEDIUM" />
            <el-option label="LOW" value="LOW" />
          </el-select>
        </el-form-item>
        <el-form-item :label="t('AlarmTriggerMode')" prop="triggerMode">
          <el-select v-model="ruleForm.triggerMode" style="width: 100%">
            <el-option label="INSTANT" value="INSTANT" />
            <el-option label="DURATION" value="DURATION" />
          </el-select>
        </el-form-item>
        <el-form-item v-if="ruleForm.triggerMode === 'DURATION'" :label="t('AlarmDurationSeconds')" prop="durationSeconds">
          <el-input-number v-model="ruleForm.durationSeconds" :min="1" :max="86400" style="width: 100%" />
        </el-form-item>
        <el-form-item :label="t('AlarmRecoveryMode')" prop="recoveryMode">
          <el-select v-model="ruleForm.recoveryMode" style="width: 100%">
            <el-option label="AUTO" value="AUTO" />
            <el-option label="MANUAL" value="MANUAL" />
          </el-select>
        </el-form-item>
        <el-form-item :label="t('AlarmDedupMode')" prop="dedupMode">
          <el-select v-model="ruleForm.dedupMode" style="width: 100%">
            <el-option label="SINGLE_ACTIVE" value="SINGLE_ACTIVE" />
            <el-option label="EVERY_TRIGGER" value="EVERY_TRIGGER" />
          </el-select>
        </el-form-item>
        <el-form-item :label="t('StatusLabel')" prop="enabled">
          <el-switch v-model="ruleForm.enabled" />
        </el-form-item>
        <el-form-item :label="t('AlarmConditionJson')" prop="conditionJson">
          <el-input v-model="ruleForm.conditionJson" type="textarea" :rows="8" :placeholder="t('AlarmConditionJsonPlaceholder')" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="ruleDialog.visible = false">{{ t('CancelButtonText') }}</el-button>
        <el-button type="primary" @click="submitRule">{{ t('ConfirmButtonText') }}</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="detailDialog.visible" :title="t('DetailLabel')" width="720px" destroy-on-close>
      <el-form label-position="top">
        <el-form-item :label="t('AlarmJson')">
          <el-input :model-value="detailDialog.json" type="textarea" :rows="14" readonly />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="detailDialog.visible = false">{{ t('CancelButtonText') }}</el-button>
      </template>
    </el-dialog>
  </el-card>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Refresh } from '@element-plus/icons-vue'
import { listDeviceGroups } from '@/views/device-groups/DeviceGroupApi'
import {
  createAlarmRule,
  deleteAlarmRule,
  getAlarmRecord,
  getAlarmRule,
  listAlarmRecords,
  listAlarmRules,
  updateAlarmRule
} from '@/views/alarm/AlarmApi'

const { t } = useI18n()
const activeTab = ref('rules')

const deviceGroupOptions = ref([])

async function loadDeviceGroups() {
  try {
    const raw = await listDeviceGroups({ pageNum: 1, pageSize: 1000 })
    deviceGroupOptions.value = Array.isArray(raw?.records) ? raw.records : (Array.isArray(raw) ? raw : [])
  } catch {
    deviceGroupOptions.value = []
  }
}

const ruleRows = ref([])
const rulePage = reactive({ pageNum: 1, pageSize: 10, total: 0 })
const recordRows = ref([])
const recordPage = reactive({ pageNum: 1, pageSize: 10, total: 0 })

function parsePage(raw) {
  const records = Array.isArray(raw?.records) ? raw.records : []
  const total = Number.isFinite(Number(raw?.total)) ? Number(raw.total) : records.length
  return { records, total }
}

async function loadRules() {
  const raw = await listAlarmRules({ pageNum: rulePage.pageNum, pageSize: rulePage.pageSize })
  const { records, total } = parsePage(raw)
  ruleRows.value = records
  rulePage.total = total
}

async function loadRecords() {
  const raw = await listAlarmRecords({ pageNum: recordPage.pageNum, pageSize: recordPage.pageSize })
  const { records, total } = parsePage(raw)
  recordRows.value = records
  recordPage.total = total
}

function onTabChange(name) {
  if (name === 'records') loadRecords()
}

const ruleDialog = reactive({ visible: false, id: null })
const ruleFormRef = ref(null)
const ruleForm = reactive({
  deviceGroupKey: '',
  name: '',
  sourceType: 'PROPERTY',
  severity: 'MEDIUM',
  triggerMode: 'INSTANT',
  durationSeconds: 0,
  recoveryMode: 'AUTO',
  dedupMode: 'SINGLE_ACTIVE',
  enabled: true,
  conditionJson: ''
})

function validateJson(str) {
  try {
    JSON.parse(str)
    return true
  } catch {
    return false
  }
}

const ruleRules = computed(() => ({
  deviceGroupKey: [{ required: true, message: t('AlarmDeviceGroupRequired'), trigger: 'change' }],
  name: [{ required: true, message: t('NameRequired'), trigger: 'blur' }],
  conditionJson: [{
    validator: (_, value, callback) => {
      if (!value || !String(value).trim()) {
        callback(new Error(t('AlarmConditionJsonRequired')))
        return
      }
      if (!validateJson(value)) {
        callback(new Error(t('IntegrationInvalidJson')))
        return
      }
      callback()
    },
    trigger: 'blur'
  }]
}))

function resetRuleForm() {
  ruleFormRef.value?.resetFields?.()
}

function openRuleCreate() {
  ruleDialog.id = null
  ruleDialog.visible = true
  ruleForm.deviceGroupKey = ''
  ruleForm.name = ''
  ruleForm.sourceType = 'PROPERTY'
  ruleForm.severity = 'MEDIUM'
  ruleForm.triggerMode = 'INSTANT'
  ruleForm.durationSeconds = 0
  ruleForm.recoveryMode = 'AUTO'
  ruleForm.dedupMode = 'SINGLE_ACTIVE'
  ruleForm.enabled = true
  ruleForm.conditionJson = ''
}

async function openRuleEdit(row) {
  const detail = await getAlarmRule(row.id)
  ruleDialog.id = detail.id
  ruleDialog.visible = true
  Object.assign(ruleForm, {
    deviceGroupKey: detail.deviceGroupKey || '',
    name: detail.name || '',
    sourceType: detail.sourceType || 'PROPERTY',
    severity: detail.severity || 'MEDIUM',
    triggerMode: detail.triggerMode || 'INSTANT',
    durationSeconds: Number(detail.durationSeconds || 0),
    recoveryMode: detail.recoveryMode || 'AUTO',
    dedupMode: detail.dedupMode || 'SINGLE_ACTIVE',
    enabled: !!detail.enabled,
    conditionJson: typeof detail.conditionJson === 'string' ? detail.conditionJson : JSON.stringify(detail.conditionJson || {}, null, 2)
  })
}

const detailDialog = reactive({ visible: false, json: '' })

async function openRuleDetail(row) {
  const detail = await getAlarmRule(row.id)
  detailDialog.json = JSON.stringify(detail, null, 2)
  detailDialog.visible = true
}

async function openRecordDetail(row) {
  const detail = await getAlarmRecord(row.id)
  detailDialog.json = JSON.stringify(detail, null, 2)
  detailDialog.visible = true
}

async function submitRule() {
  await ruleFormRef.value?.validate?.()
  const body = {
    deviceGroupKey: ruleForm.deviceGroupKey,
    name: ruleForm.name,
    sourceType: ruleForm.sourceType,
    severity: ruleForm.severity,
    triggerMode: ruleForm.triggerMode,
    durationSeconds: ruleForm.triggerMode === 'DURATION' ? Number(ruleForm.durationSeconds || 1) : 0,
    recoveryMode: ruleForm.recoveryMode,
    dedupMode: ruleForm.dedupMode,
    enabled: !!ruleForm.enabled,
    conditionJson: String(ruleForm.conditionJson || '').trim()
  }
  if (ruleDialog.id) {
    await updateAlarmRule(ruleDialog.id, body)
    ElMessage.success(t('UpdateSuccess'))
  } else {
    await createAlarmRule(body)
    ElMessage.success(t('CreateSuccess'))
  }
  ruleDialog.visible = false
  await loadRules()
}

async function confirmDeleteRule(row) {
  try {
    await ElMessageBox.confirm(t('DeleteConfirmLabel'), t('NoticeTitle'), { type: 'warning' })
    await deleteAlarmRule(row.id)
    ElMessage.success(t('DeleteSuccess'))
    await loadRules()
  } catch (e) {
    if (e !== 'cancel') {
      /* */
    }
  }
}

onMounted(async () => {
  await loadDeviceGroups()
  await loadRules()
})
</script>

<style scoped>
.toolbar {
  display: flex;
  gap: 8px;
  margin-bottom: 12px;
}
.pagination-wrap {
  margin-top: 12px;
  display: flex;
  justify-content: flex-end;
}
</style>

