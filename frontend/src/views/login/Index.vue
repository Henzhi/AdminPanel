<template>
  <div class="login-container">
    <div class="login-box">
      <div class="login-header">
        <h1 class="title">文化遗产数字平台</h1>
        <p class="subtitle">后台管理子系统</p>
      </div>

      <el-form
        ref="loginFormRef"
        :model="loginForm"
        :rules="loginRules"
        class="login-form"
        size="large"
        @keyup.enter="handleLogin"
      >
        <el-form-item prop="username">
          <el-input
            v-model="loginForm.username"
            placeholder="请输入用户名"
            :prefix-icon="User"
            clearable
          />
        </el-form-item>

        <el-form-item prop="password">
          <el-input
            v-model="loginForm.password"
            type="password"
            placeholder="请输入密码"
            :prefix-icon="Lock"
            show-password
            clearable
          />
        </el-form-item>

        <el-form-item prop="captcha">
          <div class="captcha-wrapper">
            <el-input
              v-model="loginForm.captcha"
              placeholder="请输入验证码"
              :prefix-icon="Picture"
              clearable
            />
            <div class="captcha-image" @click="refreshCaptcha">
              <img v-if="captchaImage" :src="captchaImage" alt="验证码" />
              <span v-else class="captcha-loading">加载中...</span>
            </div>
          </div>
        </el-form-item>

        <el-form-item>
          <el-button
            type="primary"
            class="login-button"
            :loading="loading"
            @click="handleLogin"
          >
            登 录
          </el-button>
        </el-form-item>
      </el-form>

      <div class="login-tips">
        <p>测试账号：</p>
        <p>超级管理员：admin / Admin@123</p>
        <p>内容审核员：auditor / Audi@123</p>
        <p>数据管理员：dataadmin / Data@123</p>
      </div>
    </div>

    <div class="login-footer">
      © 2026 文化遗产数字平台 - 后台管理子系统
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock, Picture } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { getCaptcha } from '@/api/auth'

const router = useRouter()
const userStore = useUserStore()

const loginFormRef = ref(null)
const loading = ref(false)
const captchaImage = ref('')
const captchaKey = ref('')

const loginForm = reactive({
  username: '',
  password: '',
  captcha: '',
  captchaKey: '',
})

const loginRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' },
  ],
  captcha: [{ required: true, message: '请输入验证码', trigger: 'blur' }],
}

async function refreshCaptcha() {
  try {
    const res = await getCaptcha()
    captchaImage.value = res.data.captchaImage
    captchaKey.value = res.data.captchaKey
    loginForm.captchaKey = res.data.captchaKey
  } catch (e) {
    // 错误已由拦截器处理
  }
}

async function handleLogin() {
  if (!loginFormRef.value) return
  await loginFormRef.value.validate(async (valid) => {
    if (!valid) return
    loading.value = true
    try {
      loginForm.captchaKey = captchaKey.value
      await userStore.login(loginForm)
      ElMessage.success('登录成功')
      router.push('/dashboard')
    } catch (e) {
      refreshCaptcha()
    } finally {
      loading.value = false
    }
  })
}

onMounted(() => {
  refreshCaptcha()
})
</script>

<style scoped>
.login-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 100vh;
  background: linear-gradient(135deg, #304156 0%, #1f2d3d 100%);
}

.login-box {
  width: 420px;
  padding: 40px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.2);
}

.login-header {
  text-align: center;
  margin-bottom: 32px;
}

.title {
  font-size: 24px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 8px;
}

.subtitle {
  font-size: 14px;
  color: #909399;
}

.login-form {
  margin-bottom: 16px;
}

.captcha-wrapper {
  display: flex;
  width: 100%;
  gap: 12px;
}

.captcha-wrapper .el-input {
  flex: 1;
}

.captcha-image {
  width: 120px;
  height: 40px;
  cursor: pointer;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f7fa;
  flex-shrink: 0;
}

.captcha-image img {
  width: 100%;
  height: 100%;
}

.captcha-loading {
  font-size: 12px;
  color: #909399;
}

.login-button {
  width: 100%;
}

.login-tips {
  margin-top: 16px;
  padding: 12px;
  background: #f5f7fa;
  border-radius: 4px;
  font-size: 12px;
  color: #909399;
  line-height: 1.8;
}

.login-tips p:first-child {
  color: #606266;
  font-weight: 500;
}

.login-footer {
  margin-top: 32px;
  color: rgba(255, 255, 255, 0.6);
  font-size: 12px;
}
</style>
