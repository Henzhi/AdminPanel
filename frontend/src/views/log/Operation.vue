<template>
  <div class="log-page">
    <div class="page-header">
      <h2>操作日志</h2>
      <el-button type="warning" :icon="Download" @click="handleExport">导出CSV</el-button>
    </div>

    <el-card class="search-card" shadow="never">
      <el-form :model="queryParams" inline class="search-form">
        <el-form-item label="操作人">
          <el-input
            v-model="queryParams.operatorName"
            placeholder="操作人"
            clearable
            style="width: 150px"
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="操作类型">
          <el-select v-model="queryParams.operationType" placeholder="全部" clearable style="width: 140px">
            <el-option label="新增" value="CREATE" />
            <el-option label="更新" value="UPDATE" />
            <el-option label="删除" value="DELETE" />
            <el-option label="查询" value="QUERY" />
            <el-option label="导出" value="EXPORT" />
            <el-option label="导入" value="IMPORT" />
          </el-select>
        </el-form-item>
        <el-form-item label="关键词">
          <el-input
            v-model="queryParams.keyword"
            placeholder="目标/URL"
            clearable
            style="width: 180px"
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="时间范围">
          <el-date-picker
            v-model="dateRange"
            type="datetimerange"
            range-separator="至"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
            value-format="YYYY-MM-DD HH:mm:ss"
            style="width: 360px"
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
        <el-table-column prop="operatorName" label="操作人" width="120" />
        <el-table-column prop="operationType" label="类型" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getOperationTypeTag(row.operationType)" size="small">
              {{ row.operationType || '-' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="operationTarget" label="操作目标" min-width="150" show-overflow-tooltip />
        <el-table-column prop="requestMethod" label="方法" width="80" align="center">
          <template #default="{ row }">
            <el-tag size="small" :type="getMethodTag(row.requestMethod)">{{ row.requestMethod }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="requestUrl" label="请求URL" min-width="200" show-overflow-tooltip />
        <el-table-column prop="ip" label="IP" width="130" />
        <el-table-column prop="costTime" label="耗时" width="90" align="center">
          <template #default="{ row }">
            {{ row.costTime ? row.costTime + 'ms' : '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
              {{ row.status === 1 ? '成功' : '失败' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="时间" width="170">
          <template #default="{ row }">
            {{ formatDateTime(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right" align="center">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleView(row)">详情</el-button>
            <el-button
              v-if="userStore.hasRole('SUPER_ADMIN')"
              type="danger"
              link
              size="small"
              @click="handleDelete(row)"
            >
              删除
            </el-button>
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

    <!-- 详情对话框 -->
    <el-dialog v-model="detailVisible" title="日志详情" width="720px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="日志类型">{{ detailData.logType }}</el-descriptions-item>
        <el-descriptions-item label="操作类型">{{ detailData.operationType || '-' }}</el-descriptions-item>
        <el-descriptions-item label="操作人">{{ detailData.operatorName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="操作人ID">{{ detailData.operatorId || '-' }}</el-descriptions-item>
        <el-descriptions-item label="操作目标">{{ detailData.operationTarget || '-' }}</el-descriptions-item>
        <el-descriptions-item label="方法">{{ detailData.method || '-' }}</el-descriptions-item>
        <el-descriptions-item label="请求方法">{{ detailData.requestMethod || '-' }}</el-descriptions-item>
        <el-descriptions-item label="请求URL">{{ detailData.requestUrl || '-' }}</el-descriptions-item>
        <el-descriptions-item label="IP">{{ detailData.ip || '-' }}</el-descriptions-item>
        <el-descriptions-item label="耗时">{{ detailData.costTime ? detailData.costTime + 'ms' : '-' }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="detailData.status === 1 ? 'success' : 'danger'" size="small">
            {{ detailData.status === 1 ? '成功' : '失败' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="时间">{{ formatDateTime(detailData.createTime) }}</el-descriptions-item>
        <el-descriptions-item label="请求参数" :span="2">
          <pre class="json-content">{{ formatJson(detailData.requestParams) }}</pre>
        </el-descriptions-item>
        <el-descriptions-item v-if="detailData.errorMsg" label="错误信息" :span="2">
          <span style="color: #f56c6c">{{ detailData.errorMsg }}</span>
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, watch, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, Download } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { getOperationLogs, getLogDetail, deleteLog, exportLogs } from '@/api/log'
import { formatDateTime, getOperationTypeTag, downloadBlob, safeJsonParse } from '@/utils/format'

const userStore = useUserStore()
const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const dateRange = ref([])

const queryParams = reactive({
  page: 1,
  size: 10,
  operatorName: '',
  operationType: '',
  keyword: '',
  startDate: '',
  endDate: '',
})

const detailVisible = ref(false)
const detailData = ref({})

watch(dateRange, (val) => {
  if (val && val.length === 2) {
    queryParams.startDate = val[0]
    queryParams.endDate = val[1]
  } else {
    queryParams.startDate = ''
    queryParams.endDate = ''
  }
})

async function loadData() {
  loading.value = true
  try {
    const res = await getOperationLogs(queryParams)
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
  queryParams.operatorName = ''
  queryParams.operationType = ''
  queryParams.keyword = ''
  queryParams.startDate = ''
  queryParams.endDate = ''
  dateRange.value = []
  queryParams.page = 1
  loadData()
}

async function handleView(row) {
  try {
    const res = await getLogDetail(row.id)
    detailData.value = res.data || {}
    detailVisible.value = true
  } catch (e) {
    // 错误已处理
  }
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm('确定要删除该日志记录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    })
    await deleteLog(row.id)
    ElMessage.success('删除成功')
    loadData()
  } catch (e) {
    // 取消或错误
  }
}

async function handleExport() {
  try {
    const blob = await exportLogs(queryParams)
    downloadBlob(blob, '操作日志.csv')
    ElMessage.success('导出成功')
  } catch (e) {
    // 错误已处理
  }
}

function getMethodTag(method) {
  const map = { GET: 'info', POST: 'success', PUT: 'warning', DELETE: 'danger' }
  return map[method] || 'info'
}

function formatJson(str) {
  const obj = safeJsonParse(str, null)
  if (obj === null) return str || '-'
  try {
    return JSON.stringify(obj, null, 2)
  } catch (e) {
    return str || '-'
  }
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

.json-content {
  background: #f5f7fa;
  padding: 12px;
  border-radius: 4px;
  font-family: 'Consolas', 'Monaco', monospace;
  font-size: 12px;
  max-height: 240px;
  overflow: auto;
  white-space: pre-wrap;
  word-break: break-all;
  margin: 0;
}
</style>
