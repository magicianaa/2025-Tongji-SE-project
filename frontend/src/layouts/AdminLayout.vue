<template>
  <div class="admin-layout">
    <el-container>
      <!-- 顶部导航栏 -->
      <el-header class="admin-header">
        <div class="header-left">
          <h1 class="system-title">
            <el-icon><Setting /></el-icon>
            智慧电竞酒店 - 管理端
          </h1>
        </div>
        <div class="header-right">
          <el-dropdown @command="handleCommand">
            <span class="user-info">
              <el-icon><User /></el-icon>
              {{ userStore.userInfo.username || '管理员' }}
              <el-icon class="el-icon--right"><arrow-down /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">个人资料</el-dropdown-item>
                <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <el-container>
        <!-- 侧边栏 -->
        <el-aside width="200px" class="admin-sidebar">
          <el-menu
            :default-active="currentRoute"
            router
            background-color="#304156"
            text-color="#bfcbd9"
            active-text-color="#409EFF"
          >
            <el-menu-item index="/admin/dashboard">
              <el-icon><DataAnalysis /></el-icon>
              <span>数据看板</span>
            </el-menu-item>
            <el-menu-item index="/admin/reviews">
              <el-icon><Star /></el-icon>
              <span>评价管理</span>
            </el-menu-item>
            <el-menu-item index="/admin/financial-report">
              <el-icon><Document /></el-icon>
              <span>运营报表</span>
            </el-menu-item>
            <el-menu-item index="/admin/staff">
              <el-icon><User /></el-icon>
              <span>员工管理</span>
            </el-menu-item>
            <el-menu-item index="/admin/settings">
              <el-icon><Setting /></el-icon>
              <span>系统配置</span>
            </el-menu-item>
          </el-menu>
        </el-aside>

        <!-- 主内容区 -->
        <el-main class="admin-main">
          <router-view v-slot="{ Component }">
            <transition name="fade-transform" mode="out-in">
              <component :is="Component" />
            </transition>
          </router-view>
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'
import { Setting, User, DataAnalysis, Document, ArrowDown, Star } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const currentRoute = computed(() => route.path)

const handleCommand = (command) => {
  if (command === 'logout') {
    userStore.logout()
    ElMessage.success('已退出登录')
    router.push('/login')
  } else if (command === 'profile') {
    // 跳转到个人资料页面
    router.push('/admin/profile')
  }
}
</script>

<style scoped>
.admin-layout {
  height: 100vh;
  overflow: hidden;
}

.admin-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 0 20px;
}

.header-left {
  display: flex;
  align-items: center;
}

.system-title {
  font-size: 20px;
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 10px;
  margin: 0;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 8px 16px;
  border-radius: 4px;
  transition: background-color 0.3s;
}

.user-info:hover {
  background-color: rgba(255, 255, 255, 0.1);
}

.admin-sidebar {
  background-color: #304156;
  overflow-y: auto;
}

.admin-main {
  background-color: #f0f2f5;
  padding: 20px;
  overflow-y: auto;
}

.fade-transform-leave-active,
.fade-transform-enter-active {
  transition: all 0.2s;
}

.fade-transform-enter-from {
  opacity: 0;
  transform: translateX(-30px);
}

.fade-transform-leave-to {
  opacity: 0;
  transform: translateX(30px);
}
</style>
