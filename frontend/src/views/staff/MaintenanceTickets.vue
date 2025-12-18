<template>
  <div class="maintenance-tickets">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>维修工单管理</span>
          <el-button type="primary" @click="refreshTickets">
            <el-icon><Refresh /></el-icon>
            刷新
          </el-button>
        </div>
      </template>

      <!-- 筛选栏 -->
      <el-form :inline="true" class="filter-form">
        <el-form-item label="工单状态">
          <el-select v-model="filterStatus" @change="loadTickets" clearable placeholder="全部">
            <el-option label="待处理" value="OPEN" />
            <el-option label="处理中" value="IN_PROGRESS" />
            <el-option label="已解决" value="RESOLVED" />
            <el-option label="已关闭" value="CLOSED" />
          </el-select>
        </el-form-item>
      </el-form>

      <!-- 工单表格 -->
      <el-table :data="tickets" border v-loading="loading" max-height="600" style="width: 100%">
        <el-table-column prop="ticketId" label="工单号" width="80" />
        <el-table-column prop="roomNo" label="房间" width="100">
          <template #default="{ row }">
            <el-tag>{{ row.roomNo }}号房</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="requestType" label="类型" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.requestType === 'REPAIR'" type="warning">设备维修</el-tag>
            <el-tag v-else-if="row.requestType === 'CHANGE_ROOM'" type="info">换房申请</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="故障描述" min-width="200" show-overflow-tooltip />
        <el-table-column prop="priority" label="优先级" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.priority === 'URGENT'" type="danger">紧急</el-tag>
            <el-tag v-else-if="row.priority === 'HIGH'" type="warning">高</el-tag>
            <el-tag v-else-if="row.priority === 'MEDIUM'" type="info">中</el-tag>
            <el-tag v-else type="success">低</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.status === 'OPEN'" type="danger">待处理</el-tag>
            <el-tag v-else-if="row.status === 'IN_PROGRESS'" type="warning">处理中</el-tag>
            <el-tag v-else-if="row.status === 'RESOLVED'" type="success">已解决</el-tag>
            <el-tag v-else type="info">已关闭</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="160">
          <template #default="{ row }">
            {{ formatTime(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="viewDetail(row)">详情</el-button>
            <el-button 
              link 
              type="warning" 
              @click="updateStatus(row, 'IN_PROGRESS')"
              v-if="row.status === 'OPEN'"
            >
              开始处理
            </el-button>
            <el-button 
              link 
              type="success" 
              @click="resolveTicket(row)"
              v-if="row.status === 'IN_PROGRESS'"
            >
              完成
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 工单详情对话框 -->
    <el-dialog v-model="detailVisible" title="工单详情" width="600px">
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
        <el-descriptions-item label="故障描述" :span="2">
          {{ currentTicket.description }}
        </el-descriptions-item>
        <el-descriptions-item label="创建时间" :span="2">
          {{ formatTime(currentTicket.createTime) }}
        </el-descriptions-item>
        <el-descriptions-item label="解决时间" :span="2" v-if="currentTicket.resolveTime">
          {{ formatTime(currentTicket.resolveTime) }}
        </el-descriptions-item>
        <el-descriptions-item label="处理备注" :span="2" v-if="currentTicket.resolutionNotes">
          {{ currentTicket.resolutionNotes }}
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>

    <!-- 完成工单对话框 -->
    <el-dialog v-model="resolveVisible" title="完成工单" width="500px">
      <el-form :model="resolveForm" label-width="100px">
        <el-form-item label="处理备注">
          <el-input 
            v-model="resolveForm.notes" 
            type="textarea" 
            :rows="4" 
            placeholder="请输入处理结果和备注"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="resolveVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmResolve">确认完成</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Refresh } from '@element-plus/icons-vue'
import { getMaintenanceTickets, updateTicketStatus } from '@/api/hardware'

// 数据
const tickets = ref([])
const loading = ref(false)
const filterStatus = ref('')

// 详情对话框
const detailVisible = ref(false)
const currentTicket = ref(null)

// 完成工单对话框
const resolveVisible = ref(false)
const resolveForm = ref({
  ticketId: null,
  notes: ''
})

// 获取工单列表
const loadTickets = async () => {
  loading.value = true
  try {
    const params = {}
    if (filterStatus.value) {
      params.status = filterStatus.value
    }
    tickets.value = await getMaintenanceTickets(params)
  } catch (error) {
    ElMessage.error('加载工单失败')
    console.error(error)
  } finally {
    loading.value = false
  }
}

// 刷新
const refreshTickets = () => {
  loadTickets()
}

// 查看详情
const viewDetail = (ticket) => {
  currentTicket.value = ticket
  detailVisible.value = true
}

// 更新状态
const updateStatus = async (ticket, status) => {
  try {
    await updateTicketStatus(ticket.ticketId, { status })
    ElMessage.success('状态已更新')
    loadTickets()
  } catch (error) {
    ElMessage.error('更新失败')
    console.error(error)
  }
}

// 打开完成对话框
const resolveTicket = (ticket) => {
  resolveForm.value.ticketId = ticket.ticketId
  resolveForm.value.notes = ''
  resolveVisible.value = true
}

// 确认完成
const confirmResolve = async () => {
  try {
    await updateTicketStatus(resolveForm.value.ticketId, {
      status: 'RESOLVED',
      notes: resolveForm.value.notes
    })
    ElMessage.success('工单已完成')
    resolveVisible.value = false
    loadTickets()
  } catch (error) {
    ElMessage.error('操作失败')
    console.error(error)
  }
}

// 格式化时间
const formatTime = (time) => {
  if (!time) return '-'
  return new Date(time).toLocaleString('zh-CN')
}

onMounted(() => {
  loadTickets()
})
</script>

<style scoped>
.maintenance-tickets {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.filter-form {
  margin-bottom: 20px;
}
</style>
