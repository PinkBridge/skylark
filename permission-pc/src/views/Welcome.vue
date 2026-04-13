<template>
  <div class="index-container">
    <div class="index-content">
      <div class="logo-section">
        <div class="logo-placeholder">
          <img v-if="tenant && tenant.logo" :src="tenant.logo" alt="Logo"
            style="width: 120px; height: 120px; border-radius: 50%;" />
        </div>
        <h1 class="app-title">{{tenant.systemName }}</h1>
        <el-tag v-if="
          !isTenantAvailable" type="error">
          {{ t('TenantNotAvailable') }}</el-tag>
      </div>

      <div class="action-section" v-if="isTenantAvailable">
        <el-button type="primary" size="large" class="action-button" @click="handleLogin">
          Login
        </el-button>
        <el-button type="default" size="large" class="action-button" @click="handleRegister">
          Register
        </el-button>
      </div>
      <div class="footer-section">
        <p>© 2025 {{ tenant.name }}. All rights reserved.</p>
        <p>Powered by {{ tenant.name }}</p>
        <p>Email: <a href="mailto:{{ tenant.contactEmail }}" target="_blank">{{ tenant.contactEmail }}</a></p>
        <p>Phone: <a href="tel:{{ tenant.contactPhone }}" target="_blank">{{ tenant.contactPhone }}</a></p>
        <p>Address: {{ tenant.address }}</p>
      </div>
    </div>
  </div>
</template>

<script>
import { ElMessage } from 'element-plus'
import { Avatar } from '@element-plus/icons-vue'
import { getAuthorizationUrl } from '@/api/oauth'
import { ref, onMounted, computed } from 'vue'
import { getTenantForCurrentLocation } from '@/utils/tenant'
import { useI18n } from 'vue-i18n'

export default {
  name: 'WelcomePage',
  setup() {
    const { t } = useI18n()
    const tenant = ref({
      logo: '@/assets/logo.png',
      systemName: 'Skylark',
      name: 'Skylark',
      code: '',
      status: '',
      expireTime: '',
      domain: 'skylark.com',
      address: '123 Main St, Anytown, USA',
      contactEmail: 'support@skylark.com',
      contactPhone: '+1234567890'
    })

    const handleLogin = () => {
      const authUrl = getAuthorizationUrl()
      window.location.href = authUrl
    }

    const handleRegister = () => {
      ElMessage.info('Registration function under development...')
    }


    const parseExpireTime = (expireTime) => {
      if (!expireTime) {
        return null
      }
      if (typeof expireTime === 'number') {
        return Number.isNaN(expireTime) ? null : expireTime
      }
      if (typeof expireTime === 'string') {
        const trimmed = expireTime.trim()
        if (!trimmed) {
          return null
        }
        // Support both "yyyy-MM-dd HH:mm:ss" and ISO-like strings.
        const normalized = trimmed.includes('T') ? trimmed : trimmed.replace(' ', 'T')
        const time = new Date(normalized).getTime()
        return Number.isNaN(time) ? null : time
      }
      return null
    }

    const isTenantAvailable = computed(() => {
      if (!tenant.value || tenant.value.status !== 'ACTIVE') {
        return false
      }
      const expireTimeMs = parseExpireTime(tenant.value.expireTime)
      // No expire time means no expiration limit.
      if (expireTimeMs === null) {
        return true
      }
      return expireTimeMs > Date.now()
    })


    onMounted(async () => {
      const tenantData = await getTenantForCurrentLocation()
      if (tenantData) {
        tenant.value = tenantData
      }
    })

    return {
      handleLogin,
      handleRegister,
      Avatar,
      tenant,
      t,
      isTenantAvailable
    }
  }
}
</script>

<style scoped>
.index-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #667eea 100%);
  padding: 20px;
}

.index-content {
  text-align: center;
  background: rgba(255, 255, 255, 0.95);
  border-radius: 20px;
  padding: 60px 40px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
  max-width: 500px;
  width: 100%;
}

.logo-section {
  margin-bottom: 40px;
  position: relative;
  padding: 30px;
  border-radius: 20px;
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
}

.logo {
  width: 120px;
  height: 120px;
  margin-bottom: 20px;
  object-fit: contain;
  filter: drop-shadow(0 4px 8px rgba(0, 0, 0, 0.1));
}

.logo-placeholder {
  width: 120px;
  height: 120px;
  margin: 0 auto 20px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.app-title {
  font-size: 32px;
  font-weight: 600;
  color: #333;
  margin: 0 0 10px 0;
}

.app-subtitle {
  font-size: 16px;
  color: #666;
  margin: 0;
}

.action-section {
  display: flex;
  flex-direction: column;
  gap: 15px;
  margin-top: 40px;
}

.action-button {
  width: 100%;
  height: 50px;
  font-size: 16px;
  font-weight: 500;
  transition: all 0.3s ease;
  box-sizing: border-box;
  margin: 0;
}

.action-button:first-child {
  /* background: linear-gradient(135deg, #667eea 0%, #e6e4e9 100%); */
  border: none;
}

.action-button:first-child:hover {
  opacity: 0.9;
  transform: translateY(-2px);
  box-shadow: 0 5px 15px rgba(102, 126, 234, 0.4);
}

.action-button:last-child:hover {
  transform: translateY(-2px);
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.1);
}

/* 确保按钮内部元素对齐 */
.action-button :deep(.el-button__inner) {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  height: 100%;
}

.footer-section {
  font-size: 14px;
  margin-top: 20px;
}

.footer-section p {
  padding: 3px 0;
}
</style>