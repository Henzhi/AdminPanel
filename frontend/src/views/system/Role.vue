<template>
  <div class="role-list">
    <div class="page-header">
      <h2>角色管理</h2>
      <el-button type="primary" :icon="Plus" @click="handleAdd">新增角色</el-button>
    </div>

    <el-card shadow="never">
      <el-table v-loading="loading" :data="tableData" stripe border style="width: 100%">
        <el-table-column type="index" label="#" width="60" align="center" />
        <el-table-column prop="roleCode" label="角色编码" width="180" />
        <el-table-column prop="roleName" label="角色名称" width="160">
          <template #default="{ row }">
            <el-tag :type="getRoleTag(row.roleCode)" size="small">{{ row.roleName }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="描述" min-width="200" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
              {{ row.status === 1 ? '正常' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="创建时间" width="170">
          <template #default="{ row }">
            {{ formatDateTime(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="240" fixed="right" align="center">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handlePermissions(row)">
              分配权限
            </el-button>
            <el-button type="warning" link size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button
              type="danger"
              link
              size="small"
              :disabled="row.roleCode === 'SUPER_ADMIN'"
              @click="handleDelete(row)"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 新增/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogType === 'add' ? '新增角色' : '编辑角色'"
      width="540px"
      @close="resetForm"
    >
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="100px">
        <el-form-item label="角色编码" prop="roleCode">
          <el-input
            v-model="formData.roleCode"
            placeholder="如：CONTENT_AUDITOR"
            :disabled="dialogType === 'edit'"
          />
        </el-form-item>
        <el-form-item label="角色名称" prop="roleName">
          <el-input v-model="formData.roleName" placeholder="如：内容审核员" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input
            v-model="formData.description"
            type="textarea"
            :rows="3"
            placeholder="角色描述"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="submitForm">确定</el-button>
      </template>
    </el-dialog>

    <!-- 分配权限对话框 -->
    <el-dialog v-model="permDialogVisible" title="分配权限" width="540px">
      <div class="perm-header">
        <span>角色：<strong>{{ currentRole?.roleName }}</strong></span>
      </div>
      <el-tree
        ref="permTreeRef"
        :data="permissionTree"
        :props="{ label: 'permName', children: 'children' }"
        node-key="id"
        show-checkbox
        check-strictly
        default-expand-all
        style="margin-top: 12px"
      />
      <template #footer>
        <el-button @click="permDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="permLoading" @click="submitPermissions">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import {
  getRoleList, getRoleDetail, createRole, updateRole, deleteRole,
  getRolePermissions, assignRolePermissions, getPermissionTree,
} from '@/api/role'
import { formatDateTime } from '@/utils/format'

const loading = ref(false)
const submitLoading = ref(false)
const permLoading = ref(false)
const tableData = ref([])

const dialogVisible = ref(false)
const dialogType = ref('add')
const formRef = ref(null)
const formData = reactive({
  id: null,
  roleCode: '',
  roleName: '',
  description: '',
})

const formRules = {
  roleCode: [{ required: true, message: '请输入角色编码', trigger: 'blur' }],
  roleName: [{ required: true, message: '请输入角色名称', trigger: 'blur' }],
}

const permDialogVisible = ref(false)
const permTreeRef = ref(null)
const permissionTree = ref([])
const currentRole = ref(null)

async function loadData() {
  loading.value = true
  try {
    const res = await getRoleList()
    tableData.value = res.data || []
  } catch (e) {
    // 错误已处理
  } finally {
    loading.value = false
  }
}

async function loadPermissionTree() {
  try {
    const res = await getPermissionTree()
    permissionTree.value = res.data || []
  } catch (e) {
    // 忽略
  }
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
    roleCode: row.roleCode,
    roleName: row.roleName,
    description: row.description,
  })
  dialogVisible.value = true
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(`确定要删除角色"${row.roleName}"吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    })
    await deleteRole(row.id)
    ElMessage.success('删除成功')
    loadData()
  } catch (e) {
    // 取消或错误
  }
}

async function handlePermissions(row) {
  currentRole.value = row
  if (permissionTree.value.length === 0) {
    await loadPermissionTree()
  }
  permDialogVisible.value = true
  try {
    const res = await getRolePermissions(row.id)
    const checkedIds = res.data || []
    await nextTick()
    permTreeRef.value?.setCheckedKeys(checkedIds)
  } catch (e) {
    // 错误已处理
  }
}

async function submitPermissions() {
  const checkedIds = permTreeRef.value?.getCheckedKeys() || []
  permLoading.value = true
  try {
    await assignRolePermissions(currentRole.value.id, checkedIds)
    ElMessage.success('权限分配成功')
    permDialogVisible.value = false
  } catch (e) {
    // 错误已处理
  } finally {
    permLoading.value = false
  }
}

function resetForm() {
  formData.id = null
  formData.roleCode = ''
  formData.roleName = ''
  formData.description = ''
  formRef.value?.clearValidate()
}

async function submitForm() {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    submitLoading.value = true
    try {
      const data = {
        roleCode: formData.roleCode,
        roleName: formData.roleName,
        description: formData.description,
      }
      if (dialogType.value === 'add') {
        await createRole(data)
        ElMessage.success('新增成功')
      } else {
        await updateRole(formData.id, data)
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

function getRoleTag(roleCode) {
  const map = {
    SUPER_ADMIN: 'danger',
    CONTENT_AUDITOR: 'warning',
    DATA_ADMIN: 'success',
  }
  return map[roleCode] || 'info'
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

.perm-header {
  padding: 8px 0;
  font-size: 14px;
  color: #606266;
}
</style>
