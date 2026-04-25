<template>
  <el-dialog :model-value="visible" @update:model-value="(v) => { if (!v) props.onClose?.() }" @close="() => props.onClose?.()" title="Blog Post" align-center destroy-on-close :show-close="false" style="max-width: 820px">
    <div v-loading="loading">
      <div class="title">{{ post?.title || '-' }}</div>
      <div class="meta">
        <el-tag v-if="post?.status" :type="tagType(post.status)" size="small">{{ post.status }}</el-tag>
        <span v-if="post?.createTime" class="time">Created: {{ post.createTime }}</span>
      </div>
      <div v-if="post?.summary" class="summary">{{ post.summary }}</div>
      <el-divider />
      <pre class="content">{{ post?.content || '' }}</pre>
    </div>
    <template #footer>
      <div class="dialog-footer">
        <el-button @click="props.onClose">Close</el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup name="BlogPostDetailDialog">
import { ref, watch } from 'vue'
import { getBlogPostById } from '@/views/blog/BlogPostApi'

const props = defineProps(['visible', 'id', 'onClose'])

const loading = ref(false)
const post = ref(null)

const tagType = (s) => {
  if (s === 'PUBLISHED') return 'success'
  if (s === 'ARCHIVED') return 'info'
  return 'warning'
}

const load = async () => {
  if (!props.id) return
  loading.value = true
  try {
    post.value = await getBlogPostById(props.id)
  } finally {
    loading.value = false
  }
}

watch(
  () => [props.visible, props.id],
  ([v]) => {
    if (v) load()
  },
  { immediate: true }
)
</script>

<style scoped>
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
  margin-top: 10px;
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

