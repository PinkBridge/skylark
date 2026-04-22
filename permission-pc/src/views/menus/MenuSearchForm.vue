<template>
  <el-form :inline="true" :model="formInline" class="demo-form-inline">
    <el-form-item :label="t('AppCodeLabel')">
      <el-select
        :model-value="app"
        filterable
        class="app-select"
        :placeholder="t('AppCodeLabel')"
        @update:model-value="onAppChange"
      >
        <el-option
          v-for="id in clientIds"
          :key="id"
          :label="id"
          :value="id"
        />
      </el-select>
    </el-form-item>
    <el-form-item :label="t('NameLabel')">
      <el-input v-model="formInline.name" :placeholder="t('NameLabel')" clearable />
    </el-form-item>
    <el-form-item :label="t('PermLabel')" prop="perm">
      <el-input v-model="formInline.permlabel" :placeholder="t('PermLabel')" clearable />
    </el-form-item>
    <el-form-item :label="t('ModuleKeyLabel')" prop="moduleKey">
      <el-input v-model="formInline.moduleKey" :placeholder="t('ModuleKeyLabel')" clearable />
    </el-form-item>
    <el-form-item :label="t('PathLabel')">
      <el-input v-model="formInline.path" :placeholder="t('PathLabel')" clearable />
    </el-form-item>
    <el-form-item>
      <el-button type="primary" @click="onSubmit" :icon="Search">{{ t('QueryLabel') }}</el-button>
      <el-button type="default" @click="onReset" :icon="RefreshRight">{{ t('ResetLabel') }}</el-button>
    </el-form-item>
  </el-form>
</template>

<script setup>
import { reactive } from 'vue'
import { Search, RefreshRight } from '@element-plus/icons-vue'
import { useI18n } from 'vue-i18n'

const props = defineProps({
  /** OAuth client_id 列表（已排序） */
  clientIds: {
    type: Array,
    default: () => []
  },
  /** 当前选中的应用（client_id） */
  app: {
    type: String,
    default: ''
  },
  search: Function,
  reset: Function
})

const emit = defineEmits(['update:app'])

const { t } = useI18n()

const formInline = reactive({
  name: '',
  permlabel: '',
  moduleKey: '',
  path: ''
})

const emitSearch = () => {
  props.search({ ...formInline, app: props.app })
}

const onAppChange = (v) => {
  emit('update:app', v || '')
  props.search({ ...formInline, app: v || '' })
}

const onSubmit = () => {
  emitSearch()
}

const onReset = () => {
  formInline.name = ''
  formInline.permlabel = ''
  formInline.moduleKey = ''
  formInline.path = ''
  const first = props.clientIds?.length ? props.clientIds[0] : ''
  emit('update:app', first)
  props.reset({ ...formInline, app: first })
}

</script>

<style scoped>
.demo-form-inline .el-input {
  --el-input-width: 220px;
}
.app-select {
  width: 220px;
}
</style>
