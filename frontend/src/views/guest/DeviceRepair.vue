<template>
  <div class="device-repair">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>设备报修</span>
        </div>
      </template>

      <!-- 报修表单 -->
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="100px"
        style="max-width: 600px; margin: 20px auto;"
      >
        <el-form-item label="房间号" prop="roomId">
          <el-input v-model="form.roomNo" disabled placeholder="系统自动获取当前入住房间" />
        </el-form-item>

        <el-form-item label="诉求类型" prop="requestType">
          <el-radio-group v-model="form.requestType">
            <el-radio value="REPAIR">设备维修</el-radio>
            <el-radio value="CHANGE_ROOM">申请换房</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="优先级" prop="priority">
          <el-radio-group v-model="form.priority">
            <el-radio value="LOW">低</el-radio>
            <el-radio value="MEDIUM">中</el-radio>
            <el-radio value="HIGH">高</el-radio>
            <el-radio value="URGENT">紧急</el-radio>
          </el-radio-group>
          <div class="tip">
            <el-text size="small" type="info">
              紧急：影响正常使用（如无法开机、断网）<br/>
              高：严重影响体验（如卡顿、过热）<br/>
              中：有影响但可以继续使用<br/>
              低：微小问题或建议
            </el-text>
          </div>
        </el-form-item>

        <el-form-item label="问题描述" prop="description">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="6"
            placeholder="请详细描述遇到的问题，例如：&#10;- 电脑无法开机&#10;- 键盘某些按键失灵&#10;- 显示器屏幕闪烁&#10;- 网络延迟高&#10;- 空调不制冷等"
          />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="submitRepair" :loading="submitting">
            提交报修
          </el-button>
          <el-button @click="resetForm">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 我的报修记录 -->
    <el-card class="my-repairs" style="margin-top: 20px;">
      <template #header>
        <div class="card-header">
          <span>我的报修记录</span>
          <el-button @click="loadMyTickets" :loading="loading">
            <el-icon><Refresh /></el-icon>
            刷新
          </el-button>
        </div>
      </template>

      <el-table :data="myTickets" border v-loading="loading">
        <el-table-column prop="ticketId" label="工单号" width="80" />
        <el-table-column prop="requestType" label="类型" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.requestType === 'REPAIR'" type="warning">设备维修</el-tag>
            <el-tag v-else type="info">换房申请</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="问题描述" min-width="200" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.status === 'OPEN'" type="danger">待处理</el-tag>
            <el-tag v-else-if="row.status === 'IN_PROGRESS'" type="warning">处理中</el-tag>
            <el-tag v-else-if="row.status === 'RESOLVED'" type="success">已解决</el-tag>
            <el-tag v-else type="info">已关闭</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="报修时间" width="160">
          <template #default="{ row }">
            {{ formatTime(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100">
          <template #default="{ row }">
            <el-button link type="primary" @click="viewDetail(row)">详情</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 工单详情对话框 -->
    <el-dialog v-model="detailVisible" title="报修详情" width="600px">
      <el-descriptions v-if="currentTicket" :column="2" border>
        <el-descriptions-item label="工单号">
          {{ currentTicket.ticketId }}
        </el-descriptions-item>
        <el-descriptions-item label="房间号">
          {{ currentTicket.roomNo }}号房
        </el-descriptions-item>
        <el-descriptions-item label="类型">
          <el-tag v-if="currentTicket.requestType === 'REPAIR'" type="warning">设备维修</el-tag>
          <el-tag v-else type="info">换房申请</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="优先级">
          <el-tag v-if="currentTicket.priority === 'URGENT'" type="danger">紧急</el-tag>
          <el-tag v-else-if="currentTicket.priority === 'HIGH'" type="warning">高</el-tag>
          <el-tag v-else-if="currentTicket.priority === 'MEDIUM'" type="info">中</el-tag>
          <el-tag v-else type="success">低</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="状态" :span="2">
          <el-tag v-if="currentTicket.status === 'OPEN'" type="danger">待处理</el-tag>
          <el-tag v-else-if="currentTicket.status === 'IN_PROGRESS'" type="warning">处理中</el-tag>
          <el-tag v-else-if="currentTicket.status === 'RESOLVED'" type="success">已解决</el-tag>
          <el-tag v-else type="info">已关闭</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="问题描述" :span="2">
          {{ currentTicket.description }}
        </el-descriptions-item>
        <el-descriptions-item label="报修时间" :span="2">
          {{ formatTime(currentTicket.createTime) }}
        </el-descriptions-item>
        <el-descriptions-item label="解决时间" :span="2" v-if="currentTicket.resolveTime">
          {{ formatTime(currentTicket.resolveTime) }}
        </el-descriptions-item>
        <el-descriptions-item label="处理结果" :span="2" v-if="currentTicket.resolutionNotes">
          {{ currentTicket.resolutionNotes }}
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Refresh } from '@element-plus/icons-vue'
import { createTicket, getMaintenanceTickets } from '@/api/hardware'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()

// 表单数据
const formRef = ref(null)
const form = ref({
  roomId: null,
  roomNo: '',
  requestType: 'REPAIR',
  priority: 'MEDIUM',
  description: ''
})

// 表单验证规则
const rules = {
  requestType: [
    { required: true, message: '请选择诉求类型', trigger: 'change' }
  ],
  priority: [
    { required: true, message: '请选择优先级', trigger: 'change' }
  ],
  description: [
    { required: true, message: '请输入问题描述', trigger: 'blur' },
    { min: 10, message: '请至少输入10个字符的详细描述', trigger: 'blur' }
  ]
}

// 提交状态
const submitting = ref(false)

// 我的报修记录
const myTickets = ref([])
const loading = ref(false)

// 详情对话框
const detailVisible = ref(false)
const currentTicket = ref(null)

// 提交报修
const submitRepair = async () => {
  try {
    await formRef.value.validate()
    
    // 检查用户信息
    if (!userStore.userInfo || !userStore.userInfo.userId) {
      ElMessage.error('用户信息未加载，请重新登录')
      return
    }
    
    // 检查房间信息
    if (!form.value.roomId) {
      ElMessage.error('您当前未入住，无法报修')
      return
    }
    
    submitting.value = true
    await createTicket({
      roomId: form.value.roomId,
      reporterId: userStore.userInfo.userId,
      requestType: form.value.requestType,
      description: form.value.description,
      priority: form.value.priority
    })
    
    ElMessage.success('报修已提交，工作人员将尽快处理')
    resetForm()
    loadMyTickets()
  } catch (error) {
    if (error.message) {
      ElMessage.error(error.message || '提交失败')
    }
  } finally {
    submitting.value = false
  }
}

// 重置表单
const resetForm = () => {
  formRef.value.resetFields()
  form.value.description = ''
}

// 加载我的报修记录
const loadMyTickets = async () => {
  // 检查用户信息
  if (!userStore.userInfo || !userStore.userInfo.userId) {
    console.log('用户信息未加载，跳过加载报修记录')
    myTickets.value = []
    return
  }
  
  loading.value = true
  try {
    // 获取所有工单并筛选当前用户的
    const allTickets = await getMaintenanceTickets({})
    myTickets.value = allTickets.filter(t => t.reporterId === userStore.userInfo.userId)
  } catch (error) {
    console.error('加载报修记录失败:', error)
    // 不显示错误消息，避免干扰用户体验
    myTickets.value = []
  } finally {
    loading.value = false
  }
}

// 查看详情
const viewDetail = (ticket) => {
  currentTicket.value = ticket
  detailVisible.value = true
}

// 格式化时间
const formatTime = (time) => {
  if (!time) return '-'
  return new Date(time).toLocaleString('zh-CN')
}

// 初始化：获取当前房间信息
const initRoomInfo = async () => {
  try {
    // 检查用户信息是否存在
    if (!userStore.userInfo) {
      console.log('用户信息未加载')
      return
    }
    
    // 从checkInInfo中获取房间信息
    if (userStore.checkInInfo && userStore.checkInInfo.roomId) {
      form.value.roomId = userStore.checkInInfo.roomId
      form.value.roomNo = userStore.checkInInfo.roomNo || `${userStore.checkInInfo.roomId}号房`
      console.log('已加载房间信息:', form.value.roomNo)
    } else {
      console.log('用户当前未入住')
      // 不显示警告消息，让用户自然发现需要入住
    }
  } catch (error) {
    console.error('获取房间信息失败', error)
  }
}

onMounted(() => {
  initRoomInfo()
  loadMyTickets()
})
</script>

<style scoped>
.device-repair {
  overflow-y: auto;
  max-height: calc(100vh - 120px);
}

.repair-page {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.tip {
  margin-top: 8px;
  line-height: 1.6;
}

.my-repairs {
  margin-top: 20px;
}
</style>
