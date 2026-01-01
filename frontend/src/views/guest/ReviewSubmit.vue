<template>
  <div class="review-submit-page">
    <el-page-header @back="$router.back()" content="提交评价" />

    <el-card class="review-card" v-loading="loading">
      <template #header>
        <div class="card-header">
          <span>入住评价</span>
        </div>
      </template>

      <!-- 入住信息展示 -->
      <el-descriptions v-if="checkInInfo" title="入住信息" :column="2" border>
        <el-descriptions-item label="房间号">
          {{ checkInInfo.roomNo }}
        </el-descriptions-item>
        <el-descriptions-item label="房型">
          {{ getRoomTypeLabel(checkInInfo.roomType) }}
        </el-descriptions-item>
        <el-descriptions-item label="入住时间">
          {{ formatDateTime(checkInInfo.actualCheckin) }}
        </el-descriptions-item>
        <el-descriptions-item label="退房时间">
          {{ formatDateTime(checkInInfo.actualCheckout) }}
        </el-descriptions-item>
        <el-descriptions-item label="入住天数">
          {{ calculateDays(checkInInfo.actualCheckin, checkInInfo.actualCheckout) }} 天
        </el-descriptions-item>
      </el-descriptions>

      <!-- 已评价显示和编辑 -->
      <div v-if="hasReviewedFlag && existingReview">
        <el-alert
          title="您已评价过此次入住"
          type="info"
          :closable="false"
          show-icon
          style="margin-top: 20px;"
        />

        <!-- 显示已有评价内容 -->
        <el-card v-if="!isEditing" style="margin-top: 20px;">
          <template #header>
            <div style="display: flex; justify-content: space-between; align-items: center;">
              <span>我的评价</span>
              <div>
                <el-button type="primary" size="small" @click="startEdit">
                  编辑评价
                </el-button>
                <el-popconfirm
                  title="确定要删除这条评价吗？"
                  confirm-button-text="确定"
                  cancel-button-text="取消"
                  @confirm="handleDelete"
                >
                  <template #reference>
                    <el-button type="danger" size="small" :loading="deleting">
                      删除评价
                    </el-button>
                  </template>
                </el-popconfirm>
              </div>
            </div>
          </template>
          
          <div>
            <div style="margin-bottom: 15px;">
              <span style="font-weight: bold;">评分：</span>
              <el-rate v-model="existingReview.score" disabled show-score />
            </div>
            <div style="margin-bottom: 15px;">
              <span style="font-weight: bold;">评价内容：</span>
              <p style="margin-top: 10px; white-space: pre-wrap;">{{ existingReview.comment || '无' }}</p>
            </div>
            <div style="color: #909399; font-size: 14px;">
              <span>评价时间：{{ formatDateTime(existingReview.reviewTime) }}</span>
            </div>
            <div v-if="existingReview.hotelReply" style="margin-top: 15px; padding: 10px; background-color: #f5f7fa; border-radius: 4px;">
              <div style="font-weight: bold; margin-bottom: 5px;">🏨 酒店回复：</div>
              <p style="white-space: pre-wrap;">{{ existingReview.hotelReply }}</p>
              <div style="color: #909399; font-size: 12px; margin-top: 5px;">
                {{ formatDateTime(existingReview.replyTime) }}
              </div>
            </div>
          </div>
        </el-card>

        <!-- 编辑评价表单 -->
        <el-form
          v-else
          ref="formRef"
          :model="form"
          :rules="rules"
          label-width="100px"
          style="margin-top: 30px;"
        >
          <el-form-item label="评分" prop="score" required>
            <div class="rating-container">
              <el-rate
                v-model="form.score"
                :texts="ratingTexts"
                show-text
                :colors="['#F56C6C', '#E6A23C', '#409EFF', '#67C23A', '#F7BA2A']"
                size="large"
              />
              <div class="rating-hint">{{ getRatingHint() }}</div>
            </div>
          </el-form-item>

          <el-form-item label="文字评价" prop="comment">
            <el-input
              v-model="form.comment"
              type="textarea"
              :rows="5"
              maxlength="500"
              show-word-limit
              placeholder="请告诉我们您的入住体验，我们会认真对待每一条反馈..."
            />
          </el-form-item>

          <el-form-item>
            <el-button type="primary" @click="handleUpdate" :loading="submitting" :disabled="!form.score">
              保存修改
            </el-button>
            <el-button @click="cancelEdit">取消</el-button>
          </el-form-item>
        </el-form>
      </div>

      <!-- 评价表单 -->
      <el-form
        v-else
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="100px"
        style="margin-top: 30px;"
      >
        <el-form-item label="评分" prop="score" required>
          <div class="rating-container">
            <el-rate
              v-model="form.score"
              :texts="ratingTexts"
              show-text
              :colors="['#F56C6C', '#E6A23C', '#409EFF', '#67C23A', '#F7BA2A']"
              size="large"
            />
            <div class="rating-hint">{{ getRatingHint() }}</div>
          </div>
        </el-form-item>

        <el-form-item label="文字评价" prop="comment">
          <el-input
            v-model="form.comment"
            type="textarea"
            :rows="5"
            maxlength="500"
            show-word-limit
            placeholder="请告诉我们您的入住体验，我们会认真对待每一条反馈..."
          />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="handleSubmit" :loading="submitting" :disabled="!form.score">
            提交评价
          </el-button>
          <el-button @click="$router.back()">取消</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { submitReview, hasReviewed, updateReview, deleteReview } from '@/api/review'
import request from '@/utils/request'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const submitting = ref(false)
const deleting = ref(false)
const hasReviewedFlag = ref(false)
const checkInInfo = ref(null)
const formRef = ref(null)
const isEditing = ref(false)
const existingReview = ref(null)

const form = reactive({
  recordId: null,
  score: 0,
  comment: ''
})

const rules = {
  score: [
    { required: true, message: '请选择评分', trigger: 'change' }
  ]
}

const ratingTexts = ['非常不满意', '不满意', '一般', '满意', '非常满意']

const getRatingHint = () => {
  if (form.score === 0) return '请为本次入住打分'
  return ratingTexts[form.score - 1]
}

const getRoomTypeLabel = (type) => {
  const typeMap = {
    'SINGLE': '单人房',
    'DOUBLE': '双人房',
    'FIVE_PLAYER': '五黑房',
    'VIP': 'VIP房'
  }
  return typeMap[type] || type
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

const calculateDays = (checkin, checkout) => {
  if (!checkin || !checkout) return 0
  const diff = new Date(checkout) - new Date(checkin)
  return Math.ceil(diff / (1000 * 60 * 60 * 24))
}

// 加载入住记录信息
const loadCheckInInfo = async () => {
  loading.value = true
  try {
    let recordId = route.query.recordId
    
    // 如果没有传recordId，从userStore的checkInInfo中获取
    if (!recordId) {
      if (userStore.checkInInfo?.recordId) {
        recordId = userStore.checkInInfo.recordId
        ElMessage.info('自动加载您最近的入住记录')
      } else {
        ElMessage.warning('未找到入住记录，请先退房后再评价')
        router.push('/guest/home')
        return
      }
    }

    form.recordId = Number(recordId)

    // 获取入住记录详情
    const response = await request({
      url: `/checkin/records/${recordId}`,
      method: 'get'
    })
    
    checkInInfo.value = response

    // 检查是否已评价
    const reviewed = await hasReviewed(recordId)
    hasReviewedFlag.value = reviewed

    // 如果已评价，加载评价内容
    if (reviewed) {
      try {
        const myReviews = await request({
          url: '/reviews/my',
          method: 'get'
        })
        // 找到当前入住记录的评价
        existingReview.value = myReviews.find(r => r.recordId === Number(recordId))
        if (existingReview.value) {
          // 不用填充form，只在点击编辑时填充
        }
      } catch (error) {
        console.error('加载评价记录失败', error)
      }
    }

  } catch (error) {
    ElMessage.error(error.message || '加载入住信息失败')
    router.back()
  } finally {
    loading.value = false
  }
}

// 提交评价
const handleSubmit = async () => {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (!valid) return

    submitting.value = true
    try {
      const guestId = userStore.checkInInfo?.guestId || userStore.userInfo?.guestId
      if (!guestId) {
        ElMessage.error('未找到住客信息')
        return
      }

      await submitReview({
        guestId,
        recordId: form.recordId,
        score: form.score,
        comment: form.comment
      })

      ElMessage.success({
        message: '感谢您的宝贵意见，我们会持续改进服务！',
        duration: 3000
      })

      // 延迟跳转，让用户看到成功提示
      setTimeout(() => {
        router.push('/guest/home')
      }, 1500)

    } catch (error) {
      ElMessage.error(error.message || '提交失败，请重试')
    } finally {
      submitting.value = false
    }
  })
}

// 开始编辑
const startEdit = () => {
  if (existingReview.value) {
    form.score = existingReview.value.score
    form.comment = existingReview.value.comment
    form.recordId = existingReview.value.recordId
    isEditing.value = true
  }
}

// 取消编辑
const cancelEdit = () => {
  isEditing.value = false
  form.score = 0
  form.comment = ''
}

// 更新评价
const handleUpdate = async () => {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (!valid) return

    submitting.value = true
    try {
      await updateReview(existingReview.value.reviewId, {
        recordId: form.recordId,
        score: form.score,
        comment: form.comment
      })

      ElMessage.success('评仵更新成功')
      
      // 重新加载评价内容
      await loadCheckInInfo()
      isEditing.value = false

    } catch (error) {
      ElMessage.error(error.message || '更新失败，请重试')
    } finally {
      submitting.value = false
    }
  })
}

// 删除评价
const handleDelete = async () => {
  if (!existingReview.value) return

  deleting.value = true
  try {
    await deleteReview(existingReview.value.reviewId)
    ElMessage.success('评价已删除')
    
    // 返回上一页
    setTimeout(() => {
      router.back()
    }, 1000)
  } catch (error) {
    ElMessage.error(error.message || '删除失败，请重试')
  } finally {
    deleting.value = false
  }
}

onMounted(() => {
  loadCheckInInfo()
})
</script>

<style scoped lang="scss">
.review-submit-page {
  padding: 20px;
  overflow-y: auto;
  max-height: calc(100vh - 120px);

  .review-card {
    max-width: 800px;
    margin: 20px auto;

    .card-header {
      font-size: 18px;
      font-weight: bold;
    }
  }

  .rating-container {
    display: flex;
    flex-direction: column;
    gap: 10px;

    .rating-hint {
      color: #909399;
      font-size: 14px;
      margin-top: 5px;
    }
  }

  :deep(.el-rate) {
    height: 40px;
    line-height: 40px;
  }

  :deep(.el-rate__text) {
    font-size: 16px;
    margin-left: 10px;
  }
}
</style>
