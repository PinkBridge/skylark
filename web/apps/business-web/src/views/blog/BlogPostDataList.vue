<template>
  <el-card shadow="always">
    <BlogPostSearchForm :search="handleSearch" :reset="handleReset" />

    <div class="buttons-block">
      <el-button type="primary" size="default" :icon="Plus" v-permission="'biz.blog.posts.new'" @click="handleCreate">
        New
      </el-button>
      <el-button type="default" size="default" :icon="Refresh" @click="handleRefresh">Refresh</el-button>
    </div>

    <el-table :data="tableData" style="width: 100%" stripe border show-overflow-tooltip v-loading="loading">
      <el-table-column fixed prop="id" label="ID" width="90" />
      <el-table-column prop="title" label="Title" min-width="260">
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
            <el-button link type="primary" size="default" v-permission="'biz.blog.posts.detail'" @click="handleDetail(row)">
              Detail
            </el-button>
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

    <BlogPostDetailDialog
      v-if="detailId"
      :visible="detailVisible"
      :id="detailId"
      :on-close="handleDetailClose"
    />
    <BlogPostCreateDialog :visible="createVisible" :on-submit="handleCreateSubmit" :on-cancel="handleCreateCancel" />
    <BlogPostEditDialog :visible="editVisible" :id="editId" :on-submit="handleEditSubmit" :on-cancel="handleEditCancel" />
  </el-card>
</template>

<script setup name="BlogPostDataList">
import { ref, onMounted } from 'vue'
import { ElMessageBox, ElMessage } from 'element-plus'
import { Refresh, Plus } from '@element-plus/icons-vue'
import BlogPostSearchForm from '@/views/blog/BlogPostSearchForm.vue'
import BlogPostDetailDialog from '@/views/blog/BlogPostDetailDialog.vue'
import BlogPostCreateDialog from '@/views/blog/BlogPostCreateDialog.vue'
import BlogPostEditDialog from '@/views/blog/BlogPostEditDialog.vue'
import { getBlogPostPage, deleteBlogPostById } from '@/views/blog/BlogPostApi'

const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const tableData = ref([])
const searchParams = ref({ keyword: '', status: '' })

const detailVisible = ref(false)
const createVisible = ref(false)
const editVisible = ref(false)
const detailId = ref(null)
const editId = ref(null)

const tagType = (s) => {
  if (s === 'PUBLISHED') return 'success'
  if (s === 'ARCHIVED') return 'info'
  return 'warning'
}

const loadPage = async () => {
  loading.value = true
  try {
    const res = await getBlogPostPage(currentPage.value, pageSize.value, {
      keyword: searchParams.value.keyword || undefined,
      status: searchParams.value.status || undefined
    })
    tableData.value = res.records || []
    total.value = res.total || 0
    currentPage.value = res.page || currentPage.value
    pageSize.value = res.size || pageSize.value
  } finally {
    loading.value = false
  }
}

const handleSearch = (params) => {
  searchParams.value = params || { keyword: '', status: '' }
  currentPage.value = 1
  loadPage()
}

const handleReset = (params) => {
  searchParams.value = params || { keyword: '', status: '' }
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

const handleCreate = () => {
  createVisible.value = true
}

const handleCreateSubmit = () => {
  createVisible.value = false
  loadPage()
}
const handleCreateCancel = () => {
  createVisible.value = false
}

const handleDetail = (row) => {
  detailId.value = row.id
  detailVisible.value = true
}
const handleDetailClose = () => {
  detailVisible.value = false
  detailId.value = null
}

const handleEdit = (row) => {
  editId.value = row.id
  editVisible.value = true
}
const handleEditSubmit = () => {
  editVisible.value = false
  editId.value = null
  loadPage()
}
const handleEditCancel = () => {
  editVisible.value = false
  editId.value = null
}

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
  margin-bottom: 12px;
  display: flex;
  gap: 10px;
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

