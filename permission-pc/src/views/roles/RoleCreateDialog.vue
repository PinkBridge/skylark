<template>
  <el-dialog :model-value="visible" @update:model-value="(v) => { if (!v) onCancel() }" @close="onCancel" :title="t('CreateTitle')" align-center destroy-on-close :show-close="false"
    style="max-width: 600px" :modal="false" modal-penetrable>
    <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
      <el-form-item :label="t('NameLabel')" prop="name">
        <el-input v-model="form.name" :placeholder="t('NameLabel')" />
      </el-form-item>
      <el-form-item :label="t('RemarkLabel')" prop="remark">
        <el-input v-model="form.remark" type="textarea" :rows="3" :placeholder="t('RemarkLabel')" />
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

<script setup name="RoleCreateDialog">
import { ref, computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { createRole } from '@/views/roles/RoleApi'
import { ElMessage } from 'element-plus'

const { t } = useI18n()

const props = defineProps(['visible', 'onSubmit', 'onCancel'])

const formRef = ref(null)
const form = ref({
  name: '',
  remark: '',
})

const rules = computed(() => {
  return {
    name: [
      { required: true, message: t('NameRequired'), trigger: 'blur' }
    ],
  }
})

const onCancel = () => {
  if (formRef.value) {
    formRef.value.resetFields()
  }
  form.value = {
    name: '',
    remark: '',
  }
  props.onCancel()
}

const onSubmit = async () => {
  if (!formRef.value) return

  try {
    await formRef.value.validate()
    const role = {
      name: form.value.name,
      remark: form.value.remark || '',
    }
    createRole(role).then(() => {
      formRef.value.resetFields()
      props.onSubmit()
    }).catch(error => {
      console.error('Create role failed:', error)
      ElMessage.error(error.message || 'Create role failed')
    })
  } catch (error) {
    console.error('Form validation failed:', error)
  }
}
</script>

<style scoped></style>

