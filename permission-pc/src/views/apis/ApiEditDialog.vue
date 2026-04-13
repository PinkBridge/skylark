<template>
  <el-dialog :model-value="visible" :title="t('EditTitle')" align-center destroy-on-close :modal="false"
    modal-penetrable :show-close="false" style="max-width: 600px">
    <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
      <el-form-item :label="t('IDLabel')" prop="id">
        <el-input v-model="form.id" :disabled="true" />
      </el-form-item>
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

<script setup>
import { ref, watch, computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { getApiById, updateApiById } from '@/views/apis/ApiApi'
import { ElMessage } from 'element-plus'

const { t } = useI18n()

const props = defineProps(['visible', 'row', 'onSubmit', 'onCancel'])

const formRef = ref(null)
const form = ref({})
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

const fetchApiData = () => {
  if (props.row && props.row.id) {
    getApiById(props.row.id).then(response => {
      form.value = response.api || response || {}
    }).catch(error => {
      console.error('Failed to get API information:', error)
      ElMessage.error(error.message || 'Failed to get API information')
    })
  }
}

const onSubmit = async () => {
  if (!formRef.value) return
  
  try {
    // Validate form
    await formRef.value.validate()
    
    // If validation passes, update API
    const api = {
      method: form.value.method,
      path: form.value.path,
      permlabel: form.value.permlabel,
      moduleKey: form.value.moduleKey
    }
    
    updateApiById(form.value.id, api).then(() => {
      props.onSubmit()
    }).catch(error => {
      console.error('Update API failed:', error)
      ElMessage.error(error.message || 'Update API failed')
    })
  } catch (error) {
    console.error('Form validation failed:', error)
  }
}

const onCancel = () => {
  // Reset form when canceling
  if (formRef.value) {
    formRef.value.resetFields()
  }
  props.onCancel()
}

watch(
  () => [props.visible, props.row?.id],
  ([visible, rowId]) => {
    if (visible && rowId) {
      fetchApiData()
    }
  },
  { immediate: true }
)
</script>
<style scoped></style>

