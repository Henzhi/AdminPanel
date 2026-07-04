<template>
  <div class="knowledge-list">
    <div class="page-header">
      <h2>知识图谱管理</h2>
      <el-button type="primary" :icon="Plus" @click="handleAdd">新增三元组</el-button>
    </div>

    <el-card class="search-card" shadow="never">
      <el-form :model="queryParams" inline class="search-form">
        <el-form-item label="关键词">
          <el-input
            v-model="queryParams.keyword"
            placeholder="主体/关系/客体"
            clearable
            style="width: 240px"
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="handleSearch">查询</el-button>
          <el-button :icon="Refresh" @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never">
      <el-table v-loading="loading" :data="tableData" stripe border style="width: 100%">
        <el-table-column type="index" label="#" width="60" align="center" />
        <el-table-column prop="subjectEntity" label="主体实体" min-width="150" show-overflow-tooltip />
        <el-table-column prop="relation" label="关系" width="150">
          <template #default="{ row }">
            <el-tag size="small" type="warning">{{ row.relation }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="objectEntity" label="客体实体" min-width="150" show-overflow-tooltip />
        <el-table-column prop="artifactId" label="关联文物ID" width="120" align="center">
          <template #default="{ row }">
            {{ row.artifactId || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="创建时间" width="170">
          <template #default="{ row }">
            {{ formatDateTime(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right" align="center">
          <template #default="{ row }">
            <el-button type="info" link size="small" @click="handleSync(row)">同步</el-button>
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
      :title="dialogType === 'add' ? '新增三元组' : '编辑三元组'"
      width="540px"
      @close="resetForm"
    >
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="100px">
        <el-form-item label="主体实体" prop="subjectEntity">
          <el-input v-model="formData.subjectEntity" placeholder="如：李白" />
        </el-form-item>
        <el-form-item label="关系" prop="relation">
          <el-input v-model="formData.relation" placeholder="如：创作" />
        </el-form-item>
        <el-form-item label="客体实体" prop="objectEntity">
          <el-input v-model="formData.objectEntity" placeholder="如：将进酒" />
        </el-form-item>
        <el-form-item label="关联文物ID">
          <el-input-number
            v-model="formData.artifactId"
            :min="1"
            placeholder="可选"
            style="width: 100%"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="submitForm">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search, Refresh } from '@element-plus/icons-vue'
import {
  getKnowledgeList, createKnowledge, updateKnowledge,
  deleteKnowledge, syncKnowledge,
} from '@/api/knowledge'
import { formatDateTime } from '@/utils/format'

const loading = ref(false)
const submitLoading = ref(false)
const tableData = ref([])
const total = ref(0)

const queryParams = reactive({
  page: 1,
  size: 10,
  keyword: '',
})

const dialogVisible = ref(false)
const dialogType = ref('add')
const formRef = ref(null)
const formData = reactive({
  id: null,
  subjectEntity: '',
  relation: '',
  objectEntity: '',
  artifactId: null,
})

const formRules = {
  subjectEntity: [{ required: true, message: '请输入主体实体', trigger: 'blur' }],
  relation: [{ required: true, message: '请输入关系', trigger: 'blur' }],
  objectEntity: [{ required: true, message: '请输入客体实体', trigger: 'blur' }],
}

async function loadData() {
  loading.value = true
  try {
    const res = await getKnowledgeList(queryParams)
    tableData.value = res.data?.list || []
    total.value = res.data?.total || 0
  } catch (e) {
    // 错误已处理
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  queryParams.page = 1
  loadData()
}

function handleReset() {
  queryParams.keyword = ''
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
    subjectEntity: row.subjectEntity,
    relation: row.relation,
    objectEntity: row.objectEntity,
    artifactId: row.artifactId,
  })
  dialogVisible.value = true
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm('确定要删除该三元组吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    })
    await deleteKnowledge(row.id)
    ElMessage.success('删除成功')
    loadData()
  } catch (e) {
    // 取消或错误
  }
}

async function handleSync(row) {
  try {
    await syncKnowledge(row.id)
    ElMessage.success('同步成功')
  } catch (e) {
    // 错误已处理
  }
}

function resetForm() {
  formData.id = null
  formData.subjectEntity = ''
  formData.relation = ''
  formData.objectEntity = ''
  formData.artifactId = null
  formRef.value?.clearValidate()
}

async function submitForm() {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    submitLoading.value = true
    try {
      const data = {
        subjectEntity: formData.subjectEntity,
        relation: formData.relation,
        objectEntity: formData.objectEntity,
        artifactId: formData.artifactId,
      }
      if (dialogType.value === 'add') {
        await createKnowledge(data)
        ElMessage.success('新增成功')
      } else {
        await updateKnowledge(formData.id, data)
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

onMounted(() => {
  loadData()
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
</style>
