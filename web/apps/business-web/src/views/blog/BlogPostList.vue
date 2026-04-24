<template>
  <el-card shadow="always">
    <div class="buttons-block">
      <el-input
        v-model="keyword"
        clearable
        placeholder="Search by title / summary"
        style="max-width: 360px"
        @keyup.enter="handleSearch"
      />
      <el-select v-model="status" clearable placeholder="Status" style="width: 160px" @change="handleSearch">
        <el-option label="DRAFT" value="DRAFT" />
        <el-option label="PUBLISHED" value="PUBLISHED" />
        <el-option label="ARCHIVED" value="ARCHIVED" />
      </el-select>
      <el-button
        type="primary"
        size="default"
        :icon="Plus"
        v-permission="'biz.blog.posts.new'"
        @click="handleCreate"
      >New</el-button>
      <el-button type="default" size="default" :icon="Refresh" @click="handleRefresh">Refresh</el-button>
      <el-button type="default" size="default" @click="handleReset">Reset</el-button>
    </div>

    <el-table :data="tableData" style="width: 100%" stripe border show-overflow-tooltip v-loading="loading">
      <el-table-column fixed prop="id" label="ID" width="90" />
      <el-table-column prop="title" label="Title" min-width="240">
        <template #default="{ row }">
          <el-button link type="primary" size="default" v-permission="'biz.blog.posts.detail'" @click="handleDetail(row)">
            {{ row.title }}
          </el-button>
          <div v-if="row.summary" class="summary">{{ row.summary }}</div>
        </template>
      </el-table-column>
      <el-table-column prop="status" label="Status" width="120">
        <template #default="{ row }">
          <el-tag :type="tagType(row.status)" size="small">{{ row.status }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="Created" width="180" />
      <el-table-column label="Operations" min-width="180" fixed="right" :show-overflow-tooltip="false">
        <template #default="{ row }">
          <div class="op-actions">
            <el-button link type="primary" size="default" v-permission="'biz.blog.posts.edit'" @click="handleEdit(row)">Edit</el-button>
            <el-button link type="primary" size="default" v-permission="'biz.blog.posts.delete'" @click="handleDelete(row)">Delete</el-button>
          </div>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination-block">
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :page-sizes="[10, 20, 50, 100]"
        :total="total"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>
  </el-card>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessageBox, ElMessage } from 'element-plus'
import { Refresh, Plus } from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'
import { getBlogPostPage, deleteBlogPostById } from '@/views/blog/BlogPostApi'

const router = useRouter()

const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const tableData = ref([])
const keyword = ref('')
const status = ref('')

const tagType = (s) => {
  if (s === 'PUBLISHED') return 'success'
  if (s === 'ARCHIVED') return 'info'
  return 'warning'
}

const loadPage = () => {
  loading.value = true
  return getBlogPostPage(currentPage.value, pageSize.value, {
    keyword: keyword.value || undefined,
    status: status.value || undefined
  }).then((res) => {
    tableData.value = res.records || []
    total.value = res.total || 0
    currentPage.value = res.page || currentPage.value
    pageSize.value = res.size || pageSize.value
  }).finally(() => {
    loading.value = false
  })
}

const handleSearch = () => {
  currentPage.value = 1
  loadPage()
}

const handleReset = () => {
  keyword.value = ''
  status.value = ''
  currentPage.value = 1
  loadPage()
}

const handleRefresh = () => loadPage()

const handleSizeChange = (s) => {
  pageSize.value = s
  currentPage.value = 1
  loadPage()
}

const handleCurrentChange = (p) => {
  currentPage.value = p
  loadPage()
}

const handleCreate = () => router.push('/blog/posts/new')
const handleDetail = (row) => router.push(`/blog/posts/${row.id}`)
const handleEdit = (row) => router.push(`/blog/posts/${row.id}/edit`)

const handleDelete = async (row) => {
  await ElMessageBox.confirm(`Delete "${row.title}"?`, 'Confirm', {
    type: 'warning',
    confirmButtonText: 'Delete',
    cancelButtonText: 'Cancel'
  })
  await deleteBlogPostById(row.id)
  ElMessage.success('Deleted')
  loadPage()
}

onMounted(() => loadPage())
</script>

<style scoped>
.buttons-block {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-bottom: 12px;
  align-items: center;
}
.summary {
  margin-top: 4px;
  color: var(--el-text-color-secondary);
  font-size: 12px;
}
.pagination-block {
  display: flex;
  justify-content: flex-end;
  margin-top: 12px;
}
.op-actions {
  display: flex;
  gap: 8px;
}
</style>

