<template>
  <div class="dashboard-container">
    <el-container>
      <!-- 头部 -->
      <el-header class="dashboard-header">
        <div class="header-left">
          <h2>智慧电竞酒店管理系统</h2>
        </div>
        <div class="header-right">
          <span class="username">欢迎，{{ userStore.userInfo.realName || '用户' }}</span>
          <el-button type="danger" @click="handleLogout">退出登录</el-button>
        </div>
      </el-header>
      
      <!-- 主体 -->
      <el-container>
        <!-- 侧边栏 -->
        <el-aside width="200px" class="dashboard-aside">
          <el-menu
            :default-active="activeMenu"
            router
            class="dashboard-menu"
          >
            <el-menu-item index="/dashboard">
              <el-icon><HomeFilled /></el-icon>
              <span>控制台</span>
            </el-menu-item>
            <el-menu-item index="/rooms">
              <el-icon><OfficeBuilding /></el-icon>
              <span>房态管理</span>
            </el-menu-item>
            <el-menu-item index="/hardware">
              <el-icon><Monitor /></el-icon>
              <span>硬件监控</span>
            </el-menu-item>
          </el-menu>
        </el-aside>
        
        <!-- 内容区 -->
        <el-main class="dashboard-main">
          <router-view v-slot="{ Component }">
            <component :is="Component" />
          </router-view>
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'
import { 
  HomeFilled, 
  OfficeBuilding, 
  Monitor
} from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const activeMenu = computed(() => route.path)

// 退出登录
const handleLogout = () => {
  ElMessage.success('退出登录成功')
  userStore.logout()
}
</script>

<style scoped>
.dashboard-container {
  width: 100%;
  height: 100%;
}

.el-container {
  height: 100%;
}

.dashboard-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 20px;
  background: white;
  border-bottom: 1px solid #e6e6e6;
}

.header-left h2 {
  margin: 0;
  font-size: 20px;
  color: #333;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 15px;
}

.username {
  color: #666;
  font-size: 14px;
}

.dashboard-aside {
  background: white;
  border-right: 1px solid #e6e6e6;
}

.dashboard-menu {
  border: none;
}

.dashboard-main {
  padding: 20px;
  background: #f0f2f5;
}
</style>
