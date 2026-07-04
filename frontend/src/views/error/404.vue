<template>
  <div class="not-found">
    <div class="content">
      <h1 class="error-code">404</h1>
      <p class="error-desc">抱歉，您访问的页面不存在</p>
      <el-button type="primary" @click="goHome">返回首页</el-button>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'

const router = useRouter()

function goHome() {
  router.push('/dashboard')
}
</script>

<style scoped>
.not-found {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100vh;
  background-color: #f0f2f5;
}

.content {
  text-align: center;
}

.error-code {
  font-size: 120px;
  font-weight: 700;
  color: #409eff;
  line-height: 1;
  margin-bottom: 16px;
}

.error-desc {
  font-size: 18px;
  color: #606266;
  margin-bottom: 24px;
}
</style>
