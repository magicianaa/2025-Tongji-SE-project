<template>
  <div class="checkin-history-container">
    <el-page-header @back="goBack" content="我的入住记录" class="page-header" />

    <div class="history-content">
      <!-- 数据加载中 -->
      <div v-loading="loading" class="loading-container" v-if="loading">
        <el-empty description="加载中..." />
      </div>

      <!-- 入住记录列表 -->
      <div v-else-if="records.length > 0" class="records-list">
        <el-card 
          v-for="record in records" 
          :key="record.recordId" 
          class="record-card"
          shadow="hover"
        >
          <template #header>
            <div class="card-header">
              <div class="room-info">
                <el-icon><House /></el-icon>
                <span class="room-no">{{ record.roomNo || `房间 ${record.roomId}` }}</span>
                <el-tag :type="record.roomType === 'SINGLE' ? 'primary' : 'success'" size="small">
                  {{ getRoomTypeText(record.roomType) }}
                </el-tag>
              </div>
              <el-tag v-if="record.hasReviewed" type="success" size="small">
                <el-icon><Check /></el-icon> 已评价
              </el-tag>
            </div>
          </template>

          <div class="record-details">
            <div class="detail-row">
              <el-icon><Calendar /></el-icon>
              <span class="label">入住时间：</span>
              <span class="value">{{ formatDateTime(record.actualCheckin) }}</span>
            </div>
            <div class="detail-row">
              <el-icon><Calendar /></el-icon>
              <span class="label">退房时间：</span>
              <span class="value">{{ formatDateTime(record.actualCheckout) }}</span>
            </div>
            <div class="detail-row" v-if="record.finalAmount">
              <el-icon><Money /></el-icon>
              <span class="label">总费用：</span>
              <span class="value price">¥{{ Number(record.finalAmount).toFixed(2) }}</span>
            </div>
          </div>

          <!-- 已有评价显示 -->
          <div v-if="record.hasReviewed && record.review" class="existing-review">
            <el-divider />
            <div class="review-header">
              <span class="review-title">我的评价：</span>
              <el-rate v-model="record.review.score" disabled show-score text-color="#ff9900" />
            </div>
            <div class="review-comment">{{ record.review.comment }}</div>
            <div class="review-time">
              评价时间：{{ formatDateTime(record.review.reviewTime) }}
            </div>
          </div>

          <!-- 评价按钮 -->
          <div class="card-actions" v-if="!record.hasReviewed">
            <el-button 
              type="primary" 
              @click="openReviewDialog(record)"
              :icon="Edit"
            >
              立即评价
            </el-button>
          </div>
        </el-card>
      </div>

      <!-- 空状态 -->
      <el-empty v-else description="暂无入住记录" />

      <!-- 分页 -->
      <div class="pagination" v-if="total > 0">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[5, 10, 20, 50]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </div>

    <!-- 评价对话框 -->
    <el-dialog
      v-model="reviewDialogVisible"
      title="评价入住体验"
      width="600px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="reviewFormRef"
        :model="reviewForm"
        :rules="reviewRules"
        label-width="100px"
      >
        <el-form-item label="房间信息">
          <span>{{ selectedRecord?.roomNo || `房间 ${selectedRecord?.roomId}` }}</span>
        </el-form-item>
        
        <el-form-item label="入住时间">
          <span>{{ formatDateTime(selectedRecord?.actualCheckin) }} 至 {{ formatDateTime(selectedRecord?.actualCheckout) }}</span>
        </el-form-item>

        <el-form-item label="评分" prop="score">
          <el-rate
            v-model="reviewForm.score"
            :texts="['非常差', '差', '一般', '好', '非常好']"
            show-text
            text-color="#ff9900"
            :colors="['#99A9BF', '#F7BA2A', '#FF9900']"
          />
        </el-form-item>

        <el-form-item label="评价内容" prop="comment">
          <el-input
            v-model="reviewForm.comment"
            type="textarea"
            :rows="5"
            placeholder="请分享您的入住体验，包括房间设施、服务质量、卫生状况等（10-1000字）"
            maxlength="1000"
            show-word-limit
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="reviewDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitReview" :loading="submitting">
          提交评价
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { House, Calendar, Money, Edit, Check } from '@element-plus/icons-vue'
import { getMyCheckinHistory, createReview } from '@/api/review'

const router = useRouter()

// 数据状态
const loading = ref(false)
const records = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)

// 评价对话框
const reviewDialogVisible = ref(false)
const reviewFormRef = ref(null)
const submitting = ref(false)
const selectedRecord = ref(null)

const reviewForm = reactive({
  recordId: null,
  score: 5,
  comment: ''
})

const reviewRules = {
  score: [
    { required: true, message: '请选择评分', trigger: 'change' }
  ],
  comment: [
    { required: true, message: '请填写评价内容', trigger: 'blur' },
    { min: 10, max: 1000, message: '评价内容长度在 10 到 1000 个字符', trigger: 'blur' }
  ]
}

// 获取入住记录列表
const loadCheckinHistory = async () => {
  loading.value = true
  try {
    const data = await getMyCheckinHistory(currentPage.value, pageSize.value)
    // 响应拦截器已经返回了data部分（Page对象），直接使用
    records.value = data.records || []
    total.value = data.total || 0
  } catch (error) {
    console.error('获取入住记录失败：', error)
    ElMessage.error('获取入住记录失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

// 打开评价对话框
const openReviewDialog = (record) => {
  selectedRecord.value = record
  reviewForm.recordId = record.recordId
  reviewForm.score = 5
  reviewForm.comment = ''
  reviewDialogVisible.value = true
}

// 提交评价
const submitReview = async () => {
  if (!reviewFormRef.value) return
  
  await reviewFormRef.value.validate(async (valid) => {
    if (valid) {
      submitting.value = true
      try {
        await createReview({
          recordId: reviewForm.recordId,
          score: reviewForm.score,
          comment: reviewForm.comment
        })
        
        // 成功时响应拦截器已处理，直接认为成功
        ElMessage.success('评价提交成功，感谢您的宝贵意见！')
        reviewDialogVisible.value = false
        // 刷新列表
        await loadCheckinHistory()
      } catch (error) {
        console.error('提交评价失败：', error)
        // 错误已在响应拦截器中提示，这里不需要重复提示
      } finally {
        submitting.value = false
      }
    }
  })
}

// 分页处理
const handleSizeChange = (val) => {
  pageSize.value = val
  currentPage.value = 1
  loadCheckinHistory()
}

const handleCurrentChange = (val) => {
  currentPage.value = val
  loadCheckinHistory()
}

// 返回
const goBack = () => {
  router.back()
}

// 格式化日期时间
const formatDateTime = (dateTime) => {
  if (!dateTime) return '-'
  const date = new Date(dateTime)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
}

// 获取房间类型文本
const getRoomTypeText = (type) => {
  const typeMap = {
    'SINGLE': '单人间',
    'DOUBLE': '双人间',
    'SUITE': '套房',
    'ESPORTS': '电竞房'
  }
  return typeMap[type] || type
}

// 页面加载时获取数据
onMounted(() => {
  loadCheckinHistory()
})
</script>

<style scoped>
.checkin-history-container {
  padding: 20px;
  background: #f5f7fa;
  min-height: calc(100vh - 60px);
  overflow-y: auto;
  max-height: calc(100vh - 120px);
}

.page-header {
  background: white;
  padding: 20px;
  border-radius: 8px;
  margin-bottom: 20px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.08);
}

.history-content {
  max-width: 1200px;
  margin: 0 auto;
}

.loading-container {
  min-height: 400px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.records-list {
  display: grid;
  gap: 20px;
}

.record-card {
  transition: all 0.3s;
}

.record-card:hover {
  transform: translateY(-2px);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.room-info {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 16px;
  font-weight: 600;
}

.room-no {
  color: #303133;
}

.record-details {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-bottom: 16px;
}

.detail-row {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #606266;
  font-size: 14px;
}

.detail-row .el-icon {
  color: #409EFF;
}

.label {
  font-weight: 500;
  min-width: 80px;
}

.value {
  color: #303133;
}

.value.price {
  color: #F56C6C;
  font-weight: 600;
  font-size: 16px;
}

.existing-review {
  margin-top: 16px;
}

.review-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.review-title {
  font-weight: 600;
  color: #303133;
}

.review-comment {
  padding: 12px;
  background: #f5f7fa;
  border-radius: 4px;
  color: #606266;
  line-height: 1.6;
  margin-bottom: 8px;
}

.review-time {
  font-size: 12px;
  color: #909399;
  text-align: right;
}

.card-actions {
  margin-top: 16px;
  text-align: right;
}

.pagination {
  margin-top: 30px;
  display: flex;
  justify-content: center;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .checkin-history-container {
    padding: 10px;
  }

  .card-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }

  .detail-row {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>
