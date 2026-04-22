<template>
  <div class="header-container">
    <div class="system-title">
      <div style="display: flex; align-items: center; justify-content: center;">
        <img
          v-if="tenant && tenant.logo"
          :src="tenant.logo"
          alt="Logo"
          style="width: 30px; height: 30px; border-radius: 30%; margin-right: 8px;"
        />
        <span style="display: flex; align-items: center; font-size: 20px;">{{
          tenant && tenant.systemName ? tenant.systemName : 'Skylark'
        }}</span>
      </div>
    </div>
    <div class="header-blank" />
    <div class="header-tools">
      <div class="header-tools-item">
        <ShellLanguageSelect />
      </div>
      <div class="user-avatar header-tools-item">
        <ShellUserAvatar />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, inject, onMounted } from 'vue'
import { ADMIN_SHELL_CONFIG } from '../symbols'
import ShellLanguageSelect from './ShellLanguageSelect.vue'
import ShellUserAvatar from './ShellUserAvatar.vue'

const shell = inject(ADMIN_SHELL_CONFIG)
const tenant = ref(null)

onMounted(() => {
  tenant.value = shell.getTenant ? shell.getTenant() : null
})
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
