<template>
  <el-dialog
    :model-value="visible"
    :title="t('AlarmRuleDetailTitle')"
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
        <el-descriptions-item :label="t('AlarmRuleId')">{{ detail.id ?? '-' }}</el-descriptions-item>
        <el-descriptions-item :label="t('NameLabel')">{{ detail.name || '-' }}</el-descriptions-item>
        <el-descriptions-item :label="t('IntegrationDeviceGroupsLabel')">{{ deviceGroupLabel }}</el-descriptions-item>
        <el-descriptions-item :label="t('AlarmSourceType')">{{ detail.sourceType || '-' }}</el-descriptions-item>
        <el-descriptions-item :label="t('AlarmSeverity')">{{ detail.severity || '-' }}</el-descriptions-item>
        <el-descriptions-item :label="t('AlarmTriggerMode')">
          {{ detail.triggerMode || '-' }}<span v-if="detail.triggerMode === 'DURATION'"> ({{ detail.durationSeconds }}s)</span>
        </el-descriptions-item>
        <el-descriptions-item :label="t('AlarmRecoveryMode')">{{ detail.recoveryMode || '-' }}</el-descriptions-item>
        <el-descriptions-item :label="t('AlarmDedupMode')">{{ detail.dedupMode || '-' }}</el-descriptions-item>
        <el-descriptions-item :label="t('StatusLabel')">
          <el-tag :type="detail.enabled ? 'success' : 'info'" size="small">
            {{ detail.enabled ? t('Enabled') : t('Disabled') }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item :label="t('IntegrationCreatedAt')">{{ detail.createdAt || '-' }}</el-descriptions-item>
        <el-descriptions-item :label="t('IntegrationUpdatedAt')">{{ detail.updatedAt || '-' }}</el-descriptions-item>
        <el-descriptions-item :label="t('AlarmConditionColumn')" :span="2">
          {{ conditionSummary }}
        </el-descriptions-item>
      </el-descriptions>
    </div>
    <template #footer>
      <div class="dialog-footer">
        <el-button @click="props.onCancel()">{{ t('CancelButtonText') }}</el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
/* global defineProps */
import { computed, ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { ElMessage } from 'element-plus'
import { getAlarmRule } from '@/views/alarm/AlarmApi'
import { formatAlarmConditionSummary } from '@/views/alarm/alarmRuleSupport'
import { useAlarmDeviceGroups } from '@/views/alarm/useAlarmDeviceGroups'

const props = defineProps({
  visible: { type: Boolean, default: false },
  ruleId: { type: [Number, String], default: null },
  onCancel: { type: Function, required: true }
})

const { t } = useI18n()
const { loadDeviceGroups, deviceGroupCellLabel } = useAlarmDeviceGroups()

const detail = ref({
  id: null,
  name: '',
  deviceGroupKey: '',
  sourceType: '',
  severity: '',
  triggerMode: '',
  durationSeconds: 0,
  recoveryMode: '',
  dedupMode: '',
  enabled: true,
  conditionJson: '',
  createdAt: '',
  updatedAt: ''
})

const conditionSummary = computed(() =>
  formatAlarmConditionSummary({
    sourceType: detail.value.sourceType,
    conditionJson: detail.value.conditionJson
  })
)

const deviceGroupLabel = computed(() => deviceGroupCellLabel(detail.value.deviceGroupKey))

function resetDetail() {
  detail.value = {
    id: null,
    name: '',
    deviceGroupKey: '',
    sourceType: '',
    severity: '',
    triggerMode: '',
    durationSeconds: 0,
    recoveryMode: '',
    dedupMode: '',
    enabled: true,
    conditionJson: '',
    createdAt: '',
    updatedAt: ''
  }
}

async function loadDetail() {
  const id = props.ruleId
  if (id == null || id === '') return
  try {
    await loadDeviceGroups()
    const data = await getAlarmRule(id)
    detail.value = {
      id: data.id ?? null,
      name: data.name || '',
      deviceGroupKey: data.deviceGroupKey || '',
      sourceType: data.sourceType || '',
      severity: data.severity || '',
      triggerMode: data.triggerMode || '',
      durationSeconds: Number(data.durationSeconds || 0),
      recoveryMode: data.recoveryMode || '',
      dedupMode: data.dedupMode || '',
      enabled: !!data.enabled,
      conditionJson: data.conditionJson,
      createdAt: data.createdAt || '',
      updatedAt: data.updatedAt || ''
    }
  } catch (error) {
    ElMessage.error(error?.message || t('RequestFailedNotice'))
  }
}

watch(
  () => [props.visible, props.ruleId],
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
</style>
