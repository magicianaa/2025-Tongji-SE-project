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
          <el-button type="warning" @click="generateAI" :loading="aiAnalysisLoading" :disabled="!reportData">
            <el-icon><ChatDotRound /></el-icon>
            AI生成财报分析
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- ECharts图表展示 -->
    <el-row :gutter="20" v-if="reportData" style="margin-top: 20px">
      <el-col :span="12">
        <el-card>
          <template #header>
            <span>收入结构分布</span>
          </template>
          <div ref="revenueChartRef" style="width: 100%; height: 300px"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card>
          <template #header>
            <span>订单量统计</span>
          </template>
          <div ref="orderChartRef" style="width: 100%; height: 300px"></div>
        </el-card>
      </el-col>
    </el-row>

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
          <el-button type="warning" @click="generateAI" :loading="aiAnalysisLoading" :disabled="!reportData">
            <el-icon><ChatDotRound /></el-icon>
            AI生成财报分析
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
            <div class="metric-label">总支出</div>
            <div class="metric-value danger">¥{{ reportData.totalExpense || 0 }}</div>
          </div>
        </el-col>
        <el-col :xs="24" :sm="12" :md="8" :lg="6">
          <div class="metric-item">
            <div class="metric-label">净利润</div>
            <div class="metric-value success">¥{{ reportData.netProfit || reportData.totalRevenue }}</div>
          </div>
        </el-col>
        <el-col :xs="24" :sm="12" :md="8" :lg="6">
          <div class="metric-item">
            <div class="metric-label">客房收入</div>
            <div class="metric-value">¥{{ reportData.roomRevenue }}</div>
          </div>
        </el-col>
      </el-row>

      <el-divider />

      <!-- 收入明细 -->
      <el-row :gutter="20" class="metrics-row">
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
        <el-col :xs="24" :sm="12" :md="8" :lg="6">
          <div class="metric-item">
            <div class="metric-label">进货成本</div>
            <div class="metric-value warning">¥{{ reportData.procurementCost || 0 }}</div>
          </div>
        </el-col>
        <el-col :xs="24" :sm="12" :md="8" :lg="6">
          <div class="metric-item">
            <div class="metric-label">维修成本</div>
            <div class="metric-value warning">¥{{ reportData.maintenanceCost || 0 }}</div>
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

    <!-- AI分析结果卡片 -->
    <el-card v-if="showAIAnalysis && aiAnalysis" class="ai-analysis-card" style="margin-top: 20px">
      <template #header>
        <div class="card-header">
          <span>💡 AI财报分析与经营建议</span>
          <el-button size="small" @click="showAIAnalysis = false">关闭</el-button>
        </div>
      </template>
      <div class="ai-analysis-content" v-html="renderedAnalysis"></div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, nextTick } from 'vue'
import { Search, Download, ChatDotRound } from '@element-plus/icons-vue'
import { getDailyReport, getMonthlyReport, exportFinancialReport, generateAIAnalysis } from '@/api/report'
import { ElMessage } from 'element-plus'
import * as echarts from 'echarts'
import MarkdownIt from 'markdown-it'

const md = new MarkdownIt()

const loading = ref(false)
const reportData = ref(null)
const revenueChartRef = ref(null)
const orderChartRef = ref(null)
let revenueChart = null
let orderChart = null

// AI分析相关
const aiAnalysisLoading = ref(false)
const aiAnalysis = ref('')
const showAIAnalysis = ref(false)

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
    
    // 等待DOM更新后渲染图表
    await nextTick()
    renderCharts()
  } catch (error) {
    console.error('加载报表失败:', error)
    ElMessage.error('加载报表失败：' + (error.message || '请检查网络连接'))
  } finally {
    loading.value = false
  }
}

// 渲染ECharts图表
const renderCharts = () => {
  if (!reportData.value) return
  
  // 渲染收入结构饼图
  if (revenueChartRef.value) {
    if (revenueChart) {
      revenueChart.dispose()
    }
    revenueChart = echarts.init(revenueChartRef.value)
    
    const revenueOption = {
      tooltip: {
        trigger: 'item',
        formatter: '{a} <br/>{b}: ¥{c} ({d}%)'
      },
      legend: {
        orient: 'vertical',
        left: 'left'
      },
      series: [
        {
          name: '收入来源',
          type: 'pie',
          radius: '50%',
          data: [
            { value: reportData.value.roomRevenue, name: '客房收入' },
            { value: reportData.value.posRevenue, name: 'POS收入' },
            { value: reportData.value.pointsRevenue, name: '积分收入' }
          ],
          emphasis: {
            itemStyle: {
              shadowBlur: 10,
              shadowOffsetX: 0,
              shadowColor: 'rgba(0, 0, 0, 0.5)'
            }
          }
        }
      ]
    }
    revenueChart.setOption(revenueOption)
  }
  
  // 渲染订单量柱状图
  if (orderChartRef.value) {
    if (orderChart) {
      orderChart.dispose()
    }
    orderChart = echarts.init(orderChartRef.value)
    
    const orderOption = {
      tooltip: {
        trigger: 'axis',
        axisPointer: {
          type: 'shadow'
        }
      },
      xAxis: {
        type: 'category',
        data: ['入住订单', 'POS订单']
      },
      yAxis: {
        type: 'value'
      },
      series: [
        {
          name: '订单量',
          type: 'bar',
          data: [reportData.value.checkInCount, reportData.value.posOrderCount],
          itemStyle: {
            color: function(params) {
              const colors = ['#5470c6', '#91cc75']
              return colors[params.dataIndex]
            }
          }
        }
      ]
    }
    orderChart.setOption(orderOption)
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

// 生成AI分析
const generateAI = async () => {
  if (!reportData.value) {
    ElMessage.warning('请先查询报表数据')
    return
  }

  try {
    aiAnalysisLoading.value = true
    const date = queryForm.value.reportType === 'DAILY' 
      ? queryForm.value.date 
      : `${queryForm.value.year}-${String(queryForm.value.month).padStart(2, '0')}-01`

    const response = await generateAIAnalysis(queryForm.value.reportType, date)
    aiAnalysis.value = response
    showAIAnalysis.value = true
    ElMessage.success('AI财报分析生成成功')
  } catch (error) {
    console.error('生成AI分析失败:', error)
    ElMessage.error('生成AI分析失败：' + (error.message || '请检查网络连接'))
  } finally {
    aiAnalysisLoading.value = false
  }
}

// 将Markdown渲染为HTML
const renderedAnalysis = computed(() => {
  return aiAnalysis.value ? md.render(aiAnalysis.value) : ''
})

// 初始加载今日数据
loadReport()
</script>

<style scoped>
.financial-report {
  padding: 20px;
  height: calc(100vh - 120px);
  overflow-y: auto;
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

.metric-value.danger {
  color: #f56c6c;
}

.metric-value.warning {
  color: #e6a23c;
}

@media (max-width: 768px) {
  .metric-value {
    font-size: 22px;

/* AI分析样式 */
.ai-analysis-card {
  margin-top: 20px;
}

.ai-analysis-content {
  line-height: 1.8;
  font-size: 15px;
}

.ai-analysis-content :deep(h1) {
  font-size: 24px;
  margin: 20px 0 10px;
  color: #303133;
  border-bottom: 2px solid #409eff;
  padding-bottom: 10px;
}

.ai-analysis-content :deep(h2) {
  font-size: 20px;
  margin: 18px 0 10px;
  color: #409eff;
}

.ai-analysis-content :deep(h3) {
  font-size: 18px;
  margin: 15px 0 8px;
  color: #606266;
}

.ai-analysis-content :deep(p) {
  margin: 10px 0;
  color: #606266;
}

.ai-analysis-content :deep(ul), 
.ai-analysis-content :deep(ol) {
  margin: 10px 0;
  padding-left: 25px;
}

.ai-analysis-content :deep(li) {
  margin: 5px 0;
  color: #606266;
}

.ai-analysis-content :deep(strong) {
  color: #303133;
  font-weight: 600;
}

.ai-analysis-content :deep(code) {
  background: #f5f7fa;
  padding: 2px 6px;
  border-radius: 3px;
  font-family: 'Courier New', monospace;
  color: #e6a23c;
}

.ai-analysis-content :deep(blockquote) {
  border-left: 4px solid #409eff;
  padding-left: 15px;
  margin: 15px 0;
  color: #909399;
  background: #f5f7fa;
  padding: 10px 15px;
}
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
