<template>
  <el-form :inline="true" :model="formInline" class="blog-search-form">
    <el-form-item label="Keyword">
      <el-input
        v-model="formInline.keyword"
        placeholder="Title / summary"
        clearable
        @keyup.enter="onSubmit"
        style="width: 240px"
      />
    </el-form-item>
    <el-form-item label="Status">
      <el-select v-model="formInline.status" clearable placeholder="All" style="width: 160px">
        <el-option label="DRAFT" value="DRAFT" />
        <el-option label="PUBLISHED" value="PUBLISHED" />
        <el-option label="ARCHIVED" value="ARCHIVED" />
      </el-select>
    </el-form-item>
    <el-form-item>
      <el-button type="primary" :icon="Search" @click="onSubmit">Query</el-button>
      <el-button type="default" :icon="RefreshRight" @click="onReset">Reset</el-button>
    </el-form-item>
  </el-form>
</template>

<script setup>
import { reactive } from 'vue'
import { Search, RefreshRight } from '@element-plus/icons-vue'

const props = defineProps(['search', 'reset'])

const formInline = reactive({
  keyword: '',
  status: ''
})

const onSubmit = () => {
  props.search({ ...formInline })
}

const onReset = () => {
  formInline.keyword = ''
  formInline.status = ''
  props.reset({ ...formInline })
}
</script>

<style scoped>
.blog-search-form :deep(.el-form-item) {
  margin-bottom: 10px;
}
</style>

