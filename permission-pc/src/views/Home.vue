<template>
  <div class="common-layout">
    <el-container>
      <el-header class="custom-header">
        <HeaderComponent />
      </el-header>
      <el-container>
        <el-aside class="custom-aside">
          <MenuComponent />
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


<script>
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { exchangeCodeForToken, checkToken } from '@/api/oauth'
import { saveTokens, saveUserInfo, clearTokens, 
  getUserInfo as getStoredTokenInfo, isAuthenticated, getAccessToken } from '../utils/auth'
import MenuComponent from '@/components/Menu.vue'
import HeaderComponent from '@/components/Header.vue'
export default {
  name: 'HomePage',
  components: {
    MenuComponent,
    HeaderComponent
  },
  setup() {
    const router = useRouter()
    const route = useRoute()
    const loading = ref(false)
    const tokenInfo = ref({})

    const handleOAuthCallback = async () => {
      const code = route.query.code
      const error = route.query.error

      if (error) {
        ElMessage.error('Authorization failed: ' + (route.query.error_description || error))
        router.replace({ name: 'Welcome' })
        return
      }

      if (!code) {
        loadStoredTokenInfo()
        return
      }

      try {
        loading.value = true
        const tokenData = await exchangeCodeForToken(code)

        if (!tokenData.access_token) {
          throw new Error('Access token not obtained')
        }
        const remember = true
        saveTokens(
          tokenData.access_token,
          tokenData.refresh_token,
          remember
        )
        const tokenCheckData = await checkToken(tokenData.access_token)
        const mergedTokenInfo = {
          ...tokenData,
          ...tokenCheckData
        }
        saveUserInfo(mergedTokenInfo, remember)
        tokenInfo.value = mergedTokenInfo
        router.replace({ name: 'Home' })
      } catch (error) {
        console.error('OAuth callback error:', error)
        ElMessage.error('Login failed: ' + (error.message || 'Unknown error'))
        router.replace({ name: 'Welcome' })
      } finally {
        loading.value = false
      }
    }

    const loadStoredTokenInfo = async () => {
      const stored = getStoredTokenInfo()
      if (stored) {
        tokenInfo.value = stored
        return
      }

      if (isAuthenticated()) {
        try {
          loading.value = true
          const token = getAccessToken()
          if (token) {
            const tokenCheckData = await checkToken(token)
            const mergedTokenInfo = {
              access_token: token,
              ...tokenCheckData
            }
            saveUserInfo(mergedTokenInfo, true)
            tokenInfo.value = mergedTokenInfo
          }
        } catch (error) {
          console.error('Load token info error:', error)
          ElMessage.warning('Failed to get token information, please login again')
          clearTokens()
          router.push({ name: 'Welcome' })
        } finally {
          loading.value = false
        }
      } else {
        router.push({ name: 'Welcome' })
      }
    }

    onMounted(() => {
      handleOAuthCallback()
    })

    return {
      loading,
      tokenInfo,
    }
  }
}
</script>

<style scoped>
:global(:root) {
  --header-height: 55px;
  --aside-width: 240px;
  --main-width: calc(100vw - var(--aside-width));
}

.custom-header {
  margin: 0 0 0 0;
  height: var(--header-height);
  line-height: var(--header-height);
  width: 100vw;
  padding: 0 0 0 0;
  box-shadow: 0 1px 4px rgba(0, 21, 41, .08);
}

.custom-aside {
  height: calc(100vh - var(--header-height));
  width: var(--aside-width);
  background-color: #001529;
  box-shadow: 0 1px 4px rgba(0, 21, 41, .08);
}

.custom-main {
  height: calc(100vh - var(--header-height));
  width: var(--main-width);
  color: rgba(0, 0, 0, .65);
  background: #f0f2f5;
}
</style>