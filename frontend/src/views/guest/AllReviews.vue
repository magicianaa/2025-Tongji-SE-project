<template>
  <div class="all-reviews-page">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>所有房间评价</span>
          <el-tag type="success" size="large">
            <el-icon><Star /></el-icon>
            综合评分：{{ overallAverage > 0 ? overallAverage.toFixed(1) : 'N/A' }} / 5.0
          </el-tag>
        </div>
      </template>

      <!-- 统计信息 -->
      <div class="statistics" v-if="total > 0">
        <el-row :gutter="20">
          <el-col :span="6">
            <el-statistic title="总评价数" :value="total">
              <template #suffix>
                <span style="font-size: 14px;">条</span>
              </template>
            </el-statistic>
          </el-col>
          <el-col :span="6">
            <el-statistic title="平均评分" :value="overallAverage" :precision="1">
              <template #suffix>
                <span style="font-size: 14px;">/ 5.0</span>
              </template>
            </el-statistic>
          </el-col>
          <el-col :span="6">
            <el-statistic title="好评率" :value="positiveRate" :precision="1">
              <template #suffix>
                <span style="font-size: 14px;">%</span>
              </template>
            </el-statistic>
          </el-col>
          <el-col :span="6">
            <el-statistic title="最新评价" :value="latestReviewDate">
              <template #prefix>
                <el-icon><Clock /></el-icon>
              </template>
            </el-statistic>
          </el-col>
        </el-row>
      </div>

      <el-divider />

      <!-- 评价列表 -->
      <div class="reviews-list" v-loading="loading">
        <el-empty v-if="reviews.length === 0 && !loading" description="暂无评价数据" />

        <div v-else class="review-items">
          <el-card 
            v-for="review in reviews" 
            :key="review.reviewId"
            class="review-card"
            shadow="hover"
          >
            <div class="review-header">
              <div class="left-section">
                <el-avatar :size="50" :icon="UserFilled" />
                <div class="info">
                  <div class="guest-name">{{ review.guestName || '匿名用户' }}</div>
                  <div class="room-info">
                    <el-tag size="small" type="primary">{{ review.roomNo }}</el-tag>
                    <el-tag size="small" type="info" style="margin-left: 8px;">
                      {{ getRoomTypeName(review.roomType) }}
                    </el-tag>
                  </div>
                </div>
              </div>
              <div class="right-section">
                <el-rate 
                  v-model="review.score" 
                  disabled 
                  :colors="['#99A9BF', '#F7BA2A', '#FF9900']"
                  show-score
                  text-color="#ff9900"
                />
                <div class="review-time">
                  <el-icon><Clock /></el-icon>
                  {{ formatDateTime(review.reviewTime) }}
                </div>
              </div>
            </div>

            <div class="review-content">
              <p>{{ review.comment }}</p>
            </div>

            <!-- 入住信息 -->
            <div class="checkin-detail" v-if="review.checkinTime || review.checkoutTime">
              <el-descriptions :column="3" size="small" border>
                <el-descriptions-item label="入住时间" v-if="review.checkinTime">
                  {{ formatDateTime(review.checkinTime) }}
                </el-descriptions-item>
                <el-descriptions-item label="退房时间" v-if="review.checkoutTime">
                  {{ formatDateTime(review.checkoutTime) }}
                </el-descriptions-item>
                <el-descriptions-item label="入住天数" v-if="review.stayDays">
                  {{ review.stayDays }} 天
                </el-descriptions-item>
              </el-descriptions>
            </div>

            <!-- 酒店回复 -->
            <div v-if="review.reply" class="hotel-reply">
              <div class="reply-header">
                <el-icon><Service /></el-icon>
                <span>酒店回复：</span>
              </div>
              <p>{{ review.reply }}</p>
            </div>
          </el-card>
        </div>

        <!-- 分页 -->
        <div class="pagination" v-if="total > pageSize">
          <el-pagination
            v-model:current-page="currentPage"
            v-model:page-size="pageSize"
            :page-sizes="[10, 20, 50]"
            :total="total"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="handleSizeChange"
            @current-change="handlePageChange"
          />
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Star, Clock, UserFilled, Service } from '@element-plus/icons-vue'
import { getReviews } from '@/api/review'

const loading = ref(false)
const reviews = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 计算综合平均分
const overallAverage = computed(() => {
  if (reviews.value.length === 0) return 0
  const sum = reviews.value.reduce((acc, review) => acc + review.score, 0)
  return sum / reviews.value.length
})

// 计算好评率（4星及以上）
const positiveRate = computed(() => {
  if (reviews.value.length === 0) return 0
  const positiveCount = reviews.value.filter(review => review.score >= 4).length
  return (positiveCount / reviews.value.length) * 100
})

// 最新评价日期
const latestReviewDate = computed(() => {
  if (reviews.value.length === 0) return '-'
  const latest = reviews.value[0] // 假设列表按时间倒序
  return formatDate(latest.reviewTime)
})

// 加载评价列表
const loadReviews = async () => {
  loading.value = true
  try {
    const data = await getReviews({
      page: currentPage.value,
      size: pageSize.value
    })
    reviews.value = data.records || []
    total.value = data.total || 0
  } catch (error) {
    console.error('加载评价失败：', error)
    ElMessage.error('加载评价失败')
  } finally {
    loading.value = false
  }
}

// 分页处理
const handleSizeChange = (newSize) => {
  pageSize.value = newSize
  currentPage.value = 1
  loadReviews()
}

const handlePageChange = (newPage) => {
  currentPage.value = newPage
  loadReviews()
}

// 格式化日期时间
const formatDateTime = (datetime) => {
  if (!datetime) return '-'
  const d = new Date(datetime)
  return d.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

// 格式化日期
const formatDate = (datetime) => {
  if (!datetime) return '-'
  const d = new Date(datetime)
  return d.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit'
  })
}

// 获取房型名称
const getRoomTypeName = (type) => {
  const typeMap = {
    'SINGLE': '单人电竞房',
    'DOUBLE': '双人电竞房',
    'FIVE_PLAYER': '五黑开黑房',
    'VIP': '豪华电竞套房'
  }
  return typeMap[type] || type
}

onMounted(() => {
  loadReviews()
})
</script>

<style scoped>
.all-reviews-page {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 18px;
  font-weight: bold;
}

.statistics {
  margin-bottom: 20px;
}

.reviews-list {
  min-height: 400px;
}

.review-items {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.review-card {
  transition: all 0.3s;
}

.review-card:hover {
  transform: translateY(-2px);
}

.review-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.left-section {
  display: flex;
  align-items: center;
  gap: 12px;
}

.info {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.guest-name {
  font-size: 16px;
  font-weight: bold;
  color: #303133;
}

.room-info {
  display: flex;
  align-items: center;
}

.right-section {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 8px;
}

.review-time {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: #909399;
}

.review-content {
  padding: 16px;
  background-color: #f5f7fa;
  border-radius: 8px;
  margin-bottom: 12px;
}

.review-content p {
  margin: 0;
  line-height: 1.6;
  color: #606266;
  font-size: 14px;
}

.checkin-detail {
  margin-top: 12px;
  margin-bottom: 12px;
}

.hotel-reply {
  margin-top: 12px;
  padding: 12px;
  background-color: #ecf5ff;
  border-left: 4px solid #409eff;
  border-radius: 4px;
}

.reply-header {
  display: flex;
  align-items: center;
  gap: 6px;
  font-weight: bold;
  color: #409eff;
  margin-bottom: 8px;
}

.hotel-reply p {
  margin: 0;
  color: #606266;
  font-size: 14px;
  line-height: 1.6;
}

.pagination {
  display: flex;
  justify-content: center;
  margin-top: 24px;
}

:deep(.el-statistic__head) {
  font-size: 14px;
  color: #909399;
}

:deep(.el-statistic__content) {
  font-size: 24px;
  font-weight: bold;
  color: #303133;
}
</style>
