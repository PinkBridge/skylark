<template>
  <el-dialog
    :model-value="visible"
    :title="$t('CreateTitle')"
    align-center
    destroy-on-close
    :show-close="false"
    style="max-width: 800px"
    @closed="handleClosed"
  >
    <AlarmRuleForm ref="innerFormRef" :form="form" :device-group-options="deviceGroupOptions" />
    <template #footer>
      <div class="dialog-footer">
        <el-button @click="handleCancel">{{ $t('CancelButtonText') }}</el-button>
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
import { createAlarmRule } from '@/views/alarm/AlarmApi'
import { buildConditionJson, createDefaultRuleForm, resetConditionFields } from '@/views/alarm/alarmRuleSupport'

const props = defineProps(['visible', 'deviceGroupOptions', 'onSubmit', 'onCancel'])
const { t } = useI18n()
const innerFormRef = ref(null)
const form = reactive(createDefaultRuleForm())

function resetToDefaults() {
  Object.assign(form, createDefaultRuleForm())
  resetConditionFields(form)
  innerFormRef.value?.resetFields?.()
}

watch(
  () => props.visible,
  (v) => {
    if (v) resetToDefaults()
  }
)

function handleClosed() {
  innerFormRef.value?.resetFields?.()
}

function handleCancel() {
  resetToDefaults()
  props.onCancel()
}

async function handleSubmit() {
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
    await createAlarmRule({
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
    ElMessage.success(t('CreateSuccess'))
    resetToDefaults()
    props.onSubmit()
  } catch (error) {
    if (error?.message) {
      ElMessage.error(error.message)
    }
  }
}
</script>
