<template>
  <el-dialog :model-value="visible" :title="t('EditTitle')" align-center destroy-on-close :modal="false"
    modal-penetrable :show-close="false" style="max-width: 700px">
    <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
      <el-form-item :label="t('NameLabel')" prop="name">
        <el-input v-model="form.name" :placeholder="t('NameLabel')" />
      </el-form-item>
      <el-form-item :label="t('SystemNameLabel')" prop="systemName">
        <el-input v-model="form.systemName" :placeholder="t('SystemNameLabel')" />
      </el-form-item>
      <el-form-item :label="t('DomainLabel')" prop="domain">
        <el-input v-model="form.domain" :placeholder="t('DomainLabel')" />
      </el-form-item>
      <el-form-item :label="t('LogoLabel')" prop="logo">
        <ResourceUpload :fileList="[form.logo]" :onSuccess="handleUploadSuccess" />
        <div class="upload-tip">{{ t('UploadTip') }}</div>
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
      <el-form-item :label="t('AddressLabel')" prop="address">
        <el-input v-model="form.address" type="textarea" :rows="2" :placeholder="t('AddressLabel')" />
      </el-form-item>
      <el-form-item :label="t('StatusLabel')" prop="status">
        <el-select v-model="form.status" :placeholder="t('StatusLabel')">
          <el-option :label="t('Active')" value="ACTIVE" />
          <el-option :label="t('Disabled')" value="DISABLED" />
        </el-select>
      </el-form-item>
      <el-form-item :label="t('ExpireTimeLabel')" prop="expireTime">
        <el-date-picker
          v-model="form.expireTime"
          type="datetime"
          :placeholder="t('ExpireTimeLabel')"
          style="width: 100%;"
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

<script setup>
import { ref, watch, computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { getTenantById, updateTenantById } from '@/views/tenants/TenantApi'
import { ElMessage } from 'element-plus'
import ResourceUpload from '@/components/ResourceUpload.vue'

const { t } = useI18n()

const props = defineProps(['visible', 'row', 'onSubmit', 'onCancel'])

const formRef = ref(null)
const form = ref({
  id: '',
  name: '',
  systemName: '',
  contactName: '',
  contactPhone: '',
  contactEmail: '',
  domain: '',
  logo: '',
  status: 'ACTIVE',
  expireTime: '',
})

const rules = computed(() => {
  return {
    name: [
      { required: true, message: t('NameRequired'), trigger: 'blur' }
    ],
    status: [
      { required: true, message: t('StatusRequired'), trigger: 'change' }
    ],
  }
})

const fetchTenantData = () => {
  if (props.row && props.row.id) {
    getTenantById(props.row.id).then(response => {
      const data = response.tenant || response || {}
      form.value = {
        id: data.id || '',
        name: data.name || '',
        systemName: data.systemName || '',
        contactName: data.contactName || '',
        contactPhone: data.contactPhone || '',
        contactEmail: data.contactEmail || '',
        domain: data.domain || '',
        address: data.address || '',
        logo: data.logo || '',
        status: data.status || 'ACTIVE',
        expireTime: data.expireTime || '',
      }
    }).catch(error => {
      console.error('Failed to get tenant information:', error)
      ElMessage.error(error.message || 'Failed to get tenant information')
    })
  }
}

const handleUploadSuccess = (url) => {
  console.log('handleUploadSuccess', url)
  form.value.logo = url
}


const onSubmit = async () => {
  if (!formRef.value) return

  try {
    await formRef.value.validate()
    const tenant = {
      name: form.value.name,
      systemName: form.value.systemName || '',
      contactName: form.value.contactName || '',
      contactPhone: form.value.contactPhone || '',
      contactEmail: form.value.contactEmail || '',
      domain: form.value.domain || '',
      address: form.value.address || '',
      logo: form.value.logo || '',
      status: form.value.status,
      expireTime: form.value.expireTime || '',
    }
    updateTenantById(form.value.id, tenant).then(() => {
      props.onSubmit()
    }).catch(error => {
      console.error('Update tenant failed:', error)
      ElMessage.error(error.message || 'Update tenant failed')
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
      fetchTenantData()
    }
  },
  { immediate: true }
)
</script>

<style scoped>
.logo-uploader {
  :deep(.el-upload) {
    border: 1px dashed var(--el-border-color);
    border-radius: 6px;
    cursor: pointer;
    position: relative;
    overflow: hidden;
    transition: var(--el-transition-duration-fast);
    width: 120px;
    height: 120px;
    display: flex;
    align-items: center;
    justify-content: center;
  }

  :deep(.el-upload:hover) {
    border-color: var(--el-color-primary);
  }
}

.logo-uploader-icon {
  font-size: 28px;
  color: #8c939d;
}

.logo-preview {
  width: 100%;
  height: 100%;
  max-width: 120px;
  max-height: 120px;
  object-fit: cover;
  display: block;
  border-radius: 6px;
}

.upload-tip {
  font-size: 12px;
  color: #606266;
  margin-top: 8px;
}
</style>


