<template>
  <el-dialog :model-value="visible" :title="t('DetailTitle')" align-center destroy-on-close :modal="false"
    modal-penetrable :show-close="false">
    <el-descriptions border>
      <el-descriptions-item :label="t('LogoLabel')">
        <el-image v-if="tenantInfo.logo" style="width: 80px; height: 80px" :src="tenantInfo.logo" fit="contain" />
        <span v-else>-</span>
      </el-descriptions-item>
      <el-descriptions-item :label="t('NameLabel')">{{ tenantInfo.name }}</el-descriptions-item>
      <el-descriptions-item :label="t('CodeLabel')">{{ tenantInfo.code }}</el-descriptions-item>
      <el-descriptions-item :label="t('SystemNameLabel')">{{ tenantInfo.systemName || '-' }}</el-descriptions-item>
      <el-descriptions-item :label="t('DomainLabel')">{{ tenantInfo.domain || '-' }}</el-descriptions-item>
      <el-descriptions-item :label="t('StatusLabel')">
        {{ tenantInfo.status === 'ACTIVE' ? t('Active') : t('Disabled') }}
      </el-descriptions-item>
      <el-descriptions-item :label="t('ContactNameLabel')">{{ tenantInfo.contactName }}</el-descriptions-item>
      <el-descriptions-item :label="t('ContactPhoneLabel')">{{ tenantInfo.contactPhone }}</el-descriptions-item>
      <el-descriptions-item :label="t('ContactEmailLabel')">{{ tenantInfo.contactEmail }}</el-descriptions-item>
      <el-descriptions-item :label="t('ExpireTimeLabel')">{{ tenantInfo.expireTime || '-' }}</el-descriptions-item>
      <el-descriptions-item :label="t('CreatedAtLabel')">{{ tenantInfo.createTime }}</el-descriptions-item>
      <el-descriptions-item :label="t('UpdatedAtLabel')">{{ tenantInfo.updateTime }}</el-descriptions-item>
      <el-descriptions-item :label="t('AddressLabel')" :span="2">{{ tenantInfo.address || '-' }}</el-descriptions-item>
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
import { getTenantById } from '@/views/tenants/TenantApi'
import { ElMessage } from 'element-plus'

const { t } = useI18n()

const props = defineProps(['visible', 'row', 'onConfirm'])

const tenantInfo = ref({})

const fetchTenantData = () => {
  if (props.row && props.row.id) {
    getTenantById(props.row.id).then(response => {
      tenantInfo.value = response.tenant || response || {}
    }).catch(error => {
      console.error('Failed to get tenant information:', error)
      ElMessage.error(error.message || 'Failed to get tenant information')
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
      fetchTenantData()
    }
  },
  { immediate: true }
)
</script>

<style scoped></style>


