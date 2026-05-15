<template>
  <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
    <el-form-item :label="t('IntegrationDeviceGroupsLabel')" prop="deviceGroupKey">
      <el-select v-model="form.deviceGroupKey" filterable style="width: 100%">
        <el-option
          v-for="g in deviceGroupOptions"
          :key="g.groupKey"
          :label="g.name ? `${g.name} (#${g.groupKey})` : `#${g.groupKey}`"
          :value="g.groupKey"
        />
      </el-select>
    </el-form-item>
    <el-form-item :label="t('NameLabel')" prop="name">
      <el-input v-model="form.name" />
    </el-form-item>
    <el-form-item :label="t('AlarmSourceType')" prop="sourceType">
      <el-select v-model="form.sourceType" style="width: 100%">
        <el-option label="PROPERTY" value="PROPERTY" />
        <el-option label="EVENT" value="EVENT" />
      </el-select>
    </el-form-item>
    <el-form-item :label="t('AlarmSeverity')" prop="severity">
      <el-select v-model="form.severity" style="width: 100%">
        <el-option label="HIGH" value="HIGH" />
        <el-option label="MEDIUM" value="MEDIUM" />
        <el-option label="LOW" value="LOW" />
      </el-select>
    </el-form-item>
    <el-form-item :label="t('AlarmTriggerMode')" prop="triggerMode">
      <el-select v-model="form.triggerMode" style="width: 100%">
        <el-option label="INSTANT" value="INSTANT" />
        <el-option label="DURATION" value="DURATION" />
      </el-select>
    </el-form-item>
    <el-form-item v-if="form.triggerMode === 'DURATION'" :label="t('AlarmDurationSeconds')" prop="durationSeconds">
      <el-input-number v-model="form.durationSeconds" :min="1" :max="86400" style="width: 100%" />
    </el-form-item>
    <el-form-item :label="t('AlarmRecoveryMode')" prop="recoveryMode">
      <el-select v-model="form.recoveryMode" style="width: 100%">
        <el-option label="AUTO" value="AUTO" />
        <el-option label="MANUAL" value="MANUAL" />
      </el-select>
    </el-form-item>
    <el-form-item :label="t('AlarmDedupMode')" prop="dedupMode">
      <el-select v-model="form.dedupMode" style="width: 100%">
        <el-option label="SINGLE_ACTIVE" value="SINGLE_ACTIVE" />
        <el-option label="EVERY_TRIGGER" value="EVERY_TRIGGER" />
      </el-select>
    </el-form-item>
    <el-form-item :label="t('StatusLabel')" prop="enabled">
      <el-switch v-model="form.enabled" />
    </el-form-item>

    <template v-if="form.sourceType === 'PROPERTY'">
      <el-row :gutter="12" class="alarm-condition-row">
        <el-col :xs="24" :sm="8">
          <el-form-item :label="t('AlarmConditionPropertyKey')" prop="conditionPropertyKey">
            <el-input v-model="form.conditionPropertyKey" :placeholder="t('AlarmConditionPropertyKeyPh')" />
          </el-form-item>
        </el-col>
        <el-col :xs="24" :sm="8">
          <el-form-item :label="t('AlarmConditionOperator')" prop="conditionOperator">
            <el-select v-model="form.conditionOperator" style="width: 100%">
              <el-option v-for="op in PROPERTY_OPERATORS" :key="op" :label="op" :value="op" />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col v-if="form.conditionOperator !== 'BETWEEN'" :xs="24" :sm="8">
          <el-form-item :label="t('AlarmConditionThresholdValue')" prop="conditionThresholdValue">
            <el-input
              v-model="form.conditionThresholdValue"
              inputmode="decimal"
              :placeholder="t('AlarmConditionThresholdPh')"
            />
          </el-form-item>
        </el-col>
      </el-row>
      <el-row v-if="form.conditionOperator === 'BETWEEN'" :gutter="12" class="alarm-condition-row">
        <el-col :xs="24" :sm="12">
          <el-form-item :label="t('AlarmConditionThresholdMin')" prop="conditionThresholdMin">
            <el-input
              v-model="form.conditionThresholdMin"
              inputmode="decimal"
              :placeholder="t('AlarmConditionThresholdMinPh')"
            />
          </el-form-item>
        </el-col>
        <el-col :xs="24" :sm="12">
          <el-form-item :label="t('AlarmConditionThresholdMax')" prop="conditionThresholdMax">
            <el-input
              v-model="form.conditionThresholdMax"
              inputmode="decimal"
              :placeholder="t('AlarmConditionThresholdMaxPh')"
            />
          </el-form-item>
        </el-col>
      </el-row>
    </template>
    <template v-else>
      <el-form-item :label="t('AlarmConditionEventType')" prop="conditionEventType">
        <el-input v-model="form.conditionEventType" :placeholder="t('AlarmConditionEventTypePh')" />
      </el-form-item>
      <el-form-item :label="t('AlarmConditionEventIdentifier')" prop="conditionEventIdentifier">
        <el-input v-model="form.conditionEventIdentifier" :placeholder="t('AlarmConditionEventIdentifierPh')" />
      </el-form-item>
    </template>
  </el-form>
</template>

<script setup>
import { computed, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { PROPERTY_OPERATORS } from '@/views/alarm/alarmRuleSupport'

const props = defineProps({
  form: { type: Object, required: true },
  deviceGroupOptions: { type: Array, default: () => [] }
})

const { t } = useI18n()
const formRef = ref(null)

const rules = computed(() => {
  const form = props.form
  const base = {
    deviceGroupKey: [{ required: true, message: t('AlarmDeviceGroupRequired'), trigger: 'change' }],
    name: [{ required: true, message: t('NameRequired'), trigger: 'blur' }]
  }
  if (form.sourceType === 'EVENT') {
    base.conditionEventType = [{ required: true, message: t('AlarmConditionEventTypeRequired'), trigger: 'blur' }]
    return base
  }
  base.conditionPropertyKey = [{ required: true, message: t('AlarmConditionPropertyKeyRequired'), trigger: 'blur' }]
  base.conditionOperator = [{ required: true, message: t('AlarmConditionOperatorRequired'), trigger: 'change' }]
  base.conditionThresholdValue = [
    {
      validator: (_, value, callback) => {
        if (form.conditionOperator === 'BETWEEN') {
          callback()
          return
        }
        if (value === '' || value === null || value === undefined) {
          callback(new Error(t('AlarmConditionThresholdRequired')))
          return
        }
        const n = Number(String(value).trim())
        if (!Number.isFinite(n)) {
          callback(new Error(t('AlarmConditionNumberInvalid')))
          return
        }
        callback()
      },
      trigger: 'blur'
    }
  ]
  base.conditionThresholdMin = [
    {
      validator: (_, value, callback) => {
        if (form.conditionOperator !== 'BETWEEN') {
          callback()
          return
        }
        const min = Number(String(value ?? '').trim())
        const max = Number(String(form.conditionThresholdMax ?? '').trim())
        if (!Number.isFinite(min) || !Number.isFinite(max)) {
          callback(new Error(t('AlarmConditionThresholdMinMaxRequired')))
          return
        }
        callback()
      },
      trigger: 'blur'
    }
  ]
  base.conditionThresholdMax = [
    {
      validator: (_, value, callback) => {
        if (form.conditionOperator !== 'BETWEEN') {
          callback()
          return
        }
        const min = Number(String(form.conditionThresholdMin ?? '').trim())
        const max = Number(String(value ?? '').trim())
        if (!Number.isFinite(min) || !Number.isFinite(max)) {
          callback(new Error(t('AlarmConditionThresholdMinMaxRequired')))
          return
        }
        callback()
      },
      trigger: 'blur'
    }
  ]
  return base
})

defineExpose({
  validate: () => formRef.value?.validate?.(),
  resetFields: () => formRef.value?.resetFields?.()
})
</script>

<style scoped>
.alarm-condition-row {
  margin-bottom: 0;
}
.alarm-condition-row :deep(.el-form-item) {
  margin-bottom: 18px;
}
</style>
