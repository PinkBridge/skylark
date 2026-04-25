<template>
  <el-dialog :model-value="visible" @update:model-value="(v) => { if (!v) onCancel() }" @close="onCancel" :title="t('CreateTitle')" align-center destroy-on-close :show-close="false"
    style="max-width: 600px" :modal="false" modal-penetrable>
    <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
      <el-form-item :label="t('UsernameLabel')" prop="username">
        <el-input v-model="form.username" />
      </el-form-item>
      <el-form-item :label="t('OrganizationLabel')" prop="orgId">
        <OrgSelect v-model="form.orgId" :model-value="form.orgId" :placeholder="t('OrganizationLabel')" />
      </el-form-item>
      <el-form-item :label="t('PasswordLabel')" prop="password">
        <el-input v-model="form.password" type="password" show-password />
      </el-form-item>
      <el-form-item :label="t('EmailLabel')" prop="email">
        <el-input v-model="form.email" />
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
      <el-form-item :label="t('AvatarLabel')" prop="avatar">
        <ResourceUpload :fileList="form.avatar" :on-success="handleUploadSuccess" />
        <div class="upload-tip">{{ t('UploadTip') }}</div>
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

<script setup name="UserCreateDialog">
import { ref, computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { createUser } from '@/views/users/UserApi'
import ResourceUpload from '@/components/ResourceUpload.vue'
import OrgSelect from '@/views/orgs/OrgSelect.vue'
import RoleSelect from '@/views/roles/RoleSelect.vue'

const { t } = useI18n()
// props
const props = defineProps(['visible', 'onSubmit', 'onCancel'])

const formRef = ref(null)
const form = ref({
  username: '',
  password: '',
  email: '',
  phone: '',
  roleIds: [],
  status: '',
  orgId: '',
  avatar: ''
})

const statusOptions = ref([
  { label: t('Active'), value: 'ACTIVE' },
  { label: t('Inactive'), value: 'INACTIVE' },
  { label: t('Locked'), value: 'LOCKED' }
])

const handleUploadSuccess = (file) => {
  form.value.avatar = file
  console.log('handleUploadSuccess', file)
}

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
    username: [
      { required: true, message: t('UsernameRequired'), trigger: 'blur' },
      { min: 3, max: 20, message: t('UsernameLengthError'), trigger: 'blur' }
    ],
    password: [
      { required: true, message: t('PasswordRequired'), trigger: 'blur' },
      { min: 6, max: 20, message: t('PasswordLengthError'), trigger: 'blur' }
    ],
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

const onCancel = () => {
  // Reset form when canceling
  if (formRef.value) {
    formRef.value.resetFields()
  }
  form.value = {
    username: '',
    password: '',
    email: '',
    phone: '',
    roleIds: [],
    status: '',
    orgId: '',
    avatar: ''
  }
  props.onCancel()
}

const onSubmit = async () => {
  if (!formRef.value) return
  try {
    // Validate form
    await formRef.value.validate()
    // If validation passes, call parent's onSubmit
    const user = {
      orgId: form.value.orgId,
      avatar: form.value.avatar,
      username: form.value.username,
      password: form.value.password,
      email: form.value.email,
      phone: form.value.phone,
      roleIds: form.value.roleIds,
      status: form.value.status
    }
    // Create user
    createUser(user).then(() => {
      formRef.value.resetFields()
      props.onSubmit()
    }).catch(error => {
      console.error('Create user failed:', error)
    })
  } catch (error) {
    console.error('Form validation failed:', error)
  }
}
</script>
<style scoped></style>