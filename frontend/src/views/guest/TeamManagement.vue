<template>
  <div class="team-management">
    <el-page-header @back="$router.back()" content="战队管理" />

    <div class="content-container">
      <!-- 当前战队 -->
      <el-card v-if="myTeam" class="team-card" shadow="hover">
        <template #header>
          <div class="card-header">
            <span>我的战队：{{ myTeam.teamName }}</span>
            <el-tag :type="myTeam.status === 'ACTIVE' ? 'success' : 'info'">
              {{ myTeam.status === 'ACTIVE' ? '活跃' : '已解散' }}
            </el-tag>
          </div>
        </template>

        <el-descriptions :column="2" border>
          <el-descriptions-item label="游戏类型">
            <el-tag>{{ getGameLabel(myTeam.gameType) }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="当前人数">{{ myTeam.currentMembers }} / {{ myTeam.maxMembers }}</el-descriptions-item>
          <el-descriptions-item label="队长">{{ myTeam.captainName }}</el-descriptions-item>
          <el-descriptions-item label="总游戏时长">{{ myTeam.totalPlaytimeMinutes }} 分钟</el-descriptions-item>
          <el-descriptions-item label="创建时间" :span="2">{{ formatDate(myTeam.createdAt) }}</el-descriptions-item>
        </el-descriptions>

        <el-divider />

        <div class="members-section">
          <h4>成员列表</h4>
          <el-table :data="myTeam.members">
            <el-table-column prop="guestName" label="玩家" />
            <el-table-column prop="isCaptain" label="角色" width="100">
              <template #default="{ row }">
                <el-tag v-if="row.isCaptain" type="success" size="small">队长</el-tag>
                <el-tag v-else size="small">成员</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="row.status === 'ACTIVE' ? 'success' : 'info'" size="small">
                  {{ row.status === 'ACTIVE' ? '活跃' : '已离队' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="200">
              <template #default="{ row }">
                <el-button v-if="isCaptain && !row.isCaptain" type="danger" size="small" @click="handleKick(row)">
                  踢出
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>

        <div class="team-actions">
          <el-button v-if="!isCaptain" type="warning" @click="handleLeave">离开战队</el-button>
          <el-button v-if="isCaptain" type="danger" @click="handleDisband">解散战队</el-button>
          <el-button v-if="isCaptain" @click="playtimeDialogVisible = true">更新时长</el-button>
        </div>
      </el-card>

      <!-- 创建战队提示 -->
      <el-empty v-else description="您还没有加入任何战队">
        <el-button type="primary" @click="createDialogVisible = true">创建战队</el-button>
      </el-empty>
    </div>

    <!-- 创建战队对话框 -->
    <el-dialog v-model="createDialogVisible" title="创建战队" width="500px">
      <el-form :model="createForm" ref="createFormRef" label-width="100px">
        <el-form-item label="战队名称" required>
          <el-input v-model="createForm.teamName" placeholder="请输入战队名称" />
        </el-form-item>
        <el-form-item label="游戏类型" required>
          <el-select v-model="createForm.gameType" placeholder="选择游戏">
            <el-option v-for="game in GAME_TYPES" :key="game.value" :label="game.label" :value="game.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="最大人数" required>
          <el-input-number v-model="createForm.maxMembers" :min="2" :max="10" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleCreate" :loading="loading">创建</el-button>
      </template>
    </el-dialog>

    <!-- 更新时长对话框 -->
    <el-dialog v-model="playtimeDialogVisible" title="更新游戏时长" width="400px">
      <el-form label-width="120px">
        <el-form-item label="增加时长(分钟)">
          <el-input-number v-model="additionalMinutes" :min="1" :max="1440" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="playtimeDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleUpdatePlaytime" :loading="loading">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/stores/user'
import {
  getMyTeam,
  createTeam,
  leaveTeam,
  kickMember,
  disbandTeam,
  updatePlaytime
} from '@/api/team'
import { GAME_TYPES } from '@/api/gaming'

const userStore = useUserStore()
const myTeam = ref(null)
const loading = ref(false)
const createDialogVisible = ref(false)
const playtimeDialogVisible = ref(false)
const additionalMinutes = ref(60)
const createFormRef = ref(null)

const createForm = reactive({
  teamName: '',
  gameType: 'LOL',
  maxMembers: 5
})

const isCaptain = computed(() => {
  const currentGuestId = userStore.checkInInfo?.guestId || userStore.userInfo?.guestId
  console.log('当前用户ID:', currentGuestId, '队长ID:', myTeam.value?.captainId)
  return myTeam.value?.captainId === currentGuestId
})

const loadMyTeam = async () => {
  try {
    const res = await getMyTeam()
    // 响应拦截器已经提取了data
    if (res && res.teamId) {
      myTeam.value = res
    }
  } catch (error) {
    console.log('暂无战队')
  }
}

const handleCreate = async () => {
  loading.value = true
  try {
    await createTeam(createForm)
    ElMessage.success('战队创建成功')
    createDialogVisible.value = false
    await loadMyTeam()
  } catch (error) {
    ElMessage.error(error.message || '创建失败')
  } finally {
    loading.value = false
  }
}

const handleLeave = async () => {
  try {
    await ElMessageBox.confirm('确定要离开战队吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    await leaveTeam(myTeam.value.teamId)
    ElMessage.success('已离开战队')
    myTeam.value = null
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '操作失败')
    }
  }
}

const handleKick = async (member) => {
  try {
    await ElMessageBox.confirm(`确定要踢出 ${member.guestName} 吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    await kickMember(myTeam.value.teamId, member.guestId)
    ElMessage.success('成员已被踢出')
    await loadMyTeam()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('踢出失败:', error)
      ElMessage.error(error.message || '操作失败')
    }
  }
}

const handleDisband = async () => {
  try {
    await ElMessageBox.confirm('确定要解散战队吗？此操作不可恢复！', '警告', {
      confirmButtonText: '确定解散',
      cancelButtonText: '取消',
      type: 'error'
    })

    await disbandTeam(myTeam.value.teamId)
    ElMessage.success('战队已解散')
    myTeam.value = null
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '操作失败')
    }
  }
}

const handleUpdatePlaytime = async () => {
  loading.value = true
  try {
    await updatePlaytime(myTeam.value.teamId, additionalMinutes.value)
    ElMessage.success('时长更新成功')
    playtimeDialogVisible.value = false
    await loadMyTeam()
  } catch (error) {
    ElMessage.error(error.message || '更新失败')
  } finally {
    loading.value = false
  }
}

const getGameLabel = (value) => GAME_TYPES.find(g => g.value === value)?.label || value
const formatDate = (dateStr) => {
  if (!dateStr) return ''
  return new Date(dateStr).toLocaleString('zh-CN')
}

onMounted(() => {
  loadMyTeam()
})
</script>

<style scoped lang="scss">
.team-management {
  padding: 20px;
  overflow-y: auto;
  max-height: calc(100vh - 120px);

  .content-container {
    margin-top: 20px;
  }

  .team-card {
    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      font-weight: bold;
    }

    .members-section {
      margin: 20px 0;

      h4 {
        margin-bottom: 15px;
      }
    }

    .team-actions {
      margin-top: 20px;
      display: flex;
      gap: 10px;
      justify-content: flex-end;
    }
  }
}
</style>
