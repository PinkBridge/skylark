<template>
  <el-dialog :model-value="visible" :title="t('DetailTitle')" align-center
    destroy-on-close :modal="false" modal-penetrable :show-close="false" width="800px">
    <el-descriptions border :column="2">
      <el-descriptions-item :label="t('IDLabel')">{{ loginLogInfo.id }}</el-descriptions-item>
      <el-descriptions-item :label="t('UsernameLabel')">{{ loginLogInfo.username || '-' }}</el-descriptions-item>
      <el-descriptions-item :label="t('LoginIpLabel')">{{ loginLogInfo.loginIp || '-' }}</el-descriptions-item>
      <el-descriptions-item :label="t('LoginLocationLabel')">{{ loginLogInfo.loginLocation || '-' }}</el-descriptions-item>
      <el-descriptions-item :label="t('LoginStatusLabel')">
        <el-tag :type="loginLogInfo.loginStatus === 'SUCCESS' ? 'success' : 'danger'">
          {{ loginLogInfo.loginStatus === 'SUCCESS' ? t('Success') : t('Failure') }}
        </el-tag>
      </el-descriptions-item>
      <el-descriptions-item :label="t('LoginTimeLabel')">{{ loginLogInfo.loginTime || '-' }}</el-descriptions-item>
      <el-descriptions-item :label="t('UserAgentLabel')" :span="2">{{ loginLogInfo.userAgent || '-' }}</el-descriptions-item>
      <el-descriptions-item :label="t('ClientIdLabel')">{{ loginLogInfo.clientId || '-' }}</el-descriptions-item>
      <el-descriptions-item :label="t('CreatedAtLabel')">{{ loginLogInfo.createTime || '-' }}</el-descriptions-item>
      <el-descriptions-item :label="t('UpdatedAtLabel')">{{ loginLogInfo.updateTime || '-' }}</el-descriptions-item>
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
import { getLoginLogById } from '@/views/loginlogs/LoginLogApi'
import { ElMessage } from 'element-plus'

const { t } = useI18n()

const props = defineProps(['visible', 'row', 'onConfirm'])

const loginLogInfo = ref({})

const fetchLoginLogData = () => {
  if (props.row && props.row.id) {
    getLoginLogById(props.row.id).then(response => {
      loginLogInfo.value = response || {}
    }).catch(error => {
      console.error('Failed to get login log information:', error)
      ElMessage.error(error.message || 'Failed to get login log information')
    })
  } else {
    // If row data is already available, use it directly
    loginLogInfo.value = props.row || {}
  }
}

const onConfirm = () => {
  props.onConfirm()
}

// Watch for changes in visible and row.id
watch(
  () => [props.visible, props.row?.id],
  ([visible, rowId]) => {
    if (visible) {
      if (rowId) {
        fetchLoginLogData()
      } else {
        loginLogInfo.value = props.row || {}
      }
    }
  },
  { immediate: true }
)
</script>

<style scoped></style>

