<template>
  <el-dialog :model-value="visible" title="Edit" align-center destroy-on-close :show-close="false" style="max-width: 820px">
    <el-form ref="formRef" :model="form" :rules="rules" label-position="top" v-loading="loading || saving">
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
        <el-button type="primary" v-permission="'biz.blog.posts.edit'" :disabled="!props.id" @click="onSubmit">Save</el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup name="BlogPostEditDialog">
import { ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { getBlogPostById, updateBlogPostById } from '@/views/blog/BlogPostApi'

const props = defineProps(['visible', 'id', 'onSubmit', 'onCancel'])

const formRef = ref(null)
const loading = ref(false)
const saving = ref(false)

const form = ref({
  title: '',
  summary: '',
  content: '',
  status: 'DRAFT',
  tags: ''
})

const rules = {
  title: [{ required: true, message: 'Title is required', trigger: 'blur' }],
  content: [{ required: true, message: 'Content is required', trigger: 'blur' }],
  status: [{ required: true, message: 'Status is required', trigger: 'change' }]
}

const load = async () => {
  if (!props.id) return
  loading.value = true
  try {
    const post = await getBlogPostById(props.id)
    form.value = {
      title: post?.title || '',
      summary: post?.summary || '',
      content: post?.content || '',
      status: post?.status || 'DRAFT',
      tags: post?.tags || ''
    }
  } finally {
    loading.value = false
  }
}

watch(
  () => [props.visible, props.id],
  ([v]) => {
    if (v) load()
  }
)

const onCancel = () => {
  props.onCancel?.()
}

const onSubmit = async () => {
  if (!props.id || !formRef.value) return
  await formRef.value.validate()
  saving.value = true
  try {
    await updateBlogPostById(props.id, form.value)
    ElMessage.success('Saved')
    props.onSubmit?.()
  } finally {
    saving.value = false
  }
}
</script>

<style scoped></style>

