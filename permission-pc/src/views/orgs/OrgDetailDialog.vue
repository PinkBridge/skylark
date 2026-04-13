<template>
  <el-dialog :model-value="visible" :title="t('DetailTitle')" align-center destroy-on-close :modal="false"
    modal-penetrable :show-close="false">
    <el-descriptions border>
      <el-descriptions-item :label="t('IDLabel')">{{ orgInfo.id }}</el-descriptions-item>
      <el-descriptions-item :label="t('NameLabel')">{{ orgInfo.name }}</el-descriptions-item>
      <el-descriptions-item :label="t('CodeLabel')">{{ orgInfo.code }}</el-descriptions-item>
      <el-descriptions-item :label="t('ParentOrganizationLabel')">
        {{ orgInfo.parentOrganization ? orgInfo.parentOrganization.name || orgInfo.parentOrganization : '-' }}
      </el-descriptions-item>
      <el-descriptions-item :label="t('TypeLabel')">
        {{ formatType(orgInfo.type) }}
      </el-descriptions-item>
      <el-descriptions-item :label="t('StatusLabel')">
        {{ orgInfo.status === 'ACTIVE' ? t('Active') : t('Disabled') }}
      </el-descriptions-item>
      <el-descriptions-item :label="t('CreatedAtLabel')">{{ orgInfo.createTime }}</el-descriptions-item>
      <el-descriptions-item :label="t('UpdatedAtLabel')">{{ orgInfo.updateTime }}</el-descriptions-item>
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
import { getOrgById } from '@/views/orgs/OrgApi'
import { ElMessage } from 'element-plus'

const { t } = useI18n()

const props = defineProps(['visible', 'row', 'onConfirm'])

const orgInfo = ref({})

const formatType = (type) => {
  if (!type) return '-'
  if (type === 'COMPANY') return t('OrgTypeCompany')
  if (type === 'DEPARTMENT') return t('OrgTypeDepartment')
  if (type === 'TEAM') return t('OrgTypeTeam')
  return type
}

const fetchOrgData = () => {
  if (props.row && props.row.id) {
    getOrgById(props.row.id).then(response => {
      orgInfo.value = response.org || response || {}
    }).catch(error => {
      console.error('Failed to get org information:', error)
      ElMessage.error(error.message || 'Failed to get org information')
    })
  }
}

const onConfirm = () => {
  props.onConfirm()
}

watch(
  () => [props.visible, props.row?.id],
  ([visible, id]) => {
    if (visible && id) {
      fetchOrgData()
    }
  },
  { immediate: true }
)
</script>

<style scoped></style>


