<template>
  <el-card shadow="always">
    <el-tabs v-model="activeTab" @tab-change="onTabChange">
      <el-tab-pane :label="t('AlarmRulesTab')" name="rules">
        <AlarmRuleSearchForm
          :search="handleRuleSearch"
          :reset="handleRuleReset"
          :device-group-options="deviceGroupOptions"
        />
        <div class="toolbar">
          <div class="toolbar-left">
            <el-button v-permission="'iot.alarm.edit'" type="primary" :icon="Plus" @click="createDialogVisible = true">
              {{ t('NewButtonLabel') }}
            </el-button>
            <el-button :icon="Refresh" @click="loadRules">{{ t('RefreshButtonLabel') }}</el-button>
          </div>
        </div>

        <el-table :data="ruleRows" stripe border show-overflow-tooltip empty-text=" ">
          <template #empty>
            <el-empty :description="t('AlarmRulesEmpty')" />
          </template>
          <el-table-column prop="name" :label="t('NameLabel')" min-width="180" show-overflow-tooltip />
          <el-table-column :label="t('IntegrationDeviceGroupsLabel')" min-width="200" show-overflow-tooltip>
            <template #default="{ row }">{{ deviceGroupCellLabel(row.deviceGroupKey) }}</template>
          </el-table-column>
          <el-table-column :label="t('AlarmConditionColumn')" min-width="220" show-overflow-tooltip>
            <template #default="{ row }">{{ formatAlarmConditionSummary(row) }}</template>
          </el-table-column>
          <el-table-column prop="sourceType" :label="t('AlarmSourceType')" width="110" />
          <el-table-column prop="severity" :label="t('AlarmSeverity')" width="110" />
          <el-table-column :label="t('AlarmTriggerMode')" width="140">
            <template #default="{ row }">
              {{ row.triggerMode }}<span v-if="row.triggerMode === 'DURATION'"> ({{ row.durationSeconds }}s)</span>
            </template>
          </el-table-column>
          <el-table-column :label="t('AlarmRecoveryTypeColumn')" width="120" show-overflow-tooltip>
            <template #default="{ row }">{{ alarmRuleRecoveryLabel(row) }}</template>
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
            layout="total, sizes, prev, pager, next, jumper"
            :total="rulePage.total"
            :page-sizes="[10, 20, 50, 100]"
            @current-change="loadRules"
            @size-change="handleRulePageSizeChange"
          />
        </div>
      </el-tab-pane>

      <el-tab-pane :label="t('AlarmRecordsTab')" name="records">
        <AlarmRecordSearchForm
          :search="handleRecordSearch"
          :reset="handleRecordReset"
          :device-group-options="deviceGroupOptions"
        />
        <div class="toolbar">
          <div class="toolbar-left">
            <el-button :icon="Refresh" @click="loadRecords">{{ t('RefreshButtonLabel') }}</el-button>
          </div>
        </div>
        <el-table :data="recordRows" stripe border show-overflow-tooltip empty-text=" ">
          <template #empty>
            <el-empty :description="t('AlarmRecordsEmpty')" />
          </template>
          <el-table-column prop="ruleName" :label="t('AlarmRecordNameLabel')" min-width="220" show-overflow-tooltip />
          <el-table-column prop="status" :label="t('StatusLabel')" width="160" show-overflow-tooltip />
          <el-table-column prop="severity" :label="t('AlarmSeverity')" width="110" />
          <el-table-column :label="t('IntegrationDeviceGroupsLabel')" min-width="200" show-overflow-tooltip>
            <template #default="{ row }">{{ deviceGroupCellLabel(row.deviceGroupKey) }}</template>
          </el-table-column>
          <el-table-column :label="t('AlarmObjectLabel')" min-width="200" show-overflow-tooltip>
            <template #default="{ row }">{{ formatAlarmRecordObject(row) }}</template>
          </el-table-column>
          <el-table-column :label="t('AlarmTriggerConditionColumn')" min-width="220" show-overflow-tooltip>
            <template #default="{ row }">{{ recordTriggerConditionLabel(row) }}</template>
          </el-table-column>
          <el-table-column :label="t('AlarmTriggerValueColumn')" min-width="140" show-overflow-tooltip>
            <template #default="{ row }">{{ recordTriggerValueLabel(row) }}</template>
          </el-table-column>
          <el-table-column prop="recoveredAt" :label="t('AlarmRecoveredAtLabel')" min-width="160" />
          <el-table-column prop="lastTriggeredAt" :label="t('AlarmLastTriggeredAt')" min-width="160" />
          <el-table-column :label="t('OperationsLabel')" width="200" fixed="right">
            <template #default="{ row }">
              <el-button link type="primary" @click="openRecordDetail(row)">{{ t('DetailLabel') }}</el-button>
              <el-button
                v-if="row.status === 'ACTIVE' && row.ruleRecoveryMode === 'MANUAL'"
                v-permission="'iot.alarm.edit'"
                link
                type="primary"
                @click="confirmRecoverRecord(row)"
              >
                {{ t('AlarmRecordManualRecoverButton') }}
              </el-button>
            </template>
          </el-table-column>
        </el-table>
        <div class="pagination-wrap">
          <el-pagination
            v-model:current-page="recordPage.pageNum"
            v-model:page-size="recordPage.pageSize"
            background
            layout="total, sizes, prev, pager, next, jumper"
            :total="recordPage.total"
            :page-sizes="[10, 20, 50, 100]"
            @current-change="loadRecords"
            @size-change="handleRecordPageSizeChange"
          />
        </div>
      </el-tab-pane>

      <el-tab-pane :label="t('AlarmNotifyConfigTab')" name="notify-config">
        <el-alert type="info" show-icon :closable="false" class="notify-guide">
          <template #default>
            {{ t('AlarmNotifyGuideAlert') }}
            <router-link to="/iot/notify-settings" class="notify-link">{{ t('AlarmNotifyGoSettings') }}</router-link>
          </template>
        </el-alert>
        <AlarmNotifyConfigSearchForm
          :search="handleNotifyCfgSearch"
          :reset="handleNotifyCfgReset"
          :rule-options="ruleOptions"
        />
        <div class="toolbar">
          <div class="toolbar-left">
            <el-button v-permission="'iot.alarm.edit'" type="primary" :icon="Plus" @click="notifyCreateVisible = true">
              {{ t('NewButtonLabel') }}
            </el-button>
            <el-button :icon="Refresh" @click="loadNotifyConfigs">{{ t('RefreshButtonLabel') }}</el-button>
          </div>
        </div>
        <el-table :data="notifyCfgRows" stripe border show-overflow-tooltip empty-text=" ">
          <template #empty>
            <el-empty :description="t('AlarmNotifyConfigsEmpty')" />
          </template>
          <el-table-column :label="t('AlarmNotifyRuleColumn')" min-width="200" show-overflow-tooltip>
            <template #default="{ row }">{{ row.ruleName || '—' }} (#{{ row.ruleId }})</template>
          </el-table-column>
          <el-table-column prop="name" :label="t('AlarmNotifyConfigName')" min-width="140" show-overflow-tooltip />
          <el-table-column :label="t('AlarmNotifyEmailChannel')" width="90">
            <template #default="{ row }">
              <el-tag :type="row.emailEnabled ? 'success' : 'info'" size="small">{{ row.emailEnabled ? t('Enabled') : t('Disabled') }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column :label="t('AlarmNotifySmsChannel')" width="90">
            <template #default="{ row }">
              <el-tag :type="row.smsEnabled ? 'success' : 'info'" size="small">{{ row.smsEnabled ? t('Enabled') : t('Disabled') }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column :label="t('AlarmNotifyEmailChannelLabel')" min-width="140" show-overflow-tooltip>
            <template #default="{ row }">{{ row.emailChannelName || '—' }}</template>
          </el-table-column>
          <el-table-column :label="t('AlarmNotifySmsChannelLabel')" min-width="140" show-overflow-tooltip>
            <template #default="{ row }">{{ row.smsChannelName || '—' }}</template>
          </el-table-column>
          <el-table-column :label="t('StatusLabel')" width="100">
            <template #default="{ row }">
              <el-tag :type="row.enabled ? 'success' : 'info'" size="small">{{ row.enabled ? t('Enabled') : t('Disabled') }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="updatedAt" :label="t('IntegrationUpdatedAt')" min-width="160" />
          <el-table-column :label="t('OperationsLabel')" width="280" fixed="right">
            <template #default="{ row }">
              <el-button v-permission="'iot.alarm.edit'" link type="primary" @click="runNotifyTest(row)">
                {{ t('NotifySettingsTest') }}
              </el-button>
              <el-button v-permission="'iot.alarm.edit'" link type="primary" @click="openNotifyEdit(row)">{{ t('EditLabel') }}</el-button>
              <el-button v-permission="'iot.alarm.edit'" link type="primary" @click="confirmDeleteNotifyCfg(row)">{{ t('DeleteLabel') }}</el-button>
            </template>
          </el-table-column>
        </el-table>
        <div class="pagination-wrap">
          <el-pagination
            v-model:current-page="notifyCfgPage.pageNum"
            v-model:page-size="notifyCfgPage.pageSize"
            background
            layout="total, sizes, prev, pager, next, jumper"
            :total="notifyCfgPage.total"
            :page-sizes="[10, 20, 50, 100]"
            @current-change="loadNotifyConfigs"
            @size-change="handleNotifyCfgPageSizeChange"
          />
        </div>
      </el-tab-pane>

      <el-tab-pane :label="t('AlarmNotifyDeliveryTab')" name="notify-delivery">
        <AlarmNotifyDeliverySearchForm
          :search="handleNotifyDelSearch"
          :reset="handleNotifyDelReset"
          :rule-options="ruleOptions"
        />
        <div class="toolbar">
          <div class="toolbar-left">
            <el-button :icon="Refresh" @click="loadNotifyDeliveries">{{ t('RefreshButtonLabel') }}</el-button>
          </div>
        </div>
        <el-table :data="notifyDelRows" stripe border show-overflow-tooltip empty-text=" ">
          <template #empty>
            <el-empty :description="t('AlarmNotifyDeliveriesEmpty')" />
          </template>
          <el-table-column prop="alarmEventType" :label="t('AlarmNotifyEventType')" min-width="140" />
          <el-table-column :label="t('AlarmNotifyChannel')" width="100">
            <template #default="{ row }">{{ channelLabel(row.channel) }}</template>
          </el-table-column>
          <el-table-column :label="t('AlarmNotifyDeliveryStatus')" width="100">
            <template #default="{ row }">
              <el-tag :type="row.status === 'SUCCESS' ? 'success' : 'danger'" size="small">{{ deliveryStatusLabel(row.status) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="recipientHint" :label="t('AlarmNotifyRecipientHint')" min-width="160" show-overflow-tooltip />
          <el-table-column prop="errorMessage" :label="t('AlarmNotifyErrorMessage')" min-width="200" show-overflow-tooltip />
          <el-table-column :label="t('AlarmNotifyIsTest')" width="80">
            <template #default="{ row }">{{ row.test ? t('Enabled') : '—' }}</template>
          </el-table-column>
          <el-table-column prop="createdAt" :label="t('AlarmNotifyCreatedAt')" min-width="160" />
        </el-table>
        <div class="pagination-wrap">
          <el-pagination
            v-model:current-page="notifyDelPage.pageNum"
            v-model:page-size="notifyDelPage.pageSize"
            background
            layout="total, sizes, prev, pager, next, jumper"
            :total="notifyDelPage.total"
            :page-sizes="[10, 20, 50, 100]"
            @current-change="loadNotifyDeliveries"
            @size-change="handleNotifyDelPageSizeChange"
          />
        </div>
      </el-tab-pane>
    </el-tabs>

    <AlarmRuleCreateDialog
      :visible="createDialogVisible"
      :device-group-options="deviceGroupOptions"
      :on-submit="handleRuleCreateSubmit"
      :on-cancel="() => { createDialogVisible = false }"
    />
    <AlarmRuleEditDialog
      v-if="editRow.id != null"
      :visible="editDialogVisible"
      :row="editRow"
      :device-group-options="deviceGroupOptions"
      :on-submit="handleRuleEditSubmit"
      :on-cancel="handleRuleEditCancel"
    />
    <AlarmRuleDetailDialog
      :visible="ruleDetailVisible"
      :rule-id="ruleDetailId"
      :on-cancel="closeRuleDetail"
    />
    <AlarmRecordDetailDialog
      :visible="recordDetailVisible"
      :record-id="recordDetailId"
      :on-cancel="closeRecordDetail"
      @recovered="loadRecords"
    />
    <AlarmNotifyConfigCreateDialog
      :visible="notifyCreateVisible"
      :rule-options="ruleOptions"
      :on-submit="handleNotifyCreateSubmit"
      :on-cancel="() => { notifyCreateVisible = false }"
    />
    <AlarmNotifyConfigEditDialog
      v-if="notifyEditRow.id != null"
      :visible="notifyEditVisible"
      :row="notifyEditRow"
      :rule-options="ruleOptions"
      :on-submit="handleNotifyEditSubmit"
      :on-cancel="handleNotifyEditCancel"
    />
  </el-card>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Refresh } from '@element-plus/icons-vue'
import AlarmRuleSearchForm from '@/views/alarm/AlarmRuleSearchForm.vue'
import AlarmRecordSearchForm from '@/views/alarm/AlarmRecordSearchForm.vue'
import AlarmNotifyConfigSearchForm from '@/views/alarm/AlarmNotifyConfigSearchForm.vue'
import AlarmNotifyDeliverySearchForm from '@/views/alarm/AlarmNotifyDeliverySearchForm.vue'
import AlarmRuleCreateDialog from '@/views/alarm/AlarmRuleCreateDialog.vue'
import AlarmRuleEditDialog from '@/views/alarm/AlarmRuleEditDialog.vue'
import AlarmRuleDetailDialog from '@/views/alarm/AlarmRuleDetailDialog.vue'
import AlarmRecordDetailDialog from '@/views/alarm/AlarmRecordDetailDialog.vue'
import AlarmNotifyConfigCreateDialog from '@/views/alarm/AlarmNotifyConfigCreateDialog.vue'
import AlarmNotifyConfigEditDialog from '@/views/alarm/AlarmNotifyConfigEditDialog.vue'
import {
  deleteAlarmNotifyConfig,
  deleteAlarmRule,
  listAlarmNotifyConfigs,
  listAlarmNotifyDeliveries,
  listAlarmRecords,
  listAlarmRules,
  recoverAlarmRecord,
  testAlarmNotifyConfig
} from '@/views/alarm/AlarmApi'
import { formatAlarmConditionSummary, formatAlarmRecordObject, formatAlarmRecordTriggerCondition, formatAlarmRecordTriggerValue, parsePage } from '@/views/alarm/alarmRuleSupport'
import { useAlarmDeviceGroups } from '@/views/alarm/useAlarmDeviceGroups'

const { t } = useI18n()
const activeTab = ref('rules')

const { deviceGroupOptions, loadDeviceGroups, deviceGroupCellLabel } = useAlarmDeviceGroups()

const ruleRows = ref([])
const rulePage = reactive({ pageNum: 1, pageSize: 10, total: 0 })
const ruleSearchParams = ref({
  deviceGroupKey: '',
  enabled: ''
})

const recordRows = ref([])
const recordPage = reactive({ pageNum: 1, pageSize: 10, total: 0 })
const recordSearchParams = ref({
  deviceGroupKey: '',
  ruleId: '',
  severity: '',
  status: ''
})

const createDialogVisible = ref(false)
const editDialogVisible = ref(false)
const editRow = ref({})

const ruleDetailVisible = ref(false)
const ruleDetailId = ref(null)
const recordDetailVisible = ref(false)
const recordDetailId = ref(null)

const ruleOptions = ref([])
const notifyCfgRows = ref([])
const notifyCfgPage = reactive({ pageNum: 1, pageSize: 10, total: 0 })
const notifyCfgSearchParams = ref({ ruleId: '' })
const notifyCreateVisible = ref(false)
const notifyEditVisible = ref(false)
const notifyEditRow = ref({})

const notifyDelRows = ref([])
const notifyDelPage = reactive({ pageNum: 1, pageSize: 10, total: 0 })
const notifyDelSearchParams = ref({ ruleId: '', channel: '', status: '' })

function recordTriggerConditionLabel(row) {
  const s = row?.triggerCondition != null ? String(row.triggerCondition).trim() : ''
  return s || formatAlarmRecordTriggerCondition(row)
}

function recordTriggerValueLabel(row) {
  const s = row?.triggerValue != null ? String(row.triggerValue).trim() : ''
  return s || formatAlarmRecordTriggerValue(row)
}

function alarmRuleRecoveryLabel(row) {
  const m = String(row?.recoveryMode ?? '').trim().toUpperCase()
  if (m === 'MANUAL') return t('AlarmRecoveryModeManual')
  if (m === 'AUTO') return t('AlarmRecoveryModeAuto')
  return row?.recoveryMode != null && String(row.recoveryMode).trim() !== '' ? String(row.recoveryMode) : '—'
}

function buildRuleListQuery() {
  const p = { pageNum: rulePage.pageNum, pageSize: rulePage.pageSize }
  const s = ruleSearchParams.value
  if (s.deviceGroupKey) p.deviceGroupKey = s.deviceGroupKey
  if (typeof s.enabled === 'boolean') p.enabled = s.enabled
  return p
}

async function loadRules() {
  try {
    const raw = await listAlarmRules(buildRuleListQuery())
    const { records, total } = parsePage(raw)
    ruleRows.value = records
    rulePage.total = total
  } catch (error) {
    ElMessage.error(error?.message || t('RequestFailedNotice'))
  }
}

function handleRulePageSizeChange() {
  rulePage.pageNum = 1
  loadRules()
}

function handleRuleSearch(params) {
  ruleSearchParams.value = { ...params }
  rulePage.pageNum = 1
  loadRules()
}

function handleRuleReset(params) {
  ruleSearchParams.value = { ...params }
  rulePage.pageNum = 1
  loadRules()
}

function buildRecordListQuery() {
  const p = { pageNum: recordPage.pageNum, pageSize: recordPage.pageSize }
  const s = recordSearchParams.value
  if (s.deviceGroupKey) p.deviceGroupKey = s.deviceGroupKey
  const rid = String(s.ruleId ?? '').trim()
  if (rid) {
    const n = Number(rid)
    if (Number.isFinite(n)) p.ruleId = n
  }
  if (s.severity) p.severity = s.severity
  if (s.status) p.status = s.status
  return p
}

async function loadRecords() {
  try {
    const raw = await listAlarmRecords(buildRecordListQuery())
    const { records, total } = parsePage(raw)
    recordRows.value = records
    recordPage.total = total
  } catch (error) {
    ElMessage.error(error?.message || t('RequestFailedNotice'))
  }
}

function handleRecordPageSizeChange() {
  recordPage.pageNum = 1
  loadRecords()
}

function handleRecordSearch(params) {
  recordSearchParams.value = { ...params }
  recordPage.pageNum = 1
  loadRecords()
}

function handleRecordReset(params) {
  recordSearchParams.value = { ...params }
  recordPage.pageNum = 1
  loadRecords()
}

function channelLabel(ch) {
  const u = String(ch || '').toUpperCase()
  if (u === 'EMAIL') return t('AlarmNotifyChannelEmail')
  if (u === 'SMS') return t('AlarmNotifyChannelSms')
  return ch || '—'
}

function deliveryStatusLabel(st) {
  const u = String(st || '').toUpperCase()
  if (u === 'SUCCESS') return t('AlarmNotifyStatusSuccess')
  if (u === 'FAILED') return t('AlarmNotifyStatusFailed')
  return st || '—'
}

async function loadRuleOptions() {
  try {
    const raw = await listAlarmRules({ pageNum: 1, pageSize: 500 })
    const { records } = parsePage(raw)
    ruleOptions.value = records
  } catch {
    ruleOptions.value = []
  }
}

function buildNotifyCfgQuery() {
  const p = { pageNum: notifyCfgPage.pageNum, pageSize: notifyCfgPage.pageSize }
  const s = notifyCfgSearchParams.value
  const rid = String(s.ruleId ?? '').trim()
  if (rid) {
    const n = Number(rid)
    if (Number.isFinite(n)) p.ruleId = n
  }
  return p
}

async function loadNotifyConfigs() {
  try {
    const raw = await listAlarmNotifyConfigs(buildNotifyCfgQuery())
    const { records, total } = parsePage(raw)
    notifyCfgRows.value = records
    notifyCfgPage.total = total
  } catch (error) {
    ElMessage.error(error?.message || t('RequestFailedNotice'))
  }
}

function handleNotifyCfgPageSizeChange() {
  notifyCfgPage.pageNum = 1
  loadNotifyConfigs()
}

function handleNotifyCfgSearch(params) {
  notifyCfgSearchParams.value = { ...params }
  notifyCfgPage.pageNum = 1
  loadNotifyConfigs()
}

function handleNotifyCfgReset(params) {
  notifyCfgSearchParams.value = { ...params }
  notifyCfgPage.pageNum = 1
  loadNotifyConfigs()
}

function buildNotifyDelQuery() {
  const p = { pageNum: notifyDelPage.pageNum, pageSize: notifyDelPage.pageSize }
  const s = notifyDelSearchParams.value
  const rid = String(s.ruleId ?? '').trim()
  if (rid) {
    const n = Number(rid)
    if (Number.isFinite(n)) p.ruleId = n
  }
  if (s.channel) p.channel = s.channel
  if (s.status) p.status = s.status
  return p
}

async function loadNotifyDeliveries() {
  try {
    const raw = await listAlarmNotifyDeliveries(buildNotifyDelQuery())
    const { records, total } = parsePage(raw)
    notifyDelRows.value = records
    notifyDelPage.total = total
  } catch (error) {
    ElMessage.error(error?.message || t('RequestFailedNotice'))
  }
}

function handleNotifyDelPageSizeChange() {
  notifyDelPage.pageNum = 1
  loadNotifyDeliveries()
}

function handleNotifyDelSearch(params) {
  notifyDelSearchParams.value = { ...params }
  notifyDelPage.pageNum = 1
  loadNotifyDeliveries()
}

function handleNotifyDelReset(params) {
  notifyDelSearchParams.value = { ...params }
  notifyDelPage.pageNum = 1
  loadNotifyDeliveries()
}

function handleNotifyCreateSubmit() {
  notifyCreateVisible.value = false
  notifyCfgPage.pageNum = 1
  loadNotifyConfigs()
}

function openNotifyEdit(row) {
  notifyEditRow.value = { id: row.id }
  notifyEditVisible.value = true
}

function handleNotifyEditSubmit() {
  notifyEditVisible.value = false
  notifyEditRow.value = {}
  loadNotifyConfigs()
}

function handleNotifyEditCancel() {
  notifyEditVisible.value = false
  notifyEditRow.value = {}
}

async function runNotifyTest(row) {
  try {
    await testAlarmNotifyConfig(row.id)
    ElMessage.success(t('AlarmNotifyTestOk'))
  } catch (e) {
    ElMessage.error(e?.message || t('RequestFailedNotice'))
  }
}

async function confirmDeleteNotifyCfg(row) {
  try {
    await ElMessageBox.confirm(t('DeleteConfirmLabel'), t('RemoveTitle'), {
      confirmButtonText: t('ConfirmButtonText'),
      cancelButtonText: t('CancelButtonText'),
      type: 'warning'
    })
    await deleteAlarmNotifyConfig(row.id)
    ElMessage.success(t('DeleteSuccess'))
    if (notifyCfgRows.value.length === 1 && notifyCfgPage.pageNum > 1) {
      notifyCfgPage.pageNum -= 1
    }
    await loadNotifyConfigs()
  } catch (e) {
    if (e !== 'cancel') {
      /* */
    }
  }
}

function onTabChange(name) {
  if (name === 'records') loadRecords()
  if (name === 'notify-config' || name === 'notify-delivery') {
    loadRuleOptions()
  }
  if (name === 'notify-config') {
    loadNotifyConfigs()
  }
  if (name === 'notify-delivery') {
    loadNotifyDeliveries()
  }
}

function handleRuleCreateSubmit() {
  createDialogVisible.value = false
  rulePage.pageNum = 1
  loadRules()
}

function openRuleEdit(row) {
  editRow.value = { id: row.id }
  editDialogVisible.value = true
}

function handleRuleEditSubmit() {
  editDialogVisible.value = false
  editRow.value = {}
  loadRules()
}

function handleRuleEditCancel() {
  editDialogVisible.value = false
  editRow.value = {}
}

function openRuleDetail(row) {
  ruleDetailId.value = row.id
  ruleDetailVisible.value = true
}

function closeRuleDetail() {
  ruleDetailVisible.value = false
  ruleDetailId.value = null
}

function openRecordDetail(row) {
  recordDetailId.value = row.id
  recordDetailVisible.value = true
}

function closeRecordDetail() {
  recordDetailVisible.value = false
  recordDetailId.value = null
}

async function confirmRecoverRecord(row) {
  try {
    await ElMessageBox.confirm(t('AlarmRecordManualRecoverConfirm'), t('AlarmRecordManualRecoverTitle'), {
      confirmButtonText: t('ConfirmButtonText'),
      cancelButtonText: t('CancelButtonText'),
      type: 'warning'
    })
    await recoverAlarmRecord(row.id)
    ElMessage.success(t('AlarmRecordManualRecoverSuccess'))
    await loadRecords()
  } catch (e) {
    if (e !== 'cancel') {
      ElMessage.error(e?.message || t('RequestFailedNotice'))
    }
  }
}

async function confirmDeleteRule(row) {
  try {
    await ElMessageBox.confirm(t('DeleteConfirmLabel'), t('RemoveTitle'), {
      confirmButtonText: t('ConfirmButtonText'),
      cancelButtonText: t('CancelButtonText'),
      type: 'warning'
    })
    await deleteAlarmRule(row.id)
    ElMessage.success(t('DeleteSuccess'))
    if (ruleRows.value.length === 1 && rulePage.pageNum > 1) {
      rulePage.pageNum -= 1
    }
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
  margin-bottom: 16px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
  flex-wrap: wrap;
}

.toolbar-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.pagination-wrap {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.notify-guide {
  margin-bottom: 12px;
}
.notify-link {
  margin-left: 8px;
}
</style>
