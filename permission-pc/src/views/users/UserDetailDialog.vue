<template>
  <el-dialog :model-value="visible" :title="t('DetailTitle')" align-center
    destroy-on-close :modal="false" modal-penetrable :show-close="false">
    <el-descriptions border>
      <el-descriptions-item :rowspan="2" :label="$t('UserPhotoLabel')">
        <el-image style="width: 100px; height: 100px" :src="userInfo.avatar" />
      </el-descriptions-item>
      <el-descriptions-item :label="t('UsernameLabel')">{{ userInfo.username }}</el-descriptions-item>
      <el-descriptions-item :label="t('OrganizationLabel')">{{ userInfo.organization ? (userInfo.organization.name || userInfo.organization) : '-' }}</el-descriptions-item>
      <el-descriptions-item :label="t('TelephoneLabel')">{{ userInfo.phone }}</el-descriptions-item>
      <el-descriptions-item :label="t('EmailLabel')">{{ userInfo.email }}</el-descriptions-item>
      <el-descriptions-item :label="t('GenderLabel')">{{ userInfo.gender == 'F' ? t('Boy') :
        t('Girl')}}</el-descriptions-item>
      <el-descriptions-item :label="t('StatusLabel')">{{ userInfo.status == 'active' ? t('Active') :
        t('Disabled')}}</el-descriptions-item>
      <el-descriptions-item :label="t('RoleLabel')">{{roles.map(role => role.name).join(',')}}</el-descriptions-item>
      <el-descriptions-item :label="t('CreatedAtLabel')">{{ userInfo.createTime }}</el-descriptions-item>
      <el-descriptions-item :label="t('PlaceLabel')">{{ userInfo.province + userInfo.city }}</el-descriptions-item>
      <el-descriptions-item :label="t('AddressLabel')">{{ userInfo.address }}</el-descriptions-item>
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
import { getUserById } from '@/views/users/UserApi'
import { ElMessage } from 'element-plus'

const { t } = useI18n()

const props = defineProps(['visible', 'row', 'onConfirm'])

const userInfo = ref({})
const roles = ref([])

const fetchUserData = () => {
  if (props.row && props.row.id) {
    getUserById(props.row.id).then(response => {
      userInfo.value = response.user || {}
      roles.value = response.roles || []
    }).catch(error => {
      console.error('Failed to get user information:', error)
      ElMessage.error(error.message || 'Failed to get user information')
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
      fetchUserData()
    }
  },
  { immediate: true }
)
</script>

<style scoped></style>