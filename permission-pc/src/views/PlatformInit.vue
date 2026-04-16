<template>
  <div class="init-container">
    <div class="init-card">
      <h1 class="title">{{ t('PlatformInitTitle') }}</h1>
      <p class="subtitle">{{ t('PlatformInitSubtitle') }}</p>

      <el-steps :active="activeStep" finish-status="success" align-center class="steps">
        <el-step :title="t('PlatformInitStepTenant')" />
        <el-step :title="t('PlatformInitStepAdmin')" />
      </el-steps>

      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="140px"
        class="form"
      >
        <template v-if="activeStep === 0">
          <el-form-item prop="tenant.name" :label="t('TenantNameLabel')">
            <el-input v-model="form.tenant.name" />
          </el-form-item>
          <el-form-item prop="tenant.systemName" :label="t('SystemNameLabel')">
            <el-input v-model="form.tenant.systemName" />
          </el-form-item>
          <el-form-item prop="tenant.domain" :label="t('DomainLabel')">
            <el-input v-model="form.tenant.domain" :placeholder="t('PlatformInitDomainPlaceholder')" />
          </el-form-item>
          <el-form-item prop="tenant.logo" :label="t('LogoLabel')">
            <ResourceUpload :fileList="form.tenant.logo" :onSuccess="handleLogoUploadSuccess" />
            <div class="upload-tip">{{ t('UploadTip') }}</div>
          </el-form-item>
          <el-form-item prop="tenant.contactName" :label="t('ContactNameLabel')">
            <el-input v-model="form.tenant.contactName" />
          </el-form-item>
          <el-form-item prop="tenant.contactPhone" :label="t('ContactPhoneLabel')">
            <el-input v-model="form.tenant.contactPhone" />
          </el-form-item>
          <el-form-item prop="tenant.contactEmail" :label="t('ContactEmailLabel')">
            <el-input v-model="form.tenant.contactEmail" />
          </el-form-item>
          <el-form-item prop="tenant.address" :label="t('AddressLabel')">
            <el-input v-model="form.tenant.address" />
          </el-form-item>
          <el-form-item prop="tenant.description" :label="t('DescriptionLabel')">
            <el-input v-model="form.tenant.description" type="textarea" :rows="3" />
          </el-form-item>
        </template>

        <template v-else>
          <el-form-item prop="admin.username" :label="t('UsernameLabel')">
            <el-input v-model="form.admin.username" autocomplete="off" />
          </el-form-item>
          <el-form-item prop="admin.password" :label="t('PasswordLabel')">
            <el-input v-model="form.admin.password" type="password" show-password autocomplete="new-password" />
          </el-form-item>
          <el-form-item prop="admin.phone" :label="t('PhoneLabel')">
            <el-input v-model="form.admin.phone" />
          </el-form-item>
          <el-form-item prop="admin.email" :label="t('EmailLabel')">
            <el-input v-model="form.admin.email" />
          </el-form-item>
        </template>
      </el-form>

      <div class="actions">
        <el-button v-if="activeStep === 1" @click="prevStep">{{ t('BackLabel') }}</el-button>
        <el-button type="primary" :loading="submitting" @click="nextOrSubmit">
          {{ activeStep === 0 ? t('NextLabel') : t('SubmitLabel') }}
        </el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { ElMessage } from 'element-plus'
import { getPlatformInitState, initializePlatform } from '@/api/init'
import ResourceUpload from '@/components/ResourceUpload.vue'

const router = useRouter()
const { t } = useI18n()

const activeStep = ref(0)
const submitting = ref(false)
const formRef = ref()

const form = reactive({
  tenant: {
    name: '',
    systemName: '',
    domain: '',
    logo: '',
    contactName: '',
    contactPhone: '',
    contactEmail: '',
    address: '',
    description: ''
  },
  admin: {
    username: '',
    password: '',
    phone: '',
    email: ''
  }
})

const rules = {
  'tenant.name': [{ required: true, message: t('NameRequired'), trigger: 'blur' }],
  'tenant.systemName': [{ required: true, message: t('SystemNameRequired'), trigger: 'blur' }],
  'tenant.domain': [
    { required: true, message: t('DomainRequired'), trigger: 'blur' },
    { validator: (_rule, value, callback) => validateDomainOrIpPort(value, callback), trigger: 'blur' }
  ],
  'tenant.logo': [{ required: true, message: t('LogoRequired'), trigger: 'change' }],
  'admin.username': [{ required: true, message: t('UsernameRequired'), trigger: 'blur' }],
  'admin.password': [{ required: true, message: t('PasswordRequired'), trigger: 'blur' }]
}

onMounted(async () => {
  try {
    const state = await getPlatformInitState()
    if (state?.initialized) {
      router.replace('/welcome')
    }
  } catch (e) {
    // http interceptor already shows message
  }

  // Prefill tenant domain from current location (host or ip:port).
  // Users can overwrite it if they prefer a different public domain.
  if (!form.tenant.domain) {
    const host = (window.location && window.location.host) ? window.location.host : ''
    // Tenant domain must not include port.
    form.tenant.domain = (host || '').split(':')[0]
  }
})

const validateCurrentStep = async () => {
  if (!formRef.value) return true
  const props = activeStep.value === 0
    ? ['tenant.name', 'tenant.systemName', 'tenant.domain', 'tenant.logo']
    : ['admin.username', 'admin.password']
  return await formRef.value.validateField(props).then(() => true).catch(() => false)
}

const nextOrSubmit = async () => {
  const ok = await validateCurrentStep()
  if (!ok) return

  if (activeStep.value === 0) {
    activeStep.value = 1
    return
  }

  submitting.value = true
  try {
    await initializePlatform(form)
    ElMessage.success(t('PlatformInitSuccess'))
    router.replace('/welcome')
  } finally {
    submitting.value = false
  }
}

const prevStep = () => {
  activeStep.value = 0
}

const handleLogoUploadSuccess = (url) => {
  form.tenant.logo = url
}

const validateDomainOrIpPort = (raw, callback) => {
  const v = (raw || '').toString().trim()
  if (!v) {
    callback()
    return
  }
  // Accept:
  // - domain (example.com, sub.example.com)
  // - ipv4 (1.2.3.4)
  // NOTE: port is NOT allowed in tenant domain.
  if (v.includes(':')) {
    callback(new Error(t('DomainPortNotAllowed')))
    return
  }
  const host = v
  const isIpv4 = /^(\d{1,3}\.){3}\d{1,3}$/.test(host) && host.split('.').every(n => Number(n) >= 0 && Number(n) <= 255)
  const isDomain = /^[a-zA-Z0-9]([a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?(\.[a-zA-Z0-9]([a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?)+$/.test(host)
  if (!isIpv4 && !isDomain) {
    callback(new Error(t('DomainFormatError')))
    return
  }
  callback()
}
</script>

<style scoped>
.init-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 20px;
}
.init-card {
  width: 100%;
  max-width: 860px;
  background: rgba(255, 255, 255, 0.97);
  border-radius: 18px;
  padding: 32px 28px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.25);
}
.title {
  margin: 0;
  font-size: 26px;
  font-weight: 600;
  color: #1f2937;
}
.subtitle {
  margin: 8px 0 18px;
  color: #6b7280;
}
.steps {
  margin: 18px 0 26px;
}
.form {
  margin-top: 6px;
}
.actions {
  margin-top: 18px;
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}
.upload-tip {
  font-size: 12px;
  color: #606266;
  margin-top: 8px;
}
</style>

