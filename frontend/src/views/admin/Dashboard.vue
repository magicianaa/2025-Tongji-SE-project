<template>
  <div class="admin-dashboard">
    <el-row :gutter="20" class="stats-row">
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <el-icon class="stat-icon revenue"><Money /></el-icon>
            <div class="stat-info">
              <div class="stat-label">今日营收</div>
              <div class="stat-value">¥{{ todayRevenue.toLocaleString() }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <el-icon class="stat-icon occupancy"><House /></el-icon>
            <div class="stat-info">
              <div class="stat-label">入住率</div>
              <div class="stat-value">{{ occupancyRate }}%</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <el-icon class="stat-icon orders"><ShoppingCart /></el-icon>
            <div class="stat-info">
              <div class="stat-label">今日订单</div>
              <div class="stat-value">{{ todayOrders }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <el-icon class="stat-icon guests"><User /></el-icon>
            <div class="stat-info">
              <div class="stat-label">在住客人</div>
              <div class="stat-value">{{ currentGuests }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="charts-row">
      <el-col :span="16">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>营收趋势（近7天）</span>
            </div>
          </template>
          <div class="chart-placeholder">
            <el-empty description="图表开发中，预留ECharts集成位置" />
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>房间状态分布</span>
            </div>
          </template>
          <div class="chart-placeholder">
            <el-empty description="饼图开发中" />
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20">
      <el-col :span="12">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>待办事项</span>
              <el-badge :value="pendingTasks" class="item" />
            </div>
          </template>
          <el-empty description="暂无待办事项" />
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>最新预订</span>
            </div>
          </template>
          <el-empty description="暂无新预订" />
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { Money, House, ShoppingCart, User } from '@element-plus/icons-vue'

const todayRevenue = ref(12580)
const occupancyRate = ref(78)
const todayOrders = ref(45)
const currentGuests = ref(32)
const pendingTasks = ref(5)

onMounted(() => {
  // TODO: 从API获取实时数据
  console.log('Admin Dashboard mounted')
})
</script>

<style scoped>
.admin-dashboard {
  padding: 20px;
}

.stats-row {
  margin-bottom: 20px;
}

.stat-card {
  border-radius: 8px;
  transition: transform 0.3s;
}

.stat-card:hover {
  transform: translateY(-5px);
}

.stat-content {
  display: flex;
  align-items: center;
  gap: 15px;
}

.stat-icon {
  font-size: 48px;
  padding: 10px;
  border-radius: 8px;
}

.stat-icon.revenue {
  color: #67c23a;
  background-color: #f0f9ff;
}

.stat-icon.occupancy {
  color: #409eff;
  background-color: #ecf5ff;
}

.stat-icon.orders {
  color: #e6a23c;
  background-color: #fdf6ec;
}

.stat-icon.guests {
  color: #f56c6c;
  background-color: #fef0f0;
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
  font-size: 24px;
  font-weight: bold;
  color: #303133;
}

.charts-row {
  margin-bottom: 20px;
}

.chart-placeholder {
  height: 300px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
