<template>
  <div class="task-management">
    <!-- 任务管理卡片 -->
    <el-card>
      <template #header>
        <div class="card-header">
          <span>任务管理</span>
          <el-button type="primary" @click="showCreateDialog">
            <el-icon><Plus /></el-icon>
            发布新任务
          </el-button>
        </div>
      </template>

      <el-table :data="tasks" border v-loading="loading">
        <el-table-column prop="taskId" label="任务ID" width="80" />
        <el-table-column prop="taskName" label="任务名称" width="200" />
        <el-table-column prop="description" label="任务描述" min-width="250" show-overflow-tooltip />
        <el-table-column prop="rewardPoints" label="奖励积分" width="100">
          <template #default="{ row }">
            <el-tag type="success">{{ row.rewardPoints }}分</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="taskType" label="审核方式" width="120">
          <template #default="{ row }">
            <el-tag v-if="row.taskType === 'MANUAL_AUDIT'" type="warning">人工审核</el-tag>
            <el-tag v-else type="info">自动检测</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="isRepeatable" label="可重复" width="100">
          <template #default="{ row }">
            {{ row.isRepeatable ? '是' : '否' }}
          </template>
        </el-table-column>
        <el-table-column prop="isActive" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.isActive ? 'success' : 'danger'">
              {{ row.isActive ? '启用' : '下架' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="editTask(row)">编辑</el-button>
            <el-button link type="danger" @click="deleteTaskConfirm(row)" v-if="row.isActive">下架</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 任务审核卡片 -->
    <el-card style="margin-top: 20px;">
      <template #header>
        <div class="card-header">
          <span>任务审核</span>
          <el-select v-model="auditStatusFilter" @change="loadTaskRecords" placeholder="筛选状态" clearable>
            <el-option label="待审核" value="PENDING" />
            <el-option label="已通过" value="APPROVED" />
            <el-option label="已拒绝" value="REJECTED" />
          </el-select>
        </div>
      </template>

      <el-table :data="taskRecords" border v-loading="recordsLoading">
        <el-table-column prop="taskRecordId" label="记录ID" width="80" />
        <el-table-column prop="guestUsername" label="提交人" width="120" />
        <el-table-column prop="taskName" label="任务名称" width="180" />
        <el-table-column prop="rewardPoints" label="奖励积分" width="100">
          <template #default="{ row }">
            <el-tag type="success">{{ row.rewardPoints }}分</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="submitTime" label="提交时间" width="160">
          <template #default="{ row }">
            {{ formatTime(row.submitTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="auditStatus" label="状态" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.auditStatus === 'PENDING'" type="warning">待审核</el-tag>
            <el-tag v-else-if="row.auditStatus === 'APPROVED'" type="success">已通过</el-tag>
            <el-tag v-else type="danger">已拒绝</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="viewProof(row)">查看凭证</el-button>
            <el-button 
              link 
              type="success" 
              @click="auditTaskConfirm(row, true)" 
              v-if="row.auditStatus === 'PENDING'"
            >
              通过
            </el-button>
            <el-button 
              link 
              type="danger" 
              @click="showRejectDialog(row)" 
              v-if="row.auditStatus === 'PENDING'"
            >
              拒绝
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 创建/编辑任务对话框 -->
    <el-dialog v-model="taskDialogVisible" :title="isEditMode ? '编辑任务' : '发布新任务'" width="600px">
      <el-form :model="taskForm" label-width="100px">
        <el-form-item label="任务名称">
          <el-input v-model="taskForm.taskName" placeholder="如：连续游戏4小时" />
        </el-form-item>
        <el-form-item label="任务描述">
          <el-input v-model="taskForm.description" type="textarea" :rows="3" placeholder="详细说明任务要求" />
        </el-form-item>
        <el-form-item label="奖励积分">
          <el-select v-model="taskForm.rewardPoints" placeholder="选择积分档位">
            <el-option label="100积分" :value="100" />
            <el-option label="200积分" :value="200" />
            <el-option label="300积分" :value="300" />
          </el-select>
        </el-form-item>
        <el-form-item label="审核方式">
          <el-radio-group v-model="taskForm.taskType">
            <el-radio label="MANUAL_AUDIT">人工审核</el-radio>
            <el-radio label="AUTO">自动检测</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="可重复完成">
          <el-switch v-model="taskForm.isRepeatable" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="taskDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveTask">确定</el-button>
      </template>
    </el-dialog>

    <!-- 查看凭证对话框 -->
    <el-dialog v-model="proofDialogVisible" title="任务凭证" width="700px">
      <div v-if="currentRecord">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="提交人">{{ currentRecord.guestUsername }}</el-descriptions-item>
          <el-descriptions-item label="任务名称">{{ currentRecord.taskName }}</el-descriptions-item>
          <el-descriptions-item label="奖励积分" :span="2">
            <el-tag type="success">{{ currentRecord.rewardPoints }}分</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="凭证说明" :span="2">
            {{ currentRecord.proofDescription || '无' }}
          </el-descriptions-item>
        </el-descriptions>
        <div style="margin-top: 20px;">
          <div style="font-weight: bold; margin-bottom: 10px;">凭证截图：</div>
          <el-image 
            :src="currentRecord.proofImageUrl || '/placeholder.jpg'" 
            fit="contain" 
            style="width: 100%; max-height: 400px;"
            :preview-src-list="[currentRecord.proofImageUrl]"
          />
        </div>
      </div>
    </el-dialog>

    <!-- 拒绝任务对话框 -->
    <el-dialog v-model="rejectDialogVisible" title="拒绝任务" width="500px">
      <el-form label-width="100px">
        <el-form-item label="拒绝原因">
          <el-input 
            v-model="rejectReason" 
            type="textarea" 
            :rows="4" 
            placeholder="请说明拒绝理由"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="rejectDialogVisible = false">取消</el-button>
        <el-button type="danger" @click="confirmReject">确认拒绝</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { getTasks, createTask, updateTask, deleteTask, getTaskRecords, auditTask } from '@/api/task'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()

// 任务列表
const tasks = ref([])
const loading = ref(false)

// 任务记录列表
const taskRecords = ref([])
const recordsLoading = ref(false)
const auditStatusFilter = ref('')

// 任务对话框
const taskDialogVisible = ref(false)
const isEditMode = ref(false)
const taskForm = ref({
  taskName: '',
  description: '',
  rewardPoints: 100,
  taskType: 'MANUAL_AUDIT',
  isRepeatable: false
})

// 凭证对话框
const proofDialogVisible = ref(false)
const currentRecord = ref(null)

// 拒绝对话框
const rejectDialogVisible = ref(false)
const rejectReason = ref('')
const currentRejectRecord = ref(null)

// 加载任务列表
const loadTasks = async () => {
  loading.value = true
  try {
    tasks.value = await getTasks(false)  // 显示所有任务（包括已下架）
  } catch (error) {
    ElMessage.error('加载任务列表失败')
    console.error(error)
  } finally {
    loading.value = false
  }
}

// 加载任务记录
const loadTaskRecords = async () => {
  recordsLoading.value = true
  try {
    taskRecords.value = await getTaskRecords(auditStatusFilter.value || null)
  } catch (error) {
    ElMessage.error('加载任务记录失败')
    console.error(error)
  } finally {
    recordsLoading.value = false
  }
}

// 显示创建对话框
const showCreateDialog = () => {
  isEditMode.value = false
  taskForm.value = {
    taskName: '',
    description: '',
    rewardPoints: 100,
    taskType: 'MANUAL_AUDIT',
    isRepeatable: false
  }
  taskDialogVisible.value = true
}

// 编辑任务
const editTask = (task) => {
  isEditMode.value = true
  taskForm.value = { ...task }
  taskDialogVisible.value = true
}

// 保存任务
const saveTask = async () => {
  try {
    if (isEditMode.value) {
      await updateTask(taskForm.value.taskId, taskForm.value)
      ElMessage.success('任务更新成功')
    } else {
      await createTask(taskForm.value)
      ElMessage.success('任务发布成功')
    }
    taskDialogVisible.value = false
    loadTasks()
  } catch (error) {
    ElMessage.error('操作失败')
    console.error(error)
  }
}

// 删除任务确认
const deleteTaskConfirm = async (task) => {
  try {
    await ElMessageBox.confirm('确定要下架此任务吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await deleteTask(task.taskId)
    ElMessage.success('任务已下架')
    loadTasks()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('操作失败')
      console.error(error)
    }
  }
}

// 查看凭证
const viewProof = (record) => {
  currentRecord.value = record
  proofDialogVisible.value = true
}

// 审核任务确认
const auditTaskConfirm = async (record, approved) => {
  try {
    await auditTask(record.taskRecordId, userStore.userInfo.userId, approved, null)
    ElMessage.success('审核成功，积分已发放')
    loadTaskRecords()
  } catch (error) {
    ElMessage.error('审核失败')
    console.error(error)
  }
}

// 显示拒绝对话框
const showRejectDialog = (record) => {
  currentRejectRecord.value = record
  rejectReason.value = ''
  rejectDialogVisible.value = true
}

// 确认拒绝
const confirmReject = async () => {
  if (!rejectReason.value.trim()) {
    ElMessage.warning('请填写拒绝原因')
    return
  }
  
  try {
    await auditTask(currentRejectRecord.value.taskRecordId, userStore.userInfo.userId, false, rejectReason.value)
    ElMessage.success('已拒绝该任务')
    rejectDialogVisible.value = false
    loadTaskRecords()
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
  loadTasks()
  loadTaskRecords()
})
</script>

<style scoped>
.task-management {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
