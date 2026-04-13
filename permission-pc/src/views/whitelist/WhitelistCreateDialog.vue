<template>
  <el-dialog :model-value="visible" :title="t('CreateTitle')" align-center destroy-on-close :show-close="false"
    style="max-width: 600px" :modal="false" modal-penetrable>
    <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
      <el-form-item :label="t('MethodLabel')" prop="method">
        <el-select v-model="form.method">
          <el-option v-for="item in methodOptions" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </el-form-item>
      <el-form-item :label="t('PathLabel')" prop="path">
        <el-input v-model="form.path" />
      </el-form-item>
      <el-form-item :label="t('RemarkLabel')" prop="remark">
        <el-input v-model="form.remark" type="textarea" :rows="3" />
      </el-form-item>
      <el-form-item :label="t('EnableLabel')" prop="enabled">
        <el-select v-model="form.enabled">
          <el-option :label="t('Enable')" :value="true" />
          <el-option :label="t('Disable')" :value="false" />  
        </el-select>
      </el-form-item>
    </el-form>
    <template #footer>
      <div class="dialog-footer">
        <el-button @click="onCancel">{{ t('CancelButtonText') }}</el-button>
        <el-button type="primary" @click="onSubmit">{{ t('ConfirmButtonText') }}</el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup name="WhitelistCreateDialog">
import { ref, computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { createWhitelist } from '@/views/whitelist/WhitelistApi'

const { t } = useI18n()

// props
const props = defineProps(['visible', 'onSubmit', 'onCancel'])

const formRef = ref(null)
const form = ref({
  method: '',
  path: '',
  remark: '',
  enabled: true
})

const methodOptions = ref([
  { label: 'GET', value: 'GET' },
  { label: 'POST', value: 'POST' },
  { label: 'PUT', value: 'PUT' },
  { label: 'DELETE', value: 'DELETE' },
  { label: 'PATCH', value: 'PATCH' }
])

// Validation rules - use computed to react to language changes
const rules = computed(() => {
  return {
    method: [
      { required: true, message: t('MethodRequired'), trigger: 'change' }
    ],
    path: [
      { required: true, message: t('PathRequired'), trigger: 'blur' }
    ],
    enabled: [
      { required: true, message: t('EnableRequired'), trigger: 'change' }
    ]
  }
})

const onCancel = () => {
  // Reset form when canceling
  if (formRef.value) {
    formRef.value.resetFields()
  }
  form.value = {
    method: '',
    path: '',
    remark: '',
    enabled: true
  }
  props.onCancel()
}

const onSubmit = async () => {
  if (!formRef.value) return
  try {
    // Validate form
    await formRef.value.validate()
    // If validation passes, call parent's onSubmit
    const whitelist = {
      method: form.value.method,
      path: form.value.path,
      remark: form.value.remark || '',
      enabled: form.value.enabled
    }
    // Create whitelist
    createWhitelist(whitelist).then(() => {
      formRef.value.resetFields()
      props.onSubmit()
    }).catch(error => {
      console.error('Create whitelist failed:', error)
    })
  } catch (error) {
    console.error('Form validation failed:', error)
  }
}
</script>
<style scoped></style>

