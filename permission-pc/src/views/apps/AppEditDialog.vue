<template>
  <el-dialog :model-value="visible" :title="t('EditTitle')" align-center destroy-on-close :modal="false"
    modal-penetrable :show-close="false" style="max-width: 800px">
    <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
      <el-form-item :label="t('ClientIdLabel')" prop="clientId">
        <el-input v-model="form.clientId" :disabled="true" />
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

<script setup>
import { ref, watch, computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { getAppByClientId, updateAppByClientId } from '@/views/apps/AppApi'
import { ElMessage } from 'element-plus'

const { t } = useI18n()

const props = defineProps(['visible', 'row', 'onSubmit', 'onCancel'])

const formRef = ref(null)
const form = ref({})

const rules = computed(() => {
  return {
    clientId: [
      { required: true, message: t('ClientIdRequired'), trigger: 'blur' }
    ],
  }
})

const fetchAppData = () => {
  if (props.row && props.row.clientId) {
    getAppByClientId(props.row.clientId).then(response => {
      const data = response.app || response || {}
      // Convert arrays/objects to strings for textarea inputs
      form.value = {
        ...data,
        resourceIds: Array.isArray(data.resourceIds) ? data.resourceIds.join(',') : (data.resourceIds || ''),
        scope: Array.isArray(data.scope) ? data.scope.join(',') : (data.scope || ''),
        authorizedGrantTypes: Array.isArray(data.authorizedGrantTypes) ? data.authorizedGrantTypes.join(',') : (data.authorizedGrantTypes || ''),
        authorities: Array.isArray(data.authorities) ? data.authorities.join(',') : (data.authorities || ''),
        additionalInformation: typeof data.additionalInformation === 'object' ? JSON.stringify(data.additionalInformation) : (data.additionalInformation || ''),
      }
    }).catch(error => {
      console.error('Failed to get app information:', error)
      ElMessage.error(error.message || 'Failed to get app information')
    })
  }
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
    updateAppByClientId(form.value.clientId, app).then(() => {
      props.onSubmit()
    }).catch(error => {
      console.error('Update app failed:', error)
      ElMessage.error(error.message || 'Update app failed')
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
  () => [props.visible, props.row?.clientId],
  ([visible, clientId]) => {
    if (visible && clientId) {
      fetchAppData()
    }
  },
  { immediate: true }
)
</script>

<style scoped></style>

