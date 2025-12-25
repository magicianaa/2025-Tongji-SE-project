<template>
  <div class="room-reviews">
    <!-- 评分概览 -->
    <div class="rating-overview" v-if="averageScore !== null">
      <div class="score-display">
        <div class="score-number">{{ averageScore.toFixed(1) }}</div>
        <el-rate 
          v-model="displayScore" 
          disabled 
          show-score 
          text-color="#ff9900"
          :colors="['#99A9BF', '#F7BA2A', '#FF9900']"
        />
        <div class="review-count">基于 {{ total }} 条评价</div>
      </div>
    </div>

    <!-- 评价列表 -->
    <div class="reviews-list" v-loading="loading">
      <div v-if="reviews.length === 0 && !loading" class="no-reviews">
        <el-empty description="暂无评价" :image-size="80" />
      </div>

      <div v-else class="review-items">
        <div 
          v-for="review in reviews" 
          :key="review.reviewId"
          class="review-item"
        >
          <div class="review-header">
            <div class="user-info">
              <el-avatar :size="40" :icon="UserFilled" />
              <div class="user-details">
                <span class="username">{{ review.guestName || '匿名用户' }}</span>
                <span class="review-date">{{ formatDate(review.reviewTime) }}</span>
              </div>
            </div>
            <el-rate 
              v-model="review.score" 
              disabled 
              :colors="['#99A9BF', '#F7BA2A', '#FF9900']"
            />
          </div>

          <div class="review-content">
            <p class="comment">{{ review.comment }}</p>
          </div>

          <!-- 酒店回复 -->
          <div v-if="review.reply" class="hotel-reply">
            <div class="reply-header">
              <el-icon><Service /></el-icon>
              <span>酒店回复：</span>
            </div>
            <p class="reply-content">{{ review.reply }}</p>
          </div>

          <!-- 入住信息 -->
          <div class="checkin-info" v-if="review.roomNo || review.checkinTime">
            <el-tag size="small" type="info">
              {{ review.roomNo ? `房间：${review.roomNo}` : '' }}
              {{ review.checkinTime ? ` · 入住时间：${formatDate(review.checkinTime)}` : '' }}
            </el-tag>
          </div>
        </div>
      </div>

      <!-- 分页 -->
      <div class="pagination" v-if="total > pageSize">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[5, 10, 20]"
          :total="total"
          layout="total, sizes, prev, pager, next"
          @size-change="handleSizeChange"
          @current-change="handlePageChange"
          small
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { UserFilled, Service } from '@element-plus/icons-vue'
import { getRoomReviews, getAverageScore } from '@/api/review'
import { ElMessage } from 'element-plus'

const props = defineProps({
  roomId: {
    type: Number,
    required: true
  },
  autoLoad: {
    type: Boolean,
    default: true
  }
})

// 数据
const loading = ref(false)
const reviews = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(5)
const averageScore = ref(null)

// 显示评分（用于el-rate）
const displayScore = computed(() => averageScore.value || 0)

// 加载评价列表
const loadReviews = async () => {
  if (!props.roomId) return
  
  loading.value = true
  try {
    const [reviewsRes, scoreRes] = await Promise.all([
      getRoomReviews(props.roomId, currentPage.value, pageSize.value),
      getAverageScore(props.roomId)
    ])

    if (reviewsRes.code === 200 && reviewsRes.data) {
      reviews.value = reviewsRes.data.records || []
      total.value = reviewsRes.data.total || 0
    }

    if (scoreRes.code === 200) {
      averageScore.value = scoreRes.data
    }
  } catch (error) {
    console.error('加载评价失败：', error)
    ElMessage.error('加载评价失败')
  } finally {
    loading.value = false
  }
}

// 分页处理
const handlePageChange = () => {
  loadReviews()
}

const handleSizeChange = () => {
  currentPage.value = 1
  loadReviews()
}

// 格式化日期
const formatDate = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  const now = new Date()
  const diff = now - date
  const days = Math.floor(diff / (1000 * 60 * 60 * 24))
  
  if (days === 0) return '今天'
  if (days === 1) return '昨天'
  if (days < 7) return `${days}天前`
  if (days < 30) return `${Math.floor(days / 7)}周前`
  if (days < 365) return `${Math.floor(days / 30)}个月前`
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
}

// 监听roomId变化
watch(() => props.roomId, () => {
  if (props.autoLoad) {
    currentPage.value = 1
    loadReviews()
  }
})

// 组件挂载时加载
onMounted(() => {
  if (props.autoLoad) {
    loadReviews()
  }
})

// 暴露刷新方法
defineExpose({
  loadReviews
})
</script>

<style scoped>
.room-reviews {
  width: 100%;
}

.rating-overview {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 24px;
  border-radius: 12px;
  margin-bottom: 24px;
  color: white;
}

.score-display {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
}

.score-number {
  font-size: 48px;
  font-weight: bold;
  line-height: 1;
}

.review-count {
  font-size: 14px;
  opacity: 0.9;
}

.reviews-list {
  min-height: 200px;
}

.no-reviews {
  padding: 40px 0;
  text-align: center;
}

.review-items {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.review-item {
  background: white;
  border: 1px solid #ebeef5;
  border-radius: 8px;
  padding: 20px;
  transition: all 0.3s;
}

.review-item:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  transform: translateY(-2px);
}

.review-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.user-details {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.username {
  font-weight: 600;
  color: #303133;
  font-size: 15px;
}

.review-date {
  font-size: 12px;
  color: #909399;
}

.review-content {
  margin-bottom: 12px;
}

.comment {
  color: #606266;
  line-height: 1.6;
  margin: 0;
  font-size: 14px;
}

.hotel-reply {
  background: #f5f7fa;
  padding: 12px;
  border-radius: 6px;
  margin-top: 12px;
  border-left: 3px solid #409EFF;
}

.reply-header {
  display: flex;
  align-items: center;
  gap: 6px;
  font-weight: 600;
  color: #409EFF;
  margin-bottom: 8px;
  font-size: 13px;
}

.reply-content {
  color: #606266;
  margin: 0;
  font-size: 13px;
  line-height: 1.5;
}

.checkin-info {
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px dashed #ebeef5;
}

.pagination {
  margin-top: 24px;
  display: flex;
  justify-content: center;
}

/* 响应式 */
@media (max-width: 768px) {
  .rating-overview {
    padding: 16px;
  }

  .score-number {
    font-size: 36px;
  }

  .review-item {
    padding: 16px;
  }

  .review-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }
}
</style>
