<template>
  <el-dialog :model-value="visible" :title="t('DetailTitle')" align-center
    destroy-on-close :modal="false" modal-penetrable :show-close="false">
    <el-descriptions border>
      <el-descriptions-item :label="t('IDLabel')">{{ apiInfo.id }}</el-descriptions-item>
      <el-descriptions-item :label="t('MethodLabel')">{{ apiInfo.method }}</el-descriptions-item>
      <el-descriptions-item :label="t('PathLabel')">{{ apiInfo.path }}</el-descriptions-item>
      <el-descriptions-item :label="t('PermLabelLabel')">{{ apiInfo.permlabel }}</el-descriptions-item>
      <el-descriptions-item :label="t('ModuleKeyLabel')">{{ apiInfo.moduleKey }}</el-descriptions-item>
      <el-descriptions-item :label="t('CreatedAtLabel')">{{ apiInfo.createTime }}</el-descriptions-item>
      <el-descriptions-item :label="t('UpdatedAtLabel')">{{ apiInfo.updateTime }}</el-descriptions-item>
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
import { getApiById } from '@/views/apis/ApiApi'
import { ElMessage } from 'element-plus'

const { t } = useI18n()

const props = defineProps(['visible', 'row', 'onConfirm'])

const apiInfo = ref({})

const fetchApiData = () => {
  if (props.row && props.row.id) {
    getApiById(props.row.id).then(response => {
      apiInfo.value = response.api || response || {}
    }).catch(error => {
      console.error('Failed to get API information:', error)
      ElMessage.error(error.message || 'Failed to get API information')
    })
  }
}

const onConfirm = () => {
  props.onConfirm()
}

// Watch for changes in visible and row.id
watch(
  () => [props.visible, props.row?.id],
  ([visible, rowId]) => {
    if (visible && rowId) {
      fetchApiData()
    }
  },
  { immediate: true }
)
</script>

<style scoped></style>

