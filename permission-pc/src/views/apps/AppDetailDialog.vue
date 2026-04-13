<template>
  <el-dialog :model-value="visible" :title="t('DetailTitle')" align-center destroy-on-close :modal="false"
    modal-penetrable :show-close="false">
    <el-descriptions border>
      <el-descriptions-item :label="t('ClientIdLabel')">{{ appInfo.clientId }}</el-descriptions-item>
      <el-descriptions-item :label="t('AutoapproveLabel')">
        {{ appInfo.autoapprove == true ? t('Yes') : t('No') }}
      </el-descriptions-item>
      <el-descriptions-item :label="t('ResourceIdsLabel')">{{ formatArray(appInfo.resourceIds) }}</el-descriptions-item>
      <el-descriptions-item :label="t('ScopeLabel')">{{ formatArray(appInfo.scope) }}</el-descriptions-item>
      <el-descriptions-item :label="t('AuthorizedGrantTypesLabel')">{{ formatArray(appInfo.authorizedGrantTypes) }}</el-descriptions-item>
      <el-descriptions-item :label="t('WebServerRedirectUriLabel')">{{ appInfo.webServerRedirectUri || '-' }}</el-descriptions-item>
      <el-descriptions-item :label="t('AuthoritiesLabel')">{{ formatArray(appInfo.authorities) }}</el-descriptions-item>
      <el-descriptions-item :label="t('AccessTokenValidityLabel')">{{ appInfo.accessTokenValidity || '-' }}</el-descriptions-item>
      <el-descriptions-item :label="t('RefreshTokenValidityLabel')">{{ appInfo.refreshTokenValidity || '-' }}</el-descriptions-item>
      <el-descriptions-item :label="t('AdditionalInformationLabel')">{{ formatObject(appInfo.additionalInformation) }}</el-descriptions-item>
    </el-descriptions>
    <template #footer>
      <div class="dialog-footer">
        <el-button type="primary" @click="onConfirm">{{ t('ConfirmButtonText') }}</el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { getAppByClientId } from '@/views/apps/AppApi'
import { ElMessage } from 'element-plus'

const { t } = useI18n()

const props = defineProps(['visible', 'row', 'onConfirm'])

const appInfo = ref({})

const formatArray = (value) => {
  if (!value) return '-'
  if (Array.isArray(value)) {
    return value.join(', ')
  }
  if (typeof value === 'string') {
    return value
  }
  return '-'
}

const formatObject = (value) => {
  if (!value) return '-'
  if (typeof value === 'object') {
    return JSON.stringify(value)
  }
  return value
}

const fetchAppData = () => {
  if (props.row && props.row.clientId) {
    getAppByClientId(props.row.clientId).then(response => {
      appInfo.value = response.app || response || {}
    }).catch(error => {
      console.error('Failed to get app information:', error)
      ElMessage.error(error.message || 'Failed to get app information')
    })
  }
}

const onConfirm = () => {
  props.onConfirm()
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

