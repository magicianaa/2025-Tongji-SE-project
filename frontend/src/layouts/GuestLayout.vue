<template>
  <div class="guest-layout">
    <el-container>
      <!-- 顶部导航栏 -->
      <el-header class="guest-header">
        <div class="header-left">
          <h1 class="system-title">
            <el-icon><Cpu /></el-icon>
            智慧电竞酒店 - 住客端
          </h1>
        </div>
        <div class="header-right">
          <!-- 房间状态徽章 -->
          <el-badge 
            v-if="userStore.roomAuthToken" 
            value="已入住" 
            class="room-badge"
            type="success"
          >
            <el-tag type="success">{{ roomDisplayText }}</el-tag>
          </el-badge>
          
          <el-dropdown @command="handleCommand">
            <span class="user-info">
              <el-icon><User /></el-icon>
              {{ userStore.userInfo.username || '住客' }}
              <el-icon class="el-icon--right"><arrow-down /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="gaming">电竞档案</el-dropdown-item>
                <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <el-container>
        <!-- 侧边栏 -->
        <el-aside width="200px" class="guest-sidebar">
          <el-menu
            :default-active="currentRoute"
            router
            background-color="#1a1a2e"
            text-color="#eaeaea"
            active-text-color="#b794f6"
          >
            <el-menu-item index="/guest/home">
              <el-icon><HomeFilled /></el-icon>
              <span>首页</span>
            </el-menu-item>
            <el-menu-item index="/guest/booking">
              <el-icon><Calendar /></el-icon>
              <span>预订房间</span>
            </el-menu-item>
            <el-menu-item index="/guest/all-reviews">
              <el-icon><ChatLineRound /></el-icon>
              <span>所有评价</span>
            </el-menu-item>
            <el-menu-item index="/guest/checkin-history">
              <el-icon><Tickets /></el-icon>
              <span>入住记录</span>
            </el-menu-item>
            
            <!-- 入住后功能（需要二次鉴权） -->
            <el-divider v-if="userStore.roomAuthToken" border-style="dashed">房客服务</el-divider>
            <el-menu-item v-if="userStore.roomAuthToken" index="/guest/mall">
              <el-icon><ShoppingCart /></el-icon>
              <span>商城点餐</span>
            </el-menu-item>
            <el-menu-item v-if="userStore.roomAuthToken" index="/guest/online-lobby">
              <el-icon><ChatDotRound /></el-icon>
              <span>组队招募</span>
            </el-menu-item>
            <el-menu-item v-if="userStore.roomAuthToken" index="/guest/tasks">
              <el-icon><Trophy /></el-icon>
              <span>任务积分</span>
            </el-menu-item>
            <el-menu-item v-if="userStore.roomAuthToken" index="/guest/points-shop">
              <el-icon><ShoppingBag /></el-icon>
              <span>积分商城</span>
            </el-menu-item>
            <el-menu-item v-if="userStore.roomAuthToken" index="/guest/repair">
              <el-icon><Tools /></el-icon>
              <span>设备报修</span>
            </el-menu-item>
            <el-menu-item v-if="userStore.roomAuthToken" index="/guest/billing">
              <el-icon><Money /></el-icon>
              <span>退房结算</span>
            </el-menu-item>
          </el-menu>
        </el-aside>

        <!-- 主内容区 -->
        <el-main class="guest-main">
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
import { 
  Cpu, User, HomeFilled, Calendar, ShoppingCart, 
  ChatDotRound, ChatLineRound, Trophy, Tools, Money, ArrowDown, ShoppingBag, Tickets 
} from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const currentRoute = computed(() => route.path)

// 显示房间信息
const roomDisplayText = computed(() => {
  if (userStore.checkInInfo && userStore.checkInInfo.roomNo) {
    return `${userStore.checkInInfo.roomNo}房间`
  }
  if (userStore.checkInInfo && userStore.checkInInfo.roomId) {
    return `${userStore.checkInInfo.roomId}号房`
  }
  return '房间已绑定'
})

const handleCommand = (command) => {
  if (command === 'logout') {
    userStore.logout()
    ElMessage.success('已退出登录')
    router.push('/login')
  } else if (command === 'gaming') {
    router.push('/guest/gaming-profile')
  }
}
</script>

<style scoped>
.guest-layout {
  height: 100vh;
  overflow: hidden;
}

.guest-header {
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

.header-right {
  display: flex;
  align-items: center;
  gap: 20px;
}

.room-badge {
  margin-right: 10px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 8px 16px;
  border-radius: 4px;
  transition: background-color 0.3s;
  color: #ffffff;
  font-weight: 500;
}

.user-info:hover {
  background-color: rgba(255, 255, 255, 0.1);
}

.guest-sidebar {
  background-color: #1a1a2e;
  overflow-y: auto;
}

.guest-main {
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

.el-divider {
  margin: 12px 0;
}
</style>
