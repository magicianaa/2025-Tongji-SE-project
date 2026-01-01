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
        <el-card v-loading="loadingOrders">
          <template #header>
            <span>我的订单</span>
          </template>
          <!-- 未入住时显示提示 -->
          <el-empty v-if="!userStore.roomAuthToken" description="入住后可查看订单记录" />
          <!-- 已入住但无订单 -->
          <el-empty v-else-if="recentOrders.length === 0" description="暂无订单记录" />
          <!-- 有订单时显示列表 -->
          <div v-else class="orders-list">
            <div v-for="order in recentOrders" :key="order.orderId" class="order-item">
              <div class="order-header">
                <span class="order-id">订单#{{ order.orderNo || order.orderId }}</span>
                <el-tag :type="getOrderStatusType(order.status)" size="small">
                  {{ getOrderStatusText(order.status) }}
                </el-tag>
              </div>
              <div class="order-details">
                <div class="order-info">
                  {{ getOrderSummary(order) }}
                </div>
                <div class="order-amount">¥{{ order.totalAmount }}</div>
              </div>
              <div class="order-time">{{ formatTime(order.createTime) }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card v-loading="loadingPoints">
          <template #header>
            <span>我的积分</span>
          </template>
          <div class="points-info">
            <el-statistic title="当前积分" :value="pointsBalance.currentPoints" />
            <el-divider />
            <div class="member-info">
              <el-descriptions :column="1" size="small">
                <el-descriptions-item label="会员等级">
                  <el-tag :type="getMemberLevelType(pointsBalance.memberLevel)">
                    {{ pointsBalance.memberLevel }}
                  </el-tag>
                </el-descriptions-item>
                <el-descriptions-item label="经验值">
                  {{ pointsBalance.experiencePoints }}
                </el-descriptions-item>
                <el-descriptions-item label="折扣率">
                  {{ formatDiscount(pointsBalance.discountRate) }}
                </el-descriptions-item>
              </el-descriptions>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'
import { Calendar, ShoppingCart, ChatDotRound } from '@element-plus/icons-vue'
import { getMyOrders } from '@/api/order'
import { getPointsBalance } from '@/api/points'

const router = useRouter()
const userStore = useUserStore()

// 订单数据
const recentOrders = ref([])
const loadingOrders = ref(false)

// 积分数据
const pointsBalance = ref({
  currentPoints: 0,
  experiencePoints: 0,
  memberLevel: '普通会员',
  discountRate: 1.0
})
const loadingPoints = ref(false)

// 加载订单数据 - 只有入住时才加载，绑定recordId
const loadOrders = async () => {
  const recordId = userStore.checkInInfo?.recordId
  if (!recordId) {
    console.log('未入住，跳过加载订单')
    recentOrders.value = []
    return
  }
  
  try {
    loadingOrders.value = true
    console.log('正在加载订单，recordId:', recordId)
    const res = await getMyOrders(recordId)
    console.log('订单API返回:', res)
    // res已经是data（响应拦截器处理过），只显示最近5条订单
    recentOrders.value = (Array.isArray(res) ? res : []).slice(0, 5)
    console.log('订单数据:', recentOrders.value)
  } catch (error) {
    console.error('加载订单失败:', error)
    recentOrders.value = []
  } finally {
    loadingOrders.value = false
  }
}

// 加载积分数据 - 根据tb_guest表数据显示
const loadPoints = async () => {
  try {
    loadingPoints.value = true
    const res = await getPointsBalance()
    console.log('积分API返回:', res)
    // res已经是data（响应拦截器处理过）
    if (res) {
      pointsBalance.value = {
        currentPoints: res.currentPoints ?? 0,
        experiencePoints: res.experiencePoints ?? 0,
        memberLevel: res.memberLevel || '普通会员',
        discountRate: res.discountRate ?? 1.0
      }
    }
  } catch (error) {
    console.error('加载积分失败:', error)
    // 如果获取失败（如用户没有guest记录），显示默认值
    pointsBalance.value = {
      currentPoints: 0,
      experiencePoints: 0,
      memberLevel: '普通会员',
      discountRate: 1.0
    }
  } finally {
    loadingPoints.value = false
  }
}

// 获取订单商品摘要
const getOrderSummary = (order) => {
  if (!order.items || order.items.length === 0) {
    return order.orderType === 'RENTAL' ? '租赁订单' : '商品订单'
  }
  const firstItem = order.items[0]
  if (order.items.length === 1) {
    return `${firstItem.productName} x ${firstItem.quantity}`
  }
  return `${firstItem.productName} 等${order.items.length}件商品`
}

// 格式化折扣率
const formatDiscount = (rate) => {
  if (rate === null || rate === undefined) return '10.0折'
  return (rate * 10).toFixed(1) + '折'
}

// 格式化时间
const formatTime = (timeStr) => {
  if (!timeStr) return ''
  const date = new Date(timeStr)
  const now = new Date()
  const diff = now - date
  
  // 如果是今天
  if (diff < 24 * 60 * 60 * 1000) {
    const hours = Math.floor(diff / (60 * 60 * 1000))
    if (hours < 1) {
      const minutes = Math.floor(diff / (60 * 1000))
      return `${minutes}分钟前`
    }
    return `${hours}小时前`
  }
  
  // 格式化为 MM-DD HH:mm
  const month = (date.getMonth() + 1).toString().padStart(2, '0')
  const day = date.getDate().toString().padStart(2, '0')
  const hour = date.getHours().toString().padStart(2, '0')
  const minute = date.getMinutes().toString().padStart(2, '0')
  return `${month}-${day} ${hour}:${minute}`
}

// 获取订单状态类型
const getOrderStatusType = (status) => {
  const typeMap = {
    'PENDING': 'warning',
    'DELIVERED': 'success',
    'RETURNED': 'info',
    'CANCELLED': 'danger'
  }
  return typeMap[status] || 'info'
}

// 获取订单状态文本
const getOrderStatusText = (status) => {
  const textMap = {
    'PENDING': '待配送',
    'DELIVERED': '已配送',
    'RETURNED': '已归还',
    'CANCELLED': '已取消'
  }
  return textMap[status] || status
}

// 获取会员等级类型
const getMemberLevelType = (level) => {
  const typeMap = {
    '普通会员': '',
    '银卡会员': 'info',
    '金卡会员': 'warning',
    '钻石会员': 'danger'
  }
  return typeMap[level] || ''
}

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

// 页面加载时获取数据
onMounted(async () => {
  // 始终加载积分（只要登录就有积分记录）
  loadPoints()
  
  // 如果已入住，加载订单（订单按recordId绑定）
  if (userStore.roomAuthToken && userStore.checkInInfo?.recordId) {
    loadOrders()
  }
})
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

.member-info {
  margin-top: 10px;
  text-align: left;
}

.orders-list {
  max-height: 300px;
  overflow-y: auto;
}

.order-item {
  padding: 12px;
  border-bottom: 1px solid #f0f0f0;
  cursor: pointer;
  transition: background-color 0.3s;
}

.order-item:hover {
  background-color: #f5f7fa;
}

.order-item:last-child {
  border-bottom: none;
}

.order-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.order-id {
  font-weight: bold;
  color: #303133;
  font-size: 14px;
}

.order-details {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 6px;
}

.order-info {
  color: #606266;
  font-size: 13px;
}

.order-amount {
  color: #f56c6c;
  font-weight: bold;
  font-size: 14px;
}

.order-time {
  color: #909399;
  font-size: 12px;
}
</style>
