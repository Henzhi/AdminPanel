<template>
  <div class="artifact-list">
    <div class="page-header">
      <h2>文物管理</h2>
      <div class="header-actions">
        <el-upload
          ref="importUploadRef"
          :show-file-list="false"
          :before-upload="handleImport"
          accept=".csv"
        >
          <el-button type="success" :icon="Upload" :loading="importLoading">
            导入CSV
          </el-button>
        </el-upload>
        <el-button type="warning" :icon="Download" @click="handleExport">
          导出CSV
        </el-button>
        <el-button type="primary" :icon="Plus" @click="handleAdd">
          新增文物
        </el-button>
      </div>
    </div>

    <!-- 搜索表单 -->
    <el-card class="search-card" shadow="never">
      <el-form :model="queryParams" inline class="search-form">
        <el-form-item label="关键词">
          <el-input
            v-model="queryParams.keyword"
            placeholder="名称/描述"
            clearable
            style="width: 200px"
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="朝代">
          <el-input
            v-model="queryParams.era"
            placeholder="朝代"
            clearable
            style="width: 150px"
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="分类">
          <el-select
            v-model="queryParams.category"
            placeholder="全部分类"
            clearable
            style="width: 150px"
          >
            <el-option
              v-for="cat in categories"
              :key="cat"
              :label="cat"
              :value="cat"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select
            v-model="queryParams.status"
            placeholder="全部状态"
            clearable
            style="width: 120px"
          >
            <el-option label="在展" :value="1" />
            <el-option label="库存" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="handleSearch">查询</el-button>
          <el-button :icon="Refresh" @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 数据表格 -->
    <el-card shadow="never">
      <el-table
        v-loading="loading"
        :data="tableData"
        stripe
        border
        style="width: 100%"
      >
        <el-table-column type="index" label="#" width="60" align="center" />
        <el-table-column label="图片" width="80" align="center">
          <template #default="{ row }">
            <el-image
              v-if="row.imageUrl"
              :src="row.imageUrl"
              :preview-src-list="[row.imageUrl]"
              fit="cover"
              style="width: 50px; height: 50px; border-radius: 4px"
              preview-teleported
            />
            <el-icon v-else size="32" color="#c0c4cc"><Picture /></el-icon>
          </template>
        </el-table-column>
        <el-table-column prop="name" label="名称" min-width="160" show-overflow-tooltip />
        <el-table-column prop="era" label="朝代" width="100" />
        <el-table-column prop="category" label="分类" width="120" />
        <el-table-column prop="status" label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
              {{ row.status === 1 ? '在展' : '库存' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="描述" min-width="200" show-overflow-tooltip />
        <el-table-column label="创建时间" width="170">
          <template #default="{ row }">
            {{ formatDateTime(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right" align="center">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleView(row)">查看</el-button>
            <el-button type="warning" link size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button type="danger" link size="small" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="queryParams.page"
          v-model:page-size="queryParams.size"
          :total="total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          background
          @size-change="loadData"
          @current-change="loadData"
        />
      </div>
    </el-card>

    <!-- 新增/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogType === 'add' ? '新增文物' : '编辑文物'"
      width="640px"
      @close="resetForm"
    >
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="100px"
      >
        <el-form-item label="名称" prop="name">
          <el-input v-model="formData.name" placeholder="请输入文物名称" />
        </el-form-item>
        <el-form-item label="朝代" prop="era">
          <el-input v-model="formData.era" placeholder="如：唐代、宋代" />
        </el-form-item>
        <el-form-item label="分类" prop="category">
          <el-select
            v-model="formData.category"
            placeholder="请选择分类"
            allow-create
            filterable
            style="width: 100%"
          >
            <el-option
              v-for="cat in categories"
              :key="cat"
              :label="cat"
              :value="cat"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="formData.status">
            <el-radio :label="1">在展</el-radio>
            <el-radio :label="0">库存</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="封面图片">
          <el-upload
            class="image-uploader"
            :show-file-list="false"
            :before-upload="handleUploadImage"
            accept="image/*"
          >
            <img v-if="formData.imageUrl" :src="formData.imageUrl" class="preview-image" />
            <el-icon v-else class="uploader-icon"><Plus /></el-icon>
          </el-upload>
        </el-form-item>
        <el-form-item label="描述">
          <el-input
            v-model="formData.description"
            type="textarea"
            :rows="4"
            placeholder="请输入文物描述"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="submitForm">确定</el-button>
      </template>
    </el-dialog>

    <!-- 详情对话框 -->
    <el-dialog v-model="detailVisible" title="文物详情" width="640px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="名称">{{ detailData.name }}</el-descriptions-item>
        <el-descriptions-item label="朝代">{{ detailData.era || '-' }}</el-descriptions-item>
        <el-descriptions-item label="分类">{{ detailData.category || '-' }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="detailData.status === 1 ? 'success' : 'info'" size="small">
            {{ detailData.status === 1 ? '在展' : '库存' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ formatDateTime(detailData.createTime) }}</el-descriptions-item>
        <el-descriptions-item label="更新时间">{{ formatDateTime(detailData.updateTime) }}</el-descriptions-item>
        <el-descriptions-item label="描述" :span="2">{{ detailData.description || '-' }}</el-descriptions-item>
        <el-descriptions-item label="图片" :span="2">
          <el-image
            v-if="detailData.imageUrl"
            :src="detailData.imageUrl"
            :preview-src-list="[detailData.imageUrl]"
            fit="contain"
            style="width: 100%; max-height: 300px"
            preview-teleported
          />
          <span v-else>暂无图片</span>
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search, Refresh, Download, Upload, Picture } from '@element-plus/icons-vue'
import {
  getArtifactList, getArtifactDetail, createArtifact, updateArtifact,
  deleteArtifact, importArtifacts, exportArtifacts, uploadImage, getCategories,
} from '@/api/artifact'
import { formatDateTime, downloadBlob } from '@/utils/format'

const loading = ref(false)
const submitLoading = ref(false)
const importLoading = ref(false)
const tableData = ref([])
const total = ref(0)
const categories = ref([])

const queryParams = reactive({
  page: 1,
  size: 10,
  keyword: '',
  era: '',
  category: '',
  status: undefined,
})

const dialogVisible = ref(false)
const dialogType = ref('add')
const formRef = ref(null)
const formData = reactive({
  id: null,
  name: '',
  era: '',
  category: '',
  description: '',
  imageUrl: '',
  status: 1,
})

const formRules = {
  name: [{ required: true, message: '请输入文物名称', trigger: 'blur' }],
}

const detailVisible = ref(false)
const detailData = ref({})

async function loadData() {
  loading.value = true
  try {
    const res = await getArtifactList(queryParams)
    tableData.value = res.data?.list || []
    total.value = res.data?.total || 0
  } catch (e) {
    // 错误已处理
  } finally {
    loading.value = false
  }
}

async function loadCategories() {
  try {
    const res = await getCategories()
    categories.value = res.data || []
  } catch (e) {
    // 忽略
  }
}

function handleSearch() {
  queryParams.page = 1
  loadData()
}

function handleReset() {
  queryParams.keyword = ''
  queryParams.era = ''
  queryParams.category = ''
  queryParams.status = undefined
  queryParams.page = 1
  loadData()
}

function handleAdd() {
  dialogType.value = 'add'
  resetForm()
  dialogVisible.value = true
}

function handleEdit(row) {
  dialogType.value = 'edit'
  resetForm()
  Object.assign(formData, {
    id: row.id,
    name: row.name,
    era: row.era,
    category: row.category,
    description: row.description,
    imageUrl: row.imageUrl,
    status: row.status,
  })
  dialogVisible.value = true
}

async function handleView(row) {
  try {
    const res = await getArtifactDetail(row.id)
    detailData.value = res.data || {}
    detailVisible.value = true
  } catch (e) {
    // 错误已处理
  }
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(`确定要删除文物"${row.name}"吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    })
    await deleteArtifact(row.id)
    ElMessage.success('删除成功')
    loadData()
  } catch (e) {
    // 取消或错误
  }
}

function resetForm() {
  formData.id = null
  formData.name = ''
  formData.era = ''
  formData.category = ''
  formData.description = ''
  formData.imageUrl = ''
  formData.status = 1
  formRef.value?.clearValidate()
}

async function submitForm() {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    submitLoading.value = true
    try {
      const data = {
        name: formData.name,
        era: formData.era,
        category: formData.category,
        description: formData.description,
        imageUrl: formData.imageUrl,
        status: formData.status,
      }
      if (dialogType.value === 'add') {
        await createArtifact(data)
        ElMessage.success('新增成功')
      } else {
        await updateArtifact(formData.id, data)
        ElMessage.success('更新成功')
      }
      dialogVisible.value = false
      loadData()
    } catch (e) {
      // 错误已处理
    } finally {
      submitLoading.value = false
    }
  })
}

async function handleUploadImage(file) {
  try {
    const res = await uploadImage(file)
    formData.imageUrl = res.data
    ElMessage.success('图片上传成功')
  } catch (e) {
    // 错误已处理
  }
  return false
}

async function handleImport(file) {
  importLoading.value = true
  try {
    const res = await importArtifacts(file)
    ElMessage.success(`导入成功，共 ${res.data} 条记录`)
    loadData()
  } catch (e) {
    // 错误已处理
  } finally {
    importLoading.value = false
  }
  return false
}

async function handleExport() {
  try {
    const blob = await exportArtifacts(queryParams)
    downloadBlob(blob, '文物数据.csv')
    ElMessage.success('导出成功')
  } catch (e) {
    // 错误已处理
  }
}

onMounted(() => {
  loadData()
  loadCategories()
})
</script>

<style scoped>
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.page-header h2 {
  font-size: 20px;
  font-weight: 600;
  margin: 0;
}

.header-actions {
  display: flex;
  gap: 8px;
}

.search-card {
  margin-bottom: 16px;
}

.search-form {
  margin-bottom: 0;
}

.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}

.image-uploader {
  width: 120px;
  height: 120px;
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
}

.image-uploader:hover {
  border-color: #409eff;
}

.preview-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.uploader-icon {
  font-size: 28px;
  color: #8c939d;
}
</style>
