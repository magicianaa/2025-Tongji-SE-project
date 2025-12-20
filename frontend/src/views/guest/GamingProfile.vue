<template>
  <div class="gaming-profile-page">
    <el-page-header @back="$router.back()" content="游戏档案管理" />
    
    <div class="content-container">
      <!-- 游戏选择标签页 -->
      <el-tabs v-model="activeGame" @tab-change="handleGameTabChange" class="game-tabs">
        <el-tab-pane 
          v-for="game in GAME_TYPES" 
          :key="game.value" 
          :label="game.label" 
          :name="game.value"
        >
          <!-- 当前游戏档案展示 -->
          <el-card v-if="currentProfiles[game.value]" class="profile-card" shadow="hover">
            <template #header>
              <div class="card-header">
                <span>我的{{ game.label }}档案</span>
                <el-button type="primary" size="small" @click="handleEdit(game.value)">编辑档案</el-button>
              </div>
            </template>
            
            <el-descriptions :column="2" border>
              <el-descriptions-item label="游戏账号">
                {{ currentProfiles[game.value].gameAccount || '未设置' }}
              </el-descriptions-item>
              <el-descriptions-item label="段位">
                <el-tag type="warning">{{ getRankLabel(currentProfiles[game.value].rank, game.value) }}</el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="擅长位置">
                {{ getPositionLabel(currentProfiles[game.value].preferredPosition, game.value) }}
              </el-descriptions-item>
              <el-descriptions-item label="游戏风格">
                {{ getStyleLabel(currentProfiles[game.value].playStyle) }}
              </el-descriptions-item>
              <el-descriptions-item label="组队状态">
                <el-tag :type="currentProfiles[game.value].isLookingForTeam ? 'success' : 'info'">
                  {{ currentProfiles[game.value].isLookingForTeam ? '寻找队友中' : '暂不组队' }}
                </el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="创建时间">
                {{ formatDate(currentProfiles[game.value].createdAt) }}
              </el-descriptions-item>
            </el-descriptions>

            <div class="action-buttons">
              <el-button @click="handleRecommend(game.value)">智能推荐队友</el-button>
              <el-button type="danger" plain @click="handleDelete(game.value)">删除档案</el-button>
            </div>
          </el-card>

          <!-- 创建档案提示 -->
          <el-empty v-else :description="`您还没有创建${game.label}档案`">
            <el-button type="primary" @click="handleCreate(game.value)">创建{{ game.label }}档案</el-button>
          </el-empty>
        </el-tab-pane>
      </el-tabs>

      <!-- 推荐队友列表 -->
      <el-card v-if="recommendations.length > 0" class="recommend-card" shadow="hover">
        <template #header>
          <div class="card-header">
            <span>为您推荐的队友（匹配度排序）</span>
          </div>
        </template>
        
        <el-row :gutter="16">
          <el-col v-for="player in recommendations" :key="player.profileId" :span="8">
            <el-card class="player-card" shadow="hover">
              <div class="player-info">
                <div class="player-name">{{ player.guestName }}</div>
                <div class="player-detail">
                  <el-tag size="small" type="warning">{{ getRankLabel(player.rank, activeGame) }}</el-tag>
                  <span class="position">{{ getPositionLabel(player.preferredPosition, activeGame) }}</span>
                </div>
                <div class="player-style">风格：{{ getStyleLabel(player.playStyle) }}</div>
                <div class="player-room">房间：{{ player.roomNumber }}</div>
                <el-button type="primary" size="small" class="contact-btn" @click="handleContact(player)">
                  联系TA
                </el-button>
              </div>
            </el-card>
          </el-col>
        </el-row>
      </el-card>
    </div>

    <!-- 创建/编辑档案对话框 -->
    <el-dialog 
      v-model="dialogVisible" 
      :title="isEdit ? '编辑游戏档案' : '创建游戏档案'"
      width="600px"
    >
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="游戏类型" prop="gameType">
          <el-select v-model="form.gameType" placeholder="选择游戏" @change="handleGameChange">
            <el-option 
              v-for="game in GAME_TYPES" 
              :key="game.value" 
              :label="game.label" 
              :value="game.value" 
            />
          </el-select>
        </el-form-item>

        <el-form-item label="游戏账号" prop="gameAccount">
          <el-input v-model="form.gameAccount" placeholder="请输入游戏内账号名" />
        </el-form-item>

        <el-form-item label="段位" prop="rank">
          <el-select v-model="form.rank" placeholder="选择段位">
            <el-option 
              v-for="rank in currentRanks" 
              :key="rank.value" 
              :label="rank.label" 
              :value="rank.value" 
            />
          </el-select>
        </el-form-item>

        <el-form-item label="擅长位置" prop="preferredPosition">
          <el-select v-model="form.preferredPosition" placeholder="选择位置">
            <el-option 
              v-for="pos in currentPositions" 
              :key="pos.value" 
              :label="pos.label" 
              :value="pos.value" 
            />
          </el-select>
        </el-form-item>

        <el-form-item label="游戏风格" prop="playStyle">
          <el-select v-model="form.playStyle" placeholder="选择风格">
            <el-option 
              v-for="style in PLAY_STYLES" 
              :key="style.value" 
              :label="style.label" 
              :value="style.value" 
            />
          </el-select>
        </el-form-item>

        <el-form-item label="组队状态">
          <el-switch 
            v-model="form.isLookingForTeam"
            active-text="寻找队友中"
            inactive-text="暂不组队"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="loading">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  getCurrentProfile, 
  createOrUpdateProfile, 
  deleteProfile,
  recommendTeammates,
  GAME_TYPES, 
  RANKS, 
  POSITIONS, 
  PLAY_STYLES 
} from '@/api/gaming'

// 当前激活的游戏标签
const activeGame = ref('LOL')
// 所有游戏的档案（key为gameType）
const currentProfiles = ref({})
const recommendations = ref([])
const dialogVisible = ref(false)
const isEdit = ref(false)
const loading = ref(false)
const formRef = ref(null)

const form = reactive({
  gameType: 'LOL',
  gameAccount: '',
  rank: '',
  preferredPosition: '',
  playStyle: '',
  isLookingForTeam: true
})

const rules = {
  gameType: [{ required: true, message: '请选择游戏类型', trigger: 'change' }],
  rank: [{ required: true, message: '请选择段位', trigger: 'change' }],
  preferredPosition: [{ required: true, message: '请选择擅长位置', trigger: 'change' }],
  playStyle: [{ required: true, message: '请选择游戏风格', trigger: 'change' }]
}

const currentRanks = computed(() => {
  return RANKS[form.gameType] || RANKS.DEFAULT
})

const currentPositions = computed(() => {
  return POSITIONS[form.gameType] || POSITIONS.DEFAULT
})

// 加载所有游戏的档案
const loadAllProfiles = async () => {
  for (const game of GAME_TYPES) {
    try {
      const res = await getCurrentProfile(game.value)
      console.log(`查询${game.label}档案响应：`, res)
      // 响应拦截器已经提取了data
      if (res && res.profileId) {
        currentProfiles.value[game.value] = res
      }
    } catch (error) {
      console.log(`暂无${game.label}档案`, error)
    }
  }
}

// 切换游戏标签
const handleGameTabChange = (gameType) => {
  console.log('切换到游戏：', gameType)
  recommendations.value = [] // 清空推荐列表
}

// 处理游戏类型变化
const handleGameChange = () => {
  form.rank = ''
  form.preferredPosition = ''
}

// 创建新档案
const handleCreate = (gameType) => {
  isEdit.value = false
  form.gameType = gameType
  form.gameAccount = ''
  form.rank = ''
  form.preferredPosition = ''
  form.playStyle = ''
  form.isLookingForTeam = true
  dialogVisible.value = true
}

// 编辑档案
const handleEdit = (gameType) => {
  isEdit.value = true
  const profile = currentProfiles.value[gameType]
  Object.assign(form, {
    gameType: profile.gameType,
    gameAccount: profile.gameAccount || '',
    rank: profile.rank,
    preferredPosition: profile.preferredPosition,
    playStyle: profile.playStyle,
    isLookingForTeam: profile.isLookingForTeam
  })
  dialogVisible.value = true
}

// 提交表单
const handleSubmit = async () => {
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

  loading.value = true
  try {
    console.log('提交表单数据：', form)
    const res = await createOrUpdateProfile(form)
    console.log('API响应：', res)
    
    // 响应拦截器已经提取了data，res直接是档案对象
    if (res && res.profileId) {
      ElMessage.success(isEdit.value ? '档案更新成功' : '档案创建成功')
      dialogVisible.value = false
      // 重新加载该游戏的档案
      currentProfiles.value[form.gameType] = res
    } else {
      ElMessage.error('操作失败')
    }
  } catch (error) {
    console.error('提交失败：', error)
    ElMessage.error(error.message || '操作失败')
  } finally {
    loading.value = false
  }
}

// 删除档案
const handleDelete = async (gameType) => {
  try {
    await ElMessageBox.confirm(`确定要删除${getGameLabel(gameType)}档案吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const profile = currentProfiles.value[gameType]
    await deleteProfile(profile.profileId)
    ElMessage.success('档案删除成功')
    delete currentProfiles.value[gameType]
    recommendations.value = []
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '删除失败')
    }
  }
}

// 智能推荐队友
const handleRecommend = async (gameType) => {
  try {
    const res = await recommendTeammates(gameType, 6)
    // 响应拦截器已经提取了data，res直接是数组
    if (Array.isArray(res)) {
      recommendations.value = res
      if (recommendations.value.length === 0) {
        ElMessage.info('暂无合适的队友推荐')
      } else {
        ElMessage.success(`为您推荐了 ${recommendations.value.length} 位队友`)
      }
    } else {
      recommendations.value = []
      ElMessage.info('暂无合适的队友推荐')
    }
  } catch (error) {
    ElMessage.error(error.message || '推荐失败')
  }
}

// 联系玩家
const handleContact = (player) => {
  ElMessageBox.alert(
    `玩家：${player.guestName}\n房间：${player.roomNumber}\n您可以前往TA的房间邀请组队！`,
    '联系信息',
    { confirmButtonText: '好的' }
  )
}

// 辅助函数
const getGameLabel = (value) => {
  const game = GAME_TYPES.find(g => g.value === value)
  return game?.label || value
}

const getRankLabel = (value, gameType) => {
  const ranks = RANKS[gameType] || RANKS.DEFAULT
  const rank = ranks.find(r => r.value === value)
  return rank?.label || value
}

const getPositionLabel = (value, gameType) => {
  const positions = POSITIONS[gameType] || POSITIONS.DEFAULT
  const pos = positions.find(p => p.value === value)
  return pos?.label || value
}

const getStyleLabel = (value) => {
  const style = PLAY_STYLES.find(s => s.value === value)
  return style?.label || value
}

const formatDate = (dateStr) => {
  if (!dateStr) return ''
  return new Date(dateStr).toLocaleString('zh-CN')
}

onMounted(() => {
  loadAllProfiles()
})

onMounted(() => {
  loadAllProfiles()
})
</script>

<style scoped lang="scss">
.gaming-profile-page {
  padding: 20px;

  .content-container {
    margin-top: 20px;

    .game-tabs {
      :deep(.el-tabs__nav-wrap) {
        padding: 0 20px;
      }
    }
  }

  .profile-card, .recommend-card {
    margin-bottom: 20px;

    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      font-weight: bold;
    }

    .action-buttons {
      margin-top: 20px;
      display: flex;
      gap: 10px;
    }
  }

  .player-card {
    margin-bottom: 16px;
    cursor: pointer;
    transition: transform 0.2s;

    &:hover {
      transform: translateY(-5px);
    }

    .player-info {
      text-align: center;

      .player-name {
        font-size: 18px;
        font-weight: bold;
        margin-bottom: 10px;
      }

      .player-detail {
        display: flex;
        justify-content: center;
        align-items: center;
        gap: 10px;
        margin-bottom: 8px;

        .position {
          font-size: 14px;
          color: #606266;
        }
      }

      .player-style, .player-room {
        font-size: 13px;
        color: #909399;
        margin: 5px 0;
      }

      .contact-btn {
        margin-top: 10px;
        width: 100%;
      }
    }
  }
}
</style>
