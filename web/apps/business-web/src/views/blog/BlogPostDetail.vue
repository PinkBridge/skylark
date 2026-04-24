<template>
  <el-card shadow="always" v-loading="loading">
    <div class="buttons-block">
      <el-button type="default" size="default" @click="back">Back</el-button>
      <el-button type="primary" size="default" v-permission="'biz.blog.posts.edit'" :disabled="!post?.id" @click="toEdit">Edit</el-button>
    </div>

    <div class="title">{{ post?.title || 'Blog Post' }}</div>
    <div class="meta">
      <el-tag v-if="post?.status" :type="tagType(post.status)" size="small">{{ post.status }}</el-tag>
      <span v-if="post?.createTime" class="time">Created: {{ post.createTime }}</span>
    </div>

    <div v-if="post?.summary" class="summary">{{ post.summary }}</div>
    <el-divider />
    <pre class="content">{{ post?.content }}</pre>
  </el-card>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getBlogPostById } from '@/views/blog/BlogPostApi'

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const post = ref(null)

const tagType = (s) => {
  if (s === 'PUBLISHED') return 'success'
  if (s === 'ARCHIVED') return 'info'
  return 'warning'
}

const back = () => router.push('/blog/posts')
const toEdit = () => router.push(`/blog/posts/${route.params.id}/edit`)

const load = () => {
  loading.value = true
  return getBlogPostById(route.params.id).then((res) => {
    post.value = res
  }).finally(() => {
    loading.value = false
  })
}

onMounted(() => load())
</script>

<style scoped>
.buttons-block {
  display: flex;
  gap: 10px;
  margin-bottom: 12px;
  align-items: center;
}
.title {
  font-size: 16px;
  font-weight: 600;
}
.meta {
  margin-top: 8px;
  display: flex;
  align-items: center;
  gap: 10px;
  color: var(--el-text-color-secondary);
  font-size: 12px;
}
.summary {
  color: var(--el-text-color-regular);
  font-size: 13px;
  line-height: 1.6;
}
.content {
  white-space: pre-wrap;
  line-height: 1.7;
  font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, "Liberation Mono", "Courier New", monospace;
  font-size: 13px;
}
</style>

