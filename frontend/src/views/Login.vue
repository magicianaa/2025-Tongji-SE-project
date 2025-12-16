<template>
  <div class="login-container">
    <div class="login-box">
      <div class="login-header">
        <h1>智慧电竞酒店管理系统</h1>
        <p>Smart Esports Hotel Management System</p>
      </div>
      
      <el-tabs v-model="activeTab" class="login-tabs">
        <!-- 登录标签 -->
        <el-tab-pane label="登录" name="login">
          <el-form
            ref="loginFormRef"
            :model="loginForm"
            :rules="loginRules"
            class="login-form"
            label-position="top"
          >
            <el-form-item label="手机号" prop="phone">
              <el-input
                v-model="loginForm.phone"
                placeholder="请输入手机号"
                prefix-icon="Phone"
                clearable
              />
            </el-form-item>
            
            <el-form-item label="密码" prop="password">
              <el-input
                v-model="loginForm.password"
                type="password"
                placeholder="请输入密码"
                prefix-icon="Lock"
                show-password
                clearable
                @keyup.enter="handleLogin"
              />
            </el-form-item>
            
            <el-form-item>
              <el-button
                type="primary"
                :loading="loginLoading"
                class="login-button"
                @click="handleLogin"
              >
                登录
              </el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>
        
        <!-- 注册标签 -->
        <el-tab-pane label="注册" name="register">
          <el-form
            ref="registerFormRef"
            :model="registerForm"
            :rules="registerRules"
            class="login-form"
            label-position="top"
          >
            <el-form-item label="手机号" prop="phone">
              <el-input
                v-model="registerForm.phone"
                placeholder="请输入手机号"
                prefix-icon="Phone"
                clearable
              />
            </el-form-item>
            
            <el-form-item label="验证码" prop="smsCode">
              <div class="sms-code-input">
                <el-input
                  v-model="registerForm.smsCode"
                  placeholder="请输入验证码"
                  prefix-icon="Message"
                  clearable
                />
                <el-button
                  :disabled="smsCountdown > 0"
                  @click="handleSendSms"
                >
                  {{ smsCountdown > 0 ? `${smsCountdown}秒后重试` : '获取验证码' }}
                </el-button>
              </div>
            </el-form-item>
            
            <el-form-item label="密码" prop="password">
              <el-input
                v-model="registerForm.password"
                type="password"
                placeholder="请输入密码（6-20位）"
                prefix-icon="Lock"
                show-password
                clearable
              />
            </el-form-item>
            
            <el-form-item label="确认密码" prop="confirmPassword">
              <el-input
                v-model="registerForm.confirmPassword"
                type="password"
                placeholder="请再次输入密码"
                prefix-icon="Lock"
                show-password
                clearable
                @keyup.enter="handleRegister"
              />
            </el-form-item>
            
            <el-form-item label="姓名" prop="realName">
              <el-input
                v-model="registerForm.realName"
                placeholder="请输入真实姓名"
                prefix-icon="User"
                clearable
              />
            </el-form-item>
            
            <el-form-item label="身份证号" prop="idCard">
              <el-input
                v-model="registerForm.idCard"
                placeholder="请输入身份证号"
                prefix-icon="Postcard"
                clearable
              />
            </el-form-item>
            
            <el-form-item>
              <el-button
                type="primary"
                :loading="registerLoading"
                class="login-button"
                @click="handleRegister"
              >
                注册
              </el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>
      </el-tabs>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { login, register, sendSmsCode } from '@/api/auth'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

// 当前激活的标签
const activeTab = ref('login')

// 登录表单
const loginFormRef = ref(null)
const loginLoading = ref(false)
const loginForm = reactive({
  phone: '',
  password: ''
})

const loginRules = {
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度为6-20位', trigger: 'blur' }
  ]
}

// 注册表单
const registerFormRef = ref(null)
const registerLoading = ref(false)
const registerForm = reactive({
  phone: '',
  smsCode: '',
  password: '',
  confirmPassword: '',
  realName: '',
  idCard: ''
})

const validateConfirmPassword = (rule, value, callback) => {
  if (value === '') {
    callback(new Error('请再次输入密码'))
  } else if (value !== registerForm.password) {
    callback(new Error('两次输入密码不一致'))
  } else {
    callback()
  }
}

const registerRules = {
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
  ],
  smsCode: [
    { required: true, message: '请输入验证码', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度为6-20位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, validator: validateConfirmPassword, trigger: 'blur' }
  ],
  realName: [
    { required: true, message: '请输入真实姓名', trigger: 'blur' }
  ],
  idCard: [
    { required: true, message: '请输入身份证号', trigger: 'blur' },
    { pattern: /^[1-9]\d{5}(18|19|20)\d{2}(0[1-9]|1[0-2])(0[1-9]|[12]\d|3[01])\d{3}[\dXx]$/, message: '身份证号格式不正确', trigger: 'blur' }
  ]
}

// 短信验证码倒计时
const smsCountdown = ref(0)
let smsTimer = null

// 处理登录
const handleLogin = async () => {
  if (!loginFormRef.value) return
  
  await loginFormRef.value.validate(async (valid) => {
    if (valid) {
      loginLoading.value = true
      try {
        const data = await login(loginForm)
        
        // 保存 token 和用户信息
        userStore.setToken(data.accessToken)
        userStore.setUserInfo({
          userId: data.userId,
          username: data.username,
          userType: data.userType,
          memberLevel: data.memberLevel,
          currentPoints: data.currentPoints
        })
        
        ElMessage.success('登录成功')
        
        // 根据用户角色跳转到对应的首页
        const userType = data.userType
        let targetRoute = '/guest/home' // 默认跳转到住客首页
        
        if (userType === 'ADMIN') {
          targetRoute = '/admin/dashboard'
        } else if (userType === 'STAFF') {
          targetRoute = '/staff/workbench'
        } else if (userType === 'GUEST') {
          targetRoute = '/guest/home'
        }
        
        // 如果有重定向参数且用户有权限访问，则使用重定向地址
        const redirect = route.query.redirect
        if (redirect) {
          router.push(redirect)
        } else {
          router.push(targetRoute)
        }
      } catch (error) {
        console.error('登录失败：', error)
      } finally {
        loginLoading.value = false
      }
    }
  })
}

// 发送短信验证码
const handleSendSms = async () => {
  if (!registerForm.phone) {
    ElMessage.warning('请先输入手机号')
    return
  }
  
  if (!/^1[3-9]\d{9}$/.test(registerForm.phone)) {
    ElMessage.warning('手机号格式不正确')
    return
  }
  
  try {
    await sendSmsCode(registerForm.phone)
    ElMessage.success('验证码已发送')
    
    // 开始倒计时
    smsCountdown.value = 60
    smsTimer = setInterval(() => {
      smsCountdown.value--
      if (smsCountdown.value <= 0) {
        clearInterval(smsTimer)
      }
    }, 1000)
  } catch (error) {
    console.error('发送验证码失败：', error)
  }
}

// 处理注册
const handleRegister = async () => {
  if (!registerFormRef.value) return
  
  await registerFormRef.value.validate(async (valid) => {
    if (valid) {
      registerLoading.value = true
      try {
        await register(registerForm)
        ElMessage.success('注册成功，请登录')
        
        // 切换到登录标签
        activeTab.value = 'login'
        loginForm.phone = registerForm.phone
      } catch (error) {
        console.error('注册失败：', error)
      } finally {
        registerLoading.value = false
      }
    }
  })
}
</script>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  width: 100%;
  height: 100%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.login-box {
  width: 450px;
  padding: 40px;
  background: white;
  border-radius: 10px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.2);
}

.login-header {
  text-align: center;
  margin-bottom: 30px;
}

.login-header h1 {
  font-size: 24px;
  color: #333;
  margin-bottom: 10px;
}

.login-header p {
  font-size: 14px;
  color: #999;
}

.login-tabs {
  margin-bottom: 20px;
}

.login-form {
  padding: 20px 0;
}

.login-button {
  width: 100%;
  height: 45px;
  font-size: 16px;
}

.sms-code-input {
  display: flex;
  gap: 10px;
}

.sms-code-input .el-input {
  flex: 1;
}

.sms-code-input .el-button {
  white-space: nowrap;
}
</style>
