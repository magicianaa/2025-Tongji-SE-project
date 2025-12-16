<template>
  <div class="room-management">
    <el-card class="filter-card">
      <div class="filter-bar">
        <el-radio-group v-model="statusFilter" @change="loadRooms">
          <el-radio-button label="ALL">全部</el-radio-button>
          <el-radio-button label="VACANT">空闲</el-radio-button>
          <el-radio-button label="OCCUPIED">已入住</el-radio-button>
          <el-radio-button label="DIRTY">待清洁</el-radio-button>
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
                <span>¥{{ room.pricePerHour }}/小时</span>
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
              @click="handleCheckIn(room)"
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
import { Refresh, Grid, Money, User } from '@element-plus/icons-vue'
import { getRoomList, getRoomsByStatus, checkIn, checkOut, getCheckInRecords } from '@/api/room'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()

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
    dirty: status === 'DIRTY',
    maintenance: status === 'MAINTENANCE'
  }
}

// 获取状态标签类型
const getStatusTagType = (status) => {
  const typeMap = {
    VACANT: 'success',
    OCCUPIED: 'danger',
    DIRTY: 'warning',
    MAINTENANCE: 'info'
  }
  return typeMap[status] || ''
}

// 获取状态文本
const getStatusText = (status) => {
  const textMap = {
    VACANT: '空闲',
    OCCUPIED: '已入住',
    DIRTY: '待清洁',
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

// 处理退房
const handleCheckOut = async (room) => {
  try {
    // 获取入住记录
    const records = await getCheckInRecords()
    const record = records.find(r => r.roomId === room.roomId && r.actualCheckout === null)
    
    if (!record) {
      ElMessage.warning('未找到该房间的入住记录')
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
    <p><strong>价格：</strong>¥${room.pricePerHour}/小时</p>
    <p><strong>高级房型：</strong>${room.isPremium ? '是' : '否'}</p>
    `,
    '房间详情',
    {
      dangerouslyUseHTMLString: true
    }
  )
}

onMounted(() => {
  loadRooms()
})
</script>

<style scoped>
.room-management {
  padding: 20px;
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

.room-card.dirty {
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
