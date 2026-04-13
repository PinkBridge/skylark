<template>
  <el-card shadow="always">
    <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
      <el-form-item :label="t('NameLabel')" prop="name">
        <el-input v-model="form.name" :placeholder="t('NameLabel')" />
      </el-form-item>
      <el-form-item :label="t('SystemNameLabel')" prop="systemName">
        <el-input v-model="form.systemName" :placeholder="t('SystemNameLabel')" />
      </el-form-item>
      <el-form-item :label="t('ContactNameLabel')" prop="contactName">
        <el-input v-model="form.contactName" :placeholder="t('ContactNameLabel')" />
      </el-form-item>
      <el-form-item :label="t('ContactPhoneLabel')" prop="contactPhone">
        <el-input v-model="form.contactPhone" :placeholder="t('ContactPhoneLabel')" />
      </el-form-item>
      <el-form-item :label="t('ContactEmailLabel')" prop="contactEmail">
        <el-input v-model="form.contactEmail" :placeholder="t('ContactEmailLabel')" />
      </el-form-item>
      <el-form-item :label="t('DomainLabel')" prop="domain">
        <el-input v-model="form.domain" :placeholder="t('DomainLabel')" />
      </el-form-item>
      <el-form-item :label="t('AddressLabel')" prop="address">
        <el-input v-model="form.address" :placeholder="t('AddressLabel')" />
      </el-form-item>
      <el-form-item :label="t('RemarksLabel')" prop="description">
        <el-input v-model="form.description" type="textarea" :rows="3" :placeholder="t('RemarksLabel')" />
      </el-form-item>
      <el-form-item :label="t('LogoLabel')" prop="logo">
        <ResourceUpload :fileList="[form.logo]" :onSuccess="handleUploadSuccess" />
        <div class="upload-tip">{{ t('UploadTip') }}</div>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" v-permission="'system.tenant.profile.edit'" @click="handleSubmit">
          {{ t('TenantProfileSubmitLabel') }}
        </el-button>
      </el-form-item>
    </el-form>
  </el-card>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { ElMessage } from 'element-plus'
import ResourceUpload from '@/components/ResourceUpload.vue'
import { getMyTenant, updateMyTenant } from '@/views/tenants/TenantApi'

const { t } = useI18n()
const formRef = ref(null)
const form = ref({
  name: '',
  systemName: '',
  contactName: '',
  contactPhone: '',
  contactEmail: '',
  domain: '',
  address: '',
  description: '',
  logo: '',
})

const rules = computed(() => ({
  name: [{ required: true, message: t('NameRequired'), trigger: 'blur' }]
}))

const loadTenant = async () => {
  try {
    const data = await getMyTenant()
    form.value = {
      name: data.name || '',
      systemName: data.systemName || '',
      contactName: data.contactName || '',
      contactPhone: data.contactPhone || '',
      contactEmail: data.contactEmail || '',
      domain: data.domain || '',
      address: data.address || '',
      description: data.description || '',
      logo: data.logo || '',
    }
  } catch (error) {
    ElMessage.error(error.message || 'Failed to get tenant information')
  }
}

const handleUploadSuccess = (url) => {
  form.value.logo = url
}

const handleSubmit = async () => {
  if (!formRef.value) return
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  try {
    await updateMyTenant({ ...form.value })
    ElMessage.success(t('UpdateSuccess'))
    loadTenant()
  } catch (error) {
    ElMessage.error(error.message || t('UpdateFailed'))
  }
}

onMounted(() => {
  loadTenant()
})
</script>

<style scoped>
.upload-tip {
  font-size: 12px;
  color: #606266;
  margin-top: 8px;
}
</style>
