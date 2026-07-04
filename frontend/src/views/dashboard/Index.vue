<template>
  <div class="dashboard">
    <!-- 欢迎卡片 -->
    <el-card class="welcome-card" shadow="never">
      <div class="welcome-content">
        <h2>欢迎回来，{{ userStore.realName || userStore.username }}！</h2>
        <p>文化遗产数字平台 - 后台管理子系统</p>
      </div>
    </el-card>

    <!-- 统计卡片 -->
    <el-row :gutter="20" class="stat-row">
      <el-col :xs="12" :sm="12" :md="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon artifact-icon">
              <el-icon size="32"><Picture /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.artifactCount }}</div>
              <div class="stat-label">文物总数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="12" :sm="12" :md="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon admin-icon">
              <el-icon size="32"><User /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.adminCount }}</div>
              <div class="stat-label">管理员数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="12" :sm="12" :md="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon backup-icon">
              <el-icon size="32"><FolderOpened /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.backupCount }}</div>
              <div class="stat-label">备份记录</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="12" :sm="12" :md="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon log-icon">
              <el-icon size="32"><Document /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.logCount }}</div>
              <div class="stat-label">日志总数</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 图表区域 -->
    <el-row :gutter="20">
      <el-col :xs="24" :md="12">
        <el-card shadow="never" class="chart-card">
          <template #header>
            <span>文物分类分布</span>
          </template>
          <div ref="categoryChartRef" class="chart-container"></div>
        </el-card>
      </el-col>
      <el-col :xs="24" :md="12">
        <el-card shadow="never" class="chart-card">
          <template #header>
            <span>文物朝代分布</span>
          </template>
          <div ref="dynastyChartRef" class="chart-container"></div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 最近操作日志 -->
    <el-card shadow="never" class="recent-card">
      <template #header>
        <div class="card-header">
          <span>最近操作日志</span>
          <el-button type="primary" link @click="$router.push('/log/operation')">
            查看更多
          </el-button>
        </div>
      </template>
      <el-table :data="recentLogs" v-loading="logLoading" stripe size="small">
        <el-table-column prop="operator" label="操作人" width="120" />
        <el-table-column prop="operationDesc" label="操作描述" show-overflow-tooltip />
        <el-table-column prop="operationType" label="类型" width="100">
          <template #default="{ row }">
            <el-tag :type="getOperationTypeTag(row.operationType)" size="small">
              {{ row.operationType }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="时间" width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.createTime) }}
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onBeforeUnmount, nextTick } from 'vue'
import * as echarts from 'echarts'
import { Picture, User, FolderOpened, Document } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { getArtifactList, getCategories } from '@/api/artifact'
import { getAdminList } from '@/api/admin'
import { getBackupList } from '@/api/backup'
import { getOperationLogs } from '@/api/log'
import { formatDateTime, getOperationTypeTag } from '@/utils/format'

const userStore = useUserStore()

const stats = reactive({
  artifactCount: 0,
  adminCount: 0,
  backupCount: 0,
  logCount: 0,
})

const recentLogs = ref([])
const logLoading = ref(false)

const categoryChartRef = ref(null)
const dynastyChartRef = ref(null)
let categoryChart = null
let dynastyChart = null

async function loadStats() {
  try {
    const [artifactRes, adminRes, backupRes, logRes] = await Promise.all([
      getArtifactList({ page: 1, size: 1 }),
      getAdminList({ page: 1, size: 1 }),
      getBackupList({ page: 1, size: 1 }),
      getOperationLogs({ page: 1, size: 1 }),
    ])
    stats.artifactCount = artifactRes.data?.total || 0
    stats.adminCount = adminRes.data?.total || 0
    stats.backupCount = backupRes.data?.total || 0
    stats.logCount = logRes.data?.total || 0
  } catch (e) {
    // 忽略统计错误
  }
}

async function loadRecentLogs() {
  logLoading.value = true
  try {
    const res = await getOperationLogs({ page: 1, size: 10 })
    recentLogs.value = res.data?.list || []
  } catch (e) {
    // 忽略错误
  } finally {
    logLoading.value = false
  }
}

async function loadCharts() {
  try {
    // 获取所有文物用于统计
    const res = await getArtifactList({ page: 1, size: 1000 })
    const list = res.data?.list || res.data?.records || []

    // 分类统计
    const categoryMap = {}
    const dynastyMap = {}
    list.forEach((item) => {
      const cat = item.category || '未分类'
      categoryMap[cat] = (categoryMap[cat] || 0) + 1
      const dyn = item.dynasty || '未知'
      dynastyMap[dyn] = (dynastyMap[dyn] || 0) + 1
    })

    await nextTick()
    initCategoryChart(categoryMap)
    initDynastyChart(dynastyMap)
  } catch (e) {
    // 忽略错误
  }
}

function initCategoryChart(data) {
  if (!categoryChartRef.value) return
  categoryChart = echarts.init(categoryChartRef.value)
  const keys = Object.keys(data)
  const values = Object.values(data)
  categoryChart.setOption({
    tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
    legend: { bottom: 0, type: 'scroll' },
    series: [
      {
        type: 'pie',
        radius: ['40%', '70%'],
        avoidLabelOverlap: false,
        itemStyle: { borderRadius: 6, borderColor: '#fff', borderWidth: 2 },
        label: { show: false, position: 'center' },
        emphasis: { label: { show: true, fontSize: 16, fontWeight: 'bold' } },
        labelLine: { show: false },
        data: keys.map((k, i) => ({ name: k, value: values[i] })),
      },
    ],
  })
}

function initDynastyChart(data) {
  if (!dynastyChartRef.value) return
  dynastyChart = echarts.init(dynastyChartRef.value)
  const keys = Object.keys(data)
  const values = Object.values(data)
  dynastyChart.setOption({
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
    xAxis: {
      type: 'category',
      data: keys,
      axisLabel: { rotate: keys.length > 6 ? 30 : 0 },
    },
    yAxis: { type: 'value', minInterval: 1 },
    series: [
      {
        type: 'bar',
        data: values,
        itemStyle: { color: '#409eff', borderRadius: [4, 4, 0, 0] },
      },
    ],
  })
}

function handleResize() {
  categoryChart?.resize()
  dynastyChart?.resize()
}

onMounted(() => {
  loadStats()
  loadRecentLogs()
  loadCharts()
  window.addEventListener('resize', handleResize)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  categoryChart?.dispose()
  dynastyChart?.dispose()
})
</script>

<style scoped>
.dashboard {
  padding: 0;
}

.welcome-card {
  margin-bottom: 20px;
  background: linear-gradient(135deg, #409eff 0%, #66b1ff 100%);
  border: none;
}

.welcome-card :deep(.el-card__body) {
  padding: 24px;
}

.welcome-content h2 {
  color: #fff;
  font-size: 22px;
  margin-bottom: 8px;
}

.welcome-content p {
  color: rgba(255, 255, 255, 0.85);
  font-size: 14px;
}

.stat-row {
  margin-bottom: 20px;
}

.stat-card {
  margin-bottom: 20px;
}

.stat-content {
  display: flex;
  align-items: center;
  gap: 16px;
}

.stat-icon {
  width: 64px;
  height: 64px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  flex-shrink: 0;
}

.artifact-icon {
  background: linear-gradient(135deg, #f56c6c 0%, #f89898 100%);
}

.admin-icon {
  background: linear-gradient(135deg, #e6a23c 0%, #ebb563 100%);
}

.backup-icon {
  background: linear-gradient(135deg, #67c23a 0%, #85ce61 100%);
}

.log-icon {
  background: linear-gradient(135deg, #909399 0%, #a6a9ad 100%);
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 28px;
  font-weight: 700;
  color: #303133;
  line-height: 1.2;
}

.stat-label {
  font-size: 14px;
  color: #909399;
  margin-top: 4px;
}

.chart-card {
  margin-bottom: 20px;
}

.chart-container {
  height: 320px;
}

.recent-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
