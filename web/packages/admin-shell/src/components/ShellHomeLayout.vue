<template>
  <div class="common-layout" v-loading="loading">
    <el-container>
      <el-header class="custom-header">
        <ShellHeader />
      </el-header>
      <el-container>
        <el-aside class="custom-aside">
          <ShellSidebarMenu />
        </el-aside>
        <el-container>
          <el-main class="custom-main">
            <router-view />
          </el-main>
        </el-container>
      </el-container>
    </el-container>
  </div>
</template>

<script setup>
import { ref, onMounted, inject } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ADMIN_SHELL_CONFIG } from '../symbols'
import { resolvePostLoginRouteName } from '@skylark/login-redirect-client'
import ShellHeader from './ShellHeader.vue'
import ShellSidebarMenu from './ShellSidebarMenu.vue'

const shell = inject(ADMIN_SHELL_CONFIG)
const router = useRouter()
const route = useRoute()
const loading = ref(false)

const welcomeName = shell.routeNames?.welcome || 'Welcome'
const homeName = shell.routeNames?.home || 'Home'
const postLoginFallbackName = shell.routeNames?.home || 'Home'

const persistTokens = (tokenData, remember = true) => {
  if (typeof shell.persistTokens === 'function') {
    shell.persistTokens(tokenData, remember)
  }
}

/**
 * Align skylark.tenant with the current browser host before any API that sends
 * X-Tenant-Id. Stale tenant (e.g. another tab, old domain) causes /users/me to
 * return「用户不存在」when TenantInterceptor filters sys_user by tenant_id.
 */
const resolveTenantForCurrentHost = async () => {
  if (shell && typeof shell.getTenantForCurrentLocation === 'function') {
    await shell.getTenantForCurrentLocation()
  }
}

const handleOAuthCallback = async () => {
  const code = route.query.code
  const error = route.query.error

  if (error) {
    ElMessage.error('Authorization failed: ' + (route.query.error_description || error))
    router.replace({ name: welcomeName })
    return
  }

  if (!code) {
    await loadStoredTokenInfo()
    return
  }

  try {
    loading.value = true
    await resolveTenantForCurrentHost()
    const tokenData = await shell.exchangeCodeForToken(code)
    if (!tokenData?.access_token) {
      throw new Error('Access token not obtained')
    }
    const remember = true
    persistTokens(tokenData, remember)
    const tokenCheckData = await shell.checkToken(tokenData.access_token)
    const mergedTokenInfo = {
      ...tokenData,
      ...tokenCheckData
    }
    shell.saveUserInfo(mergedTokenInfo, remember)
    const preferred = Array.isArray(shell.postLoginRouteNames) ? shell.postLoginRouteNames : undefined
    const targetName = resolvePostLoginRouteName(router, {
      preferred,
      fallback: postLoginFallbackName
    })
    router.replace({ name: targetName })
  } catch (err) {
    console.error('OAuth callback error:', err)
    ElMessage.error('Login failed: ' + (err.message || 'Unknown error'))
    router.replace({ name: welcomeName })
  } finally {
    loading.value = false
  }
}

const loadStoredTokenInfo = async () => {
  await resolveTenantForCurrentHost()

  const stored = shell.getUserInfo()
  if (stored) {
    return
  }

  if (shell.isAuthenticated()) {
    try {
      loading.value = true
      const token = shell.getAccessToken()
      if (token) {
        const tokenCheckData = await shell.checkToken(token)
        const mergedTokenInfo = {
          access_token: token,
          ...tokenCheckData
        }
        shell.saveUserInfo(mergedTokenInfo, true)
      }
    } catch (err) {
      console.error('Load token info error:', err)
      ElMessage.warning('Failed to get token information, please login again')
      shell.clearTokens()
      router.push({ name: welcomeName })
    } finally {
      loading.value = false
    }
  } else {
    router.push({ name: welcomeName })
  }
}

onMounted(() => {
  handleOAuthCallback()
})
</script>

<style scoped>
:global(:root) {
  --header-height: 55px;
  --aside-width: 240px;
  --main-width: calc(100vw - var(--aside-width));
}

.custom-header {
  margin: 0;
  height: var(--header-height);
  line-height: var(--header-height);
  width: 100vw;
  padding: 0;
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
}

.custom-aside {
  height: calc(100vh - var(--header-height));
  width: var(--aside-width);
  background-color: #001529;
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
}

.custom-main {
  height: calc(100vh - var(--header-height));
  width: var(--main-width);
  color: rgba(0, 0, 0, 0.65);
  background: #f0f2f5;
}
</style>
