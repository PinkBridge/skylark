<template>
  <el-dialog :model-value="visible" :title="t('CreateTitle')" align-center destroy-on-close :show-close="false"
    style="max-width: 600px" :modal="false" modal-penetrable>
    <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
      <el-form-item :label="t('NameLabel')" prop="name">
        <el-input v-model="form.name" :placeholder="t('NameLabel')" />
      </el-form-item>
      <el-form-item :label="t('CodeLabel')" prop="code">
        <el-input v-model="form.code" :placeholder="t('CodeLabel')" />
      </el-form-item>
      <el-form-item :label="t('ParentOrganizationLabel')" prop="parentId">
        <OrgSelect v-model="form.parentId" :placeholder="t('ParentOrganizationLabel')" />
      </el-form-item>
      <el-form-item :label="t('TypeLabel')" prop="type">
        <el-select v-model="form.type" :placeholder="t('TypeLabel')">
          <el-option :label="t('OrgTypeCompany')" value="COMPANY" />
          <el-option :label="t('OrgTypeDepartment')" value="DEPARTMENT" />
          <el-option :label="t('OrgTypeTeam')" value="TEAM" />
        </el-select>
      </el-form-item>
      <el-form-item :label="t('StatusLabel')" prop="status">
        <el-select v-model="form.status" :placeholder="t('StatusLabel')">
          <el-option :label="t('Active')" value="ACTIVE" />
          <el-option :label="t('Disabled')" value="DISABLED" />
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

<script setup name="OrgCreateDialog">
import { ref, computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { createOrg } from '@/views/orgs/OrgApi'
import { ElMessage } from 'element-plus'
import OrgSelect from '@/views/orgs/OrgSelect.vue'

const { t } = useI18n()

const props = defineProps(['visible', 'onSubmit', 'onCancel'])

const formRef = ref(null)
const form = ref({
  name: '',
  code: '',
  parentId: '',
  type: '',
  status: 'ACTIVE',
})

const rules = computed(() => {
  return {
    name: [
      { required: true, message: t('NameRequired'), trigger: 'blur' }
    ],
    code: [
      { required: true, message: t('CodeRequired'), trigger: 'blur' }
    ],
    type: [
      { required: true, message: t('TypeRequired'), trigger: 'change' }
    ],
    status: [
      { required: true, message: t('StatusRequired'), trigger: 'change' }
    ],
  }
})

const onCancel = () => {
  if (formRef.value) {
    formRef.value.resetFields()
  }
  form.value = {
    name: '',
    code: '',
    parentId: '',
    type: '',
    status: 'ACTIVE',
  }
  props.onCancel()
}

const onSubmit = async () => {
  if (!formRef.value) return

  try {
    await formRef.value.validate()
    const org = {
      name: form.value.name,
      code: form.value.code,
      parentId: form.value.parentId || '',
      type: form.value.type,
      status: form.value.status,
    }
    createOrg(org).then(() => {
      formRef.value.resetFields()
      props.onSubmit()
    }).catch(error => {
      console.error('Create org failed:', error)
      ElMessage.error(error.message || 'Create org failed')
    })
  } catch (error) {
    console.error('Form validation failed:', error)
  }
}
</script>

<style scoped></style>


