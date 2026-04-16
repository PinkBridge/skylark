<template>
  <el-dialog
    :model-value="visible"
    :title="t('CreateTitle')"
    align-center
    destroy-on-close
    :show-close="false"
    style="max-width: 700px"
    :modal="false"
    modal-penetrable
  >
    <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
      <el-form-item :label="t('NameLabel')" prop="name">
        <el-input v-model="form.name" :placeholder="t('NameLabel')" />
      </el-form-item>
      <el-form-item :label="t('TypeLabel')" prop="type">
        <el-select v-model="form.type" :placeholder="t('TypeLabel')" style="width: 100%;">
          <el-option label="ORG_ALL" value="ORG_ALL" />
          <el-option label="ORG_AND_CHILD" value="ORG_AND_CHILD" />
          <el-option label="ORG_ONLY" value="ORG_ONLY" />
          <el-option label="SELF" value="SELF" />
        </el-select>
      </el-form-item>
      <el-form-item :label="t('DescriptionLabel')" prop="description">
        <el-input
          v-model="form.description"
          type="textarea"
          :rows="3"
          :placeholder="t('DescriptionLabel')"
        />
      </el-form-item>
      <el-form-item :label="t('EnabledLabel')" prop="enabled">
        <el-switch
          v-model="form.enabled"
          :active-text="t('Yes')"
          :inactive-text="t('No')"
        />
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

<script setup name="DataDomainCreateDialog">
import { ref, computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { createDataDomain } from '@/views/datadomains/DataDomainApi'
import { ElMessage } from 'element-plus'

const { t } = useI18n()

const props = defineProps(['visible', 'onSubmit', 'onCancel'])

const formRef = ref(null)
const form = ref({
  name: '',
  type: 'ORG_ALL',
  description: '',
  enabled: true
})

const rules = computed(() => {
  return {
    name: [{ required: true, message: t('NameRequired'), trigger: 'blur' }],
    type: [{ required: true, message: t('TypeRequired'), trigger: 'change' }]
  }
})

const resetForm = () => {
  if (formRef.value) {
    formRef.value.resetFields()
  }
  form.value = {
    name: '',
    type: 'ORG_ALL',
    description: '',
    enabled: true
  }
}

const onCancel = () => {
  resetForm()
  props.onCancel()
}

const onSubmit = async () => {
  if (!formRef.value) return

  try {
    await formRef.value.validate()
    const payload = {
      name: form.value.name,
      type: form.value.type,
      description: form.value.description || '',
      enabled: !!form.value.enabled
    }
    createDataDomain(payload)
      .then(() => {
        resetForm()
        props.onSubmit()
      })
      .catch((error) => {
        console.error('Create data domain failed:', error)
        ElMessage.error(error.message || 'Create data domain failed')
      })
  } catch (error) {
    console.error('Form validation failed:', error)
  }
}
</script>

<style scoped></style>


