<template>
  <div class="staff-layout">
    <el-container>
      <!-- 顶部导航栏 -->
      <el-header class="staff-header">
        <div class="header-left">
          <h1 class="system-title">
            <el-icon><Monitor /></el-icon>
            智慧电竞酒店 - 前台工作台
          </h1>
        </div>
        <div class="header-right">
          <el-dropdown @command="handleCommand">
            <span class="user-info">
              <el-icon><User /></el-icon>
              {{ userStore.userInfo.username || '前台员工' }}
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
        <el-aside width="200px" class="staff-sidebar">
          <el-menu
            :default-active="currentRoute"
            router
            background-color="#2c3e50"
            text-color="#bfcbd9"
            active-text-color="#409EFF"
          >
            <el-menu-item index="/staff/workbench">
              <el-icon><HomeFilled /></el-icon>
              <span>工作台</span>
            </el-menu-item>
            <el-menu-item index="/staff/checkin">
              <el-icon><User /></el-icon>
              <span>入住登记</span>
            </el-menu-item>
            <el-menu-item index="/staff/rooms">
              <el-icon><House /></el-icon>
              <span>房态管理</span>
            </el-menu-item>
            <el-menu-item index="/staff/hardware">
              <el-icon><Monitor /></el-icon>
              <span>硬件监控</span>
            </el-menu-item>
            <el-menu-item index="/staff/products">
              <el-icon><ShoppingCart /></el-icon>
              <span>商品管理</span>
            </el-menu-item>
            <el-menu-item index="/staff/pos-orders">
              <el-icon><List /></el-icon>
              <span>POS订单</span>
            </el-menu-item>
            <el-menu-item index="/staff/maintenance">
              <el-icon><Tools /></el-icon>
              <span>维修工单</span>
            </el-menu-item>
            <el-menu-item index="/staff/tasks">
              <el-icon><List /></el-icon>
              <span>任务审核</span>
            </el-menu-item>
          </el-menu>
        </el-aside>

        <!-- 主内容区 -->
        <el-main class="staff-main">
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
import { Monitor, User, HomeFilled, House, ShoppingCart, Tools, List, ArrowDown } from '@element-plus/icons-vue'

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
    router.push('/staff/profile')
  }
}
</script>

<style scoped>
.staff-layout {
  height: 100vh;
  overflow: hidden;
}

.staff-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: linear-gradient(135deg, #11998e 0%, #38ef7d 100%);
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

.staff-sidebar {
  background-color: #2c3e50;
  overflow-y: auto;
}

.staff-main {
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
