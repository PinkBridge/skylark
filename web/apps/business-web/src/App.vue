<template>
  <router-view />
</template>

<script setup>
import { onMounted } from 'vue'
import { getTenantForCurrentLocation } from '@/utils/tenant'

onMounted(() => {
  getTenantForCurrentLocation()
})
</script>

<style>
#app {
  font-family: Avenir, Helvetica, Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  margin: 0;
  padding: 0;
}

* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

body {
  margin: 0;
  padding: 0;
}
</style>
