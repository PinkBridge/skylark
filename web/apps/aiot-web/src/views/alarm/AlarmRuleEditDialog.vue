<template>
  <el-dialog
    :model-value="visible"
    :title="$t('EditTitle')"
    align-center
    destroy-on-close
    :show-close="false"
    style="max-width: 800px"
    @closed="onDialogClosed"
  >
    <AlarmRuleForm ref="innerFormRef" :form="form" :device-group-options="deviceGroupOptions" />
    <template #footer>
      <div class="dialog-footer">
        <el-button @click="props.onCancel()">{{ $t('CancelButtonText') }}</el-button>
        <el-button type="primary" @click="handleSubmit">{{ $t('ConfirmButtonText') }}</el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
/* global defineProps */
import { reactive, ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { ElMessage } from 'element-plus'
import AlarmRuleForm from '@/views/alarm/AlarmRuleForm.vue'
import { getAlarmRule, updateAlarmRule } from '@/views/alarm/AlarmApi'
import {
  buildConditionJson,
  createDefaultRuleForm,
  hydrateConditionFields
} from '@/views/alarm/alarmRuleSupport'

const props = defineProps(['visible', 'row', 'deviceGroupOptions', 'onSubmit', 'onCancel'])
const { t } = useI18n()
const innerFormRef = ref(null)
const form = reactive(createDefaultRuleForm())
const ruleId = ref(null)

watch(
  () => [props.visible, props.row?.id],
  async ([visible, id]) => {
    if (visible && id != null) {
      ruleId.value = id
      const detail = await getAlarmRule(id)
      Object.assign(form, {
        deviceGroupKey: detail.deviceGroupKey || '',
        name: detail.name || '',
        sourceType: detail.sourceType || 'PROPERTY',
        severity: detail.severity || 'MEDIUM',
        triggerMode: detail.triggerMode || 'INSTANT',
        durationSeconds: Number(detail.durationSeconds || 0),
        recoveryMode: detail.recoveryMode || 'AUTO',
        dedupMode: detail.dedupMode || 'SINGLE_ACTIVE',
        enabled: !!detail.enabled
      })
      const cj = typeof detail.conditionJson === 'string' ? detail.conditionJson : JSON.stringify(detail.conditionJson || {})
      hydrateConditionFields(form, detail.sourceType, cj)
    }
  },
  { immediate: true }
)

function onDialogClosed() {
  innerFormRef.value?.resetFields?.()
}

async function handleSubmit() {
  if (!ruleId.value) return
  try {
    await innerFormRef.value?.validate?.()
    if (form.sourceType === 'PROPERTY' && form.conditionOperator === 'BETWEEN') {
      const min = Number(String(form.conditionThresholdMin ?? '').trim())
      const max = Number(String(form.conditionThresholdMax ?? '').trim())
      if (Number.isFinite(min) && Number.isFinite(max) && min >= max) {
        ElMessage.error(t('AlarmConditionRangeInvalid'))
        return
      }
    }
    const conditionJson = buildConditionJson(form)
    await updateAlarmRule(ruleId.value, {
      deviceGroupKey: form.deviceGroupKey,
      name: form.name,
      sourceType: form.sourceType,
      severity: form.severity,
      triggerMode: form.triggerMode,
      durationSeconds: form.triggerMode === 'DURATION' ? Number(form.durationSeconds || 1) : 0,
      recoveryMode: form.recoveryMode,
      dedupMode: form.dedupMode,
      enabled: !!form.enabled,
      conditionJson
    })
    ElMessage.success(t('UpdateSuccess'))
    props.onSubmit()
  } catch (error) {
    if (error?.message) {
      ElMessage.error(error.message)
    }
  }
}
</script>
