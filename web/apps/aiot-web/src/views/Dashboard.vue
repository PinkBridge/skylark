<template>
  <div class="wrap">
    <el-card shadow="never">
      <template #header>
        <span>aiot system</span>
      </template>
      <p v-if="userLabel">Signed in as: {{ userLabel }}</p>
      <p v-else class="muted">Loading user…</p>
      <p v-permission="'demo:secret-button'" class="mt">
        <el-button type="primary" plain size="small">Secret (permlabel in menu cache)</el-button>
      </p>
    </el-card>
  </div>
</template>

<script>
import { getUserInfo } from '@/utils/auth'

export default {
  name: 'DashboardPage',
  data() {
    return {
      userLabel: ''
    }
  },
  mounted() {
    const u = getUserInfo()
    this.userLabel = u?.user_name || u?.username || u?.sub || ''
  }
}
</script>

<style scoped>
.wrap {
  padding: 16px;
}
.muted {
  color: var(--el-text-color-secondary);
}
.mt {
  margin-top: 12px;
}
</style>
