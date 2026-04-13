<template>
  <div class="header-container">
    <div class="system-title">
      <div>
        <div style="display: flex; align-items: center; justify-content: center;">
          <img v-if="tenant && tenant.logo" :src="tenant.logo ? tenant.logo : '@/assets/logo.png'" 
            alt="Logo" style="width: 30px; height: 30px; border-radius: 30%; margin-right: 8px;" />
          <span style="display: flex; align-items: center; font-size: 20px;">{{ tenant && tenant.systemName ? tenant.systemName : 'Skylark' }}</span>
        </div>
      </div>
    </div>
    <div class="header-blank"></div>
    <div class="header-tools">
      <div class="header-tools-item">
        <LanguageComponent />
      </div>  
      <div class="user-avatar header-tools-item">
        <UserAvatarComponent />
      </div>
    </div>
  </div>
</template>

<script>
import { ref } from 'vue'
import UserAvatarComponent from '@/components/UserAvatar.vue'
import LanguageComponent from '@/components/LanguageSelect.vue'
import { getTenant } from '@/utils/tenant'
export default {
  name: 'HeaderComponent',
  components: {
    UserAvatarComponent,
    LanguageComponent,
  },
  setup() {
    const tenant = ref(null)
    tenant.value = getTenant()
    return {
      tenant,
    }
  }
}
</script>

<style scoped>
:global(:root) {
  --system-title-width: 240px;
  --header-tools-width: 480px;
  --header-blank-width: calc(100vw - var(--system-title-width) - var(--header-tools-width));
}

.header-container {
  height: 100%;
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.system-title {
  height: 100%;
  width: var(--system-title-width);
  line-height: var(--header-height);
  font-size: 20px;
  font-weight: bold;
  font-family: 'Arial', sans-serif;
  text-align: center;
  background-color: #001529;
  color: #fff;
}

.header-blank {
  width: var(--header-blank-width);
}

.header-tools {
  width: var(--header-tools-width);
  display: flex;
  align-items: center;
  justify-content: flex-end;
  height: 100%;
  padding-right: 40px;
}

.header-tools-item {
  margin-left: 20px;
}
</style>