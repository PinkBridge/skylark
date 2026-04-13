<template>
  <div style="display: block;width: 100%;">
    <el-upload 
      ref="uploadRef"
      v-model:file-list="fileList" 
      list-type="picture-card" 
      :on-preview="handlePictureCardPreview" 
      :on-remove="handleRemove"
      :http-request="handleUpload"
      :before-upload="beforeUpload"
      :limit="1"
      :on-exceed="handleExceed"
      :auto-upload="true"
      accept="image/*">
      <el-icon>
        <Plus />
      </el-icon>
    </el-upload>
    <el-dialog v-model="dialogVisible">
      <img w-full :src="dialogImageUrl" alt="Preview Image" />
    </el-dialog>
  </div>
</template>

<script setup name="ResourceUpload">
import { ref, nextTick, watch } from 'vue'
import { Plus } from '@element-plus/icons-vue'
import { uploadResource } from '@/views/resources/ResourceApi'

const dialogVisible = ref(false)
const dialogImageUrl = ref('')
const uploadRef = ref(null)

const props = defineProps({
  fileList: {
    type: [Array, String],
    default: () => []
  },
  onSuccess: {
    type: Function,
    default: null
  }
})

const normalizeFileList = (value) => {
  if (!value) {
    return []
  }
  
  if (typeof value === 'string') {
    return [{
      name: value.split('/').pop() || 'file',
      url: value,
      status: 'success'
    }]
  }
  
  if (Array.isArray(value)) {
    return value.map(item => {
      if (typeof item === 'string') {
        return {
          name: item.split('/').pop() || 'file',
          url: item,
          status: 'success'
        }
      }
      if (typeof item === 'object' && item !== null) {
        return {
          name: item.name || item.url?.split('/').pop() || 'file',
          url: item.url || item,
          status: item.status || 'success',
          ...item
        }
      }
      return item
    }).filter(item => item !== null && item !== undefined)
  }
  
  return []
}

const fileList = ref(normalizeFileList(props.fileList))

watch(() => props.fileList, (newVal) => {
  fileList.value = normalizeFileList(newVal)
}, { immediate: true, deep: true })


const beforeUpload = () => {
  if (fileList.value.length > 0) {
    fileList.value = []
  }
  return true
}

const handleUpload = async (options) => {
  const { file } = options
  
  try {
    const response = await uploadResource(file)
    console.log('handleUpload response', response)
    
    let resourceData = null
    if (response && response.data) {
      resourceData = response.data
    } else if (response) {
      resourceData = response
    }
    
    if (resourceData && resourceData.url) {
      fileList.value = [{
        name: resourceData.name || resourceData.originalName || file.name,
        url: resourceData.url || resourceData.filePath,
        status: 'success',
        response: resourceData
      }]
      
      if (props.onSuccess && typeof props.onSuccess === 'function') {
        props.onSuccess(resourceData.url, resourceData)
      }
    } else {
      const reader = new FileReader()
      reader.onload = (e) => {
        fileList.value = [{
          name: file.name,
          url: e.target.result,
          status: 'success'
        }]
      }
      reader.readAsDataURL(file)
    }
  } catch (error) {
    console.error('Upload error:', error)
    fileList.value = []
  }
}


const handleRemove = () => {
  fileList.value = []
}

const handlePictureCardPreview = (uploadFile) => {
  dialogImageUrl.value = uploadFile.url || uploadFile.response?.url
  dialogVisible.value = true
}

const handleExceed = (files) => {
  
  fileList.value = []
  
  if (files && files.length > 0) {
    const newFile = files[0]
    nextTick(() => {
      handleUpload({ file: newFile })
    })
  }
}
</script>