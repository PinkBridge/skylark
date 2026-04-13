<template>
  <el-dialog :model-value="visible" :title="t('DetailTitle')" align-center
    destroy-on-close :modal="false" modal-penetrable :show-close="false" width="800px">
    <el-descriptions border :column="2">
      <el-descriptions-item :label="t('IDLabel')">{{ info.id }}</el-descriptions-item>
      <el-descriptions-item :label="t('UsernameLabel')">{{ info.username || '-' }}</el-descriptions-item>
      <el-descriptions-item :label="t('HttpMethodLabel')">{{ info.httpMethod || '-' }}</el-descriptions-item>
      <el-descriptions-item :label="t('HttpStatusLabel')">{{ info.httpStatus ?? '-' }}</el-descriptions-item>
      <el-descriptions-item :label="t('RequestUriLabel')" :span="2">{{ info.requestUri || '-' }}</el-descriptions-item>
      <el-descriptions-item :label="t('DurationMsLabel')">{{ info.durationMs ?? '-' }}</el-descriptions-item>
      <el-descriptions-item :label="t('LoginIpLabel')">{{ info.clientIp || '-' }}</el-descriptions-item>
      <el-descriptions-item :label="t('CreatedAtLabel')" :span="2">{{ info.createdAt || '-' }}</el-descriptions-item>
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
import { getOperationLogById } from '@/views/operationlogs/OperationLogApi'
import { ElMessage } from 'element-plus'

const { t } = useI18n()

const props = defineProps(['visible', 'row', 'onConfirm'])

const info = ref({})

const fetchData = () => {
  if (props.row && props.row.id) {
    getOperationLogById(props.row.id).then(response => {
      info.value = response || {}
    }).catch(error => {
      console.error('Failed to get operation log:', error)
      ElMessage.error(error.message || 'Failed to get operation log')
    })
  } else {
    info.value = props.row || {}
  }
}

const onConfirm = () => {
  props.onConfirm()
}

watch(
  () => [props.visible, props.row?.id],
  ([visible, rowId]) => {
    if (visible) {
      if (rowId) {
        fetchData()
      } else {
        info.value = props.row || {}
      }
    }
  },
  { immediate: true }
)
</script>

<style scoped></style>
