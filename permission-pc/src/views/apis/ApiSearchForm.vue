<template>
  <el-form :inline="true" :model="formInline" class="demo-form-inline">
    <el-form-item :label="t('AppCodeLabel')">
      <el-select
        v-model="formInline.app"
        :placeholder="t('AppCodePlaceholder')"
        clearable
        filterable
      >
        <el-option
          v-for="app in appOptions"
          :key="app.clientId"
          :label="app.clientId"
          :value="app.clientId"
        />
      </el-select>
    </el-form-item>
    <el-form-item :label="t('PathLabel')">
      <el-input v-model="formInline.path" :placeholder="t('PathLabel')" clearable />
    </el-form-item>
    <el-form-item :label="t('PermLabelLabel')">
      <el-input v-model="formInline.permlabel" :placeholder="t('PermLabelLabel')" clearable />
    </el-form-item>
    <el-form-item :label="t('ModuleKeyLabel')">
      <el-input v-model="formInline.moduleKey" :placeholder="t('ModuleKeyLabel')" clearable />
    </el-form-item>
    <el-form-item>
      <el-button type="primary" @click="onSubmit" :icon="Search">{{ t('QueryLabel') }}</el-button>
      <el-button type="default" @click="onReset" :icon="RefreshRight">{{ t('ResetLabel') }}</el-button>
    </el-form-item>
  </el-form>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import { Search, RefreshRight } from '@element-plus/icons-vue'
import { useI18n } from 'vue-i18n'
import { getAppList } from '@/views/apps/AppApi'

const props = defineProps(['search', 'reset'])

const { t } = useI18n()

const appOptions = ref([])

const formInline = reactive({
  app: '',
  path: '',
  permlabel: '',
  moduleKey: ''
})

const onSubmit = () => {
  props.search(formInline)
}

const onReset = () => {
  formInline.app = ''
  formInline.path = ''
  formInline.permlabel = ''
  formInline.moduleKey = ''
  props.reset(formInline)
}

onMounted(() => {
  getAppList()
    .then((response) => {
      const rows = Array.isArray(response) ? response : (response.records || response.data || [])
      appOptions.value = rows || []
    })
    .catch(() => {
      appOptions.value = []
    })
})
</script>

<style scoped>
.demo-form-inline .el-input {
  --el-input-width: 220px;
}

.demo-form-inline .el-select {
  --el-select-width: 220px;
}
</style>

