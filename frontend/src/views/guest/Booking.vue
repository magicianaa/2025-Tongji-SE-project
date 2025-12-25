<template>
  <div class="guest-booking">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>房间预订</span>
        </div>
      </template>

      <el-tabs v-model="activeTab" @tab-change="handleTabChange">
        <el-tab-pane label="预订房间" name="booking">
          <el-form :model="bookingForm" :rules="bookingRules" ref="bookingFormRef" label-width="120px">
        <el-form-item label="入住日期" prop="checkInDate">
          <el-date-picker
            v-model="bookingForm.checkInDate"
            type="date"
            placeholder="选择入住日期"
            :disabled-date="disabledBeforeToday"
            style="width: 100%;"
            @change="loadAvailableRooms"
          />
        </el-form-item>

        <el-form-item label="退房日期" prop="checkOutDate">
          <el-date-picker
            v-model="bookingForm.checkOutDate"
            type="date"
            placeholder="选择退房日期"
            :disabled-date="disabledBeforeCheckIn"
            style="width: 100%;"
            @change="loadAvailableRooms"
          />
        </el-form-item>

        <el-form-item label="主住客姓名" prop="mainGuestName">
          <el-input
            v-model="bookingForm.mainGuestName"
            placeholder="请输入主住客姓名（用于入住验证）"
            maxlength="20"
          >
            <template #prefix>
              <el-icon><User /></el-icon>
            </template>
          </el-input>
        </el-form-item>

        <el-form-item label="特殊要求">
          <el-input
            v-model="bookingForm.specialRequests"
            type="textarea"
            :rows="3"
            placeholder="有任何特殊要求请在此说明"
            maxlength="200"
            show-word-limit
          />
        </el-form-item>
      </el-form>

      <el-divider />

      <div class="room-list">
        <h3>可预订房间</h3>
        <el-empty description="请先选择入住和退房日期" v-if="!bookingForm.checkInDate || !bookingForm.checkOutDate" />
        
        <el-row :gutter="20" v-else>
          <el-col :span="8" v-for="room in availableRooms" :key="room.roomId">
            <el-card shadow="hover" class="room-card">
              <div class="room-info">
                <div class="room-no">{{ room.roomNo }}</div>
                <div class="room-type">{{ getRoomTypeName(room.roomType) }}</div>
                <div class="room-price">¥{{ room.pricePerDay }}/天</div>
                <div class="room-status" v-if="room.hasBooking">
                  <el-tag type="warning" size="small">已有预订</el-tag>
                </div>
                <el-button 
                  type="primary" 
                  @click="handleBook(room)" 
                  :disabled="room.hasBooking"
                  style="width: 100%; margin-top: 10px;"
                  :loading="bookingLoading"
                >
                  {{ room.hasBooking ? '已被预订' : '立即预订' }}
                </el-button>
                <el-button 
                  type="info" 
                  plain
                  @click="viewRoomReviews(room)" 
                  style="width: 100%; margin-top: 8px;"
                  :icon="Document"
                >
                  查看评价
                </el-button>
              </div>
            </el-card>
          </el-col>
        </el-row>
      </div>
        </el-tab-pane>

        <el-tab-pane label="我的预订" name="myBookings">
          <div class="my-bookings">
            <el-button type="primary" @click="loadMyBookings" :loading="loadingBookings" style="margin-bottom: 20px;">
              <el-icon><Refresh /></el-icon> 刷新
            </el-button>

            <el-empty description="暂无预订记录" v-if="myBookings.length === 0 && !loadingBookings" />

            <el-table :data="myBookings" v-else stripe style="width: 100%;">
              <el-table-column prop="roomNo" label="房间号" width="100" />
              <el-table-column label="房型" width="150">
                <template #default="{ row }">
                  {{ getRoomTypeName(row.roomType) }}
                </template>
              </el-table-column>
              <el-table-column prop="mainGuestName" label="主住客" width="120" />
              <el-table-column label="预订时间" width="180">
                <template #default="{ row }">
                  {{ formatDateTime(row.bookingTime) }}
                </template>
              </el-table-column>
              <el-table-column label="入住日期" width="120">
                <template #default="{ row }">
                  {{ formatDate(row.plannedCheckin) }}
                </template>
              </el-table-column>
              <el-table-column label="退房日期" width="120">
                <template #default="{ row }">
                  {{ formatDate(row.plannedCheckout) }}
                </template>
              </el-table-column>
              <el-table-column label="状态" width="100">
                <template #default="{ row }">
                  <el-tag :type="getStatusType(row.status)">
                    {{ getStatusName(row.status) }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column label="支付状态" width="100">
                <template #default="{ row }">
                  <el-tag :type="getPaymentStatusType(row.depositPaymentStatus || 'UNPAID')">
                    {{ getPaymentStatusName(row.depositPaymentStatus || 'UNPAID') }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column label="押金" width="100">
                <template #default="{ row }">
                  ¥{{ row.depositAmount }}
                </template>
              </el-table-column>
              <el-table-column label="操作" fixed="right" width="200">
                <template #default="{ row }">
                  <el-button 
                    v-if="row.depositPaymentStatus === 'UNPAID'"
                    type="primary" 
                    size="small" 
                    @click="handlePayDeposit(row)"
                  >
                    支付订金
                  </el-button>
                  <el-button 
                    v-if="row.depositPaymentStatus === 'PAID' && row.status === 'CONFIRMED'"
                    type="warning" 
                    size="small" 
                    @click="handleRefundDeposit(row)"
                  >
                    申请退款
                  </el-button>
                  <el-button 
                    v-if="row.status === 'CANCELLED'"
                    type="info" 
                    size="small"
                    disabled
                  >
                    已取消
                  </el-button>
                  <el-button 
                    v-if="row.status === 'CHECKED_IN'"
                    type="success" 
                    size="small"
                    disabled
                  >
                    已入住
                  </el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <!-- 房间评价对话框 -->
    <el-dialog
      v-model="reviewDialogVisible"
      :title="`${selectedRoom?.roomNo || ''} - 住客评价`"
      width="700px"
      top="5vh"
    >
      <RoomReviews 
        v-if="selectedRoom && reviewDialogVisible" 
        :room-id="selectedRoom.roomId" 
        :auto-load="true"
      />
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, h } from 'vue'
import { ElMessage, ElMessageBox, ElRadioGroup, ElRadio } from 'element-plus'
import { User, Refresh, Document } from '@element-plus/icons-vue'
import axios from 'axios'
import RoomReviews from '@/components/RoomReviews.vue'

const activeTab = ref('booking')
const bookingFormRef = ref(null)
const bookingForm = reactive({
  checkInDate: null,
  checkOutDate: null,
  mainGuestName: '',
  specialRequests: ''
})

const myBookings = ref([])
const loadingBookings = ref(false)

const bookingRules = {
  checkInDate: [
    { required: true, message: '请选择入住日期', trigger: 'change' }
  ],
  checkOutDate: [
    { required: true, message: '请选择退房日期', trigger: 'change' }
  ],
  mainGuestName: [
    { required: true, message: '请输入主住客姓名', trigger: 'blur' },
    { min: 2, max: 20, message: '姓名长度在2-20个字符', trigger: 'blur' }
  ]
}

const availableRooms = ref([])
const bookingLoading = ref(false)

// 评价对话框
const reviewDialogVisible = ref(false)
const selectedRoom = ref(null)

const disabledBeforeToday = (date) => {
  // 允许选择今天及以后的日期
  const today = new Date(new Date().setHours(0, 0, 0, 0))
  return date < today
}

const disabledBeforeCheckIn = (date) => {
  if (!bookingForm.checkInDate) return true
  return date <= bookingForm.checkInDate
}

const getRoomTypeName = (type) => {
  const typeMap = {
    'SINGLE': '单人电竞房',
    'DOUBLE': '双人电竞房',
    'FIVE_PLAYER': '五黑开黑房',
    'VIP': '豪华电竞套房'
  }
  return typeMap[type] || type
}

const loadAvailableRooms = async () => {
  if (!bookingForm.checkInDate || !bookingForm.checkOutDate) {
    availableRooms.value = []
    return
  }

  try {
    const response = await axios.get('/api/rooms', {
      headers: {
        'Authorization': `Bearer ${localStorage.getItem('token')}`
      }
    })
    
    if (response.data.code === 200) {
      // 筛选VACANT和已预订的房间
      availableRooms.value = response.data.data.filter(room => 
        room.status === 'VACANT'
      )
    }
  } catch (error) {
    console.error('加载房间失败:', error)
    ElMessage.error('加载可用房间失败')
  }
}

// 查看房间评价
const viewRoomReviews = (room) => {
  selectedRoom.value = room
  reviewDialogVisible.value = true
}

const handleBook = async (room) => {
  // 验证表单
  if (!bookingFormRef.value) return
  
  try {
    await bookingFormRef.value.validate()
  } catch {
    ElMessage.warning('请完整填写预订信息')
    return
  }

  try {
    await ElMessageBox.confirm(
      `确认预订房间 ${room.roomNo}？<br>
      入住日期：${formatDate(bookingForm.checkInDate)}<br>
      退房日期：${formatDate(bookingForm.checkOutDate)}<br>
      主住客：${bookingForm.mainGuestName}`,
      '确认预订',
      {
        confirmButtonText: '确认',
        cancelButtonText: '取消',
        dangerouslyUseHTMLString: true,
        type: 'info'
      }
    )

    bookingLoading.value = true
    
    // 从localStorage获取用户手机号（用户名就是手机号）
    const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
    const contactPhone = userInfo.username || ''
    
    if (!contactPhone) {
      ElMessage.error('无法获取用户手机号，请重新登录')
      bookingLoading.value = false
      return
    }
    
    const bookingData = {
      roomId: room.roomId,
      plannedCheckin: formatDate(bookingForm.checkInDate),
      plannedCheckout: formatDate(bookingForm.checkOutDate),
      mainGuestName: bookingForm.mainGuestName.trim(),
      contactPhone: contactPhone,
      specialRequests: bookingForm.specialRequests.trim() || null
    }

    const response = await axios.post('/api/bookings', bookingData, {
      headers: {
        'Authorization': `Bearer ${localStorage.getItem('token')}`,
        'Content-Type': 'application/json'
      }
    })

    if (response.data.code === 200) {
      const bookingResponse = response.data.data
      ElMessage.success('预订创建成功，请支付订金')
      
      // 弹出支付对话框
      await showPaymentDialog(bookingResponse)
      
      // 重置表单
      bookingForm.mainGuestName = ''
      bookingForm.specialRequests = ''
      // 重新加载房间列表
      await loadAvailableRooms()
    } else {
      ElMessage.error(response.data.message || '预订失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('预订失败:', error)
      ElMessage.error(error.response?.data?.message || '预订失败，请稍后重试')
    }
  } finally {
    bookingLoading.value = false
  }
}

// 支付对话框
const showPaymentDialog = async (bookingResponse) => {
  const paymentMethodOptions = [
    { label: '现金支付', value: 'CASH' },
    { label: '微信支付', value: 'WECHAT' },
    { label: '支付宝支付', value: 'ALIPAY' },
    { label: '银行卡支付', value: 'CARD' }
  ]
  
  const paymentMethod = ref('WECHAT')
  
  try {
    await ElMessageBox({
      title: '支付订金',
      message: h('div', [
        h('p', { style: 'margin-bottom: 15px; font-size: 16px;' }, [
          '预订订金：',
          h('span', { 
            style: 'color: #f56c6c; font-weight: bold; font-size: 20px;' 
          }, `¥${bookingResponse.depositAmount}`)
        ]),
        h('p', { style: 'margin-bottom: 10px;' }, '请选择支付方式：'),
        h(ElRadioGroup, {
          modelValue: paymentMethod.value,
          'onUpdate:modelValue': (val) => { paymentMethod.value = val }
        }, () => paymentMethodOptions.map(option => 
          h(ElRadio, { label: option.value, key: option.value }, () => option.label)
        ))
      ]),
      showCancelButton: true,
      confirmButtonText: '确认支付',
      cancelButtonText: '稍后支付',
      beforeClose: async (action, instance, done) => {
        if (action === 'confirm') {
          instance.confirmButtonLoading = true
          try {
            const payResponse = await axios.post(
              `/api/bookings/${bookingResponse.bookingId}/pay-deposit`,
              null,
              {
                params: { paymentMethod: paymentMethod.value },
                headers: {
                  'Authorization': `Bearer ${localStorage.getItem('token')}`
                }
              }
            )
            
            if (payResponse.data.code === 200) {
              ElMessage.success('支付成功！')
              done()
              // 提示查看预订
              setTimeout(() => {
                ElMessageBox.confirm(
                  '支付成功，是否前往查看我的预订？',
                  '支付成功',
                  {
                    confirmButtonText: '查看预订',
                    cancelButtonText: '继续预订',
                    type: 'success'
                  }
                ).then(() => {
                  activeTab.value = 'myBookings'
                  loadMyBookings()
                }).catch(() => {})
              }, 100)
            } else {
              ElMessage.error(payResponse.data.message || '支付失败')
            }
          } catch (error) {
            console.error('支付失败:', error)
            ElMessage.error(error.response?.data?.message || '支付失败')
          } finally {
            instance.confirmButtonLoading = false
          }
        } else {
          ElMessage.info('您可以稍后在"我的预订"中完成支付')
          done()
        }
      }
    })
  } catch (error) {
    // 用户取消或关闭对话框
  }
}

const formatDate = (date) => {
  if (!date) return ''
  const d = new Date(date)
  const year = d.getFullYear()
  const month = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

const formatDateTime = (datetime) => {
  if (!datetime) return ''
  const d = new Date(datetime)
  const year = d.getFullYear()
  const month = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  const hour = String(d.getHours()).padStart(2, '0')
  const minute = String(d.getMinutes()).padStart(2, '0')
  return `${year}-${month}-${day} ${hour}:${minute}`
}

const getStatusType = (status) => {
  const typeMap = {
    'PENDING': 'info',
    'CONFIRMED': 'success',
    'CHECKED_IN': 'warning',
    'CANCELLED': 'danger'
  }
  return typeMap[status] || 'info'
}

const getStatusName = (status) => {
  const nameMap = {
    'PENDING': '待支付',
    'CONFIRMED': '已确认',
    'CHECKED_IN': '已入住',
    'CANCELLED': '已取消'
  }
  return nameMap[status] || status
}

const getPaymentStatusType = (status) => {
  const typeMap = {
    'UNPAID': 'warning',
    'PAID': 'success',
    'REFUNDED': 'info'
  }
  return typeMap[status] || 'info'
}

const getPaymentStatusName = (status) => {
  const nameMap = {
    'UNPAID': '未支付',
    'PAID': '已支付',
    'REFUNDED': '已退款'
  }
  return nameMap[status] || status
}

const handlePayDeposit = async (booking) => {
  await showPaymentDialog(booking)
  loadMyBookings()
}

const handleRefundDeposit = async (booking) => {
  // 检查是否可以退款（入住日期之前）
  const checkinDate = new Date(booking.plannedCheckin)
  const now = new Date()
  now.setHours(0, 0, 0, 0)
  checkinDate.setHours(0, 0, 0, 0)
  
  if (now >= checkinDate) {
    ElMessage.error('已到入住日期，无法退订')
    return
  }
  
  try {
    await ElMessageBox.confirm(
      `确认申请退款？<br>
      房间：${booking.roomNo}<br>
      入住日期：${formatDate(booking.plannedCheckin)}<br>
      退款金额：¥${booking.depositAmount}`,
      '申请退款',
      {
        confirmButtonText: '确认退款',
        cancelButtonText: '取消',
        dangerouslyUseHTMLString: true,
        type: 'warning'
      }
    )

    const response = await axios.post(
      `/api/bookings/${booking.bookingId}/refund-deposit`,
      null,
      {
        headers: {
          'Authorization': `Bearer ${localStorage.getItem('token')}`
        }
      }
    )

    if (response.data.code === 200) {
      ElMessage.success('退款成功')
      loadMyBookings()
      // 如果在预订tab，也刷新房间列表
      if (bookingForm.checkInDate && bookingForm.checkOutDate) {
        loadAvailableRooms()
      }
    } else {
      ElMessage.error(response.data.message || '退款失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('退款失败:', error)
      ElMessage.error(error.response?.data?.message || '退款失败，请稍后重试')
    }
  }
}

const loadMyBookings = async () => {
  loadingBookings.value = true
  try {
    const response = await axios.get('/api/bookings/my', {
      headers: {
        'Authorization': `Bearer ${localStorage.getItem('token')}`
      }
    })
    
    if (response.data.code === 200) {
      myBookings.value = response.data.data || []
    } else {
      ElMessage.error('加载预订列表失败')
    }
  } catch (error) {
    console.error('加载预订列表失败:', error)
    ElMessage.error('加载预订列表失败')
  } finally {
    loadingBookings.value = false
  }
}

const handleTabChange = (tabName) => {
  if (tabName === 'myBookings') {
    loadMyBookings()
  }
}

onMounted(() => {
  // 初始化时不加载，等用户选择日期后再加载
})
</script>

<style scoped>
.guest-booking {
  padding: 20px;
}

.room-list {
  margin-top: 20px;
}

.room-card {
  margin-bottom: 20px;
  transition: transform 0.3s;
}

.room-card:hover {
  transform: translateY(-5px);
}

.room-info {
  text-align: center;
}

.room-no {
  font-size: 24px;
  font-weight: bold;
  color: #409eff;
  margin-bottom: 5px;
}

.room-type {
  font-size: 14px;
  color: #909399;
  margin-bottom: 10px;
}

.room-price {
  font-size: 20px;
  color: #f56c6c;
  font-weight: bold;
}

.my-bookings {
  padding: 20px 0;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
