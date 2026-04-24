<template>
  <el-card shadow="always" v-loading="loading">
    <div class="buttons-block">
      <el-button type="default" size="default" @click="back">Back</el-button>
      <el-button type="primary" size="default" :loading="saving" v-permission="isEdit ? 'biz.blog.posts.edit' : 'biz.blog.posts.new'" @click="submit">Save</el-button>
    </div>

    <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
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
  </el-card>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { createBlogPost, getBlogPostById, updateBlogPostById } from '@/views/blog/BlogPostApi'

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const saving = ref(false)
const formRef = ref()

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

const isEdit = computed(() => !!route.params.id && route.name !== 'BlogPostCreate')

const back = () => router.push('/blog/posts')

const load = () => {
  loading.value = true
  return getBlogPostById(route.params.id).then((post) => {
    form.value.title = post.title || ''
    form.value.summary = post.summary || ''
    form.value.content = post.content || ''
    form.value.status = post.status || 'DRAFT'
    form.value.tags = post.tags || ''
  }).finally(() => {
    loading.value = false
  })
}

const submit = async () => {
  await formRef.value.validate()
  saving.value = true
  try {
    if (isEdit.value) {
      const id = route.params.id
      await updateBlogPostById(id, form.value)
      ElMessage.success('Saved')
      router.push(`/blog/posts/${id}`)
      return
    }
    const created = await createBlogPost(form.value)
    ElMessage.success('Created')
    router.push(`/blog/posts/${created.id}`)
  } finally {
    saving.value = false
  }
}

onMounted(() => {
  if (isEdit.value) load()
})
</script>

<style scoped>
.buttons-block {
  display: flex;
  gap: 10px;
  margin-bottom: 12px;
  align-items: center;
}
</style>

