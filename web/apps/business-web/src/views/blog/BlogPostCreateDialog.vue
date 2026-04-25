<template>
  <el-dialog :model-value="visible" @update:model-value="(v) => { if (!v) onCancel() }" @close="onCancel" title="Create" align-center destroy-on-close :show-close="false" style="max-width: 820px">
    <el-form ref="formRef" :model="form" :rules="rules" label-position="top" v-loading="saving">
      <el-form-item label="Title" prop="title">
        <el-input v-model="form.title" maxlength="200" show-word-limit />
      </el-form-item>
      <el-form-item label="Summary" prop="summary">
        <el-input v-model="form.summary" maxlength="500" show-word-limit />
      </el-form-item>
      <el-form-item label="Status" prop="status">
        <el-radio-group v-model="form.status">
          <el-radio-button label="DRAFT" />
          <el-radio-button label="PUBLISHED" />
          <el-radio-button label="ARCHIVED" />
        </el-radio-group>
      </el-form-item>
      <el-form-item label="Tags" prop="tags">
        <el-input v-model="form.tags" placeholder="comma,separated,tags" maxlength="500" show-word-limit />
      </el-form-item>
      <el-form-item label="Content" prop="content">
        <el-input v-model="form.content" type="textarea" :rows="14" />
      </el-form-item>
    </el-form>
    <template #footer>
      <div class="dialog-footer">
        <el-button @click="onCancel">Cancel</el-button>
        <el-button type="primary" v-permission="'biz.blog.posts.new'" @click="onSubmit">Create</el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup name="BlogPostCreateDialog">
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { createBlogPost } from '@/views/blog/BlogPostApi'

const props = defineProps(['visible', 'onSubmit', 'onCancel'])

const formRef = ref(null)
const saving = ref(false)

const emptyForm = () => ({
  title: '',
  summary: '',
  content: '',
  status: 'DRAFT',
  tags: ''
})

const form = ref(emptyForm())

const rules = {
  title: [{ required: true, message: 'Title is required', trigger: 'blur' }],
  content: [{ required: true, message: 'Content is required', trigger: 'blur' }],
  status: [{ required: true, message: 'Status is required', trigger: 'change' }]
}

const onCancel = () => {
  if (formRef.value) formRef.value.resetFields()
  form.value = emptyForm()
  props.onCancel?.()
}

const onSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate()
  saving.value = true
  try {
    const created = await createBlogPost(form.value)
    ElMessage.success('Created')
    if (formRef.value) formRef.value.resetFields()
    form.value = emptyForm()
    props.onSubmit?.(created)
  } finally {
    saving.value = false
  }
}
</script>

<style scoped></style>

