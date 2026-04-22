<template>
  <div class="index-container">
    <div class="index-content">
      <div class="logo-section">
        <div class="logo-placeholder">
          <img
            v-if="logoSrc"
            :src="logoSrc"
            alt="Logo"
            class="tenant-logo"
            @error="onLogoError"
          />
          <div v-else class="tenant-logo-fallback" v-html="DEFAULT_WELCOME_LOGO_SVG" />
        </div>
        <h1 class="app-title">{{tenant.systemName }}</h1>
        <el-tag v-if="
          !isTenantAvailable" type="error">
          {{ t('TenantNotAvailable') }}</el-tag>
      </div>

      <div class="action-section" v-if="isTenantAvailable">
        <el-button type="primary" size="large" class="action-button" @click="handleLogin">
          {{ t('WelcomeLogin') }}
        </el-button>
      </div>
      <div class="footer-section">
        <p>© 2025 {{ tenant.name }}. {{ t('WelcomeAllRightsReserved') }}</p>
        <p>{{ t('WelcomePoweredBy') }} {{ tenant.name }}</p>
        <p v-if="tenant.contactEmail">
          {{ t('WelcomeEmail') }}
          <a :href="'mailto:' + tenant.contactEmail" target="_blank">{{ tenant.contactEmail }}</a>
        </p>
        <p v-if="tenant.contactPhone">
          {{ t('WelcomePhone') }}
          <a :href="'tel:' + tenant.contactPhone" target="_blank">{{ tenant.contactPhone }}</a>
        </p>
        <p v-if="tenant.address">{{ t('WelcomeAddress') }} {{ tenant.address }}</p>
      </div>
    </div>
  </div>
</template>

<script>
import { getAuthorizationUrl } from '@/api/oauth'
import { ref, onMounted, computed, watch } from 'vue'
import { getTenantForCurrentLocation } from '@/utils/tenant'
import { useI18n } from 'vue-i18n'

export default {
  name: 'WelcomePage',
  setup() {
    const { t } = useI18n()
    const DEFAULT_WELCOME_LOGO_SVG = `
<svg t="1776652496982" class="icon" viewBox="0 0 1024 1024" version="1.1" xmlns="http://www.w3.org/2000/svg" p-id="7829" width="256" height="256"><path d="M422.6944 694.2976l134.56128 134.9376-2.6368 2.64192H203.10016l219.59424-137.57952z m110.2464-334.69952l245.71136 246.36672v0.7424l-193.72032 194.20928L145.92 360.02048h386.67008l0.35072-0.4224z m2.95936 63.83104a6.7584 6.7584 0 0 0-9.22624-0.3072l-0.33792 0.31744-0.2816 0.30208a6.77376 6.77376 0 0 0-1.66912 5.19424l0.06144 0.45568-20.72064 20.8384-16.25344-16.22784a6.7584 6.7584 0 0 0-11.29984-6.55872 6.77632 6.77632 0 0 0-0.32512 9.23648l0.31488 0.33792 0.29952 0.2816c1.39264 1.23648 3.1744 1.79456 4.91776 1.67936l0.47616-0.04608 16.61184 16.58624-13.97248 14.05184c-0.1792 0.18176-0.34048 0.37888-0.47872 0.58624a3.7248 3.7248 0 0 0-0.17408 4.9152l0.25088 0.27648 56.75008 56.63232-12.49024 12.50048-35.264-35.1488a6.7584 6.7584 0 0 0-11.36384-6.30784 6.77376 6.77376 0 0 0-0.01024 9.5744 6.7328 6.7328 0 0 0 5.504 1.93536l0.45312-0.064 35.40736 35.2896-11.50464 11.51488-85.82144-85.69856-0.25344-0.2304-0.53248-0.41472a3.70688 3.70688 0 0 0-4.49792 0.17152l-0.26624 0.24576-40.41728 40.64a6.76352 6.76352 0 0 0-6.28224 1.82016 6.7584 6.7584 0 1 0 9.216 9.8816l0.33792-0.31744 0.2816-0.30208a6.77376 6.77376 0 0 0 1.664-5.24544l-0.06656-0.4608 37.72672-37.9392 83.44064 83.32544-11.64032 11.65056-62.83008-62.71488a6.7584 6.7584 0 0 0-11.34336-6.38976 6.77376 6.77376 0 0 0-0.01024 9.5744 6.7328 6.7328 0 0 0 5.4272 1.9456l0.44544-0.05888 63.14752 63.03232c-12.42112 14.7712-12.84608 36.21632-1.27232 51.44576l0.57088 0.7296-11.35104 11.456a3.74272 3.74272 0 0 0-0.24576 4.992l0.26624 0.29696 0.25088 0.22528c1.36192 1.12896 3.3408 1.13152 4.70272 0.01024l0.2816-0.256 11.17184-11.27168 11.776 11.78368-11.0464 11.1488a3.74272 3.74272 0 0 0-0.24576 4.992l0.256 0.2816a3.70432 3.70432 0 0 0 4.9664 0.24832l0.2816-0.256 11.0464-11.1488 12.45952 12.47232-10.7776 10.87232a3.74272 3.74272 0 0 0-0.24576 4.992l0.256 0.28416a3.70432 3.70432 0 0 0 4.9664 0.24832l0.2816-0.256 10.7776-10.87744 11.65056 11.6608-11.04384 11.14624a3.74272 3.74272 0 0 0-0.24576 4.992l0.256 0.2816a3.70432 3.70432 0 0 0 4.9664 0.24832l0.2816-0.256 11.0464-11.14624 11.65312 11.6608-11.0464 11.14624a3.74272 3.74272 0 0 0-0.24576 4.992l0.256 0.28416a3.70432 3.70432 0 0 0 4.9664 0.24576l0.2816-0.256 11.1616-11.264c15.01696 12.5696 36.87168 12.75904 52.09344 0.56832l0.71936-0.5888 10.944 10.86464c1.37216 1.36192 3.5328 1.44384 4.99968 0.24576l0.2816-0.256a3.72224 3.72224 0 0 0 0.24832-4.98432l-0.256-0.2816-10.8288-10.752 11.67616-11.68896 10.8288 10.752c1.3696 1.36192 3.5328 1.44384 4.99712 0.24576l0.28416-0.256a3.72224 3.72224 0 0 0 0.24576-4.98176l-0.256-0.2816-10.8288-10.75456 11.67872-11.6864 11.09504 11.01824c1.37216 1.36192 3.5328 1.44384 4.99968 0.24832l0.2816-0.256a3.72224 3.72224 0 0 0 0.24832-4.98432l-0.256-0.2816-11.10016-11.0208 12.48768-12.49792 10.8288 10.752c1.3696 1.36192 3.5328 1.44384 4.99712 0.24576l0.2816-0.256a3.72224 3.72224 0 0 0 0.24832-4.98432l-0.256-0.2816-10.8288-10.752 11.8016-11.81184 10.70336 10.62912c1.37216 1.36192 3.5328 1.44384 4.99968 0.24832l0.2816-0.256a3.72224 3.72224 0 0 0 0.24832-4.98432l-0.256-0.2816-10.8288-10.752c12.49792-15.01952 12.67456-36.8384 0.5376-52.05248l-0.5888-0.7168 11.12832-11.22816c1.3568-1.3696 1.43872-3.52768 0.24576-4.992l-0.256-0.2816a3.70432 3.70432 0 0 0-4.9664-0.24832l-0.2816 0.256-11.01312 11.1104-11.65312-11.6608 11.01056-11.1104c1.3568-1.3696 1.43872-3.52768 0.24576-4.992l-0.256-0.2816a3.70432 3.70432 0 0 0-4.9664-0.24832l-0.2816 0.256-11.01312 11.1104-11.648-11.6608 11.2768-11.38176c1.3568-1.3696 1.43872-3.52768 0.24576-4.992l-0.256-0.2816a3.70432 3.70432 0 0 0-4.9664-0.24832l-0.2816 0.256-11.27936 11.38432-12.45952-12.47232 11.01056-11.1104c1.3568-1.3696 1.43872-3.52768 0.24576-4.992l-0.256-0.2816a3.70432 3.70432 0 0 0-4.9664-0.24576l-0.2816 0.256-11.01312 11.1104-11.776-11.78624 10.89024-10.98752c1.3568-1.3696 1.43872-3.52768 0.24576-4.992l-0.256-0.2816a3.70432 3.70432 0 0 0-4.9664-0.24832l-0.2816 0.256-11.07456 11.1744c-14.93248-11.90912-36.2112-11.93728-51.1744-0.08448l-0.7168 0.58368-54.33088-54.25152 20.56448-20.67456c2.2016 0.512 4.61312-0.0896 6.33344-1.80992a6.77376 6.77376 0 0 0 0.01024-9.5744zM778.59328 192v357.72928l-200.54016-201.0496L708.29056 192h70.30272zM503.73376 460.7744l42.24512 42.19392c0.1024 0.15872 0.21248 0.31232 0.33792 0.45824l0.19712 0.21248 11.37152 11.28448-11.77088 11.78112-54.17728-54.0672 11.79648-11.86048zM814.272 192L954.88 332.96384h-140.608V192z" fill="#1890FF" p-id="7830"></path><path d="M617.68192 546.51392l0.6528 0.64 32.59392 32.5888c15.936 15.93856 16.14592 41.64864 0.6272 57.84064l-0.6272 0.64256-33.18272 33.18272-0.56064 0.5504c-15.96928 15.3856-41.26208 15.41376-57.2672 0.08704l-0.6528-0.64-32.59392-32.5888-0.5504-0.5632c-15.3856-15.96928-15.41376-41.26208-0.08704-57.2672l0.64-0.6528 33.1776-33.18272 0.5632-0.5504c15.96928-15.3856 41.26208-15.41376 57.2672-0.08704z m-57.48992 31.02976l-0.64256 0.62976-2.00704 2.00704c-15.93856 15.93856-16.14848 41.64608-0.62976 57.84064l0.62976 0.64 1.87392 1.87648c15.93856 15.93856 41.64608 16.14848 57.84064 0.62976l0.64-0.62976 2.0096-2.00704c15.936-15.936 16.14592-41.64608 0.62976-57.84064l-0.62976-0.64-1.87392-1.87648c-15.93856-15.936-41.64608-16.14592-57.84064-0.62976z" fill="#000000" fill-opacity=".65" p-id="7831"></path></svg>
`
    const tenant = ref({
      logo: '',
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

    // Registration is intentionally not exposed in this app.

    const logoErrored = ref(false)
    const logoSrc = ref('')
    const onLogoError = () => {
      logoErrored.value = true
      // Hard fallback: remove src so template switches to SVG.
      logoSrc.value = ''
    }
    watch(
      () => tenant.value?.logo,
      (v) => {
        logoErrored.value = false
        logoSrc.value = v ? String(v) : ''
      }
    )


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
      tenant,
      t,
      isTenantAvailable,
      DEFAULT_WELCOME_LOGO_SVG,
      logoErrored,
      logoSrc,
      onLogoError
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

.tenant-logo {
  width: 120px;
  height: 120px;
  border-radius: 50%;
  object-fit: cover;
}

.tenant-logo-fallback {
  width: 120px;
  height: 120px;
  border-radius: 50%;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
}

.tenant-logo-fallback :deep(svg) {
  width: 120px;
  height: 120px;
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