<template>
  <div class="tasks-and-points">
    <!-- 积分信息卡片 -->
    <el-card>
      <template #header>
        <span>我的积分</span>
      </template>
      <div class="points-info">
        <el-row :gutter="20">
          <el-col :span="6">
            <el-statistic title="当前积分" :value="pointsBalance.currentPoints || 0">
              <template #suffix>分</template>
            </el-statistic>
          </el-col>
          <el-col :span="6">
            <el-statistic title="总经验值" :value="pointsBalance.experiencePoints || 0">
              <template #suffix>点</template>
            </el-statistic>
          </el-col>
          <el-col :span="6">
            <div class="member-level">
              <div class="label">会员等级</div>
              <el-tag :type="getMemberLevelType(pointsBalance.memberLevel)" size="large">
                {{ getMemberLevelText(pointsBalance.memberLevel) }}
              </el-tag>
            </div>
          </el-col>
          <el-col :span="6">
            <div class="member-level">
              <div class="label">会员折扣</div>
              <el-tag type="success" size="large">
                {{ ((1 - (pointsBalance.discountRate || 1)) * 100).toFixed(0) }}% OFF
              </el-tag>
            </div>
          </el-col>
        </el-row>
      </div>
    </el-card>

    <!-- 可用任务列表 -->
    <el-card style="margin-top: 20px;">
      <template #header>
        <span>可完成任务</span>
      </template>
      <el-row :gutter="20">
        <el-col :span="8" v-for="task in tasks" :key="task.taskId">
          <el-card shadow="hover" class="task-card">
            <div class="task-header">
              <h3>{{ task.taskName }}</h3>
              <el-tag type="success">{{ task.rewardPoints }}分</el-tag>
            </div>
            <p class="task-description">{{ task.description }}</p>
            <div class="task-footer">
              <el-tag v-if="!task.isRepeatable" type="info" size="small">不可重复</el-tag>
              <el-button type="primary" @click="submitTaskProof(task)">提交凭证</el-button>
            </div>
          </el-card>
        </el-col>
      </el-row>
      <el-empty description="暂无可用任务" v-if="tasks.length === 0" />
    </el-card>

    <!-- 我的任务记录 -->
    <el-card style="margin-top: 20px;">
      <template #header>
        <div class="card-header">
          <span>我的任务记录</span>
          <el-button @click="loadMyTaskRecords">
            <el-icon><Refresh /></el-icon>
            刷新
          </el-button>
        </div>
      </template>
      <el-table :data="myTaskRecords" border v-loading="recordsLoading">
        <el-table-column prop="taskName" label="任务名称" width="200" />
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
        <el-table-column prop="auditStatus" label="审核状态" width="120">
          <template #default="{ row }">
            <el-tag v-if="row.auditStatus === 'PENDING'" type="warning">待审核</el-tag>
            <el-tag v-else-if="row.auditStatus === 'APPROVED'" type="success">已通过</el-tag>
            <el-tag v-else type="danger">已拒绝</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="rejectReason" label="拒绝原因" min-width="200">
          <template #default="{ row }">
            {{ row.rejectReason || '-' }}
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 积分流水记录 -->
    <el-card style="margin-top: 20px;">
      <template #header>
        <span>积分流水</span>
      </template>
      <el-table :data="transactions" border v-loading="transactionsLoading">
        <el-table-column prop="amount" label="积分变动" width="120">
          <template #default="{ row }">
            <el-tag :type="row.amount > 0 ? 'success' : 'danger'">
              {{ row.amount > 0 ? '+' : '' }}{{ row.amount }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="transactionType" label="类型" width="150">
          <template #default="{ row }">
            <el-tag v-if="row.transactionType === 'TASK_REWARD'" type="success">任务奖励</el-tag>
            <el-tag v-else-if="row.transactionType === 'REDEMPTION'" type="warning">商品兑换</el-tag>
            <el-tag v-else-if="row.transactionType === 'CHECKIN_REWARD'" type="success">退房奖励</el-tag>
            <el-tag v-else type="info">其他</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="说明" min-width="200" />
        <el-table-column prop="balanceAfter" label="余额" width="120">
          <template #default="{ row }">
            {{ row.balanceAfter }}分
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="时间" width="160">
          <template #default="{ row }">
            {{ formatTime(row.createTime) }}
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 提交任务凭证对话框 -->
    <el-dialog v-model="submitDialogVisible" title="提交任务凭证" width="600px">
      <el-form :model="submitForm" label-width="100px" v-if="currentTask">
        <el-form-item label="任务名称">
          <el-input v-model="currentTask.taskName" disabled />
        </el-form-item>
        <el-form-item label="奖励积分">
          <el-tag type="success">{{ currentTask.rewardPoints }}分</el-tag>
        </el-form-item>
        <el-form-item label="截图上传" required>
          <el-upload
            class="upload-demo"
            :action="uploadAction"
            :headers="uploadHeaders"
            :on-success="handleUploadSuccess"
            :on-error="handleUploadError"
            :before-upload="beforeUpload"
            :file-list="fileList"
            list-type="picture-card"
            :limit="1"
            accept="image/*"
          >
            <el-icon><Plus /></el-icon>
            <template #tip>
              <div class="el-upload__tip">
                只能上传jpg/png/gif文件，且不超过5MB
              </div>
            </template>
          </el-upload>
        </el-form-item>
        <el-form-item label="完成说明">
          <el-input v-model="submitForm.proofDescription" type="textarea" :rows="4" placeholder="请描述完成情况" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="submitDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmSubmit" :disabled="!submitForm.proofImageUrl">提交</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { Refresh, Plus } from '@element-plus/icons-vue'
import { getTasks, submitTask, getMyTaskRecords } from '@/api/task'
import { getPointsBalance, getPointsTransactions } from '@/api/points'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()

// 上传配置
const uploadAction = '/api/upload/task-proof'

const uploadHeaders = computed(() => {
  return {
    'Authorization': `Bearer ${localStorage.getItem('token')}`
  }
})

// 积分信息
const pointsBalance = ref({})

// 任务列表
const tasks = ref([])

// 任务记录
const myTaskRecords = ref([])
const recordsLoading = ref(false)

// 积分流水
const transactions = ref([])
const transactionsLoading = ref(false)

// 提交对话框
const submitDialogVisible = ref(false)
const currentTask = ref(null)
const submitForm = ref({
  proofImageUrl: '',
  proofDescription: ''
})
const fileList = ref([])

// 文件上传前验证
const beforeUpload = (file) => {
  const isImage = file.type.startsWith('image/')
  const isLt5M = file.size / 1024 / 1024 < 5

  if (!isImage) {
    ElMessage.error('只能上传图片文件!')
    return false
  }
  if (!isLt5M) {
    ElMessage.error('图片大小不能超过 5MB!')
    return false
  }
  return true
}

// 文件上传成功
const handleUploadSuccess = (response) => {
  if (response.code === 200) {
    submitForm.value.proofImageUrl = response.data.url
    ElMessage.success('图片上传成功')
  } else {
    ElMessage.error(response.msg || '上传失败')
  }
}

// 文件上传失败
const handleUploadError = (error) => {
  console.error('上传失败:', error)
  ElMessage.error('图片上传失败，请重试')
}

// 加载积分余额
const loadPointsBalance = async () => {
  try {
    pointsBalance.value = await getPointsBalance()
  } catch (error) {
    ElMessage.error('加载积分信息失败')
    console.error(error)
  }
}

// 加载可用任务
const loadTasks = async () => {
  try {
    tasks.value = await getTasks(true)  // 仅显示启用的任务
  } catch (error) {
    ElMessage.error('加载任务列表失败')
    console.error(error)
  }
}

// 加载我的任务记录
const loadMyTaskRecords = async () => {
  recordsLoading.value = true
  try {
    myTaskRecords.value = await getMyTaskRecords()
  } catch (error) {
    ElMessage.error('加载任务记录失败')
    console.error(error)
  } finally {
    recordsLoading.value = false
  }
}

// 加载积分流水
const loadTransactions = async () => {
  transactionsLoading.value = true
  try {
    transactions.value = await getPointsTransactions(20)
  } catch (error) {
    ElMessage.error('加载积分流水失败')
    console.error(error)
  } finally {
    transactionsLoading.value = false
  }
}

// 提交任务凭证
const submitTaskProof = (task) => {
  currentTask.value = task
  submitForm.value = {
    proofImageUrl: '',
    proofDescription: ''
  }
  fileList.value = []  // 清空文件列表
  submitDialogVisible.value = true
}

// 确认提交
const confirmSubmit = async () => {
  if (!submitForm.value.proofImageUrl.trim()) {
    ElMessage.warning('请先上传截图')
    return
  }

  try {
    await submitTask({
      taskId: currentTask.value.taskId,
      proofImageUrl: submitForm.value.proofImageUrl,
      proofDescription: submitForm.value.proofDescription
    })
    ElMessage.success('任务提交成功，请等待审核')
    submitDialogVisible.value = false
    loadMyTaskRecords()
  } catch (error) {
    ElMessage.error('提交失败')
    console.error(error)
  }
}

// 获取会员等级文本
const getMemberLevelText = (level) => {
  const levelMap = {
    'BRONZE': '青铜会员',
    'SILVER': '白银会员',
    'GOLD': '黄金会员',
    'PLATINUM': '铂金会员'
  }
  return levelMap[level] || level
}

// 获取会员等级标签类型
const getMemberLevelType = (level) => {
  const typeMap = {
    'BRONZE': 'info',
    'SILVER': '',
    'GOLD': 'warning',
    'PLATINUM': 'danger'
  }
  return typeMap[level] || 'info'
}

// 格式化时间
const formatTime = (time) => {
  if (!time) return '-'
  return new Date(time).toLocaleString('zh-CN')
}

onMounted(() => {
  loadPointsBalance()
  loadTasks()
  loadMyTaskRecords()
  loadTransactions()
})
</script>

<style scoped>
.tasks-and-points {
  padding: 20px;
  overflow-y: auto;
  max-height: calc(100vh - 120px);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.points-info {
  padding: 20px 0;
}

.member-level {
  text-align: center;
}

.member-level .label {
  color: #909399;
  font-size: 14px;
  margin-bottom: 10px;
}

.task-card {
  margin-bottom: 20px;
  height: 220px;
  display: flex;
  flex-direction: column;
}

.task-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.task-header h3 {
  margin: 0;
  font-size: 16px;
}

.task-description {
  flex: 1;
  color: #606266;
  font-size: 14px;
  line-height: 1.5;
  margin-bottom: 15px;
}

.task-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.upload-demo {
  width: 100%;
}

.el-upload__tip {
  color: #999;
  font-size: 12px;
  margin-top: 7px;
}
</style>
