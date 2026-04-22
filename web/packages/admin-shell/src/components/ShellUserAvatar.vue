<template>
  <div class="user-avatar">
    <el-dropdown>
      <span class="el-dropdown-link">
        <img :src="headerAvatarUrl" alt="User Avatar" class="user-avatar-img" />
        {{ my.username }}
      </span>
      <template #dropdown>
        <el-dropdown-menu>
          <el-dropdown-item :icon="User" @click="userInfoDialogVisible = true">{{
            t('UserInfoButtonText')
          }}</el-dropdown-item>
          <el-dropdown-item :icon="Lock" @click="changePasswordDialogVisible = true">{{
            t('ResetPasswordButtonText')
          }}</el-dropdown-item>
          <el-dropdown-item :icon="UserFilled" @click="logoutDialogVisible = true">{{
            t('LogoutButtonText')
          }}</el-dropdown-item>
        </el-dropdown-menu>
      </template>
    </el-dropdown>
    <el-dialog
      v-model="userInfoDialogVisible"
      :title="t('UserInfoButtonText')"
      align-center
      destroy-on-close
    >
      <ShellUserInfo @updated="handleProfileUpdated" />
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="userInfoDialogVisible = false">{{
            t('ConfirmButtonText')
          }}</el-button>
        </div>
      </template>
    </el-dialog>

    <el-dialog v-model="logoutDialogVisible" :title="t('NoticeTitle')" width="500px" align-center>
      <span>{{ t('ConfirmLogoutText') }}</span>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="logoutDialogVisible = false">{{ t('CancelButtonText') }}</el-button>
          <el-button type="primary" @click="logout">{{ t('ConfirmButtonText') }}</el-button>
        </div>
      </template>
    </el-dialog>

    <el-dialog
      v-model="changePasswordDialogVisible"
      :title="t('ChangePasswordTitle')"
      destroy-on-close
      align-center
      width="500px"
    >
      <el-form
        ref="changePasswordFormRef"
        :model="changePasswordForm"
        :rules="changePasswordRules"
        label-position="top"
      >
        <el-form-item :label="t('OldPasswordLabel')" prop="oldPassword">
          <el-input
            v-model="changePasswordForm.oldPassword"
            type="password"
            :placeholder="t('OldPasswordPlaceholder')"
          />
        </el-form-item>
        <el-form-item :label="t('NewPasswordLabel')" prop="newPassword">
          <el-input
            v-model="changePasswordForm.newPassword"
            type="password"
            :placeholder="t('NewPasswordPlaceholder')"
          />
        </el-form-item>
        <el-form-item :label="t('ConfirmPasswordLabel')" prop="confirmPassword">
          <el-input
            v-model="changePasswordForm.confirmPassword"
            type="password"
            :placeholder="t('ConfirmPasswordPlaceholder')"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="handleCancelChangePassword">{{ t('CancelButtonText') }}</el-button>
          <el-button type="primary" @click="changePassword">{{ t('ConfirmButtonText') }}</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, inject } from 'vue'
import { User, Lock, UserFilled } from '@element-plus/icons-vue'
import { useI18n } from 'vue-i18n'
import { ElMessage } from 'element-plus'
import { ADMIN_SHELL_CONFIG } from '../symbols'
import ShellUserInfo from './ShellUserInfo.vue'

const DEFAULT_AVATAR = 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png'

const { t } = useI18n()
const shell = inject(ADMIN_SHELL_CONFIG)

const my = ref({
  avatar: DEFAULT_AVATAR,
  username: '—'
})
const userInfoDialogVisible = ref(false)
const logoutDialogVisible = ref(false)
const changePasswordDialogVisible = ref(false)
const changePasswordFormRef = ref(null)
const changePasswordForm = ref({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const changePasswordRules = computed(() => {
  const validateConfirmPassword = (_rule, value, callback) => {
    if (value !== changePasswordForm.value.newPassword) {
      callback(new Error(t('ConfirmPasswordNotMatch')))
    } else {
      callback()
    }
  }
  return {
    oldPassword: [{ required: true, message: t('OldPasswordPlaceholder'), trigger: 'blur' }],
    newPassword: [
      { required: true, message: t('NewPasswordPlaceholder'), trigger: 'blur' },
      { min: 6, max: 20, message: t('PasswordLengthError'), trigger: 'blur' }
    ],
    confirmPassword: [
      { required: true, message: t('ConfirmPasswordPlaceholder'), trigger: 'blur' },
      { validator: validateConfirmPassword, trigger: 'blur' }
    ]
  }
})

const handleCancelChangePassword = () => {
  changePasswordDialogVisible.value = false
  if (changePasswordFormRef.value) {
    changePasswordFormRef.value.resetFields()
  }
  changePasswordForm.value = {
    oldPassword: '',
    newPassword: '',
    confirmPassword: ''
  }
}

const changePassword = async () => {
  if (!changePasswordFormRef.value) return
  try {
    await changePasswordFormRef.value.validate()
    await shell.changePassword(changePasswordForm.value)
    changePasswordDialogVisible.value = false
    ElMessage.success(t('ResetPasswordSuccess'))
    changePasswordFormRef.value.resetFields()
    changePasswordForm.value = {
      oldPassword: '',
      newPassword: '',
      confirmPassword: ''
    }
  } catch (error) {
    console.warn(t('FormValidationFailed'), error)
  }
}

const welcomeName = shell.routeNames?.welcome || 'Welcome'

const logout = async () => {
  logoutDialogVisible.value = false
  const token = shell.getAccessToken()
  try {
    if (token) {
      await shell.logout(token)
    }
  } catch (error) {
    console.error(t('LogoutFailed'), error)
  } finally {
    shell.clearTokens()
    shell.router.push({ name: welcomeName })
  }
}

const headerAvatarUrl = computed(() => {
  const a = my.value?.avatar
  if (!a || typeof a !== 'string' || !a.trim()) {
    return DEFAULT_AVATAR
  }
  return a
})

const reloadHeaderUser = async () => {
  try {
    const data = await shell.getMyInfo()
    if (data?.user) {
      my.value = data.user
    }
  } catch (error) {
    console.error('Failed to get user information:', error)
  }
}

const handleProfileUpdated = () => {
  reloadHeaderUser()
}

onMounted(() => {
  reloadHeaderUser()
})
</script>

<style scoped>
.user-avatar {
  display: flex;
  align-items: center;
  justify-content: center;
}

.user-avatar-img {
  width: 26px;
  height: 26px;
  border-radius: 50%;
  object-fit: cover;
  border: 1px solid rgba(0, 0, 0, 0.08);
  margin-right: 8px;
}

.el-dropdown-link {
  display: flex;
  align-items: center;
  cursor: pointer;
  border: none;
  color: rgba(0, 0, 0, 0.85);
}
</style>
