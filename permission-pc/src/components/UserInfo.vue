<template>
  <div class="my-profile" v-loading="loading">
    <el-descriptions border size="small" :column="2" class="readonly-block">
      <el-descriptions-item :label="t('UsernameLabel')">{{ userInfo.username }}</el-descriptions-item>
      <el-descriptions-item :label="t('RoleLabel')">{{ roles.map((r) => r.name).join(', ') || '—' }}</el-descriptions-item>
      <el-descriptions-item :label="t('StatusLabel')">{{ statusLabel }}</el-descriptions-item>
      <el-descriptions-item :label="t('CreatedAtLabel')">{{ userInfo.createTime || '—' }}</el-descriptions-item>
    </el-descriptions>

    <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
      <el-form-item :label="t('AvatarLabel')" prop="avatar">
        <ResourceUpload :file-list="form.avatar" :on-success="handleUploadSuccess" />
        <div class="upload-tip">{{ t('UploadTip') }}</div>
      </el-form-item>
      <el-form-item :label="t('PhoneLabel')" prop="phone">
        <el-input v-model="form.phone" clearable maxlength="20" show-word-limit />
      </el-form-item>
      <el-form-item :label="t('EmailLabel')" prop="email">
        <el-input v-model="form.email" clearable maxlength="120" />
      </el-form-item>
      <el-form-item :label="t('GenderLabel')" prop="gender">
        <el-radio-group v-model="form.gender">
          <el-radio label="M">{{ t('Boy') }}</el-radio>
          <el-radio label="F">{{ t('Girl') }}</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item :label="t('AddressLabel')" prop="address">
        <el-input v-model="form.address" type="textarea" :rows="3" maxlength="500" show-word-limit />
      </el-form-item>
    </el-form>

    <div class="actions">
      <el-button type="primary" :loading="saving" @click="submit">{{ t('SaveProfileButtonText') }}</el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { ElMessage } from 'element-plus'
import { getMyInfo, updateMyProfile } from '@/api/me'
import ResourceUpload from '@/components/ResourceUpload.vue'

const { t } = useI18n()
const emit = defineEmits(['updated'])

const loading = ref(false)
const saving = ref(false)
const formRef = ref(null)
const userInfo = ref({
  username: '',
  phone: '',
  email: '',
  gender: '',
  status: '',
  createTime: '',
  address: '',
  avatar: ''
})
const roles = ref([])

const form = ref({
  avatar: '',
  phone: '',
  email: '',
  gender: 'M',
  address: ''
})

const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
const phoneRegex = /^1[3-9]\d{9}$/

const rules = computed(() => ({
  email: [
    {
      validator: (_r, v, cb) => {
        if (v && !emailRegex.test(String(v))) {
          cb(new Error(t('EmailFormatError')))
        } else {
          cb()
        }
      },
      trigger: 'blur'
    }
  ],
  phone: [
    {
      validator: (_r, v, cb) => {
        if (v && !phoneRegex.test(String(v))) {
          cb(new Error(t('PhoneFormatError')))
        } else {
          cb()
        }
      },
      trigger: 'blur'
    }
  ]
}))

const statusLabel = computed(() => {
  const s = (userInfo.value.status || '').toUpperCase()
  if (s === 'ACTIVE') return t('Active')
  if (s === 'INACTIVE') return t('Inactive')
  if (s === 'LOCKED') return t('Locked')
  return userInfo.value.status || '—'
})

function syncFormFromUser() {
  const u = userInfo.value
  const g = u.gender ? String(u.gender).toUpperCase() : ''
  form.value = {
    avatar: u.avatar || '',
    phone: u.phone || '',
    email: u.email || '',
    gender: g === 'F' ? 'F' : 'M',
    address: u.address || ''
  }
}

const fetchUserInfo = async () => {
  loading.value = true
  try {
    const data = await getMyInfo()
    userInfo.value = data.user || {}
    roles.value = data.roles || []
    syncFormFromUser()
  } catch (error) {
    console.error('Failed to get user information:', error)
    ElMessage.error(error?.message || t('RequestFailedNotice'))
  } finally {
    loading.value = false
  }
}

const handleUploadSuccess = (file) => {
  form.value.avatar = file
}

const submit = async () => {
  if (!formRef.value) return
  try {
    await formRef.value.validate()
  } catch {
    return
  }
  saving.value = true
  try {
    await updateMyProfile({
      avatar: form.value.avatar || '',
      phone: form.value.phone || '',
      email: form.value.email || '',
      gender: form.value.gender,
      address: form.value.address || ''
    })
    ElMessage.success(t('ProfileSaveSuccess'))
    await fetchUserInfo()
    emit('updated')
  } catch (error) {
    ElMessage.error(error?.message || t('RequestFailedNotice'))
  } finally {
    saving.value = false
  }
}

onMounted(() => {
  fetchUserInfo()
})
</script>

<style scoped>
.my-profile {
  min-width: 320px;
}
.readonly-block {
  margin-bottom: 8px;
}
.upload-tip {
  font-size: 12px;
  color: var(--el-text-color-secondary);
  margin-top: 6px;
}
.actions {
  margin-top: 12px;
}
</style>
