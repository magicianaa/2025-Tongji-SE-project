<template>
  <div class="admin-dashboard">
    <el-page-header title="管理控制台" content="运营看板" style="margin-bottom: 20px" />
    
    <!-- 核心指标卡片 -->
    <el-row :gutter="20" class="stats-row">
      <el-col :xs="12" :sm="8" :md="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <el-icon class="stat-icon occupancy"><House /></el-icon>
            <div class="stat-info">
              <div class="stat-label">入住率</div>
              <div class="stat-value">{{ dashboardStats.occupancyRate || 0 }}%</div>
              <div class="stat-detail">
                {{ dashboardStats.occupiedRooms || 0 }}/{{ dashboardStats.totalRooms || 0 }} 间
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :xs="12" :sm="8" :md="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <el-icon class="stat-icon revenue"><TrendCharts /></el-icon>
            <div class="stat-info">
              <div class="stat-label">RevPAR</div>
              <div class="stat-value">¥{{ dashboardStats.revPAR || 0 }}</div>
              <div class="stat-detail">平均客房收益</div>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :xs="12" :sm="8" :md="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <el-icon class="stat-icon alert"><BellFilled /></el-icon>
            <div class="stat-info">
              <div class="stat-label">待处理报警</div>
              <div class="stat-value warning-text">{{ dashboardStats.pendingAlerts || 0 }}</div>
              <div class="stat-detail">
                维修工单 {{ dashboardStats.pendingMaintenanceTickets || 0 }}
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :xs="12" :sm="8" :md="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <el-icon class="stat-icon money"><Money /></el-icon>
            <div class="stat-info">
              <div class="stat-label">本月营收</div>
              <div class="stat-value">¥{{ dashboardStats.monthlyRevenue || 0 }}</div>
              <div class="stat-detail">
                订单 {{ dashboardStats.monthlyOrders || 0 }} 笔
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 今日数据 -->
    <el-row :gutter="20" class="stats-row">
      <el-col :xs="12" :sm="8" :md="6">
        <el-card shadow="hover" class="stat-card secondary">
          <div class="stat-content">
            <el-icon class="stat-icon"><Upload /></el-icon>
            <div class="stat-info">
              <div class="stat-label">今日入住</div>
              <div class="stat-value">{{ dashboardStats.todayCheckIns || 0 }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :xs="12" :sm="8" :md="6">
        <el-card shadow="hover" class="stat-card secondary">
          <div class="stat-content">
            <el-icon class="stat-icon"><Download /></el-icon>
            <div class="stat-info">
              <div class="stat-label">今日退房</div>
              <div class="stat-value">{{ dashboardStats.todayCheckOuts || 0 }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :xs="12" :sm="8" :md="6">
        <el-card shadow="hover" class="stat-card secondary">
          <div class="stat-content">
            <el-icon class="stat-icon"><User /></el-icon>
            <div class="stat-info">
              <div class="stat-label">活跃会员</div>
              <div class="stat-value">{{ dashboardStats.activeMembers || 0 }}</div>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :xs="12" :sm="8" :md="6">
        <el-card shadow="hover" class="stat-card secondary">
          <div class="stat-content">
            <el-icon class="stat-icon"><Refresh /></el-icon>
            <div class="stat-info">
              <div class="stat-label">最后更新</div>
              <div class="stat-value small">{{ lastUpdateTime }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 快捷入口 -->
    <el-row :gutter="20" class="quick-actions">
      <el-col :span="24">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>快捷入口</span>
            </div>
          </template>
          <div class="action-buttons">
            <el-button type="primary" @click="$router.push('/admin/financial-report')">
              <el-icon><Document /></el-icon>
              财务报表
            </el-button>
            <el-button type="success" @click="$router.push('/admin/hardware-analysis')">
              <el-icon><Monitor /></el-icon>
              硬件分析
            </el-button>
            <el-button type="warning" @click="refreshDashboard">
              <el-icon><Refresh /></el-icon>
              刷新数据
            </el-button>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { 
  Money, House, User, TrendCharts, BellFilled, 
  Upload, Download, Refresh, Document, Monitor 
} from '@element-plus/icons-vue'
import { getDashboardStats } from '@/api/report'
import { ElMessage } from 'element-plus'

const dashboardStats = ref({})
const loading = ref(false)

// 计算最后更新时间
const lastUpdateTime = computed(() => {
  const now = new Date()
  return now.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
})

// 加载看板数据
const loadDashboardData = async () => {
  loading.value = true
  try {
    const data = await getDashboardStats()
    dashboardStats.value = data
  } catch (error) {
    console.error('加载看板数据失败:', error)
    ElMessage.error('加载看板数据失败：' + (error.message || '请检查网络连接'))
  } finally {
    loading.value = false
  }
}

// 刷新看板
const refreshDashboard = () => {
  loadDashboardData()
  ElMessage.success('数据已刷新')
}

onMounted(() => {
  loadDashboardData()
  // 每30秒自动刷新一次
  const interval = setInterval(loadDashboardData, 30000)
  
  // 组件卸载时清除定时器
  return () => clearInterval(interval)
})
</script>

<style scoped>
.admin-dashboard {
  padding: 20px;
  height: calc(100vh - 120px);
  overflow-y: auto;
}

.stats-row {
  margin-bottom: 20px;
}

.stat-card {
  border-radius: 8px;
  transition: transform 0.3s;
  height: 100%;
}

.stat-card:hover {
  transform: translateY(-5px);
}

.stat-card.secondary {
  background: linear-gradient(135deg, #f5f7fa 0%, #ffffff 100%);
}

.stat-content {
  display: flex;
  align-items: center;
  gap: 15px;
  padding: 10px 0;
}

.stat-icon {
  font-size: 42px;
  padding: 12px;
  border-radius: 12px;
  color: #409eff;
  background-color: #ecf5ff;
}

.stat-icon.occupancy {
  color: #409eff;
  background-color: #ecf5ff;
}

.stat-icon.revenue {
  color: #67c23a;
  background-color: #f0f9ff;
}

.stat-icon.alert {
  color: #f56c6c;
  background-color: #fef0f0;
}

.stat-icon.money {
  color: #e6a23c;
  background-color: #fdf6ec;
}

.stat-info {
  flex: 1;
}

.stat-label {
  font-size: 14px;
  color: #909399;
  margin-bottom: 8px;
}

.stat-value {
  font-size: 28px;
  font-weight: bold;
  color: #303133;
  line-height: 1.2;
}

.stat-value.small {
  font-size: 16px;
  font-weight: normal;
}

.stat-value.warning-text {
  color: #f56c6c;
}

.stat-detail {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}

.quick-actions {
  margin-top: 20px;
}

.action-buttons {
  display: flex;
  gap: 15px;
  flex-wrap: wrap;
}

.action-buttons .el-button {
  flex: 1;
  min-width: 140px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: 600;
}

.chart-placeholder {
  height: 300px;
  display: flex;
  align-items: center;
  justify-content: center;
}

/* 响应式布局 */
@media (max-width: 768px) {
  .stat-value {
    font-size: 22px;
  }
  
  .stat-icon {
    font-size: 36px;
    padding: 8px;
  }
  
  .action-buttons .el-button {
    flex: 1 1 100%;
  }
}
</style>
