<template>
  <div class="review-management">
    <el-page-header @back="$router.back()" content="评价管理" />

    <!-- 筛选工具栏 -->
    <el-card class="filter-card" shadow="never">
      <el-form :inline="true" :model="filters">
        <el-form-item label="评分筛选">
          <el-select v-model="filters.scoreFilter" placeholder="全部" clearable @change="handleSearch">
            <el-option label="全部评价" value="" />
            <el-option label="低分预警 (< 3星)" value="low" />
            <el-option label="高分评价 (≥ 4星)" value="high" />
          </el-select>
        </el-form-item>

        <el-form-item label="回访状态">
          <el-select v-model="filters.followUpStatus" placeholder="全部" clearable @change="handleSearch">
            <el-option label="全部" value="" />
            <el-option label="未处理" value="NONE" />
            <el-option label="已联系" value="CONTACTED" />
            <el-option label="已解决" value="RESOLVED" />
          </el-select>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon> 搜索
          </el-button>
          <el-button @click="handleReset">
            <el-icon><Refresh /></el-icon> 重置
          </el-button>
          <el-button type="warning" @click="showLowScoreOnly">
            <el-icon><Warning /></el-icon> 待处理低分
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 评价列表 -->
    <el-card class="table-card">
      <el-table
        :data="reviews"
        v-loading="loading"
        :row-class-name="getRowClassName"
        style="width: 100%"
      >
        <el-table-column prop="reviewId" label="评价ID" width="80" />
        
        <el-table-column label="评分" width="150">
          <template #default="{ row }">
            <el-rate
              v-model="row.score"
              disabled
              show-score
              :colors="['#F7BA2A', '#F7BA2A', '#F7BA2A']"
            />
          </template>
        </el-table-column>

        <el-table-column label="住客信息" width="150">
          <template #default="{ row }">
            <div>{{ row.guestName || '未知' }}</div>
            <div class="text-secondary">{{ row.guestPhone || '-' }}</div>
          </template>
        </el-table-column>

        <el-table-column prop="roomNo" label="房间号" width="100" />

        <el-table-column label="评价内容" min-width="200" show-overflow-tooltip>
          <template #default="{ row }">
            {{ row.comment || '无文字评价' }}
          </template>
        </el-table-column>

        <el-table-column label="评价时间" width="160">
          <template #default="{ row }">
            {{ formatDateTime(row.reviewTime) }}
          </template>
        </el-table-column>

        <el-table-column label="回访状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getFollowUpStatusType(row.followUpStatus)">
              {{ getFollowUpStatusLabel(row.followUpStatus) }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="showDetail(row)">
              <el-icon><View /></el-icon> 详情
            </el-button>
            <el-button
              v-if="row.followUpStatus === 'NONE'"
              type="warning"
              size="small"
              @click="showFollowUpDialog(row)"
            >
              <el-icon><Phone /></el-icon> 回访
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <el-pagination
        v-model:current-page="pagination.page"
        v-model:page-size="pagination.size"
        :total="pagination.total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="loadReviews"
        @current-change="loadReviews"
        style="margin-top: 20px; justify-content: center;"
      />
    </el-card>

    <!-- 评价详情对话框 -->
    <el-dialog v-model="detailDialogVisible" title="评价详情" width="700px">
      <el-descriptions v-if="currentReview" :column="2" border>
        <el-descriptions-item label="评价ID">
          {{ currentReview.reviewId }}
        </el-descriptions-item>
        <el-descriptions-item label="评分">
          <el-rate v-model="currentReview.score" disabled :colors="['#F7BA2A', '#F7BA2A', '#F7BA2A']" />
        </el-descriptions-item>
        <el-descriptions-item label="住客">
          {{ currentReview.guestName }} ({{ currentReview.guestPhone }})
        </el-descriptions-item>
        <el-descriptions-item label="房间">
          {{ currentReview.roomNo }} ({{ currentReview.roomType }})
        </el-descriptions-item>
        <el-descriptions-item label="入住时间" :span="2">
          {{ formatDateTime(currentReview.checkInTime) }} 至 {{ formatDateTime(currentReview.checkOutTime) }}
        </el-descriptions-item>
        <el-descriptions-item label="评价时间" :span="2">
          {{ formatDateTime(currentReview.reviewTime) }}
        </el-descriptions-item>
        <el-descriptions-item label="评价内容" :span="2">
          {{ currentReview.comment || '无文字评价' }}
        </el-descriptions-item>
        
        <!-- 低分评价显示回访信息 -->
        <template v-if="currentReview.isLowScore">
          <el-descriptions-item label="回访状态" :span="2">
            <el-tag :type="getFollowUpStatusType(currentReview.followUpStatus)">
              {{ getFollowUpStatusLabel(currentReview.followUpStatus) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item v-if="currentReview.followUpNotes" label="回访备注" :span="2">
            {{ currentReview.followUpNotes }}
          </el-descriptions-item>
          <el-descriptions-item v-if="currentReview.handlerName" label="处理人" :span="2">
            {{ currentReview.handlerName }}
          </el-descriptions-item>
        </template>

        <el-descriptions-item label="酒店回复" :span="2">
          {{ currentReview.reply || '暂无回复' }}
        </el-descriptions-item>
      </el-descriptions>

      <template #footer>
        <el-button @click="detailDialogVisible = false">关闭</el-button>
        <el-button
          v-if="currentReview && currentReview.followUpStatus === 'NONE'"
          type="warning"
          @click="showFollowUpDialogFromDetail"
        >
          回访登记
        </el-button>
        <el-button type="primary" @click="showReplyDialog">
          {{ currentReview?.reply ? '修改回复' : '回复评价' }}
        </el-button>
      </template>
    </el-dialog>

    <!-- 回访登记对话框 -->
    <el-dialog v-model="followUpDialogVisible" title="回访登记" width="600px">
      <el-form ref="followUpFormRef" :model="followUpForm" :rules="followUpRules" label-width="100px">
        <el-form-item label="回访状态" prop="followUpStatus">
          <el-radio-group v-model="followUpForm.followUpStatus">
            <el-radio value="CONTACTED">已联系</el-radio>
            <el-radio value="RESOLVED">已解决</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="回访备注" prop="followUpNotes">
          <el-input
            v-model="followUpForm.followUpNotes"
            type="textarea"
            :rows="4"
            placeholder="请记录与住客的沟通内容和问题处理情况..."
          />
        </el-form-item>

        <el-form-item label="酒店回复">
          <el-input
            v-model="followUpForm.reply"
            type="textarea"
            :rows="3"
            placeholder="（可选）此回复可能会展示给其他用户"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="followUpDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleFollowUpSubmit" :loading="submitting">
          确认提交
        </el-button>
      </template>
    </el-dialog>

    <!-- 酒店回复对话框 -->
    <el-dialog v-model="replyDialogVisible" title="回复评价" width="600px">
      <el-form>
        <el-form-item label="原评价内容">
          <el-input
            :value="currentReview?.comment || '无文字评价'"
            type="textarea"
            :rows="3"
            readonly
          />
        </el-form-item>

        <el-form-item label="回复内容">
          <el-input
            v-model="replyContent"
            type="textarea"
            :rows="4"
            placeholder="感谢您的反馈，我们已了解到您的问题..."
          />
          <div class="hint">提示：此回复可能会展示给其他用户</div>
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="replyDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleReplySubmit" :loading="submitting">
          提交回复
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, Refresh, Warning, View, Phone } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { getReviews, getLowScoreReviews, getReviewById, updateFollowUp, replyReview } from '@/api/review'

const userStore = useUserStore()

const loading = ref(false)
const submitting = ref(false)
const reviews = ref([])
const currentReview = ref(null)

const detailDialogVisible = ref(false)
const followUpDialogVisible = ref(false)
const replyDialogVisible = ref(false)
const replyContent = ref('')

const followUpFormRef = ref(null)

const filters = reactive({
  scoreFilter: '',
  followUpStatus: ''
})

const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

const followUpForm = reactive({
  followUpStatus: 'CONTACTED',
  followUpNotes: '',
  reply: ''
})

const followUpRules = {
  followUpStatus: [
    { required: true, message: '请选择回访状态', trigger: 'change' }
  ],
  followUpNotes: [
    { required: true, message: '请填写回访备注', trigger: 'blur' }
  ]
}

// 行样式 - 低分评价高亮
const getRowClassName = ({ row }) => {
  return row.isLowScore ? 'low-score-row' : ''
}

const getFollowUpStatusType = (status) => {
  const typeMap = {
    'NONE': 'info',
    'CONTACTED': 'warning',
    'RESOLVED': 'success'
  }
  return typeMap[status] || 'info'
}

const getFollowUpStatusLabel = (status) => {
  const labelMap = {
    'NONE': '未处理',
    'CONTACTED': '已联系',
    'RESOLVED': '已解决'
  }
  return labelMap[status] || status
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

// 加载评价列表
const loadReviews = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.page,
      size: pagination.size
    }

    // 评分筛选
    if (filters.scoreFilter === 'low') {
      params.lowScoreOnly = true
    }

    // 回访状态筛选
    if (filters.followUpStatus) {
      params.followUpStatus = filters.followUpStatus
    }

    const result = await getReviews(params)
    reviews.value = result.records || []
    pagination.total = result.total || 0

  } catch (error) {
    ElMessage.error(error.message || '加载评价列表失败')
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  pagination.page = 1
  loadReviews()
}

// 重置筛选
const handleReset = () => {
  filters.scoreFilter = ''
  filters.followUpStatus = ''
  pagination.page = 1
  loadReviews()
}

// 只显示待处理的低分评价
const showLowScoreOnly = () => {
  filters.scoreFilter = 'low'
  filters.followUpStatus = 'NONE'
  pagination.page = 1
  loadReviews()
}

// 显示详情
const showDetail = async (row) => {
  try {
    currentReview.value = await getReviewById(row.reviewId)
    detailDialogVisible.value = true
  } catch (error) {
    ElMessage.error(error.message || '加载详情失败')
  }
}

// 显示回访对话框
const showFollowUpDialog = (row) => {
  currentReview.value = row
  followUpForm.followUpStatus = 'CONTACTED'
  followUpForm.followUpNotes = ''
  followUpForm.reply = ''
  followUpDialogVisible.value = true
}

const showFollowUpDialogFromDetail = () => {
  detailDialogVisible.value = false
  followUpForm.followUpStatus = 'CONTACTED'
  followUpForm.followUpNotes = ''
  followUpForm.reply = ''
  followUpDialogVisible.value = true
}

// 提交回访
const handleFollowUpSubmit = async () => {
  if (!followUpFormRef.value) return

  await followUpFormRef.value.validate(async (valid) => {
    if (!valid) return

    submitting.value = true
    try {
      const handlerId = userStore.userInfo?.userId
      if (!handlerId) {
        ElMessage.error('未找到用户信息')
        return
      }

      await updateFollowUp(currentReview.value.reviewId, handlerId, {
        followUpStatus: followUpForm.followUpStatus,
        followUpNotes: followUpForm.followUpNotes,
        reply: followUpForm.reply || undefined
      })

      ElMessage.success('回访信息更新成功')
      followUpDialogVisible.value = false
      loadReviews()

    } catch (error) {
      ElMessage.error(error.message || '操作失败')
    } finally {
      submitting.value = false
    }
  })
}

// 显示回复对话框
const showReplyDialog = () => {
  replyContent.value = currentReview.value?.reply || ''
  replyDialogVisible.value = true
}

// 提交回复
const handleReplySubmit = async () => {
  if (!replyContent.value.trim()) {
    ElMessage.warning('请输入回复内容')
    return
  }

  submitting.value = true
  try {
    await replyReview(currentReview.value.reviewId, replyContent.value)
    ElMessage.success('回复成功')
    replyDialogVisible.value = false
    detailDialogVisible.value = false
    loadReviews()
  } catch (error) {
    ElMessage.error(error.message || '回复失败')
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  loadReviews()
})
</script>

<style scoped lang="scss">
.review-management {
  padding: 20px;
  overflow-y: auto;
  max-height: calc(100vh - 120px);

  .filter-card {
    margin: 20px 0;
  }

  .table-card {
    margin-top: 20px;
  }

  .text-secondary {
    font-size: 12px;
    color: #909399;
  }

  .hint {
    margin-top: 5px;
    font-size: 12px;
    color: #909399;
  }
}

// 低分评价行高亮样式
:deep(.low-score-row) {
  background-color: #fef0f0 !important;
  
  &:hover > td {
    background-color: #fde2e2 !important;
  }
}
</style>
