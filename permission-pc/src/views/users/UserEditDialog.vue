<template>
  <el-dialog :model-value="visible" @update:model-value="(v) => { if (!v) onCancel() }" @close="onCancel" :title="t('EditTitle')" align-center destroy-on-close :modal="false"
    modal-penetrable :show-close="false" style="max-width: 600px">
    <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
      <el-form-item :label="t('UsernameLabel')" prop="username">
        <el-input v-model="form.username" :disabled="true" />
      </el-form-item>
      <el-form-item :label="t('OrganizationLabel')" prop="orgId">
        <OrgSelect v-model="form.orgId" :model-value="form.orgId" :placeholder="t('OrganizationLabel')" />
      </el-form-item>
      <el-form-item :label="t('AvatarLabel')" prop="avatar">
        <ResourceUpload :fileList="form.avatar" :on-success="handleUploadSuccess"/>
        <div class="upload-tip">{{ t('UploadTip') }}</div>
      </el-form-item>
      <el-form-item :label="t('EmailLabel')" prop="email">
        <el-input v-model="form.email" placeholder="example@email.com" />
      </el-form-item>
      <el-form-item :label="t('PhoneLabel')" prop="phone">
        <el-input v-model="form.phone" />
      </el-form-item>
      <el-form-item :label="t('RoleLabel')" prop="roleIds">
        <RoleSelect v-model="form.roleIds" :model-value="form.roleIds" :placeholder="t('RoleLabel')" />
      </el-form-item>
      <el-form-item :label="t('StatusLabel')" prop="status">
        <el-select v-model="form.status">
          <el-option v-for="item in statusOptions" :key="item.value" :label="item.label" :value="item.value" />
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
import { getUserById, updateUserById } from '@/views/users/UserApi'
import { ElMessage } from 'element-plus'
import OrgSelect from '@/views/orgs/OrgSelect.vue'
import ResourceUpload from '@/components/ResourceUpload.vue'
import RoleSelect from '@/views/roles/RoleSelect.vue'

const { t } = useI18n()

const props = defineProps(['visible', 'row', 'onSubmit', 'onCancel'])

const formRef = ref(null)
const form = ref({})
const statusOptions = ref([
  { label: t('Active'), value: 'ACTIVE' },
  { label: t('Inactive'), value: 'INACTIVE' },
  { label: t('Locked'), value: 'LOCKED' }
])

// Email validation regex
const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
// Phone validation regex (supports common formats)
const phoneRegex = /^1[3-9]\d{9}$/

// Validation rules - use computed to react to language changes
const rules = computed(() => {
  const validateEmail = (rule, value, callback) => {
    if (value && !emailRegex.test(value)) {
      callback(new Error(t('EmailFormatError')))
    } else {
      callback()
    }
  }

  const validatePhone = (rule, value, callback) => {
    if (value && !phoneRegex.test(value)) {
      callback(new Error(t('PhoneFormatError')))
    } else {
      callback()
    }
  }

  return {
    email: [
      { validator: validateEmail, trigger: 'blur' }
    ],
    phone: [
      { validator: validatePhone, trigger: 'blur' }
    ],
    orgId: [
      { required: true, message: t('OrganizationRequired'), trigger: 'change' }
    ],
    roleIds: [
      { required: true, message: t('RoleRequired'), trigger: 'change' }
    ],
    status: [
      { required: true, message: t('StatusRequired'), trigger: 'change' }
    ]
  }
})

const fetchUserData = () => {
  if (props.row && props.row.id) {
    getUserById(props.row.id).then(response => {
      form.value = response.user || {}
      form.value.roleIds = response.roles ? response.roles.map(item => item.id) : []
    }).catch(error => {
      console.error('Failed to get user information:', error)
      ElMessage.error(error.message || 'Failed to get user information')
    })
  }
}

const handleUploadSuccess = (file) => {
  form.value.avatar = file
  console.log('handleUploadSuccess', file)
}

const onSubmit = async () => {
  if (!formRef.value) return
  
  try {
    // Validate form
    await formRef.value.validate()
    
    // If validation passes, update user
    const user = {
      orgId: form.value.orgId,
      avatar: form.value.avatar,
      username: form.value.username,
      email: form.value.email,
      phone: form.value.phone,
      roleIds: form.value.roleIds,
      status: form.value.status
    }
    
    updateUserById(form.value.id, user).then(() => {
      props.onSubmit()
    }).catch(error => {
      console.error('Update user failed:', error)
      ElMessage.error(error.message || 'Update user failed')
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
      fetchUserData()
    }
  },
  { immediate: true }
)
</script>
<style scoped></style>