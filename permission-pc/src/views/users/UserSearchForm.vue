<template>
  <el-form :inline="true" :model="formInline" class="demo-form-inline">
    <el-form-item :label="t('UsernameLabel')">
      <el-input v-model="formInline.username" :placeholder="t('UsernameLabel')" clearable />
    </el-form-item>
    <el-form-item :label="t('PhoneLabel')">
      <el-input v-model="formInline.phone" :placeholder="t('PhoneLabel')" clearable />
    </el-form-item>
    <el-form-item :label="t('EmailLabel')">
      <el-input v-model="formInline.email" :placeholder="t('EmailLabel')" clearable />
    </el-form-item>
    <el-form-item :label="t('CreatedAtLabel')">
      <el-date-picker
        v-model="formInline.createTime"
        type="date"
        :placeholder="t('CreatedAtLabel')"
        clearable
      />
    </el-form-item>
    <el-form-item>
      <el-button type="primary" @click="onSubmit" :icon="Search" >{{ t('QueryLabel') }}</el-button>
      <el-button type="default" @click="onReset" :icon="RefreshRight">{{ t('ResetLabel') }}</el-button>
    </el-form-item>
  </el-form>
</template>

<script setup>
import { reactive } from 'vue'
import { Search, RefreshRight } from '@element-plus/icons-vue'
import { useI18n } from 'vue-i18n'
import { defineProps } from 'vue'

const props = defineProps(['search', 'reset'])

const { t } = useI18n()

const formInline = reactive({
  username: '',
  phone: '',
  email: '',
  createTime: '',
})

const onSubmit = () => {
  props.search(formInline)
}

const onReset = () => {
  formInline.createTime = ''
  formInline.username = ''
  formInline.phone = ''
  formInline.email = ''
  props.reset(formInline)
}
</script>

<style scoped>
.demo-form-inline .el-input {
  --el-input-width: 220px;
}

.demo-form-inline .el-select {
  --el-select-width: 220px;
}
</style>