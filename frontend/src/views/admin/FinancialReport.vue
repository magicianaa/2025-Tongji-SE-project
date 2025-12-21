<template>
  <div class="financial-report">
    <el-page-header title="返回" content="财务报表" @back="$router.back()" />

    <el-card class="filter-card" style="margin-top: 20px">
      <el-form :inline="true" :model="queryForm" class="filter-form">
        <el-form-item label="报表类型">
          <el-radio-group v-model="queryForm.reportType" @change="handleReportTypeChange">
            <el-radio-button label="DAILY">日报</el-radio-button>
            <el-radio-button label="MONTHLY">月报</el-radio-button>
          </el-radio-group>
        </el-form-item>

        <el-form-item v-if="queryForm.reportType === 'DAILY'" label="选择日期">
          <el-date-picker
            v-model="queryForm.date"
            type="date"
            placeholder="选择日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
            @change="loadReport"
          />
        </el-form-item>

        <el-form-item v-else label="选择月份">
          <el-date-picker
            v-model="queryForm.monthDate"
            type="month"
            placeholder="选择月份"
            format="YYYY-MM"
            @change="handleMonthChange"
          />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="loadReport" :loading="loading">
            <el-icon><Search /></el-icon>
            查询
          </el-button>
          <el-button type="success" @click="exportReport">
            <el-icon><Download /></el-icon>
            导出CSV
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card v-if="reportData" class="report-card" style="margin-top: 20px">
      <template #header>
        <div class="card-header">
          <span>{{ reportTitle }}</span>
          <el-tag>{{ reportData.reportDate }}</el-tag>
        </div>
      </template>

      <!-- 核心指标 -->
      <el-row :gutter="20" class="metrics-row">
        <el-col :xs="24" :sm="12" :md="8" :lg="6">
          <div class="metric-item">
            <div class="metric-label">总收入</div>
            <div class="metric-value primary">¥{{ reportData.totalRevenue }}</div>
          </div>
        </el-col>
        <el-col :xs="24" :sm="12" :md="8" :lg="6">
          <div class="metric-item">
            <div class="metric-label">客房收入</div>
            <div class="metric-value">¥{{ reportData.roomRevenue }}</div>
          </div>
        </el-col>
        <el-col :xs="24" :sm="12" :md="8" :lg="6">
          <div class="metric-item">
            <div class="metric-label">POS收入</div>
            <div class="metric-value">¥{{ reportData.posRevenue }}</div>
          </div>
        </el-col>
        <el-col :xs="24" :sm="12" :md="8" :lg="6">
          <div class="metric-item">
            <div class="metric-label">积分收入</div>
            <div class="metric-value">¥{{ reportData.pointsRevenue }}</div>
          </div>
        </el-col>
      </el-row>

      <el-divider />

      <!-- 运营指标 -->
      <el-row :gutter="20" class="metrics-row">
        <el-col :xs="24" :sm="12" :md="8" :lg="6">
          <div class="metric-item">
            <div class="metric-label">入住订单数</div>
            <div class="metric-value">{{ reportData.checkInCount }}</div>
          </div>
        </el-col>
        <el-col :xs="24" :sm="12" :md="8" :lg="6">
          <div class="metric-item">
            <div class="metric-label">POS订单数</div>
            <div class="metric-value">{{ reportData.posOrderCount }}</div>
          </div>
        </el-col>
        <el-col :xs="24" :sm="12" :md="8" :lg="6">
          <div class="metric-item">
            <div class="metric-label">平均客单价</div>
            <div class="metric-value">¥{{ reportData.avgRoomPrice }}</div>
          </div>
        </el-col>
        <el-col :xs="24" :sm="12" :md="8" :lg="6">
          <div class="metric-item">
            <div class="metric-label">平均POS消费</div>
            <div class="metric-value">¥{{ reportData.avgPosConsumption }}</div>
          </div>
        </el-col>
      </el-row>

      <el-divider />

      <!-- 绩效指标 -->
      <el-row :gutter="20" class="metrics-row">
        <el-col :xs="24" :sm="12" :md="8">
          <div class="metric-item">
            <div class="metric-label">入住率</div>
            <div class="metric-value success">{{ reportData.occupancyRate }}%</div>
          </div>
        </el-col>
        <el-col :xs="24" :sm="12" :md="8">
          <div class="metric-item">
            <div class="metric-label">RevPAR（平均客房收益）</div>
            <div class="metric-value success">¥{{ reportData.revPAR }}</div>
          </div>
        </el-col>
        <el-col :xs="24" :sm="12" :md="8">
          <div class="metric-item">
            <div class="metric-label">报表类型</div>
            <div class="metric-value">
              <el-tag v-if="reportData.reportType === 'DAILY'" type="info">日报</el-tag>
              <el-tag v-else type="success">月报</el-tag>
            </div>
          </div>
        </el-col>
      </el-row>
    </el-card>

    <el-empty v-else description="请选择日期查询报表" />
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { Search, Download } from '@element-plus/icons-vue'
import { getDailyReport, getMonthlyReport, exportFinancialReport } from '@/api/report'
import { ElMessage } from 'element-plus'

const loading = ref(false)
const reportData = ref(null)

const queryForm = ref({
  reportType: 'DAILY',
  date: new Date().toISOString().split('T')[0],
  monthDate: new Date(),
  year: new Date().getFullYear(),
  month: new Date().getMonth() + 1
})

const reportTitle = computed(() => {
  if (queryForm.value.reportType === 'DAILY') {
    return '财务日报'
  }
  return '财务月报'
})

// 加载报表
const loadReport = async () => {
  loading.value = true
  try {
    let response
    if (queryForm.value.reportType === 'DAILY') {
      response = await getDailyReport(queryForm.value.date)
    } else {
      response = await getMonthlyReport(queryForm.value.year, queryForm.value.month)
    }

    reportData.value = response
  } catch (error) {
    console.error('加载报表失败:', error)
    ElMessage.error('加载报表失败：' + (error.message || '请检查网络连接'))
  } finally {
    loading.value = false
  }
}

// 报表类型切换
const handleReportTypeChange = () => {
  reportData.value = null
  if (queryForm.value.reportType === 'DAILY') {
    queryForm.value.date = new Date().toISOString().split('T')[0]
  }
}

// 月份选择变化
const handleMonthChange = (value) => {
  if (value) {
    queryForm.value.year = value.getFullYear()
    queryForm.value.month = value.getMonth() + 1
    loadReport()
  }
}

// 导出报表
const exportReport = async () => {
  try {
    const date = queryForm.value.reportType === 'DAILY' 
      ? queryForm.value.date 
      : `${queryForm.value.year}-${String(queryForm.value.month).padStart(2, '0')}-01`

    const blob = await exportFinancialReport(queryForm.value.reportType, date)
    
    // 创建下载链接
    const link = document.createElement('a')
    const url = URL.createObjectURL(blob)
    
    link.href = url
    link.download = `financial_report_${queryForm.value.reportType.toLowerCase()}_${date}.csv`
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    
    URL.revokeObjectURL(url)
    ElMessage.success('报表导出成功')
  } catch (error) {
    console.error('导出报表失败:', error)
    ElMessage.error('导出报表失败：' + (error.message || '请检查网络连接'))
  }
}

// 初始加载今日数据
loadReport()
</script>

<style scoped>
.financial-report {
  padding: 20px;
}

.filter-card {
  margin-bottom: 20px;
}

.filter-form {
  margin: 0;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: 600;
}

.metrics-row {
  margin-bottom: 10px;
}

.metric-item {
  padding: 20px;
  text-align: center;
  background: #f5f7fa;
  border-radius: 8px;
  transition: all 0.3s;
  height: 100%;
}

.metric-item:hover {
  background: #ecf5ff;
  transform: translateY(-2px);
}

.metric-label {
  font-size: 14px;
  color: #909399;
  margin-bottom: 10px;
}

.metric-value {
  font-size: 28px;
  font-weight: bold;
  color: #303133;
}

.metric-value.primary {
  color: #409eff;
  font-size: 36px;
}

.metric-value.success {
  color: #67c23a;
}

@media (max-width: 768px) {
  .metric-value {
    font-size: 22px;
  }
  
  .metric-value.primary {
    font-size: 28px;
  }
  
  .filter-form .el-form-item {
    margin-right: 0;
    width: 100%;
  }
}
</style>
