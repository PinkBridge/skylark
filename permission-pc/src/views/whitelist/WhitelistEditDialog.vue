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

<script setup>
import { ref, watch, computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { getWhitelistById, updateWhitelistById } from '@/views/whitelist/WhitelistApi'
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
    enabled: [
      { required: true, message: t('EnableRequired'), trigger: 'change' }
    ]
  }
})

const fetchWhitelistData = () => {
  if (props.row && props.row.id) {
    getWhitelistById(props.row.id).then(response => {
      form.value = response.whitelist || response || {}
    }).catch(error => {
      console.error('Failed to get whitelist information:', error)
      ElMessage.error(error.message || 'Failed to get whitelist information')
    })
  }
}

const onSubmit = async () => {
  if (!formRef.value) return
  
  try {
    // Validate form
    await formRef.value.validate()
    
    // If validation passes, update whitelist
    const whitelist = {
      method: form.value.method,
      path: form.value.path,
      remark: form.value.remark || '',
      enabled: form.value.enabled
    }
    
    updateWhitelistById(form.value.id, whitelist).then(() => {
      props.onSubmit()
    }).catch(error => {
      console.error('Update whitelist failed:', error)
      ElMessage.error(error.message || 'Update whitelist failed')
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
      fetchWhitelistData()
    }
  },
  { immediate: true }
)
</script>
<style scoped></style>

