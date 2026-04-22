<template>
  <el-dialog :model-value="visible" :title="t('CreateTitle')" align-center destroy-on-close :show-close="false"
    style="max-width: 600px" :modal="false" modal-penetrable>
    <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
      <el-form-item :label="t('NameLabel')" prop="name">
        <el-input v-model="form.name" :placeholder="t('NameLabel')" />
      </el-form-item>
      <el-form-item :label="t('AppCodeLabel')" prop="appCode">
        <el-select v-model="form.appCode" filterable :placeholder="t('AppCodeLabel')" class="full-width">
          <el-option v-for="id in clientIds" :key="id" :label="id" :value="id" />
        </el-select>
      </el-form-item>
      <el-form-item v-if="form.appCode" :label="t('ParentNameLabel')" prop="parentId">
        <MenuSelect v-model="form.parentId" :disabled-ids="[form.id]" :app-filter="form.appCode" />
      </el-form-item>
      <el-form-item :label="t('TypeLabel')" prop="type">
        <el-select v-model="form.type" :placeholder="t('TypeLabel')">
          <el-option :label="t('MenuLabel')" value="menu" />
          <el-option :label="t('ButtonLabel')" value="button" />
        </el-select>
      </el-form-item>
      <el-form-item v-if="form.type == 'menu'" :label="t('PathLabel')" prop="path">
        <el-input v-model="form.path" :placeholder="t('PathLabel')" />
      </el-form-item>
      <el-form-item v-if="form.type == 'menu'" :label="t('IconLabel')" prop="icon">
        <el-input v-model="form.icon" :placeholder="t('IconLabel')" />
        <div class="upload-tip">
          <a href="https://element-plus.org/zh-CN/component/icon#icon-collection" target="_blank">
            element-plus-icon
          </a>
          {{ t('IconTip') }}
        </div>
      </el-form-item>
      <el-form-item :label="t('SortLabel')" prop="sort">
        <el-input-number v-model="form.sort" :placeholder="t('SortLabel')" />
      </el-form-item>
      <el-form-item :label="t('HiddenLabel')" prop="hidden">
        <el-select v-model="form.hidden" :placeholder="t('HiddenLabel')">
          <el-option :label="t('Yes')" :value="true" />
          <el-option :label="t('No')" :value="false" />
        </el-select>
      </el-form-item>
      <el-form-item :label="t('PermLabel')" prop="permlabel">
        <el-input v-model="form.permlabel" :placeholder="t('PermLabel')" />
      </el-form-item>
      <el-form-item :label="t('ModuleKeyLabel')" prop="moduleKey">
        <el-input v-model="form.moduleKey" :placeholder="t('ModuleKeyLabel')" />
      </el-form-item>
    </el-form>
    <template #footer>
      <div class="dialog-footer">
        <el-button @click="onCancel">{{ t('CancelButtonText') }}</el-button>
        <el-button type="primary" @click="onSubmit">{{ t('ConfirmButtonText') }}</el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup name="MenuCreateDialog">
import { ref, computed, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { createMenu } from '@/views/menus/MenuApi'
import { ElMessage } from 'element-plus'
import MenuSelect from '@/views/menus/MenuSelect.vue'

const { t } = useI18n()

const props = defineProps({
  visible: Boolean,
  clientIds: { type: Array, default: () => [] },
  defaultAppCode: { type: String, default: '' },
  onSubmit: Function,
  onCancel: Function
})

const formRef = ref(null)
const form = ref({
  name: '',
  parentId: '',
  path: '',
  hidden: false,
  moduleKey: '',
  sort: 0,
  icon: '',
  type: 'menu',
  permlabel: '',
  appCode: '',
})

const rules = computed(() => {
  return {
    name: [
      { required: true, message: t('NameRequired'), trigger: 'blur' }
    ],
    type: [
      { required: true, message: t('TypeRequired'), trigger: 'blur' }
    ],
    permlabel: [
      { required: true, message: t('PermLabelRequired'), trigger: 'blur' }
    ],
    appCode: [
      { required: true, message: t('AppCodeRequired'), trigger: 'change' }
    ]
  }
})

watch(
  () => props.visible,
  (v) => {
    if (v) {
      const d = props.defaultAppCode || props.clientIds[0] || ''
      form.value.appCode = d
    }
  }
)

const onCancel = () => {
  if (formRef.value) {
    formRef.value.resetFields()
  }
  form.value = {
    name: '',
    parentId: '',
    path: '',
    hidden: false,
    moduleKey: '',
    sort: 0,
    icon: '',
    type: 'menu',
    permlabel: '',
    appCode: props.defaultAppCode || props.clientIds[0] || '',
  }
  props.onCancel()
}

const onSubmit = async () => {
  if (!formRef.value) return

  try {
    await formRef.value.validate()
    const menu = {
      name: form.value.name,
      parentId: form.value.parentId || '',
      path: form.value.path || '',
      hidden: form.value.hidden,
      moduleKey: form.value.moduleKey || '',
      sort: form.value.sort || 0,
      icon: form.value.icon || '',
      type: form.value.type || 'menu',
      permlabel: form.value.permlabel || '',
      appCode: form.value.appCode || props.clientIds[0] || '',
    }
    createMenu(menu).then(() => {
      formRef.value.resetFields()
      props.onSubmit()
    }).catch(error => {
      console.error('Create menu failed:', error)
      ElMessage.error(error.message || 'Create menu failed')
    })
  } catch (error) {
    console.error('Form validation failed:', error)
  }
}
</script>

<style scoped>
.full-width {
  width: 100%;
}
</style>

