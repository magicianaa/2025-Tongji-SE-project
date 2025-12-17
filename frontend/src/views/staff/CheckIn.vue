<template>
  <div class="checkin-container">
    <el-card class="form-card">
      <template #header>
        <div class="card-header">
          <h2><el-icon><UserFilled /></el-icon> 入住登记</h2>
          <p class="subtitle">办理客人入住手续</p>
        </div>
      </template>

      <el-tabs v-model="activeTab" @tab-change="handleTabChange">
        <!-- Walk-in 入住标签页 -->
        <el-tab-pane label="未预订入住 (Walk-in)" name="walkin">
          <el-form
        ref="checkInFormRef"
        :model="checkInForm"
        :rules="formRules"
        label-width="120px"
        label-position="top"
      >
        <!-- 房间选择 -->
        <el-form-item label="选择房间" prop="roomNo">
          <el-select
            v-model="checkInForm.roomNo"
            placeholder="请选择房间"
            filterable
            @change="handleRoomChange"
            style="width: 100%"
          >
            <el-option
              v-for="room in availableRooms"
              :key="room.roomId"
              :label="`${room.roomNo} - ${getRoomTypeLabel(room.roomType)} (${room.currentOccupancy}/${room.maxOccupancy}人)`"
              :value="room.roomNo"
              :disabled="room.status !== 'VACANT'"
            >
              <div class="room-option">
                <span class="room-no">{{ room.roomNo }}</span>
                <span class="room-type">{{ getRoomTypeLabel(room.roomType) }}</span>
                <el-tag :type="getStatusType(room.status)" size="small">
                  {{ getStatusLabel(room.status) }}
                </el-tag>
                <span class="occupancy">{{ room.currentOccupancy }}/{{ room.maxOccupancy }}人</span>
                <span class="price">¥{{ room.pricePerHour }}/小时</span>
              </div>
            </el-option>
          </el-select>
        </el-form-item>

        <!-- 房间信息展示 -->
        <el-alert
          v-if="selectedRoom"
          type="info"
          :closable="false"
          style="margin-bottom: 20px"
        >
          <div class="room-info">
            <div><strong>房间类型：</strong>{{ getRoomTypeLabel(selectedRoom.roomType) }}</div>
            <div><strong>最大容纳：</strong>{{ selectedRoom.maxOccupancy }}人</div>
            <div><strong>房间价格：</strong>¥{{ selectedRoom.pricePerHour }}/小时</div>
            <div><strong>当前入住：</strong>{{ selectedRoom.currentOccupancy }}人</div>
          </div>
        </el-alert>

        <!-- 预期退房时间 -->
        <el-form-item label="预期退房时间" prop="expectedCheckout">
          <el-date-picker
            v-model="checkInForm.expectedCheckout"
            type="datetime"
            placeholder="选择预期退房时间"
            :disabled-date="disabledDate"
            format="YYYY-MM-DD HH:mm"
            value-format="YYYY-MM-DDTHH:mm:ss"
            style="width: 100%"
          />
        </el-form-item>

        <!-- 住客信息列表 -->
        <el-form-item label="住客信息">
          <div class="guests-section">
            <div
              v-for="(guest, index) in checkInForm.guests"
              :key="index"
              class="guest-item"
            >
              <div class="guest-header">
                <h3>住客 {{ index + 1 }}</h3>
                <el-button
                  v-if="checkInForm.guests.length > 1"
                  type="danger"
                  text
                  @click="removeGuest(index)"
                >
                  <el-icon><Close /></el-icon> 移除
                </el-button>
              </div>

              <el-row :gutter="20">
                <el-col :span="8">
                  <el-form-item
                    :prop="'guests.' + index + '.phone'"
                    :rules="formRules.phone"
                    label="手机号"
                  >
                    <el-input
                      v-model="guest.phone"
                      placeholder="请输入手机号"
                      maxlength="11"
                    >
                      <template #prefix>
                        <el-icon><Phone /></el-icon>
                      </template>
                    </el-input>
                  </el-form-item>
                </el-col>

                <el-col :span="8">
                  <el-form-item
                    :prop="'guests.' + index + '.realName'"
                    :rules="formRules.realName"
                    label="真实姓名"
                  >
                    <el-input
                      v-model="guest.realName"
                      placeholder="请输入真实姓名"
                    >
                      <template #prefix>
                        <el-icon><User /></el-icon>
                      </template>
                    </el-input>
                  </el-form-item>
                </el-col>

                <el-col :span="8">
                  <el-form-item
                    :prop="'guests.' + index + '.idCard'"
                    :rules="formRules.idCard"
                    label="身份证号"
                  >
                    <el-input
                      v-model="guest.idCard"
                      placeholder="请输入身份证号"
                      maxlength="18"
                    >
                      <template #prefix>
                        <el-icon><Postcard /></el-icon>
                      </template>
                    </el-input>
                  </el-form-item>
                </el-col>
              </el-row>
            </div>

            <!-- 添加住客按钮 -->
            <el-button
              type="primary"
              :disabled="!canAddMoreGuests"
              @click="addGuest"
              style="width: 100%; margin-top: 10px"
            >
              <el-icon><Plus /></el-icon> 添加住客
            </el-button>
            <div v-if="!canAddMoreGuests" class="warning-text">
              已达到房间最大容纳人数
            </div>
          </div>
        </el-form-item>

        <!-- 操作按钮 -->
        <el-form-item>
          <el-button
            type="primary"
            size="large"
            :loading="submitting"
            @click="handleSubmit"
            style="width: 200px"
          >
            <el-icon><Check /></el-icon> 办理入住
          </el-button>
          <el-button size="large" @click="handleReset">
            <el-icon><RefreshLeft /></el-icon> 重置
          </el-button>
        </el-form-item>
      </el-form>
        </el-tab-pane>

        <!-- 预订入住标签页 -->
        <el-tab-pane label="预订入住" name="booking">
          <el-form
            ref="bookingCheckInFormRef"
            :model="bookingCheckInForm"
            :rules="bookingFormRules"
            label-width="120px"
            label-position="top"
          >
            <!-- 手机号查询 -->
            <el-form-item label="预订者手机号" prop="bookingPhone">
              <el-input
                v-model="bookingCheckInForm.bookingPhone"
                placeholder="请输入预订者手机号"
                maxlength="11"
                style="width: 300px"
              >
                <template #prefix>
                  <el-icon><Phone /></el-icon>
                </template>
                <template #append>
                  <el-button
                    type="primary"
                    :loading="queryingBooking"
                    @click="queryBooking"
                  >
                    查询预订
                  </el-button>
                </template>
              </el-input>
            </el-form-item>

            <!-- 预订信息展示 -->
            <el-alert
              v-if="bookingInfo"
              type="success"
              :closable="false"
              style="margin-bottom: 20px"
            >
              <div class="booking-info">
                <h3>预订信息</h3>
                <el-row :gutter="20">
                  <el-col :span="12">
                    <p><strong>房间号：</strong>{{ bookingInfo.roomNo }}</p>
                    <p><strong>房间类型：</strong>{{ getRoomTypeLabel(bookingInfo.roomType) }}</p>
                  </el-col>
                  <el-col :span="12">
                    <p><strong>计划入住：</strong>{{ formatDateTime(bookingInfo.plannedCheckin) }}</p>
                    <p><strong>计划退房：</strong>{{ formatDateTime(bookingInfo.plannedCheckout) }}</p>
                  </el-col>
                </el-row>
                <p v-if="bookingInfo.specialRequests"><strong>特殊要求：</strong>{{ bookingInfo.specialRequests }}</p>
              </div>
            </el-alert>

            <template v-if="bookingInfo">
              <!-- 预期退房时间 -->
              <el-form-item label="预期退房时间" prop="expectedCheckout">
                <el-date-picker
                  v-model="bookingCheckInForm.expectedCheckout"
                  type="datetime"
                  placeholder="选择预期退房时间"
                  :disabled-date="disabledDate"
                  format="YYYY-MM-DD HH:mm"
                  value-format="YYYY-MM-DDTHH:mm:ss"
                  style="width: 100%"
                />
              </el-form-item>

              <!-- 住客信息列表 -->
              <el-form-item label="住客信息">
                <el-alert type="info" :closable="false" style="margin-bottom: 15px">
                  <p>请根据预订房间的最大容纳人数录入住客信息</p>
                  <p><strong>该房间最大容纳：{{ bookingMaxOccupancy }}人</strong></p>
                </el-alert>

                <div class="guests-section">
                  <div
                    v-for="(guest, index) in bookingCheckInForm.guests"
                    :key="index"
                    class="guest-item"
                  >
                    <div class="guest-header">
                      <h3>住客 {{ index + 1 }}</h3>
                      <el-button
                        v-if="bookingCheckInForm.guests.length > 1"
                        type="danger"
                        text
                        @click="removeBookingGuest(index)"
                      >
                        <el-icon><Close /></el-icon> 移除
                      </el-button>
                    </div>

                    <el-row :gutter="20">
                      <el-col :span="8">
                        <el-form-item
                          :prop="'guests.' + index + '.phone'"
                          label="手机号"
                        >
                          <el-input
                            v-model="guest.phone"
                            placeholder="请输入手机号"
                            maxlength="11"
                          >
                            <template #prefix>
                              <el-icon><Phone /></el-icon>
                            </template>
                          </el-input>
                        </el-form-item>
                      </el-col>

                      <el-col :span="8">
                        <el-form-item
                          :prop="'guests.' + index + '.realName'"
                          label="真实姓名"
                        >
                          <el-input
                            v-model="guest.realName"
                            placeholder="请输入真实姓名"
                          >
                            <template #prefix>
                              <el-icon><User /></el-icon>
                            </template>
                          </el-input>
                        </el-form-item>
                      </el-col>

                      <el-col :span="8">
                        <el-form-item
                          :prop="'guests.' + index + '.idCard'"
                          label="身份证号"
                        >
                          <el-input
                            v-model="guest.idCard"
                            placeholder="请输入身份证号"
                            maxlength="18"
                          >
                            <template #prefix>
                              <el-icon><Postcard /></el-icon>
                            </template>
                          </el-input>
                        </el-form-item>
                      </el-col>
                    </el-row>
                  </div>

                  <!-- 添加住客按钮 -->
                  <el-button
                    type="primary"
                    :disabled="!canAddMoreBookingGuests"
                    @click="addBookingGuest"
                    style="width: 100%; margin-top: 10px"
                  >
                    <el-icon><Plus /></el-icon> 添加住客
                  </el-button>
                  <div v-if="!canAddMoreBookingGuests" class="warning-text">
                    已达到房间最大容纳人数
                  </div>
                </div>
              </el-form-item>

              <!-- 操作按钮 -->
              <el-form-item>
                <el-button
                  type="primary"
                  size="large"
                  :loading="submitting"
                  @click="handleBookingSubmit"
                  style="width: 200px"
                >
                  <el-icon><Check /></el-icon> 办理入住
                </el-button>
                <el-button size="large" @click="handleBookingReset">
                  <el-icon><RefreshLeft /></el-icon> 重置
                </el-button>
              </el-form-item>
            </template>
          </el-form>
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <!-- 入住成功对话框 -->
    <el-dialog
      v-model="successDialogVisible"
      title="入住登记成功"
      width="600px"
      :close-on-click-modal="false"
    >
      <div v-if="checkInResult" class="success-content">
        <el-result icon="success" title="入住登记成功">
          <template #sub-title>
            <div class="result-info">
              <p><strong>房间号：</strong>{{ checkInResult.roomNo }}</p>
              <p><strong>入住时间：</strong>{{ formatDateTime(checkInResult.checkInTime) }}</p>
              <p><strong>预期退房：</strong>{{ formatDateTime(checkInResult.expectedCheckout) }}</p>
              <p><strong>房费：</strong>¥{{ checkInResult.pricePerHour }}/小时</p>
              <p><strong>入住人数：</strong>{{ checkInResult.currentOccupancy }}/{{ checkInResult.maxOccupancy }}人</p>
            </div>
          </template>
        </el-result>

        <el-divider />

        <h4>住客信息：</h4>
        <el-table :data="checkInResult.guests" border style="margin-top: 10px">
          <el-table-column prop="realName" label="姓名" width="120" />
          <el-table-column prop="phone" label="手机号" width="130" />
          <el-table-column label="账号状态" width="120">
            <template #default="{ row }">
              <el-tag :type="row.isNewUser ? 'warning' : 'success'">
                {{ row.isNewUser ? '新注册' : '已注册' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="房间权限" width="100">
            <template #default="{ row }">
              <el-tag v-if="row.roomAuthToken" type="primary">主住客</el-tag>
              <span v-else>-</span>
            </template>
          </el-table-column>
        </el-table>

        <el-alert
          v-if="hasNewUsers"
          type="warning"
          :closable="false"
          style="margin-top: 20px"
        >
          <p><strong>新注册用户初始密码：123456</strong></p>
          <p>请告知客人登录后尽快修改密码</p>
        </el-alert>
      </div>

      <template #footer>
        <el-button @click="successDialogVisible = false">关闭</el-button>
        <el-button type="primary" @click="handleContinue">
          继续登记
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  UserFilled,
  User,
  Phone,
  Postcard,
  Plus,
  Close,
  Check,
  RefreshLeft
} from '@element-plus/icons-vue'
import axios from 'axios'

// 数据定义
const activeTab = ref('walkin')
const checkInFormRef = ref()
const bookingCheckInFormRef = ref()
const availableRooms = ref([])
const selectedRoom = ref(null)
const submitting = ref(false)
const successDialogVisible = ref(false)
const checkInResult = ref(null)
const queryingBooking = ref(false)
const bookingInfo = ref(null)

// Walk-in 表单数据
const checkInForm = reactive({
  roomNo: '',
  expectedCheckout: null,
  guests: [
    {
      phone: '',
      realName: '',
      idCard: ''
    }
  ]
})

// 预订入住表单数据
const bookingCheckInForm = reactive({
  bookingPhone: '',
  roomNo: '',
  expectedCheckout: null,
  guests: [
    {
      phone: '',
      realName: '',
      idCard: ''
    }
  ]
})

// 表单验证规则
const formRules = {
  roomNo: [
    { required: true, message: '请选择房间', trigger: 'change' }
  ],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
  ],
  realName: [
    { required: true, message: '请输入真实姓名', trigger: 'blur' },
    { min: 2, max: 20, message: '姓名长度2-20个字符', trigger: 'blur' }
  ],
  idCard: [
    { required: true, message: '请输入身份证号', trigger: 'blur' },
    {
      pattern: /^[1-9]\d{5}(18|19|20)\d{2}(0[1-9]|1[0-2])(0[1-9]|[12]\d|3[01])\d{3}[\dXx]$/,
      message: '身份证号格式不正确',
      trigger: 'blur'
    }
  ]
}

const bookingFormRules = {
  bookingPhone: [
    { required: true, message: '请输入预订者手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
  ],
  expectedCheckout: [
    { required: true, message: '请选择预期退房时间', trigger: 'change' }
  ],
  'guests.0.phone': [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
  ],
  'guests.0.realName': [
    { required: true, message: '请输入真实姓名', trigger: 'blur' },
    { min: 2, max: 20, message: '姓名长度2-20个字符', trigger: 'blur' }
  ],
  'guests.0.idCard': [
    { required: true, message: '请输入身份证号', trigger: 'blur' },
    {
      pattern: /^[1-9]\d{5}(18|19|20)\d{2}(0[1-9]|1[0-2])(0[1-9]|[12]\d|3[01])\d{3}[\dXx]$/,
      message: '身份证号格式不正确',
      trigger: 'blur'
    }
  ]
}

// 计算属性
const canAddMoreGuests = computed(() => {
  if (!selectedRoom.value) return false
  return checkInForm.guests.length < selectedRoom.value.maxOccupancy
})

const bookingMaxOccupancy = computed(() => {
  if (!bookingInfo.value) return 0
  // 根据房间类型返回最大容纳人数
  const occupancyMap = {
    'SINGLE': 1,
    'DOUBLE': 2,
    'FIVE_PLAYER': 5,
    'VIP': 2
  }
  return occupancyMap[bookingInfo.value.roomType] || 1
})

const canAddMoreBookingGuests = computed(() => {
  if (!bookingInfo.value) return false
  return bookingCheckInForm.guests.length < bookingMaxOccupancy.value
})

const hasNewUsers = computed(() => {
  if (!checkInResult.value) return false
  return checkInResult.value.guests.some(g => g.isNewUser)
})

// 方法
const fetchAvailableRooms = async () => {
  try {
    const token = localStorage.getItem('accessToken')
    const response = await axios.get('/api/rooms', {
      headers: { Authorization: `Bearer ${token}` }
    })
    availableRooms.value = response.data.data
  } catch (error) {
    ElMessage.error('加载房间列表失败')
    console.error(error)
  }
}

const handleRoomChange = () => {
  selectedRoom.value = availableRooms.value.find(
    room => room.roomNo === checkInForm.roomNo
  )
  
  // 重置住客列表（保留第一个）
  if (checkInForm.guests.length > 1) {
    checkInForm.guests = [checkInForm.guests[0]]
  }
}

const addGuest = () => {
  if (canAddMoreGuests.value) {
    checkInForm.guests.push({
      phone: '',
      realName: '',
      idCard: ''
    })
  }
}

const removeGuest = (index) => {
  checkInForm.guests.splice(index, 1)
}

const handleSubmit = async () => {
  try {
    await checkInFormRef.value.validate()

    // 二次确认
    await ElMessageBox.confirm(
      `确认为 ${checkInForm.guests.length} 位住客办理入住 ${checkInForm.roomNo} 房间？`,
      '确认入住',
      {
        confirmButtonText: '确认',
        cancelButtonText: '取消',
        type: 'info'
      }
    )

    submitting.value = true
    const token = localStorage.getItem('accessToken')
    const response = await axios.post('/api/checkin', checkInForm, {
      headers: { Authorization: `Bearer ${token}` }
    })

    checkInResult.value = response.data.data
    successDialogVisible.value = true
    ElMessage.success('入住登记成功')
    
    // 刷新房间列表
    await fetchAvailableRooms()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.response?.data?.message || '入住登记失败')
    }
  } finally {
    submitting.value = false
  }
}

const handleReset = () => {
  checkInFormRef.value.resetFields()
  checkInForm.guests = [
    {
      phone: '',
      realName: '',
      idCard: ''
    }
  ]
  selectedRoom.value = null
}

const handleContinue = () => {
  successDialogVisible.value = false
  handleReset()
}

const getRoomTypeLabel = (type) => {
  const types = {
    SINGLE: '单人间',
    DOUBLE: '双人间',
    FIVE_PLAYER: '五人黑房',
    VIP: 'VIP套房'
  }
  return types[type] || type
}

const getStatusLabel = (status) => {
  const statuses = {
    VACANT: '空闲',
    OCCUPIED: '已入住',
    RESERVED: '已预订',
    CLEANING: '清洁中',
    MAINTENANCE: '维修中'
  }
  return statuses[status] || status
}

const getStatusType = (status) => {
  const types = {
    VACANT: 'success',
    OCCUPIED: 'danger',
    RESERVED: 'warning',
    CLEANING: 'info',
    MAINTENANCE: 'danger'
  }
  return types[status] || 'info'
}

const disabledDate = (time) => {
  return time.getTime() < Date.now()
}

const formatDateTime = (dateTime) => {
  if (!dateTime) return '-'
  return new Date(dateTime).toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

// 标签页切换
const handleTabChange = (tabName) => {
  if (tabName === 'booking') {
    handleBookingReset()
  }
}

// 查询预订
const queryBooking = async () => {
  try {
    await bookingCheckInFormRef.value.validateField('bookingPhone')
    
    queryingBooking.value = true
    const response = await axios.get(
      `/api/bookings/by-phone/${bookingCheckInForm.bookingPhone}`
    )
    
    if (response.data.code === 200) {
      bookingInfo.value = response.data.data
      
      // 验证计划入住时间是否为今天
      const plannedCheckin = new Date(bookingInfo.value.plannedCheckin)
      const today = new Date()
      today.setHours(0, 0, 0, 0)
      plannedCheckin.setHours(0, 0, 0, 0)
      
      if (plannedCheckin.getTime() !== today.getTime()) {
        ElMessage.warning('预订的入住日期不是今天，请确认预订信息')
      }
      
      // 自动设置房间号和预期退房时间
      bookingCheckInForm.roomNo = bookingInfo.value.roomNo
      bookingCheckInForm.expectedCheckout = bookingInfo.value.plannedCheckout
      
      ElMessage.success('查询到预订信息')
    }
  } catch (error) {
    if (error !== 'bookingPhone') {
      ElMessage.error(error.response?.data?.message || '查询预订失败')
      bookingInfo.value = null
    }
  } finally {
    queryingBooking.value = false
  }
}

// 预订入住 - 添加住客
const addBookingGuest = () => {
  if (canAddMoreBookingGuests.value) {
    bookingCheckInForm.guests.push({
      phone: '',
      realName: '',
      idCard: ''
    })
  }
}

// 预订入住 - 移除住客
const removeBookingGuest = (index) => {
  bookingCheckInForm.guests.splice(index, 1)
}

// 预订入住 - 提交
const handleBookingSubmit = async () => {
  try {
    if (!bookingInfo.value) {
      ElMessage.warning('请先查询预订信息')
      return
    }
    
    await bookingCheckInFormRef.value.validate()

    // 二次确认
    await ElMessageBox.confirm(
      `确认为 ${bookingCheckInForm.guests.length} 位住客办理预订入住 ${bookingCheckInForm.roomNo} 房间？`,
      '确认入住',
      {
        confirmButtonText: '确认',
        cancelButtonText: '取消',
        type: 'info'
      }
    )

    submitting.value = true
    const token = localStorage.getItem('accessToken')
    const response = await axios.post('/api/checkin', bookingCheckInForm, {
      headers: { Authorization: `Bearer ${token}` }
    })

    checkInResult.value = response.data.data
    successDialogVisible.value = true
    ElMessage.success('入住登记成功')
    
    // 刷新房间列表
    await fetchAvailableRooms()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.response?.data?.message || '入住登记失败')
    }
  } finally {
    submitting.value = false
  }
}

// 预订入住 - 重置
const handleBookingReset = () => {
  if (bookingCheckInFormRef.value) {
    bookingCheckInFormRef.value.resetFields()
  }
  bookingCheckInForm.bookingPhone = ''
  bookingCheckInForm.roomNo = ''
  bookingCheckInForm.expectedCheckout = null
  bookingCheckInForm.guests = [
    {
      phone: '',
      realName: '',
      idCard: ''
    }
  ]
  bookingInfo.value = null
}

// 生命周期
onMounted(() => {
  fetchAvailableRooms()
})
</script>

<style scoped>
.checkin-container {
  padding: 20px;
  height: calc(100vh - 100px);
  overflow-y: auto;
}

.form-card {
  max-width: 1200px;
  margin: 0 auto;
}

.card-header {
  text-align: center;
}

.card-header h2 {
  margin: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  color: #409eff;
}

.subtitle {
  margin: 5px 0 0;
  color: #909399;
  font-size: 14px;
}

.room-option {
  display: flex;
  align-items: center;
  gap: 10px;
}

.room-no {
  font-weight: bold;
  min-width: 50px;
}

.room-type {
  color: #606266;
  min-width: 80px;
}

.occupancy {
  color: #909399;
  font-size: 12px;
  min-width: 60px;
}

.price {
  color: #f56c6c;
  font-weight: bold;
  margin-left: auto;
}

.room-info {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 10px;
  font-size: 14px;
}

.guests-section {
  width: 100%;
}

.guest-item {
  border: 1px solid #dcdfe6;
  border-radius: 8px;
  padding: 20px;
  margin-bottom: 15px;
  background-color: #f5f7fa;
}

.guest-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.guest-header h3 {
  margin: 0;
  color: #409eff;
  font-size: 16px;
}

.warning-text {
  color: #e6a23c;
  font-size: 12px;
  margin-top: 5px;
  text-align: center;
}

.success-content {
  padding: 20px;
}

.result-info {
  text-align: left;
  margin-top: 20px;
}

.result-info p {
  margin: 8px 0;
  font-size: 14px;
}

.booking-info {
  padding: 10px;
}

.booking-info h3 {
  margin: 0 0 15px 0;
  color: #409eff;
}

.booking-info p {
  margin: 8px 0;
  font-size: 14px;
}
</style>
