<template>
  <div class="recruitment-page">
    <el-page-header @back="$router.back()" content="组队招募" />

    <el-tabs v-model="activeTab" class="tabs-container">
      <!-- 招募广场 -->
      <el-tab-pane label="招募广场" name="square">
        <el-button type="primary" @click="dialogVisible = true" style="margin-bottom: 20px;">
          <el-icon><Plus /></el-icon> 发布招募
        </el-button>

        <el-table :data="recruitments" v-loading="loading">
          <el-table-column label="游戏" width="100">
            <template #default="{ row }">
              <el-tag>{{ getGameLabel(row.gameType) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="段位要求" width="100">
            <template #default="{ row }">
              <el-tag type="warning">{{ getRankLabel(row.requiredRank) || '不限' }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="requiredPosition" label="位置" width="100">
            <template #default="{ row }">
              {{ row.requiredPosition || '不限' }}
            </template>
          </el-table-column>
          <el-table-column prop="description" label="描述" min-width="200" show-overflow-tooltip />
          <el-table-column prop="maxMembers" label="需要人数" width="100" />
          <el-table-column prop="status" label="状态" width="100">
            <template #default="{ row }">
              <el-tag :type="row.status === 'OPEN' ? 'success' : 'info'">
                {{ row.status === 'OPEN' ? '招募中' : '已关闭' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="publisherName" label="发布者" width="120" />
          <el-table-column label="操作" width="150">
            <template #default="{ row }">
              <el-button v-if="row.status === 'OPEN'" type="primary" size="small" @click="handleApply(row)">
                申请加入
              </el-button>
            </template>
          </el-table-column>
        </el-table>

        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.size"
          :total="pagination.total"
          @current-change="loadRecruitments"
          layout="total, prev, pager, next"
          style="margin-top: 20px; justify-content: center;"
        />
      </el-tab-pane>

      <!-- 我的招募 -->
      <el-tab-pane label="我的招募" name="my">
        <el-table :data="myRecruitments" v-loading="loading">
          <el-table-column label="游戏" width="100">
            <template #default="{ row }">
              <el-tag>{{ getGameLabel(row.gameType) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="maxMembers" label="需要人数" width="100" />
          <el-table-column prop="status" label="状态" width="100">
            <template #default="{ row }">
              <el-tag :type="row.status === 'OPEN' ? 'success' : 'info'">
                {{ row.status === 'OPEN' ? '招募中' : '已关闭' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="200">
            <template #default="{ row }">
              <el-button v-if="row.status === 'OPEN'" size="small" @click="handleClose(row)">关闭</el-button>
              <el-button type="danger" size="small" @click="handleDeleteRecruitment(row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>
    </el-tabs>

    <!-- 发布招募对话框 -->
    <el-dialog v-model="dialogVisible" title="发布招募" width="500px">
      <el-form :model="form" ref="formRef" label-width="100px">
        <el-form-item label="游戏类型" required>
          <el-select v-model="form.gameType" placeholder="选择游戏">
            <el-option v-for="game in GAME_TYPES" :key="game.value" :label="game.label" :value="game.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="段位要求">
          <el-select v-model="form.requiredRank" clearable placeholder="不限">
            <el-option v-for="rank in RANKS.DEFAULT" :key="rank.value" :label="rank.label" :value="rank.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="位置要求">
          <el-input v-model="form.requiredPosition" placeholder="如：中单、ADC（可选）" />
        </el-form-item>
        <el-form-item label="招募描述" required>
          <el-input 
            v-model="form.description" 
            type="textarea" 
            :rows="3"
            placeholder="描述你的招募需求，如：招募LOL钻石以上队友，擅长打野优先..." 
          />
        </el-form-item>
        <el-form-item label="需要人数" required>
          <el-input-number v-model="form.maxMembers" :min="2" :max="10" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handlePublish" :loading="loading">发布</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import {
  searchRecruitments,
  publishRecruitment,
  getMyRecruitments,
  closeRecruitment,
  deleteRecruitment,
  applyToRecruitment
} from '@/api/team'
import { GAME_TYPES, RANKS } from '@/api/gaming'

const activeTab = ref('square')
const recruitments = ref([])
const myRecruitments = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const formRef = ref(null)

const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

const form = reactive({
  gameType: 'LOL',
  requiredRank: '',
  requiredPosition: '',
  description: '',
  maxMembers: 2
})

const loadRecruitments = async () => {
  loading.value = true
  try {
    const res = await searchRecruitments({ page: pagination.page, size: pagination.size })
    // 响应拦截器已经提取了data
    if (res && res.records) {
      recruitments.value = res.records
      pagination.total = res.total
    }
  } catch (error) {
    ElMessage.error(error.message || '加载失败')
  } finally {
    loading.value = false
  }
}

const loadMyRecruitments = async () => {
  loading.value = true
  try {
    const res = await getMyRecruitments()
    // 响应拦截器已经提取了data，res直接是数组
    if (Array.isArray(res)) {
      myRecruitments.value = res
    }
  } catch (error) {
    ElMessage.error(error.message || '加载失败')
  } finally {
    loading.value = false
  }
}

const handlePublish = async () => {
  loading.value = true
  try {
    await publishRecruitment(form)
    ElMessage.success('招募发布成功')
    dialogVisible.value = false
    loadRecruitments()
    loadMyRecruitments()
  } catch (error) {
    ElMessage.error(error.message || '发布失败')
  } finally {
    loading.value = false
  }
}

const handleApply = async (row) => {
  try {
    await applyToRecruitment(row.recruitmentId)
    ElMessage.success('申请已发送')
  } catch (error) {
    ElMessage.error(error.message || '申请失败')
  }
}

const handleClose = async (row) => {
  try {
    await closeRecruitment(row.recruitmentId)
    ElMessage.success('招募已关闭')
    loadMyRecruitments()
  } catch (error) {
    ElMessage.error(error.message || '关闭失败')
  }
}

const handleDeleteRecruitment = async (row) => {
  try {
    await deleteRecruitment(row.recruitmentId)
    ElMessage.success('招募已删除')
    loadMyRecruitments()
  } catch (error) {
    ElMessage.error(error.message || '删除失败')
  }
}

const getGameLabel = (value) => GAME_TYPES.find(g => g.value === value)?.label || value
const getRankLabel = (value) => {
  for (const ranks of Object.values(RANKS)) {
    const rank = ranks.find(r => r.value === value)
    if (rank) return rank.label
  }
  return value
}

watch(activeTab, (newVal) => {
  if (newVal === 'square') {
    loadRecruitments()
  } else if (newVal === 'my') {
    loadMyRecruitments()
  }
})

onMounted(() => {
  loadRecruitments()
})
</script>

<style scoped lang="scss">
.recruitment-page {
  padding: 20px;

  .tabs-container {
    margin-top: 20px;
    background: #fff;
    padding: 20px;
    border-radius: 4px;
  }
}
</style>
