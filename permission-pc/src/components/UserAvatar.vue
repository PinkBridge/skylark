<template>
  <div class="user-avatar">
    <el-dropdown>
      <span class="el-dropdown-link">
        <img :src="headerAvatarUrl" alt="User Avatar" class="user-avatar-img" />
        {{ my.username }}
      </span>
      <template #dropdown>
        <el-dropdown-menu>
          <el-dropdown-item :icon="User" @click="userInfoDialogVisible = true">{{ $t('UserInfoButtonText') }}</el-dropdown-item>
          <el-dropdown-item :icon="Lock" @click="changePasswordDialogVisible = true">{{ $t('ResetPasswordButtonText') }}</el-dropdown-item>
          <el-dropdown-item :icon="UserFilled" @click="logoutDialogVisible = true">{{ $t('LogoutButtonText') }}</el-dropdown-item>
        </el-dropdown-menu>
      </template>
    </el-dropdown>
    <el-dialog v-model="userInfoDialogVisible" :title="$t('UserInfoButtonText')" align-center destroy-on-close>
      <UserInfoComponent />
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="userInfoDialogVisible = false">{{ $t('ConfirmButtonText') }}</el-button>
        </div>
      </template>
    </el-dialog>

    <el-dialog v-model="logoutDialogVisible" :title="$t('NoticeTitle')" width="500px" align-center>
      <span>{{ $t('ConfirmLogoutText') }}</span>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="logoutDialogVisible = false">{{ $t('CancelButtonText') }}</el-button>
          <el-button type="primary" @click="logout">
            {{ $t('ConfirmButtonText') }}
          </el-button>
        </div>
      </template>
    </el-dialog>

    <el-dialog v-model="changePasswordDialogVisible" :title="$t('ChangePasswordTitle')" destroy-on-close align-center width="500px">
      <el-form ref="changePasswordFormRef" :model="changePasswordForm" :rules="changePasswordRules" label-position="top">
        <el-form-item :label="$t('OldPasswordLabel')" prop="oldPassword">
          <el-input v-model="changePasswordForm.oldPassword" type="password" :placeholder="$t('OldPasswordPlaceholder')" />
        </el-form-item>
        <el-form-item :label="$t('NewPasswordLabel')" prop="newPassword">
          <el-input v-model="changePasswordForm.newPassword" type="password" :placeholder="$t('NewPasswordPlaceholder')" />
        </el-form-item>
        <el-form-item :label="$t('ConfirmPasswordLabel')" prop="confirmPassword">
          <el-input v-model="changePasswordForm.confirmPassword" type="password" :placeholder="$t('ConfirmPasswordPlaceholder')" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="handleCancelChangePassword">{{ $t('CancelButtonText') }}</el-button>
          <el-button type="primary" @click="changePassword">{{ $t('ConfirmButtonText') }}</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { ref, computed, onMounted } from 'vue'
import { getUserInfo } from '../utils/auth'
import {
  User,
  Lock,
  UserFilled
} from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'
import { clearTokens, getAccessToken } from '@/utils/auth'
import { ElMessage } from 'element-plus'
import UserInfoComponent from './UserInfo.vue'
import { useI18n } from 'vue-i18n'
import { changePassword as changePasswordApi } from '@/api/me'
import { getMyInfo as getMyInfoApi } from '@/api/me'
import { logout as oauthLogout } from '@/api/oauth'

const DEFAULT_AVATAR = 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png'

export default {
  name: 'UserAvatarComponent',
  components: {
    UserInfoComponent
  },
  setup() {
    const { t } = useI18n()
    const userInfo = ref(getUserInfo() || {})
    const my = ref({
      avatar: DEFAULT_AVATAR,
      username: 'Username'
    })
    const userInfoDialogVisible = ref(false)
    const logoutDialogVisible = ref(false)
    const router = useRouter()
    const changePasswordDialogVisible = ref(false)
    const changePasswordFormRef = ref(null)
    const changePasswordForm = ref({
      oldPassword: '',
      newPassword: '',
      confirmPassword: ''
    })

    const changePasswordRules = computed(() => {

      const validateConfirmPassword = (rule, value, callback) => {
        if (value !== changePasswordForm.value.newPassword) {
          callback(new Error(t('ConfirmPasswordNotMatch')))
        } else {
          callback()
        }
      }

      return {
        oldPassword: [
          { required: true, message: t('OldPasswordPlaceholder'), trigger: 'blur' }
        ],
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
      console.log(changePasswordForm.value)
      try {
        await changePasswordFormRef.value.validate()
        const data = await changePasswordApi(changePasswordForm.value)
        console.log(data)
        changePasswordDialogVisible.value = false
        ElMessage.success(t('ResetPasswordSuccess'))
        changePasswordFormRef.value.resetFields()
        changePasswordForm.value = {
          oldPassword: '',
          newPassword: '',
          confirmPassword: ''
        }
      } catch (error) {
        console.log(t('FormValidationFailed'), error)
      }
    }

    const logout = async () => {
      logoutDialogVisible.value = false
      const token = getAccessToken()
      try {
        if (token) {
          await oauthLogout(token)
        }
      } catch (error) {
        // Still proceed with local cleanup even if server logout fails.
        console.error(t('LogoutFailed'), error)
      } finally {
        clearTokens()
        router.push('/welcome')
      }
    }

    const avatarUrl = computed(() => {
      const avatar = userInfo.value?.avatar || userInfo.value?.avatarUrl
      if (!avatar || typeof avatar !== 'string' || !avatar.trim()) {
        return DEFAULT_AVATAR
      }
      return avatar
    })

    const headerAvatarUrl = computed(() => {
      const a = my.value?.avatar
      if (!a || typeof a !== 'string' || !a.trim()) {
        return DEFAULT_AVATAR
      }
      return a
    })

    const reloadHeaderUser = async () => {
      try {
        const data = await getMyInfoApi()
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

    return {
      userInfo,
      avatarUrl,
      User,
      Lock,
      UserFilled,
      logoutDialogVisible,
      logout,
      changePasswordDialogVisible,
      changePasswordFormRef,
      changePasswordForm,
      changePasswordRules,
      handleCancelChangePassword,
      changePassword,
      userInfoDialogVisible,
      my,
      headerAvatarUrl,
      handleProfileUpdated
    }
  }
}
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
  border: 1px solid rgba(255, 255, 255, 0.35);
  margin-right: 8px;
}

.el-dropdown-link {
  display: flex;
  align-items: center;
  cursor: pointer;
  border: none;
}
</style>