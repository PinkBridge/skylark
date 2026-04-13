<template>
  <el-dialog :model-value="visible" :title="t('CreateTitle')" align-center destroy-on-close :show-close="false"
    style="max-width: 800px" :modal="false" modal-penetrable>
    <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
      <el-form-item :label="t('ClientIdLabel')" prop="clientId">
        <el-input v-model="form.clientId" :placeholder="t('ClientIdLabel')" />
      </el-form-item>
      <el-form-item :label="t('ResourceIdsLabel')" prop="resourceIds">
        <el-input v-model="form.resourceIds" type="textarea" :rows="2" :placeholder="t('ResourceIdsPlaceholder')" />
      </el-form-item>
      <el-form-item :label="t('ScopeLabel')" prop="scope">
        <el-input v-model="form.scope" type="textarea" :rows="2" :placeholder="t('ScopePlaceholder')" />
      </el-form-item>
      <el-form-item :label="t('AuthorizedGrantTypesLabel')" prop="authorizedGrantTypes">
        <el-input v-model="form.authorizedGrantTypes" type="textarea" :rows="2" :placeholder="t('AuthorizedGrantTypesPlaceholder')" />
      </el-form-item>
      <el-form-item :label="t('WebServerRedirectUriLabel')" prop="webServerRedirectUri">
        <el-input v-model="form.webServerRedirectUri" :placeholder="t('WebServerRedirectUriLabel')" />
      </el-form-item>
      <el-form-item :label="t('AuthoritiesLabel')" prop="authorities">
        <el-input v-model="form.authorities" type="textarea" :rows="2" :placeholder="t('AuthoritiesPlaceholder')" />
      </el-form-item>
      <el-form-item :label="t('AccessTokenValidityLabel')" prop="accessTokenValidity">
        <el-input-number v-model="form.accessTokenValidity" :min="0" :placeholder="t('AccessTokenValidityLabel')" style="width: 100%" />
      </el-form-item>
      <el-form-item :label="t('RefreshTokenValidityLabel')" prop="refreshTokenValidity">
        <el-input-number v-model="form.refreshTokenValidity" :min="0" :placeholder="t('RefreshTokenValidityLabel')" style="width: 100%" />
      </el-form-item>
      <el-form-item :label="t('AdditionalInformationLabel')" prop="additionalInformation">
        <el-input v-model="form.additionalInformation" type="textarea" :rows="3" :placeholder="t('AdditionalInformationPlaceholder')" />
      </el-form-item>
      <el-form-item :label="t('AutoapproveLabel')" prop="autoapprove">
        <el-select v-model="form.autoapprove" :placeholder="t('AutoapproveLabel')">
          <el-option :label="t('Yes')" :value="true" />
          <el-option :label="t('No')" :value="false" />
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

<script setup name="AppCreateDialog">
import { ref, computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { createApp } from '@/views/apps/AppApi'
import { ElMessage } from 'element-plus'

const { t } = useI18n()

const props = defineProps(['visible', 'onSubmit', 'onCancel'])

const formRef = ref(null)
const form = ref({
  clientId: '',
  resourceIds: '',
  scope: '',
  authorizedGrantTypes: '',
  webServerRedirectUri: '',
  authorities: '',
  accessTokenValidity: null,
  refreshTokenValidity: null,
  additionalInformation: '',
  autoapprove: false,
})

const rules = computed(() => {
  return {
    clientId: [
      { required: true, message: t('ClientIdRequired'), trigger: 'blur' }
    ],
  }
})

const onCancel = () => {
  if (formRef.value) {
    formRef.value.resetFields()
  }
  form.value = {
    clientId: '',
    resourceIds: '',
    scope: '',
    authorizedGrantTypes: '',
    webServerRedirectUri: '',
    authorities: '',
    accessTokenValidity: null,
    refreshTokenValidity: null,
    additionalInformation: '',
    autoapprove: false,
  }
  props.onCancel()
}

const onSubmit = async () => {
  if (!formRef.value) return

  try {
    await formRef.value.validate()
    const app = {
      clientId: form.value.clientId,
      resourceIds: form.value.resourceIds || '',
      scope: form.value.scope || '',
      authorizedGrantTypes: form.value.authorizedGrantTypes || '',
      webServerRedirectUri: form.value.webServerRedirectUri || '',
      authorities: form.value.authorities || '',
      accessTokenValidity: form.value.accessTokenValidity,
      refreshTokenValidity: form.value.refreshTokenValidity,
      additionalInformation: form.value.additionalInformation || '',
      autoapprove: form.value.autoapprove,
    }
    createApp(app).then(() => {
      formRef.value.resetFields()
      props.onSubmit()
    }).catch(error => {
      console.error('Create app failed:', error)
      ElMessage.error(error.message || 'Create app failed')
    })
  } catch (error) {
    console.error('Form validation failed:', error)
  }
}
</script>

<style scoped></style>

