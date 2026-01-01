<template>
  <div class="guest-home">
    <el-alert 
      v-if="!userStore.roomAuthToken"
      title="提示：您尚未绑定房间" 
      type="info"
      description="入住后请完成房间绑定，即可使用更多客房服务"
      show-icon
      :closable="false"
      class="room-tip"
    />

    <el-card class="welcome-card" shadow="hover">
      <h2>欢迎光临智慧电竞酒店！</h2>
      <p class="welcome-text">您的电竞之旅从这里开始</p>
    </el-card>

    <el-row :gutter="20" class="service-row">
      <el-col :span="8">
        <el-card shadow="hover" class="service-card" @click="handleBooking">
          <div class="service-content">
            <el-icon class="service-icon"><Calendar /></el-icon>
            <div class="service-title">预订房间</div>
            <div class="service-desc">查看房型与预订</div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="8">
        <el-card 
          shadow="hover" 
          class="service-card" 
          :class="{ disabled: !userStore.roomAuthToken }"
          @click="handlePOS"
        >
          <div class="service-content">
            <el-icon class="service-icon"><ShoppingCart /></el-icon>
            <div class="service-title">商城点餐</div>
            <div class="service-desc">零食饮料一键送达</div>
            <el-tag v-if="!userStore.roomAuthToken" type="info" size="small" class="lock-tag">
              需入住
            </el-tag>
          </div>
        </el-card>
      </el-col>

      <el-col :span="8">
        <el-card 
          shadow="hover" 
          class="service-card"
          :class="{ disabled: !userStore.roomAuthToken }"
          @click="handleSocial"
        >
          <div class="service-content">
            <el-icon class="service-icon"><ChatDotRound /></el-icon>
            <div class="service-title">社交匹配</div>
            <div class="service-desc">找队友一起开黑</div>
            <el-tag v-if="!userStore.roomAuthToken" type="info" size="small" class="lock-tag">
              需入住
            </el-tag>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20">
      <el-col :span="12">
        <el-card>
          <template #header>
            <span>我的订单</span>
          </template>
          <el-empty description="暂无订单记录" />
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card>
          <template #header>
            <span>我的积分</span>
          </template>
          <div class="points-info">
            <el-statistic title="当前积分" :value="0" />
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'
import { Calendar, ShoppingCart, ChatDotRound } from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()

const handleBooking = () => {
  router.push('/guest/booking')
}

const handlePOS = () => {
  if (!userStore.roomAuthToken) {
    ElMessage.warning('请先入住并绑定房间')
    return
  }
  router.push('/guest/pos')
}

const handleSocial = () => {
  if (!userStore.roomAuthToken) {
    ElMessage.warning('请先入住并绑定房间')
    return
  }
  router.push('/guest/online-lobby')
}
</script>

<style scoped>
.guest-home {
  padding: 20px;
  overflow-y: auto;
  max-height: calc(100vh - 120px);
}

.room-tip {
  margin-bottom: 20px;
}

.welcome-card {
  text-align: center;
  padding: 30px;
  margin-bottom: 30px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.welcome-card h2 {
  margin: 0 0 10px;
  font-size: 28px;
}

.welcome-text {
  margin: 0;
  font-size: 16px;
  opacity: 0.9;
}

.service-row {
  margin-bottom: 20px;
}

.service-card {
  cursor: pointer;
  transition: all 0.3s;
  position: relative;
}

.service-card:hover:not(.disabled) {
  transform: translateY(-10px);
  box-shadow: 0 8px 16px rgba(0, 0, 0, 0.15);
}

.service-card.disabled {
  cursor: not-allowed;
  opacity: 0.6;
}

.service-content {
  text-align: center;
  padding: 20px;
  position: relative;
}

.service-icon {
  font-size: 64px;
  color: #409eff;
  margin-bottom: 15px;
}

.service-title {
  font-size: 18px;
  font-weight: bold;
  margin-bottom: 8px;
}

.service-desc {
  font-size: 14px;
  color: #909399;
}

.lock-tag {
  position: absolute;
  top: 10px;
  right: 10px;
}

.points-info {
  text-align: center;
  padding: 20px;
}
</style>
