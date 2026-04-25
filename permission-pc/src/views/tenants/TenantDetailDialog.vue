<template>
  <el-dialog :model-value="visible" @update:model-value="(v) => { if (!v) onConfirm() }" @close="onConfirm" :title="t('DetailTitle')" align-center destroy-on-close :modal="false"
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

    <el-divider content-position="left">{{ t('TenantInitInfoTitle') }}</el-divider>
    <el-descriptions border>
      <el-descriptions-item :label="t('InitializedLabel')">
        <el-tag :type="tenantInfo.initialized ? 'success' : 'info'">
          {{ tenantInfo.initialized ? t('Yes') : t('No') }}
        </el-tag>
      </el-descriptions-item>
      <el-descriptions-item :label="t('OrganizationLabel')">
        {{ initInfo?.org?.name ? `${initInfo.org.name} (${initInfo.org.type || '-'})` : '-' }}
      </el-descriptions-item>
      <el-descriptions-item :label="t('UsernameLabel')">
        {{ initInfo?.user?.username || '-' }}
      </el-descriptions-item>
      <el-descriptions-item :label="t('RoleLabel')">
        {{ initInfo?.role?.name || '-' }}
      </el-descriptions-item>
      <el-descriptions-item :label="t('PasswordLabel')">
        <el-button
          type="primary"
          link
          :disabled="!initInfo?.user?.id"
          @click="handleResetDefaultUserPassword"
        >
          {{ t('AdminResetPasswordLabel') }}
        </el-button>
      </el-descriptions-item>
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
import { getTenantById, getTenantInitInfo } from '@/views/tenants/TenantApi'
import { adminResetUserPassword } from '@/views/users/UserApi'
import { ElMessage, ElMessageBox } from 'element-plus'

const { t } = useI18n()

const props = defineProps(['visible', 'row', 'onConfirm'])

const tenantInfo = ref({})
const initInfo = ref(null)

const fetchTenantData = () => {
  if (props.row && props.row.id) {
    getTenantById(props.row.id).then(response => {
      tenantInfo.value = response.tenant || response || {}
    }).catch(error => {
      console.error('Failed to get tenant information:', error)
      ElMessage.error(error.message || 'Failed to get tenant information')
    })
    getTenantInitInfo(props.row.id).then((res) => {
      initInfo.value = res || null
    }).catch(() => {
      initInfo.value = null
    })
  }
}

const handleResetDefaultUserPassword = () => {
  const userId = initInfo.value?.user?.id
  if (!userId) return
  ElMessageBox.prompt(t('AdminResetPasswordPrompt'), t('AdminResetPasswordTitle'), {
    confirmButtonText: t('ConfirmButtonText'),
    cancelButtonText: t('CancelButtonText'),
    inputType: 'password',
    inputPlaceholder: t('NewPasswordPlaceholder'),
    inputPattern: /^.{6,20}$/,
    inputErrorMessage: t('PasswordLengthError'),
  })
    .then(({ value }) => adminResetUserPassword(userId, value))
    .then(() => {
      ElMessage.success(t('AdminResetPasswordSuccess'))
    })
    .catch((err) => {
      // cancel => ignore
      if (err && err.message) {
        ElMessage.error(err.message)
      }
    })
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


