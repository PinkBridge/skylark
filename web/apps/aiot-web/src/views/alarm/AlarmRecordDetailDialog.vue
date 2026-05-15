<template>
  <el-dialog
    :model-value="visible"
    :title="t('AlarmRecordDetailTitle')"
    align-center
    destroy-on-close
    :show-close="false"
    style="max-width: 800px"
    class="alarm-detail-dialog"
    @closed="handleClosed"
  >
    <div class="detail-section">
      <div class="section-title">{{ t('BasicInfoTitle') }}</div>
      <el-descriptions :column="2" border class="detail-descriptions">
        <el-descriptions-item :label="t('AlarmRecoveryMode')">{{ detail.ruleRecoveryMode || '-' }}</el-descriptions-item>
        <el-descriptions-item :label="t('AlarmRecordInternalIdLabel')">{{ detail.id ?? '-' }}</el-descriptions-item>
        <el-descriptions-item :label="t('AlarmRecordNameLabel')">{{ detail.ruleName || '-' }}</el-descriptions-item>
        <el-descriptions-item :label="t('AlarmRuleId')">{{ detail.ruleId ?? '-' }}</el-descriptions-item>
        <el-descriptions-item :label="t('IntegrationDeviceGroupsLabel')">{{ deviceGroupLabel }}</el-descriptions-item>
        <el-descriptions-item :label="t('AlarmObjectLabel')">{{ objectLabel }}</el-descriptions-item>
        <el-descriptions-item :label="t('StatusLabel')">{{ detail.status || '-' }}</el-descriptions-item>
        <el-descriptions-item :label="t('AlarmSeverity')">{{ detail.severity || '-' }}</el-descriptions-item>
        <el-descriptions-item :label="t('AlarmTriggerConditionColumn')" :span="2">{{ triggerConditionLabel }}</el-descriptions-item>
        <el-descriptions-item :label="t('AlarmTriggerValueColumn')" :span="2">{{ triggerValueLabel }}</el-descriptions-item>
        <el-descriptions-item :label="t('AlarmLastTriggeredAt')">{{ detail.lastTriggeredAt || '-' }}</el-descriptions-item>
        <el-descriptions-item :label="t('AlarmRecoveredAtLabel')">{{ detail.recoveredAt || '-' }}</el-descriptions-item>
        <el-descriptions-item :label="t('IntegrationCreatedAt')">{{ detail.createdAt || '-' }}</el-descriptions-item>
        <el-descriptions-item :label="t('AlarmFirstTriggeredAtLabel')">{{ detail.firstTriggeredAt || '-' }}</el-descriptions-item>
        <el-descriptions-item :label="t('AlarmTriggerCountLabel')">{{ detail.triggerCount ?? '-' }}</el-descriptions-item>
        <el-descriptions-item :label="t('AlarmLastEventTypeLabel')">{{ detail.lastEventType || '-' }}</el-descriptions-item>
        <el-descriptions-item :label="t('AlarmLastEventIdLabel')">{{ detail.lastEventId || '-' }}</el-descriptions-item>
      </el-descriptions>
    </div>
    <template #footer>
      <div class="dialog-footer">
        <el-button @click="props.onCancel()">{{ t('CancelButtonText') }}</el-button>
        <el-button
          v-if="canManualRecover"
          v-permission="'iot.alarm.edit'"
          type="primary"
          @click="confirmManualRecover"
        >
          {{ t('AlarmRecordManualRecoverButton') }}
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
/* global defineProps */
import { computed, ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getAlarmRecord, recoverAlarmRecord } from '@/views/alarm/AlarmApi'
import {
  formatAlarmRecordObject,
  formatAlarmRecordTriggerCondition,
  formatAlarmRecordTriggerValue
} from '@/views/alarm/alarmRuleSupport'
import { useAlarmDeviceGroups } from '@/views/alarm/useAlarmDeviceGroups'

const emit = defineEmits(['recovered'])

const props = defineProps({
  visible: { type: Boolean, default: false },
  recordId: { type: [Number, String], default: null },
  onCancel: { type: Function, required: true }
})

const { t } = useI18n()
const { loadDeviceGroups, deviceGroupCellLabel } = useAlarmDeviceGroups()

const detail = ref({
  id: null,
  ruleName: '',
  ruleRecoveryMode: '',
  ruleId: null,
  deviceGroupKey: '',
  deviceName: '',
  deviceKey: '',
  status: '',
  severity: '',
  triggerCondition: '',
  triggerValue: '',
  evidenceJson: '',
  lastTriggeredAt: '',
  recoveredAt: '',
  firstTriggeredAt: '',
  createdAt: '',
  triggerCount: null,
  lastEventType: '',
  lastEventId: ''
})

const deviceGroupLabel = computed(() => deviceGroupCellLabel(detail.value.deviceGroupKey))

const canManualRecover = computed(
  () => detail.value.status === 'ACTIVE' && detail.value.ruleRecoveryMode === 'MANUAL'
)

const objectLabel = computed(() =>
  formatAlarmRecordObject({
    deviceName: detail.value.deviceName,
    deviceKey: detail.value.deviceKey
  })
)

const triggerConditionLabel = computed(() => {
  const s = detail.value.triggerCondition != null ? String(detail.value.triggerCondition).trim() : ''
  return (
    s ||
    formatAlarmRecordTriggerCondition({
      evidenceJson: detail.value.evidenceJson
    })
  )
})

const triggerValueLabel = computed(() => {
  const s = detail.value.triggerValue != null ? String(detail.value.triggerValue).trim() : ''
  return (
    s ||
    formatAlarmRecordTriggerValue({
      evidenceJson: detail.value.evidenceJson,
      lastEventType: detail.value.lastEventType,
      lastEventId: detail.value.lastEventId
    })
  )
})

function resetDetail() {
  detail.value = {
    id: null,
    ruleName: '',
    ruleRecoveryMode: '',
    ruleId: null,
    deviceGroupKey: '',
    deviceName: '',
    deviceKey: '',
    status: '',
    severity: '',
    triggerCondition: '',
    triggerValue: '',
    evidenceJson: '',
    lastTriggeredAt: '',
    recoveredAt: '',
    firstTriggeredAt: '',
    createdAt: '',
    triggerCount: null,
    lastEventType: '',
    lastEventId: ''
  }
}

async function loadDetail() {
  const id = props.recordId
  if (id == null || id === '') return
  try {
    await loadDeviceGroups()
    const data = await getAlarmRecord(id)
    detail.value = {
      id: data.id ?? null,
      ruleName: data.ruleName || '',
      ruleRecoveryMode: data.ruleRecoveryMode || '',
      ruleId: data.ruleId ?? null,
      deviceGroupKey: data.deviceGroupKey || '',
      deviceName: data.deviceName || '',
      deviceKey: data.deviceKey || '',
      status: data.status || '',
      severity: data.severity || '',
      triggerCondition: data.triggerCondition || '',
      triggerValue: data.triggerValue || '',
      evidenceJson: data.evidenceJson,
      lastTriggeredAt: data.lastTriggeredAt || '',
      recoveredAt: data.recoveredAt || '',
      firstTriggeredAt: data.firstTriggeredAt || '',
      createdAt: data.createdAt || '',
      triggerCount: data.triggerCount ?? null,
      lastEventType: data.lastEventType || '',
      lastEventId: data.lastEventId || ''
    }
  } catch (error) {
    ElMessage.error(error?.message || t('RequestFailedNotice'))
  }
}

async function confirmManualRecover() {
  const id = detail.value.id
  if (id == null) return
  try {
    await ElMessageBox.confirm(t('AlarmRecordManualRecoverConfirm'), t('AlarmRecordManualRecoverTitle'), {
      confirmButtonText: t('ConfirmButtonText'),
      cancelButtonText: t('CancelButtonText'),
      type: 'warning'
    })
    await recoverAlarmRecord(id)
    ElMessage.success(t('AlarmRecordManualRecoverSuccess'))
    await loadDetail()
    emit('recovered')
  } catch (e) {
    if (e !== 'cancel') {
      ElMessage.error(e?.message || t('RequestFailedNotice'))
    }
  }
}

watch(
  () => [props.visible, props.recordId],
  ([v, id]) => {
    if (v && id != null && id !== '') loadDetail()
  },
  { immediate: true }
)

function handleClosed() {
  resetDetail()
}
</script>

<style scoped>
.detail-section {
  max-height: min(70vh, 640px);
  overflow-y: auto;
}

.section-title {
  margin-bottom: 16px;
  font-size: 14px;
  font-weight: 600;
  color: var(--el-text-color-primary);
}

.detail-descriptions {
  background: #fff;
}

.detail-descriptions :deep(.el-descriptions__label.el-descriptions__cell) {
  background: #fafbfd;
}

.dialog-footer {
  display: flex;
  flex-wrap: wrap;
  justify-content: flex-end;
  gap: 12px;
}
</style>
