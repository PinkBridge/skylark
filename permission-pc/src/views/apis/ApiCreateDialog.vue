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
      <el-form-item :label="t('PermLabelLabel')" prop="permlabel">
        <el-input v-model="form.permlabel" />
      </el-form-item>
      <el-form-item :label="t('ModuleKeyLabel')" prop="moduleKey">
        <el-input v-model="form.moduleKey" />
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

<script setup name="ApiCreateDialog">
import { ref, computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { createApi } from '@/views/apis/ApiApi'

const { t } = useI18n()

// props
const props = defineProps(['visible', 'onSubmit', 'onCancel'])

const formRef = ref(null)
const form = ref({
  method: '',
  path: '',
  permlabel: '',
  moduleKey: ''
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
    permlabel: [
      { required: true, message: t('PermLabelRequired'), trigger: 'blur' }
    ],
    module_key: [
      { required: true, message: t('ModuleKeyRequired'), trigger: 'blur' }
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
    permlabel: '',
    moduleKey: ''
  }
  props.onCancel()
}

const onSubmit = async () => {
  if (!formRef.value) return
  try {
    // Validate form
    await formRef.value.validate()
    // If validation passes, call parent's onSubmit
    const api = {
      method: form.value.method,
      path: form.value.path,
      permlabel: form.value.permlabel,
      moduleKey: form.value.moduleKey
    }
    // Create API
    createApi(api).then(() => {
      formRef.value.resetFields()
      props.onSubmit()
    }).catch(error => {
      console.error('Create API failed:', error)
    })
  } catch (error) {
    console.error('Form validation failed:', error)
  }
}
</script>
<style scoped></style>

