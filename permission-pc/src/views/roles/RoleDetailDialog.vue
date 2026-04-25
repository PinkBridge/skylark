<template>
  <el-dialog :model-value="visible" @update:model-value="(v) => { if (!v) onConfirm() }" @close="onConfirm" :title="t('DetailTitle')" align-center destroy-on-close :modal="false"
    modal-penetrable :show-close="false">
    <el-descriptions border>
      <el-descriptions-item :label="t('NameLabel')">{{ roleInfo.name }}</el-descriptions-item>
      <el-descriptions-item :label="t('TenantNameLabel')">{{ roleInfo.tenantName || '-' }}</el-descriptions-item>
      <el-descriptions-item :label="t('RemarkLabel')">{{ roleInfo.remark }}</el-descriptions-item>
      <el-descriptions-item :label="t('CreatedAtLabel')">{{ roleInfo.createTime }}</el-descriptions-item>
      <el-descriptions-item :label="t('UpdatedAtLabel')">{{ roleInfo.updateTime }}</el-descriptions-item>
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
import { getRoleById } from '@/views/roles/RoleApi'
import { ElMessage } from 'element-plus'

const { t } = useI18n()

const props = defineProps(['visible', 'row', 'onConfirm'])

const roleInfo = ref({})

const fetchRoleData = () => {
  if (props.row && props.row.id) {
    getRoleById(props.row.id).then(response => {
      roleInfo.value = response.role || response || {}
    }).catch(error => {
      console.error('Failed to get role information:', error)
      ElMessage.error(error.message || 'Failed to get role information')
    })
  }
}

const onConfirm = () => {
  props.onConfirm()
}

watch(
  () => [props.visible, props.row?.id],
  ([visible, rowId]) => {
    if (visible && rowId) {
      fetchRoleData()
    }
  },
  { immediate: true }
)
</script>

<style scoped></style>

