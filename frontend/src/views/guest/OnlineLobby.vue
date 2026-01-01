<template>
  <div class="online-lobby">
    <el-page-header @back="$router.back()" content="组队招募大厅" />
    
    <!-- 快速导航 -->
    <el-card class="quick-nav" shadow="never">
      <el-row :gutter="16">
        <el-col :span="8">
          <el-button type="primary" @click="$router.push('/guest/gaming-profile')" style="width: 100%;">
            <el-icon><User /></el-icon> 我的电竞档案
          </el-button>
        </el-col>
        <el-col :span="8">
          <el-button type="success" @click="dialogVisible = true" style="width: 100%;">
            <el-icon><Plus /></el-icon> 发布招募
          </el-button>
        </el-col>
        <el-col :span="8">
          <el-button type="warning" @click="$router.push('/guest/team-management')" style="width: 100%;">
            <el-icon><UserFilled /></el-icon> 我的战队
          </el-button>
        </el-col>
      </el-row>
    </el-card>
    
    <div class="filter-bar">
      <el-form :inline="true">
        <el-form-item label="游戏类型">
          <el-select v-model="filters.gameType" clearable placeholder="全部" @change="handleSearch">
            <el-option v-for="game in GAME_TYPES" :key="game.value" :label="game.label" :value="game.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="招募者段位">
          <el-select v-model="filters.rank" clearable placeholder="全部" @change="handleSearch">
            <el-option v-for="rank in currentRanks" :key="rank.value" :label="rank.label" :value="rank.value" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <el-row :gutter="20" v-loading="loading">
      <el-col v-for="recruitment in recruitments" :key="recruitment.recruitmentId" :span="8">
        <el-card class="recruitment-card" shadow="hover">
          <template #header>
            <div class="card-header">
              <div class="header-left">
                <el-tag>{{ getGameLabel(recruitment.gameType) }}</el-tag>
                <el-tag :type="recruitment.status === 'OPEN' ? 'success' : 'info'">
                  {{ recruitment.status === 'OPEN' ? '招募中' : '已关闭' }}
                </el-tag>
              </div>
              <el-button 
                v-if="recruitment.publisherId === (userStore.checkInInfo?.guestId || userStore.userInfo?.guestId)"
                type="danger" 
                size="small"
                text
                @click="handleDelete(recruitment)"
              >
                <el-icon><Delete /></el-icon>
              </el-button>
            </div>
          </template>
          
          <div class="recruitment-info">
            <div class="info-row">
              <span class="label">发布者：</span>
              <span class="value">{{ recruitment.publisherName }}</span>
            </div>
            <div class="info-row" v-if="recruitment.requiredRank">
              <span class="label">段位要求：</span>
              <el-tag type="warning" size="small">{{ getRankLabel(recruitment.requiredRank) }}</el-tag>
            </div>
            <div class="info-row" v-if="recruitment.requiredPosition">
              <span class="label">位置要求：</span>
              <span class="value">{{ recruitment.requiredPosition }}</span>
            </div>
            <div class="info-row">
              <span class="label">需要人数：</span>
              <span class="value">{{ recruitment.maxMembers }} 人</span>
            </div>
            <div class="description">
              <el-text line-clamp="3">{{ recruitment.description || '无描述' }}</el-text>
            </div>
          </div>

          <el-button 
            v-if="recruitment.status === 'OPEN'" 
            type="primary" 
            size="default" 
            @click="handleApply(recruitment)" 
            style="width: 100%; margin-top: 15px;"
          >
            申请加入
          </el-button>
        </el-card>
      </el-col>
    </el-row>

    <el-empty v-if="!loading && recruitments.length === 0" description="暂无招募信息" />

    <el-pagination
      v-if="pagination.total > 0"
      v-model:current-page="pagination.page"
      v-model:page-size="pagination.size"
      :total="pagination.total"
      @current-change="handleSearch"
      layout="total, prev, pager, next"
      style="margin-top: 30px; justify-content: center;"
    />

    <!-- 发布招募对话框 -->
    <el-dialog v-model="dialogVisible" title="发布招募" width="500px">
      <el-form :model="form" ref="formRef" :rules="rules" label-width="100px">
        <el-form-item label="游戏类型" prop="gameType">
          <el-select v-model="form.gameType" placeholder="选择游戏">
            <el-option v-for="game in GAME_TYPES" :key="game.value" :label="game.label" :value="game.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="段位要求" prop="requiredRank">
          <el-select v-model="form.requiredRank" clearable placeholder="不限">
            <el-option v-for="rank in RANKS.DEFAULT" :key="rank.value" :label="rank.label" :value="rank.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="位置要求" prop="requiredPosition">
          <el-input v-model="form.requiredPosition" placeholder="如：中单、ADC（可选）" />
        </el-form-item>
        <el-form-item label="招募描述" prop="description">
          <el-input 
            v-model="form.description" 
            type="textarea" 
            :rows="4" 
            placeholder="请描述你的招募需求..." 
          />
        </el-form-item>
        <el-form-item label="需要人数" prop="maxMembers">
          <el-input-number v-model="form.maxMembers" :min="2" :max="10" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handlePublish" :loading="publishLoading">发布</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onBeforeUnmount } from 'vue'
import { ElMessage, ElMessageBox, ElNotification } from 'element-plus'
import { User, Plus, UserFilled, Delete } from '@element-plus/icons-vue'
import { GAME_TYPES, RANKS } from '@/api/gaming'
import { 
  searchRecruitments, 
  publishRecruitment, 
  applyToRecruitment,
  approveApplication,
  rejectApplication,
  deleteRecruitment
} from '@/api/team'
import { useUserStore } from '@/stores/user'
import websocketService from '@/utils/websocket'

const userStore = useUserStore()
const recruitments = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const publishLoading = ref(false)
const formRef = ref(null)

const filters = reactive({
  gameType: '',
  rank: ''
})

const pagination = reactive({
  page: 1,
  size: 12,
  total: 0
})

const form = reactive({
  gameType: '',
  requiredRank: '',
  requiredPosition: '',
  description: '',
  maxMembers: 5
})

const rules = {
  gameType: [{ required: true, message: '请选择游戏类型', trigger: 'change' }],
  description: [{ required: true, message: '请填写招募描述', trigger: 'blur' }],
  maxMembers: [{ required: true, message: '请设置需要人数', trigger: 'blur' }]
}

const currentRanks = computed(() => {
  return filters.gameType ? (RANKS[filters.gameType] || RANKS.DEFAULT) : RANKS.DEFAULT
})

const handleSearch = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.page, // MyBatis-Plus从1开始
      size: pagination.size
    }
    if (filters.gameType) params.gameType = filters.gameType
    if (filters.rank) params.rank = filters.rank
    
    const res = await searchRecruitments(params)
    console.log('招募列表响应：', res)
    
    // 响应拦截器已经提取了data
    // MyBatis-Plus的Page对象使用records和total
    if (res && res.records) {
      recruitments.value = res.records
      pagination.total = res.total || 0
    } else if (Array.isArray(res)) {
      recruitments.value = res
      pagination.total = res.length
    } else {
      recruitments.value = []
      pagination.total = 0
    }
  } catch (error) {
    console.error('加载招募信息失败：', error)
    ElMessage.error(error.message || '加载失败')
  } finally {
    loading.value = false
  }
}

const handleReset = () => {
  filters.gameType = ''
  filters.rank = ''
  pagination.page = 1
  handleSearch()
}

const handlePublish = async () => {
  try {
    const valid = await formRef.value.validate()
    if (!valid) {
      ElMessage.warning('请填写完整信息')
      return
    }
  } catch (error) {
    ElMessage.warning('请检查表单填写')
    return
  }

  publishLoading.value = true
  try {
    console.log('发布招募数据：', form)
    const res = await publishRecruitment(form)
    console.log('发布招募响应：', res)
    
    ElMessage.success('招募发布成功')
    dialogVisible.value = false
    
    // 重置表单
    formRef.value.resetFields()
    Object.assign(form, {
      gameType: '',
      requiredRank: '',
      requiredPosition: '',
      description: '',
      maxMembers: 5
    })
    
    // 刷新列表
    handleSearch()
  } catch (error) {
    console.error('发布招募失败：', error)
    ElMessage.error(error.message || '发布失败')
  } finally {
    publishLoading.value = false
  }
}

const handleApply = async (recruitment) => {
  try {
    await ElMessageBox.confirm(
      `确定要申请加入"${recruitment.publisherName}"的队伍吗？`,
      '申请确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'info'
      }
    )
    
    await applyToRecruitment(recruitment.recruitmentId)
    ElMessage.success('申请已发送！等待队长审核')
  } catch (error) {
    if (error !== 'cancel') {
      console.error('申请失败：', error)
      ElMessage.error(error.message || '申请失败')
    }
  }
}

const handleDelete = async (recruitment) => {
  try {
    await ElMessageBox.confirm(
      '确定要删除这条招募信息吗？删除后无法恢复！',
      '删除确认',
      {
        confirmButtonText: '确定删除',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    await deleteRecruitment(recruitment.recruitmentId)
    ElMessage.success('招募已删除')
    handleSearch() // 刷新列表
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败：', error)
      ElMessage.error(error.message || '删除失败')
    }
  }
}

const getGameLabel = (value) => {
  const game = GAME_TYPES.find(g => g.value === value)
  return game?.label || value
}

const getRankLabel = (value) => {
  for (const ranks of Object.values(RANKS)) {
    const rank = ranks.find(r => r.value === value)
    if (rank) return rank.label
  }
  return value
}

// WebSocket连接和通知处理
const connectWebSocket = () => {
  const guestId = userStore.checkInInfo?.guestId || userStore.userInfo?.guestId
  if (!guestId) {
    console.warn('未找到用户ID，无法连接WebSocket')
    return
  }

  console.log('正在连接WebSocket，用户ID:', guestId)
  websocketService.connect(
    guestId,
    () => {
      console.log('WebSocket连接成功')
      ElMessage.success('实时通知已启用')
    },
    (error) => {
      console.error('WebSocket连接失败:', error)
    }
  )
}

const handleRecruitmentNotification = async (event) => {
  const notification = event.detail
  console.log('收到招募通知:', notification)

  if (notification.type === 'NEW_APPLICATION') {
    // 收到新申请通知（作为发布者）
    handleNewApplication(notification)
  } else if (notification.type === 'APPLICATION_RESULT') {
    // 收到申请结果通知（作为申请者）
    handleApplicationResult(notification)
  }
}

const handleNewApplication = (notification) => {
  const applicantInfo = notification.applicantRoom 
    ? `${notification.applicantName}（房间${notification.applicantRoom}）`
    : notification.applicantName

  ElMessageBox.confirm(
    `${applicantInfo} 申请加入您的 ${notification.gameType} 招募`,
    '新的申请',
    {
      confirmButtonText: '同意',
      cancelButtonText: '拒绝',
      type: 'info',
      distinguishCancelAndClose: true
    }
  ).then(async () => {
    // 同意申请
    try {
      await approveApplication(notification.recruitmentId, notification.applicantId)
      ElMessage.success('已同意申请，队员已加入战队')
      handleSearch() // 刷新列表
    } catch (error) {
      console.error('同意申请失败:', error)
      ElMessage.error(error.message || '操作失败')
    }
  }).catch(async (action) => {
    if (action === 'cancel') {
      // 拒绝申请
      try {
        await rejectApplication(notification.recruitmentId, notification.applicantId)
        ElMessage.info('已拒绝申请')
      } catch (error) {
        console.error('拒绝申请失败:', error)
        ElMessage.error(error.message || '操作失败')
      }
    }
  })
}

const handleApplicationResult = (notification) => {
  if (notification.approved) {
    ElNotification.success({
      title: '申请成功',
      message: notification.message || '您的申请已被同意，已加入战队！',
      duration: 5000
    })
    // 可以跳转到战队管理页面
    setTimeout(() => {
      ElMessageBox.confirm(
        '是否立即查看我的战队？',
        '提示',
        {
          confirmButtonText: '查看战队',
          cancelButtonText: '稍后',
          type: 'success'
        }
      ).then(() => {
        // 使用useRouter需要在setup顶层声明
        window.location.href = '/#/guest/team-management'
      }).catch(() => {})
    }, 1000)
  } else {
    ElNotification.warning({
      title: '申请被拒绝',
      message: notification.message || '很遗憾，您的申请未被通过',
      duration: 4000
    })
  }
}

onMounted(() => {
  handleSearch()
  connectWebSocket()
  
  // 监听招募通知事件
  window.addEventListener('recruitment-notification', handleRecruitmentNotification)
})

onBeforeUnmount(() => {
  // 清理事件监听器
  window.removeEventListener('recruitment-notification', handleRecruitmentNotification)
  
  // 断开WebSocket（可选，如果希望全局保持连接可以不断开）
  // websocketService.disconnect()
})
</script>

<style scoped lang="scss">
.online-lobby {
  padding: 20px;
  overflow-y: auto;
  max-height: calc(100vh - 120px);

  .quick-nav {
    margin: 20px 0;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    
    :deep(.el-card__body) {
      padding: 15px;
    }
  }

  .filter-bar {
    background: #fff;
    padding: 20px;
    margin: 20px 0;
    border-radius: 4px;
    box-shadow: 0 2px 4px rgba(0,0,0,0.1);
  }

  .recruitment-card {
    margin-bottom: 20px;
    transition: all 0.3s;
    height: 100%;

    &:hover {
      transform: translateY(-5px);
      box-shadow: 0 8px 16px rgba(0,0,0,0.15);
    }

    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      gap: 10px;

      .header-left {
        display: flex;
        gap: 10px;
        align-items: center;
      }
    }

    .recruitment-info {
      .info-row {
        display: flex;
        align-items: center;
        margin-bottom: 10px;
        font-size: 14px;

        .label {
          color: #909399;
          margin-right: 8px;
          min-width: 70px;
        }

        .value {
          color: #303133;
          font-weight: 500;
        }
      }

      .description {
        margin-top: 15px;
        padding: 10px;
        background: #f5f7fa;
        border-radius: 4px;
        min-height: 60px;
        color: #606266;
        font-size: 13px;
        line-height: 1.6;
      }
    }
  }
}
</style>
