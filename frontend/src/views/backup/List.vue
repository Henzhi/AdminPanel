<template>
  <div class="backup-list">
    <div class="page-header">
      <h2>备份管理</h2>
      <div class="header-actions">
        <el-button type="primary" :icon="Plus" :loading="createLoading" @click="handleCreate(false)">
          立即备份
        </el-button>
        <el-button type="warning" :icon="Lock" :loading="createLoading" @click="handleCreate(true)">
          加密备份
        </el-button>
      </div>
    </div>

    <!-- 维护状态提示 -->
    <el-alert
      v-if="maintaining"
      title="系统正在恢复中，请稍候..."
      type="warning"
      :closable="false"
      show-icon
      style="margin-bottom: 16px"
    />

    <el-card class="search-card" shadow="never">
      <el-form :model="queryParams" inline class="search-form">
        <el-form-item label="备份类型">
          <el-select v-model="queryParams.backupType" placeholder="全部" clearable style="width: 140px">
            <el-option label="全量备份" value="FULL" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryParams.status" placeholder="全部" clearable style="width: 120px">
            <el-option label="成功" :value="1" />
            <el-option label="失败" :value="0" />
          </el-select>
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
        <el-table-column prop="filename" label="文件名" min-width="200" show-overflow-tooltip />
        <el-table-column prop="backupType" label="类型" width="100" align="center">
          <template #default="{ row }">
            <el-tag size="small">{{ row.backupType || 'FULL' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="fileSize" label="大小" width="120">
          <template #default="{ row }">
            {{ formatFileSize(row.fileSize) }}
          </template>
        </el-table-column>
        <el-table-column prop="isEncrypted" label="加密" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.isEncrypted === 1 ? 'danger' : 'info'" size="small">
              {{ row.isEncrypted === 1 ? '是' : '否' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="operatorName" label="操作人" width="120" />
        <el-table-column prop="status" label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
              {{ row.status === 1 ? '成功' : '失败' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="备份时间" width="170">
          <template #default="{ row }">
            {{ formatDateTime(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right" align="center">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleDownload(row)">下载</el-button>
            <el-button
              v-if="userStore.hasRole('SUPER_ADMIN')"
              type="danger"
              link
              size="small"
              @click="handleRestore(row)"
            >
              恢复
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="queryParams.page"
          v-model:page-size="queryParams.size"
          :total="total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next, jumper"
          background
          @size-change="loadData"
          @current-change="loadData"
        />
      </div>
    </el-card>

    <!-- 恢复确认对话框 (2FA) -->
    <el-dialog v-model="restoreDialogVisible" title="恢复确认" width="480px">
      <el-alert
        title="危险操作"
        type="error"
        description="恢复操作将覆盖当前数据库的所有数据，此操作不可逆！"
        :closable="false"
        show-icon
        style="margin-bottom: 16px"
      />
      <el-form :model="restoreForm" label-width="100px">
        <el-form-item label="备份文件">
          <span>{{ restoreForm.filename }}</span>
        </el-form-item>
        <el-form-item label="备份时间">
          <span>{{ formatDateTime(restoreForm.createTime) }}</span>
        </el-form-item>
        <el-form-item label="确认密码">
          <el-input
            v-model="restoreForm.confirmPassword"
            type="password"
            placeholder="请输入您的登录密码以确认"
            show-password
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="restoreDialogVisible = false">取消</el-button>
        <el-button
          type="danger"
          :loading="restoreLoading"
          @click="confirmRestore"
        >
          确认恢复
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search, Refresh, Lock } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import {
  getBackupList, createBackup, restoreBackup, downloadBackup, getBackupStatus,
} from '@/api/backup'
import { formatDateTime, formatFileSize, downloadBlob } from '@/utils/format'

const userStore = useUserStore()

const loading = ref(false)
const createLoading = ref(false)
const restoreLoading = ref(false)
const maintaining = ref(false)
const tableData = ref([])
const total = ref(0)

const queryParams = reactive({
  page: 1,
  size: 10,
  backupType: '',
  status: undefined,
})

const restoreDialogVisible = ref(false)
const restoreForm = reactive({
  id: null,
  filename: '',
  createTime: null,
  confirmPassword: '',
})

async function loadData() {
  loading.value = true
  try {
    const res = await getBackupList(queryParams)
    tableData.value = res.data?.list || []
    total.value = res.data?.total || 0
  } catch (e) {
    // 错误已处理
  } finally {
    loading.value = false
  }
}

async function loadStatus() {
  try {
    const res = await getBackupStatus()
    maintaining.value = res.data?.maintaining || false
  } catch (e) {
    // 忽略
  }
}

function handleSearch() {
  queryParams.page = 1
  loadData()
}

function handleReset() {
  queryParams.backupType = ''
  queryParams.status = undefined
  queryParams.page = 1
  loadData()
}

async function handleCreate(encrypt) {
  try {
    await ElMessageBox.confirm(
      `确定要执行${encrypt ? '加密' : ''}全量备份吗？`,
      '提示',
      { confirmButtonText: '确定', cancelButtonText: '取消', type: 'info' }
    )
    createLoading.value = true
    await createBackup({ encrypt })
    ElMessage.success('备份创建成功')
    loadData()
  } catch (e) {
    // 取消或错误
  } finally {
    createLoading.value = false
  }
}

function handleRestore(row) {
  restoreForm.id = row.id
  restoreForm.filename = row.filename
  restoreForm.createTime = row.createTime
  restoreForm.confirmPassword = ''
  restoreDialogVisible.value = true
}

async function confirmRestore() {
  if (!restoreForm.confirmPassword) {
    ElMessage.warning('请输入确认密码')
    return
  }
  restoreLoading.value = true
  try {
    await restoreBackup(restoreForm.id, {
      backupId: restoreForm.id,
      confirmPassword: restoreForm.confirmPassword,
    })
    ElMessage.success('恢复操作已提交，系统进入维护状态')
    restoreDialogVisible.value = false
    loadStatus()
  } catch (e) {
    // 错误已处理
  } finally {
    restoreLoading.value = false
  }
}

async function handleDownload(row) {
  try {
    const blob = await downloadBackup(row.id)
    downloadBlob(blob, row.filename || 'backup.sql')
  } catch (e) {
    // 错误已处理
  }
}

onMounted(() => {
  loadData()
  loadStatus()
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
</style>
