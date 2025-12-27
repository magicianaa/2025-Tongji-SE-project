<template>
  <div class="room-management">
    <el-card class="filter-card">
      <div class="filter-bar">
        <el-radio-group v-model="statusFilter" @change="loadRooms">
          <el-radio-button label="ALL">全部</el-radio-button>
          <el-radio-button label="VACANT">空闲</el-radio-button>
          <el-radio-button label="OCCUPIED">已入住</el-radio-button>
          <el-radio-button label="CLEANING">待清洁</el-radio-button>
          <el-radio-button label="MAINTENANCE">维修中</el-radio-button>
        </el-radio-group>
        
        <el-button type="primary" @click="loadRooms">
          <el-icon><Refresh /></el-icon>
          刷新
        </el-button>
      </div>
    </el-card>
    
    <el-card class="rooms-card">
      <div class="rooms-grid">
        <div
          v-for="room in filteredRooms"
          :key="room.roomId"
          class="room-card"
          :class="getRoomClass(room.status)"
        >
          <div class="room-header">
            <div class="room-number">{{ room.roomNo }}</div>
            <el-tag :type="getStatusTagType(room.status)">
              {{ getStatusText(room.status) }}
            </el-tag>
          </div>
          
          <div class="room-body">
            <div class="room-info">
              <div class="info-item">
                <el-icon><Grid /></el-icon>
                <span>{{ getRoomTypeText(room.roomType) }}</span>
              </div>
              <div class="info-item">
                <el-icon><Money /></el-icon>
                <span>¥{{ room.pricePerDay }}/天</span>
              </div>
              <div class="info-item">
                <span>楼层: {{ room.floor }}F</span>
              </div>
            </div>
          </div>
          
          <div class="room-footer">
            <el-button
              v-if="room.status === 'VACANT'"
              type="primary"
              size="small"
              @click="goToCheckIn(room)"
            >
              办理入住
            </el-button>
            <el-button
              v-if="room.status === 'OCCUPIED'"
              type="warning"
              size="small"
              @click="handleCheckOut(room)"
            >
              办理退房
            </el-button>
            <el-button
              v-if="room.status === 'OCCUPIED'"
              type="primary"
              size="small"
              @click="handleViewBill(room)"
            >
              <el-icon><Tickets /></el-icon>
              账单明细
            </el-button>
            <el-button
              v-if="room.status === 'CLEANING'"
              type="success"
              size="small"
              @click="handleMarkCleaned(room)"
            >
              <el-icon><Check /></el-icon>
              打扫完毕
            </el-button>
            <el-button
              v-if="room.status === 'MAINTENANCE'"
              type="success"
              size="small"
              @click="handleMarkRepaired(room)"
            >
              <el-icon><Tools /></el-icon>
              已维修
            </el-button>
            <el-button
              v-if="room.status === 'VACANT' || room.status === 'CLEANING'"
              type="info"
              size="small"
              @click="handleMarkMaintenance(room)"
            >
              <el-icon><Tools /></el-icon>
              设为维修
            </el-button>
            <el-button
              size="small"
              @click="handleViewDetail(room)"
            >
              查看详情
            </el-button>
          </div>
        </div>
      </div>
      
      <el-empty v-if="filteredRooms.length === 0" description="暂无房间数据" />
    </el-card>
    
    <!-- 账单明细对话框 -->
    <el-dialog
      v-model="billDialogVisible"
      title="账单明细"
      width="800px"
    >
      <div v-if="billDetail">
        <!-- 账单总览 -->
        <el-descriptions :column="2" border>
          <el-descriptions-item label="房间号">{{ billDetail.roomNo }}</el-descriptions-item>
          <el-descriptions-item label="姓名">{{ billDetail.realName }}</el-descriptions-item>
          <el-descriptions-item label="账单总额">
            <span style="color: #409eff; font-size: 18px; font-weight: bold">
              ¥{{ billDetail.totalAmount }}
            </span>
          </el-descriptions-item>
          <el-descriptions-item label="已支付">
            <span style="color: #67c23a; font-size: 16px; font-weight: bold">
              ¥{{ billDetail.paidAmount }}
            </span>
          </el-descriptions-item>
          <el-descriptions-item label="待支付">
            <span style="color: #f56c6c; font-size: 16px; font-weight: bold">
              ¥{{ billDetail.unpaidAmount }}
            </span>
          </el-descriptions-item>
          <el-descriptions-item label="支付状态">
            <el-tag :type="billDetail.unpaidAmount === 0 ? 'success' : 'danger'">
              {{ billDetail.unpaidAmount === 0 ? '已结清' : '未结清' }}
            </el-tag>
          </el-descriptions-item>
        </el-descriptions>

        <!-- 房费明细 -->
        <el-divider content-position="left">房费明细</el-divider>
        <el-descriptions :column="2" border>
          <el-descriptions-item label="入住时间">{{ billDetail.actualCheckin }}</el-descriptions-item>
          <el-descriptions-item label="房费单价">¥{{ billDetail.pricePerDay }}/天</el-descriptions-item>
          <el-descriptions-item label="已入住天数">{{ billDetail.days }}天</el-descriptions-item>
          <el-descriptions-item label="房费小计">
            <span style="color: #409eff; font-weight: bold">¥{{ billDetail.roomFee }}</span>
          </el-descriptions-item>
        </el-descriptions>

        <!-- POS消费明细 -->
        <el-divider content-position="left">POS消费明细</el-divider>
        <el-table 
          v-if="billDetail.posOrders && billDetail.posOrders.length > 0"
          :data="billDetail.posOrders"
          border
          style="width: 100%"
        >
          <el-table-column prop="orderNo" label="订单号" width="180" />
          <el-table-column prop="createTime" label="下单时间" width="180" />
          <el-table-column prop="totalAmount" label="订单金额" width="120">
            <template #default="{ row }">
              <span style="color: #409eff">¥{{ row.totalAmount }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="status" label="状态" width="100">
            <template #default="{ row }">
              <el-tag :type="row.status === 'COMPLETED' ? 'success' : 'warning'">
                {{ row.status === 'COMPLETED' ? '已完成' : row.status === 'PENDING' ? '待处理' : '已取消' }}
              </el-tag>
            </template>
          </el-table-column>
        </el-table>
        <el-empty v-else description="暂无POS消费记录" :image-size="100" />

        <!-- POS总计 -->
        <div style="text-align: right; margin-top: 15px; font-size: 16px">
          <span>POS消费总计：</span>
          <span style="color: #409eff; font-weight: bold">¥{{ billDetail.posTotal }}</span>
        </div>
      </div>

      <template #footer>
        <el-button @click="billDialogVisible = false">关闭</el-button>
        <el-button
          v-if="billDetail && billDetail.unpaidAmount > 0"
          type="primary"
          @click="settlementDialogVisible = true"
        >
          账单清付
        </el-button>
      </template>
    </el-dialog>

    <!-- 账单清付对话框 -->
    <el-dialog
      v-model="settlementDialogVisible"
      title="账单清付"
      width="400px"
    >
      <el-form :model="settlementForm" label-width="100px">
        <el-form-item label="待支付金额">
          <span style="color: #f56c6c; font-size: 20px; font-weight: bold">
            ¥{{ billDetail?.unpaidAmount }}
          </span>
        </el-form-item>
        <el-form-item label="支付方式" required>
          <el-radio-group v-model="settlementForm.paymentMethod">
            <el-radio label="CASH">现金</el-radio>
            <el-radio label="WECHAT">微信支付</el-radio>
            <el-radio label="ALIPAY">支付宝</el-radio>
            <el-radio label="CARD">银行卡</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="settlementDialogVisible = false">取消</el-button>
        <el-button
          type="primary"
          :loading="settlementLoading"
          @click="confirmSettlement"
        >
          确认支付
        </el-button>
      </template>
    </el-dialog>

    <!-- 入住对话框 -->
    <el-dialog
      v-model="checkInDialogVisible"
      title="办理入住"
      width="500px"
    >
      <el-form
        ref="checkInFormRef"
        :model="checkInForm"
        :rules="checkInRules"
        label-width="100px"
      >
        <el-form-item label="房间号">
          <el-input v-model="selectedRoom.roomNo" disabled />
        </el-form-item>
        
        <el-form-item label="真实姓名" prop="realName">
          <el-input
            v-model="checkInForm.realName"
            placeholder="请输入真实姓名"
          />
        </el-form-item>
        
        <el-form-item label="身份证号" prop="identityCard">
          <el-input
            v-model="checkInForm.identityCard"
            placeholder="请输入身份证号"
          />
        </el-form-item>
        
        <el-form-item label="预计退房" prop="expectedCheckout">
          <el-date-picker
            v-model="checkInForm.expectedCheckout"
            type="datetime"
            placeholder="选择退房时间"
            style="width: 100%"
          />
        </el-form-item>
        
        <el-form-item label="特殊要求">
          <el-input
            v-model="checkInForm.specialRequests"
            type="textarea"
            placeholder="如有特殊要求请填写"
            :rows="2"
          />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="checkInDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="checkInLoading" @click="confirmCheckIn">
          确认入住
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Refresh, Grid, Money, User, Check, Tools, Tickets } from '@element-plus/icons-vue'
import { getRoomList, getRoomsByStatus, checkIn, checkOut, getCheckInRecords } from '@/api/room'
import { getBillDetailByRoomId, settleBill } from '@/api/billing'
import { createBillAlipay, queryAlipayStatus } from '@/api/payment'
import { useUserStore } from '@/stores/user'
import { useRouter } from 'vue-router'
import axios from 'axios'

const userStore = useUserStore()

// 支付轮询相关
let pollingTimer = null
let currentOutTradeNo = null
let pollingCount = 0
const MAX_POLLING_COUNT = 60

const startPaymentPolling = (outTradeNo, roomId) => {
  stopPaymentPolling()
  currentOutTradeNo = outTradeNo
  pollingCount = 0
  
  pollingTimer = setInterval(async () => {
    pollingCount++
    
    if (pollingCount > MAX_POLLING_COUNT) {
      stopPaymentPolling()
      ElMessage.warning('支付状态检测超时，请手动刷新查看')
      return
    }
    
    try {
      const res = await queryAlipayStatus(currentOutTradeNo)
      const { tradeStatus, success } = res.data
      
      if (success) {
        stopPaymentPolling()
        ElMessage.success('支付成功！')
        // 刷新账单
        if (roomId) {
          billDetail.value = await getBillDetailByRoomId(roomId)
        }
      } else if (tradeStatus === 'TRADE_CLOSED') {
        stopPaymentPolling()
        ElMessage.error('交易已关闭')
      }
    } catch (error) {
      console.error('轮询支付状态失败:', error)
    }
  }, 5000)
}

const stopPaymentPolling = () => {
  if (pollingTimer) {
    clearInterval(pollingTimer)
    pollingTimer = null
  }
}

// 房间列表
const rooms = ref([])
const statusFilter = ref('ALL')

// 过滤后的房间
const filteredRooms = computed(() => {
  if (statusFilter.value === 'ALL') {
    return rooms.value
  }
  return rooms.value.filter(room => room.status === statusFilter.value)
})

// 加载房间列表
const loadRooms = async () => {
  try {
    if (statusFilter.value === 'ALL') {
      rooms.value = await getRoomList()
    } else {
      rooms.value = await getRoomsByStatus(statusFilter.value)
    }
  } catch (error) {
    console.error('加载房间列表失败：', error)
  }
}

// 获取房间卡片样式类
const getRoomClass = (status) => {
  return {
    vacant: status === 'VACANT',
    occupied: status === 'OCCUPIED',
    cleaning: status === 'CLEANING',
    maintenance: status === 'MAINTENANCE'
  }
}

// 获取状态标签类型
const getStatusTagType = (status) => {
  const typeMap = {
    VACANT: 'success',
    OCCUPIED: 'danger',
    CLEANING: 'warning',
    MAINTENANCE: 'info'
  }
  return typeMap[status] || ''
}

// 获取状态文本
const getStatusText = (status) => {
  const textMap = {
    VACANT: '空闲',
    OCCUPIED: '已入住',
    CLEANING: '待清洁',
    MAINTENANCE: '维修中'
  }
  return textMap[status] || status
}
// 获取房型文本
const getRoomTypeText = (roomType) => {
  const typeMap = {
    SINGLE: '单人房',
    DOUBLE: '双人房',
    FIVE_PLAYER: '五人黑房',
    VIP: 'VIP房'
  }
  return typeMap[roomType] || roomType
}
// 入住对话框
const checkInDialogVisible = ref(false)
const selectedRoom = ref({})
const checkInFormRef = ref(null)
const checkInLoading = ref(false)
const checkInForm = reactive({
  realName: '',
  identityCard: '',
  expectedCheckout: null,
  specialRequests: ''
})

// 账单相关状态
const billDialogVisible = ref(false)
const billDetail = ref(null)
const settlementDialogVisible = ref(false)
const settlementLoading = ref(false)
const settlementForm = reactive({
  paymentMethod: 'ALIPAY'
})

const checkInRules = {
  realName: [
    { required: true, message: '请输入真实姓名', trigger: 'blur' }
  ],
  identityCard: [
    { required: true, message: '请输入身份证号', trigger: 'blur' },
    { pattern: /^[1-9]\d{5}(18|19|20)\d{2}(0[1-9]|1[0-2])(0[1-9]|[12]\d|3[01])\d{3}[\dXx]$/, message: '身份证号格式不正确', trigger: 'blur' }
  ],
  expectedCheckout: [
    { required: true, message: '请选择预计退房时间', trigger: 'change' }
  ]
}

// 处理入住
const handleCheckIn = (room) => {
  selectedRoom.value = room
  checkInForm.realName = ''
  checkInForm.identityCard = ''
  checkInForm.expectedCheckout = null
  checkInForm.specialRequests = ''
  checkInDialogVisible.value = true
}

// 确认入住
const confirmCheckIn = async () => {
  if (!checkInFormRef.value) return
  
  await checkInFormRef.value.validate(async (valid) => {
    if (valid) {
      checkInLoading.value = true
      try {
        const data = await checkIn({
          guestId: userStore.userInfo.userId,
          roomId: selectedRoom.value.roomId,
          realName: checkInForm.realName,
          identityCard: checkInForm.identityCard,
          expectedCheckout: checkInForm.expectedCheckout,
          specialRequests: checkInForm.specialRequests
        })
        
        // 保存 Room-Auth-Token
        if (data.roomAuthToken) {
          userStore.setRoomAuthToken(data.roomAuthToken)
        }
        
        ElMessage.success('入住成功')
        checkInDialogVisible.value = false
        loadRooms()
      } catch (error) {
        console.error('入住失败：', error)
      } finally {
        checkInLoading.value = false
      }
    }
  })
}

// 查看账单
const handleViewBill = async (room) => {
  try {
    billDetail.value = await getBillDetailByRoomId(room.roomId)
    billDialogVisible.value = true
  } catch (error) {
    console.error('获取账单失败：', error)
    ElMessage.error('获取账单信息失败')
  }
}

// 确认结算
const confirmSettlement = async () => {
  if (!settlementForm.paymentMethod) {
    ElMessage.warning('请选择支付方式')
    return
  }
  
  settlementLoading.value = true
  try {
    // 如果选择支付宝支付，跳转到支付宝页面
    if (settlementForm.paymentMethod === 'ALIPAY') {
      const payResponse = await createBillAlipay(billDetail.value.recordId)
      const { outTradeNo, formHtml } = payResponse.data
      
      // 打开新窗口显示支付宝支付页面
      const payWindow = window.open('', '_blank')
      if (payWindow) {
        payWindow.document.write(formHtml)
        payWindow.document.close()
        
        ElMessage.info('支付页面已打开，系统将自动检测支付结果...')
        settlementDialogVisible.value = false
        
        // 开始轮询检测支付状态
        startPaymentPolling(outTradeNo, billDetail.value.roomId)
      } else {
        ElMessage.error('无法打开支付窗口，请检查浏览器弹窗设置')
      }
    } else {
      // 其他支付方式直接调用原有接口
      await settleBill(billDetail.value.recordId, settlementForm.paymentMethod)
      ElMessage.success('结算成功')
      settlementDialogVisible.value = false
      billDialogVisible.value = false
      
      // 重新加载账单信息
      if (billDetail.value) {
        billDetail.value = await getBillDetailByRoomId(billDetail.value.roomId)
      }
    }
  } catch (error) {
    console.error('结算失败：', error)
    ElMessage.error('结算失败：' + (error.message || '未知错误'))
  } finally {
    settlementLoading.value = false
  }
}

// 处理退房
const handleCheckOut = async (room) => {
  try {
    // 获取入住记录
    const records = await getCheckInRecords()
    const record = records.find(r => r.roomId === room.roomId && !r.actualCheckout)
    
    if (!record) {
      ElMessage.warning('未找到该房间的入住记录')
      return
    }
    
    // 检查账单是否结清
    try {
      const billDetail = await getBillDetailByRoomId(room.roomId)
      if (billDetail.unpaidAmount > 0) {
        ElMessage.error(`该房间费用未结清，待支付金额：¥${billDetail.unpaidAmount}`)
        return
      }
    } catch (error) {
      console.error('获取账单信息失败：', error)
      ElMessage.error('无法获取账单信息，请稍后重试')
      return
    }
    
    await ElMessageBox.confirm(
      `确认为房间 ${room.roomNo} 办理退房？`,
      '退房确认',
      {
        confirmButtonText: '确认退房',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    await checkOut(record.recordId)
    
    // 清除 Room-Auth-Token
    userStore.setRoomAuthToken('')
    
    ElMessage.success('退房成功')
    loadRooms()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('退房失败：', error)
    }
  }
}

// 查看详情
const handleViewDetail = (room) => {
  ElMessageBox.alert(
    `
    <p><strong>房间号：</strong>${room.roomNo}</p>
    <p><strong>房型：</strong>${getRoomTypeText(room.roomType)}</p>
    <p><strong>楼层：</strong>${room.floor}楼</p>
    <p><strong>状态：</strong>${getStatusText(room.status)}</p>
    <p><strong>价格：</strong>¥${room.pricePerDay}/天</p>
    <p><strong>高级房型：</strong>${room.isPremium ? '是' : '否'}</p>
    `,
    '房间详情',
    {
      dangerouslyUseHTMLString: true
    }
  )
}

// 跳转到入住登记页面
const router = useRouter()
const goToCheckIn = (room) => {
  // 跳转到入住登记页面，携带房间信息
  router.push({
    path: '/staff/checkin',
    query: {
      roomId: room.roomId,
      roomNo: room.roomNo
    }
  })
}

// 标记房间为打扫完毕
const handleMarkCleaned = async (room) => {
  try {
    await ElMessageBox.confirm(`确认将房间 ${room.roomNo} 标记为打扫完毕？`, '提示', {
      confirmButtonText: '确认',
      cancelButtonText: '取消',
      type: 'info'
    })
    
    await axios.post(`/api/rooms/${room.roomId}/mark-cleaned`)
    ElMessage.success('已标记为打扫完毕')
    loadRooms()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('标记失败：', error)
      ElMessage.error('标记失败')
    }
  }
}

// 标记房间为维修完成
const handleMarkRepaired = async (room) => {
  try {
    await ElMessageBox.confirm(`确认将房间 ${room.roomNo} 标记为维修完成？`, '提示', {
      confirmButtonText: '确认',
      cancelButtonText: '取消',
      type: 'info'
    })
    
    await axios.post(`/api/rooms/${room.roomId}/mark-repaired`)
    ElMessage.success('已标记为维修完成')
    loadRooms()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('标记失败：', error)
      ElMessage.error('标记失败')
    }
  }
}

// 设置房间为维修中
const handleMarkMaintenance = async (room) => {
  try {
    const { value: reason } = await ElMessageBox.prompt('请输入维修原因', '设置为维修中', {
      confirmButtonText: '确认',
      cancelButtonText: '取消',
      inputPattern: /.+/,
      inputErrorMessage: '请输入维修原因'
    })
    
    await axios.post(`/api/rooms/${room.roomId}/mark-maintenance`, null, {
      params: { reason }
    })
    ElMessage.success('已设置为维修中')
    loadRooms()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('设置失败：', error)
      ElMessage.error('设置失败')
    }
  }
}

onMounted(() => {
  loadRooms()
})
</script>

<style scoped>
.room-management {
  padding: 20px;
  height: calc(100vh - 120px);
  overflow-y: auto;
}

.filter-card {
  margin-bottom: 20px;
}

.filter-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.rooms-card {
  min-height: 500px;
}

.rooms-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 20px;
}

.room-card {
  border: 2px solid #e6e6e6;
  border-radius: 8px;
  padding: 15px;
  transition: all 0.3s;
}

.room-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  transform: translateY(-2px);
}

.room-card.vacant {
  border-color: #67c23a;
  background: linear-gradient(135deg, #f0f9ff 0%, #e0f2fe 100%);
}

.room-card.occupied {
  border-color: #f56c6c;
  background: linear-gradient(135deg, #fef2f2 0%, #fee2e2 100%);
}

.room-card.cleaning {
  border-color: #e6a23c;
  background: linear-gradient(135deg, #fffbeb 0%, #fef3c7 100%);
}

.room-card.maintenance {
  border-color: #909399;
  background: linear-gradient(135deg, #f9fafb 0%, #f3f4f6 100%);
}

.room-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.room-number {
  font-size: 24px;
  font-weight: bold;
  color: #333;
}

.room-body {
  margin-bottom: 15px;
}

.room-info {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 5px;
  color: #666;
  font-size: 14px;
}

.room-footer {
  display: flex;
  gap: 8px;
}

.room-footer .el-button {
  flex: 1;
}
</style>
