<template>
  <el-dialog :model-value="visible" :title="t('DetailTitle')" align-center
    destroy-on-close :modal="false" modal-penetrable :show-close="false">
    <el-descriptions border>
      <el-descriptions-item :label="t('IDLabel')">{{ whitelistInfo.id }}</el-descriptions-item>
      <el-descriptions-item :label="t('MethodLabel')">{{ whitelistInfo.method }}</el-descriptions-item>
      <el-descriptions-item :label="t('PathLabel')">{{ whitelistInfo.path }}</el-descriptions-item>
      <el-descriptions-item :label="t('EnableLabel')">
        {{ whitelistInfo.enabled == true ? t('Enable') : t('Disable') }}
      </el-descriptions-item>
      <el-descriptions-item :label="t('CreatedAtLabel')">{{ whitelistInfo.createTime }}</el-descriptions-item>
      <el-descriptions-item :label="t('UpdatedAtLabel')">{{ whitelistInfo.updateTime }}</el-descriptions-item>
      <el-descriptions-item :label="t('RemarkLabel')">{{ whitelistInfo.remark }}</el-descriptions-item>
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
import { getWhitelistById } from '@/views/whitelist/WhitelistApi'
import { ElMessage } from 'element-plus'

const { t } = useI18n()

const props = defineProps(['visible', 'row', 'onConfirm'])

const whitelistInfo = ref({})

const fetchWhitelistData = () => {
  if (props.row && props.row.id) {
    getWhitelistById(props.row.id).then(response => {
      whitelistInfo.value = response.whitelist || response || {}
    }).catch(error => {
      console.error('Failed to get whitelist information:', error)
      ElMessage.error(error.message || 'Failed to get whitelist information')
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
      fetchWhitelistData()
    }
  },
  { immediate: true }
)
</script>

<style scoped></style>

