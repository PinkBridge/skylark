<template>
  <el-dialog :model-value="visible" :title="t('EditTitle')" align-center destroy-on-close :modal="false"
    modal-penetrable :show-close="false" style="max-width: 600px">
    <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
      <el-form-item :label="t('IDLabel')" prop="id">
        <el-input v-model="form.id" :disabled="true" />
      </el-form-item>
      <el-form-item :label="t('NameLabel')" prop="name">
        <el-input v-model="form.name" :placeholder="t('NameLabel')" />
      </el-form-item>
      <el-form-item :label="t('CodeLabel')" prop="code">
        <el-input v-model="form.code" :placeholder="t('CodeLabel')" />
      </el-form-item>
      <el-form-item :label="t('ParentOrganizationLabel')" prop="parentId">
        <OrgSelect v-model="form.parentId" 
        :model-value="form.parentId" :disabled-ids="[form.id]" :placeholder="t('ParentOrganizationLabel')" />
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

<script setup>
import { ref, watch, computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { getOrgById, updateOrgById } from '@/views/orgs/OrgApi'
import { ElMessage } from 'element-plus'
import OrgSelect from '@/views/orgs/OrgSelect.vue'

const { t } = useI18n()

const props = defineProps(['visible', 'row', 'onSubmit', 'onCancel'])

const formRef = ref(null)
const form = ref({})

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

const fetchOrgData = () => {
  if (props.row && props.row.id) {
    getOrgById(props.row.id).then(response => {
      const data = response.org || response || {}
      form.value = {
        ...data,
      }
    }).catch(error => {
      console.error('Failed to get org information:', error)
      ElMessage.error(error.message || 'Failed to get org information')
    })
  }
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
    updateOrgById(form.value.id, org).then(() => {
      props.onSubmit()
    }).catch(error => {
      console.error('Update org failed:', error)
      ElMessage.error(error.message || 'Update org failed')
    })
  } catch (error) {
    console.error('Form validation failed:', error)
  }
}

const onCancel = () => {
  if (formRef.value) {
    formRef.value.resetFields()
  }
  props.onCancel()
}

watch(
  () => [props.visible, props.row?.id],
  ([visible, id]) => {
    if (visible && id) {
      fetchOrgData()
    }
  },
  { immediate: true }
)
</script>

<style scoped></style>


