<template>
  <div class="user-list">
    <div class="page-header">
      <h2>普通用户管理</h2>
      <el-button type="primary" :icon="Plus" @click="handleAdd">新增用户</el-button>
    </div>

    <el-card class="search-card" shadow="never">
      <el-form :model="queryParams" inline class="search-form">
        <el-form-item label="用户名">
          <el-input
            v-model="queryParams.username"
            placeholder="用户名"
            clearable
            style="width: 180px"
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryParams.status" placeholder="全部状态" clearable style="width: 140px">
            <el-option label="正常" :value="1" />
            <el-option label="禁用" :value="0" />
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
        <el-table-column label="头像" width="70" align="center">
          <template #default="{ row }">
            <el-avatar :size="36" :src="row.avatar">
              {{ row.nickname?.charAt(0) || row.username?.charAt(0) }}
            </el-avatar>
          </template>
        </el-table-column>
        <el-table-column prop="username" label="用户名" width="140" />
        <el-table-column prop="nickname" label="昵称" width="140" />
        <el-table-column prop="gender" label="性别" width="80" align="center">
          <template #default="{ row }">
            {{ getGenderText(row.gender) }}
          </template>
        </el-table-column>
        <el-table-column prop="email" label="邮箱" min-width="180" show-overflow-tooltip />
        <el-table-column prop="phone" label="手机号" width="140" />
        <el-table-column prop="status" label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusTag(row.status)" size="small">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="最后登录" width="170">
          <template #default="{ row }">
            {{ formatDateTime(row.lastLoginTime) }}
          </template>
        </el-table-column>
        <el-table-column label="注册时间" width="170">
          <template #default="{ row }">
            {{ formatDateTime(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="240" fixed="right" align="center">
          <template #default="{ row }">
            <el-button type="warning" link size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button type="info" link size="small" @click="handleResetPwd(row)">重置密码</el-button>
            <el-button
              :type="row.status === 1 ? 'danger' : 'success'"
              link
              size="small"
              @click="handleToggleStatus(row)"
            >
              {{ row.status === 1 ? '禁用' : '启用' }}
            </el-button>
            <el-button type="danger" link size="small" @click="handleDelete(row)">删除</el-button>
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

    <!-- 新增/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogType === 'add' ? '新增用户' : '编辑用户'"
      width="560px"
      @close="resetForm"
    >
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="100px">
        <el-form-item label="用户名" prop="username">
          <el-input
            v-model="formData.username"
            placeholder="请输入用户名"
            :disabled="dialogType === 'edit'"
          />
        </el-form-item>
        <el-form-item v-if="dialogType === 'add'" label="密码" prop="password">
          <el-input v-model="formData.password" type="password" show-password placeholder="请输入密码" />
        </el-form-item>
        <el-form-item label="昵称" prop="nickname">
          <el-input v-model="formData.nickname" placeholder="请输入昵称" />
        </el-form-item>
        <el-form-item label="性别" prop="gender">
          <el-radio-group v-model="formData.gender">
            <el-radio :label="1">男</el-radio>
            <el-radio :label="2">女</el-radio>
            <el-radio :label="0">未知</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="formData.email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="formData.phone" placeholder="请输入手机号" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="submitForm">确定</el-button>
      </template>
    </el-dialog>

    <!-- 重置密码对话框 -->
    <el-dialog v-model="resetPwdVisible" title="重置密码" width="440px">
      <el-form :model="resetPwdForm" label-width="100px">
        <el-form-item label="用户名">
          <span>{{ resetPwdForm.username }}</span>
        </el-form-item>
        <el-form-item label="新密码" required>
          <el-input
            v-model="resetPwdForm.newPassword"
            type="password"
            show-password
            placeholder="请输入新密码"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="resetPwdVisible = false">取消</el-button>
        <el-button type="primary" :loading="resetPwdLoading" @click="submitResetPwd">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search, Refresh } from '@element-plus/icons-vue'
import {
  getUserList, createUser, updateUser, deleteUser,
  resetUserPassword, toggleUserStatus,
} from '@/api/user'
import { formatDateTime } from '@/utils/format'

const loading = ref(false)
const submitLoading = ref(false)
const resetPwdLoading = ref(false)
const tableData = ref([])
const total = ref(0)

const queryParams = reactive({
  page: 1,
  size: 10,
  username: '',
  status: undefined,
})

const dialogVisible = ref(false)
const dialogType = ref('add')
const formRef = ref(null)
const formData = reactive({
  id: null,
  username: '',
  password: '',
  nickname: '',
  email: '',
  phone: '',
  gender: 0,
})

const formRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 8, message: '密码长度不能少于8位', trigger: 'blur' },
  ],
  email: [{ type: 'email', message: '邮箱格式不正确', trigger: 'blur' }],
}

const resetPwdVisible = ref(false)
const resetPwdForm = reactive({
  id: null,
  username: '',
  newPassword: '',
})

async function loadData() {
  loading.value = true
  try {
    const res = await getUserList(queryParams)
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
  queryParams.username = ''
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
    username: row.username,
    password: '',
    nickname: row.nickname,
    email: row.email,
    phone: row.phone,
    gender: row.gender ?? 0,
  })
  dialogVisible.value = true
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(`确定要删除用户"${row.username}"吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    })
    await deleteUser(row.id)
    ElMessage.success('删除成功')
    loadData()
  } catch (e) {
    // 取消或错误
  }
}

async function handleToggleStatus(row) {
  try {
    const action = row.status === 1 ? '禁用' : '启用'
    await ElMessageBox.confirm(`确定要${action}用户"${row.username}"吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    })
    await toggleUserStatus(row.id)
    ElMessage.success(`${action}成功`)
    loadData()
  } catch (e) {
    // 取消或错误
  }
}

function handleResetPwd(row) {
  resetPwdForm.id = row.id
  resetPwdForm.username = row.username
  resetPwdForm.newPassword = ''
  resetPwdVisible.value = true
}

async function submitResetPwd() {
  if (!resetPwdForm.newPassword) {
    ElMessage.warning('请输入新密码')
    return
  }
  if (resetPwdForm.newPassword.length < 8) {
    ElMessage.warning('密码长度不能少于8位')
    return
  }
  resetPwdLoading.value = true
  try {
    await resetUserPassword(resetPwdForm.id, { newPassword: resetPwdForm.newPassword })
    ElMessage.success('密码重置成功')
    resetPwdVisible.value = false
  } catch (e) {
    // 错误已处理
  } finally {
    resetPwdLoading.value = false
  }
}

function resetForm() {
  formData.id = null
  formData.username = ''
  formData.password = ''
  formData.nickname = ''
  formData.email = ''
  formData.phone = ''
  formData.gender = 0
  formRef.value?.clearValidate()
}

async function submitForm() {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    submitLoading.value = true
    try {
      const data = {
        username: formData.username,
        nickname: formData.nickname,
        email: formData.email,
        phone: formData.phone,
        gender: formData.gender,
      }
      if (dialogType.value === 'add') {
        data.password = formData.password
        await createUser(data)
        ElMessage.success('新增成功')
      } else {
        if (formData.password) {
          data.password = formData.password
        }
        await updateUser(formData.id, data)
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

function getGenderText(gender) {
  if (gender === 1) return '男'
  if (gender === 2) return '女'
  return '未知'
}

function getStatusTag(status) {
  if (status === 1) return 'success'
  if (status === 0) return 'danger'
  return 'info'
}

function getStatusText(status) {
  if (status === 1) return '正常'
  if (status === 0) return '禁用'
  return '未知'
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
