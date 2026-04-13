<template>
  <el-dialog :model-value="visible" :title="t('CreateTitle')" align-center destroy-on-close :show-close="false"
    style="max-width: 600px" :modal="false" modal-penetrable>
    <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
      <el-form-item :label="t('NameLabel')" prop="name">
        <el-input v-model="form.name" :placeholder="t('NameLabel')" />
      </el-form-item>
      <el-form-item :label="t('ParentNameLabel')" prop="parentId">
        <MenuSelect v-model="form.parentId" :disabled-ids="[form.id]" />
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
import { ref, computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { createMenu } from '@/views/menus/MenuApi'
import { ElMessage } from 'element-plus'
import MenuSelect from '@/views/menus/MenuSelect.vue'

const { t } = useI18n()

const props = defineProps(['visible', 'onSubmit', 'onCancel'])

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
    ]
  }
})

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

<style scoped></style>

