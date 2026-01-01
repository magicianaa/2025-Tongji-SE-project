<template>
  <div class="hardware-analysis">
    <el-page-header title="返回" content="硬件损耗分析与采购预测" @back="$router.back()" />

    <el-card class="filter-card" style="margin-top: 20px">
      <el-form :inline="true" :model="queryForm" class="filter-form">
        <el-form-item label="分析时间范围">
          <el-select v-model="queryForm.days" @change="loadAnalysis">
            <el-option label="最近7天" :value="7" />
            <el-option label="最近15天" :value="15" />
            <el-option label="最近30天" :value="30" />
            <el-option label="最近60天" :value="60" />
            <el-option label="最近90天" :value="90" />
          </el-select>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="loadAnalysis" :loading="loading">
            <el-icon><Search /></el-icon>
            分析
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <div v-if="analysisData">
      <!-- 概览卡片 -->
      <el-row :gutter="20" style="margin-top: 20px">
        <el-col :xs="24" :sm="12" :md="8">
          <el-card class="stat-card">
            <el-statistic title="总维修工单数" :value="analysisData.totalMaintenanceTickets">
              <template #prefix>
                <el-icon><Tools /></el-icon>
              </template>
            </el-statistic>
          </el-card>
        </el-col>
        <el-col :xs="24" :sm="12" :md="8">
          <el-card class="stat-card">
            <el-statistic title="设备类型数" :value="analysisData.analysisItems?.length || 0">
              <template #prefix>
                <el-icon><Monitor /></el-icon>
              </template>
            </el-statistic>
          </el-card>
        </el-col>
        <el-col :xs="24" :sm="12" :md="8">
          <el-card class="stat-card">
            <el-statistic 
              title="建议采购项" 
              :value="analysisData.purchaseRecommendations?.length || 0"
            >
              <template #prefix>
                <el-icon><ShoppingCart /></el-icon>
              </template>
            </el-statistic>
          </el-card>
        </el-col>
      </el-row>

      <!-- TOP3高频故障设备 -->
      <el-card style="margin-top: 20px" v-if="analysisData.topFailureDevices?.length > 0">
        <template #header>
          <div class="card-header">
            <span>🔥 高频故障设备 TOP3</span>
          </div>
        </template>
        <el-row :gutter="20">
          <el-col 
            v-for="(item, index) in analysisData.topFailureDevices" 
            :key="index"
            :xs="24" :sm="12" :md="8"
          >
            <div class="top-failure-card" :class="`rank-${index + 1}`">
              <div class="rank-badge">TOP {{ index + 1 }}</div>
              <div class="device-info">
                <h3>{{ item.deviceType }}</h3>
                <p class="brand">{{ item.brandModel }}</p>
                <div class="stats">
                  <el-tag type="danger" size="large">
                    {{ item.failureCount }} 次故障
                  </el-tag>
                  <el-tag type="warning" style="margin-top: 10px">
                    涉及 {{ item.affectedRoomCount }} 个房间
                  </el-tag>
                </div>
                <p class="repair-time">
                  平均修复时间: {{ item.avgRepairTime?.toFixed(1) }} 小时
                </p>
              </div>
            </div>
          </el-col>
        </el-row>
      </el-card>

      <!-- 采购建议清单 -->
      <el-card style="margin-top: 20px">
        <template #header>
          <div class="card-header">
            <span>📋 建议采购清单</span>
            <el-tag v-if="analysisData.purchaseRecommendations?.length > 0" type="warning">
              {{ analysisData.purchaseRecommendations.length }} 项建议
            </el-tag>
            <el-tag v-else type="success">无需采购</el-tag>
          </div>
        </template>

        <el-table 
          v-if="analysisData.purchaseRecommendations?.length > 0"
          :data="analysisData.purchaseRecommendations" 
          border
          stripe
        >
          <el-table-column type="index" label="#" width="50" />
          <el-table-column prop="deviceType" label="设备类型" min-width="100" />
          <el-table-column prop="brandModel" label="品牌型号" min-width="120" />
          <el-table-column prop="failureCount" label="故障次数" width="100" align="center">
            <template #default="{ row }">
              <el-tag :type="row.failureCount >= 5 ? 'danger' : 'warning'">
                {{ row.failureCount }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="affectedRoomCount" label="涉及房间" width="100" align="center" />
          <el-table-column prop="failureRate" label="故障率" width="100" align="center">
            <template #default="{ row }">
              {{ (row.failureRate * 100).toFixed(2) }}%
            </template>
          </el-table-column>
          <el-table-column prop="recommendedPurchaseQty" label="建议采购" width="100" align="center">
            <template #default="{ row }">
              <el-tag type="success" size="large">
                {{ row.recommendedPurchaseQty }} 件
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="recommendationReason" label="采购原因" min-width="150" />
        </el-table>

        <el-empty v-else description="当前所有设备运行状况良好，暂无采购需求" />
      </el-card>

      <!-- ECharts图表展示 -->
      <el-row :gutter="20" style="margin-top: 20px">
        <el-col :span="12">
          <el-card>
            <template #header>
              <span>设备故障次数统计</span>
            </template>
            <div ref="failureChartRef" style="width: 100%; height: 400px"></div>
          </el-card>
        </el-col>
        <el-col :span="12">
          <el-card>
            <template #header>
              <span>设备故障率分布</span>
            </template>
            <div ref="rateChartRef" style="width: 100%; height: 400px"></div>
          </el-card>
        </el-col>
      </el-row>

      <!-- 详细损耗统计 -->
      <el-card style="margin-top: 20px">
        <template #header>
          <div class="card-header">
            <span>📊 详细损耗统计</span>
          </div>
        </template>

        <el-table 
          :data="analysisData.analysisItems" 
          border
          stripe
          :default-sort="{ prop: 'failureCount', order: 'descending' }"
        >
          <el-table-column type="index" label="#" width="50" />
          <el-table-column prop="deviceType" label="设备类型" min-width="100" sortable />
          <el-table-column prop="brandModel" label="品牌型号" min-width="120" />
          <el-table-column prop="failureCount" label="故障次数" width="100" align="center" sortable>
            <template #default="{ row }">
              <el-tag :type="getFailureCountType(row.failureCount)">
                {{ row.failureCount }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="affectedRoomCount" label="涉及房间" width="100" align="center" />
          <el-table-column prop="failureRate" label="故障率" width="100" align="center" sortable>
            <template #default="{ row }">
              {{ (row.failureRate * 100).toFixed(2) }}%
            </template>
          </el-table-column>
          <el-table-column prop="avgRepairTime" label="平均修复时间" width="120" align="center">
            <template #default="{ row }">
              {{ row.avgRepairTime?.toFixed(1) }} 小时
            </template>
          </el-table-column>
          <el-table-column prop="recommendationReason" label="状态" min-width="120">
            <template #default="{ row }">
              <el-tag 
                :type="row.recommendedPurchaseQty > 0 ? 'warning' : 'success'"
                size="small"
              >
                {{ row.recommendationReason }}
              </el-tag>
            </template>
          </el-table-column>
        </el-table>
      </el-card>
    </div>

    <el-empty v-else description="暂无数据，请点击分析按钮" style="margin-top: 50px" />
  </div>
</template>

<script setup>
import { ref, nextTick } from 'vue'
import { Search, Tools, Monitor, ShoppingCart } from '@element-plus/icons-vue'
import { getHardwareAnalysis } from '@/api/report'
import { ElMessage } from 'element-plus'
import * as echarts from 'echarts'

const loading = ref(false)
const analysisData = ref(null)
const failureChartRef = ref(null)
const rateChartRef = ref(null)
let failureChart = null
let rateChart = null

const queryForm = ref({
  days: 30
})

// 加载分析数据
const loadAnalysis = async () => {
  loading.value = true
  try {
    const data = await getHardwareAnalysis(queryForm.value.days)
    analysisData.value = data
    
    // 等待DOM更新后渲染图表
    await nextTick()
    renderCharts()
    
    ElMessage.success('分析完成')
  } catch (error) {
    console.error('硬件分析失败:', error)
    ElMessage.error('硬件分析失败：' + (error.message || '请检查网络连接'))
  } finally {
    loading.value = false
  }
}

// 渲染ECharts图表
const renderCharts = () => {
  if (!analysisData.value || !analysisData.value.analysisItems) return
  
  const items = analysisData.value.analysisItems.slice(0, 10) // 取前10个设备
  
  // 渲染故障次数柱状图
  if (failureChartRef.value) {
    if (failureChart) {
      failureChart.dispose()
    }
    failureChart = echarts.init(failureChartRef.value)
    
    const failureOption = {
      tooltip: {
        trigger: 'axis',
        axisPointer: {
          type: 'shadow'
        }
      },
      xAxis: {
        type: 'category',
        data: items.map(item => item.deviceType),
        axisLabel: {
          interval: 0,
          rotate: 30
        }
      },
      yAxis: {
        type: 'value',
        name: '故障次数'
      },
      series: [
        {
          name: '故障次数',
          type: 'bar',
          data: items.map(item => item.failureCount),
          itemStyle: {
            color: function(params) {
              const colors = ['#f56c6c', '#e6a23c', '#67c23a']
              if (params.value >= 5) return colors[0]
              if (params.value >= 3) return colors[1]
              return colors[2]
            }
          }
        }
      ]
    }
    failureChart.setOption(failureOption)
  }
  
  // 渲染故障率饼图
  if (rateChartRef.value) {
    if (rateChart) {
      rateChart.dispose()
    }
    rateChart = echarts.init(rateChartRef.value)
    
    const rateOption = {
      tooltip: {
        trigger: 'item',
        formatter: '{a} <br/>{b}: {c}% ({d}%)'
      },
      legend: {
        orient: 'vertical',
        left: 'left'
      },
      series: [
        {
          name: '设备故障率',
          type: 'pie',
          radius: '60%',
          data: items.map(item => ({
            value: (item.failureRate * 100).toFixed(2),
            name: item.deviceType
          })),
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
    rateChart.setOption(rateOption)
  }
}

// 根据故障次数返回标签类型
const getFailureCountType = (count) => {
  if (count >= 5) return 'danger'
  if (count >= 3) return 'warning'
  return 'info'
}

// 初始加载
loadAnalysis()
</script>

<style scoped>
.hardware-analysis {
  padding: 20px;
  overflow-y: auto;
  max-height: calc(100vh - 120px);
}

.filter-card {
  margin-bottom: 20px;
}

.stat-card {
  height: 100%;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: 600;
}

.top-failure-card {
  position: relative;
  padding: 20px;
  border-radius: 12px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  margin-bottom: 20px;
  transition: transform 0.3s;
}

.top-failure-card:hover {
  transform: translateY(-5px);
}

.top-failure-card.rank-1 {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
}

.top-failure-card.rank-2 {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
}

.top-failure-card.rank-3 {
  background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
}

.rank-badge {
  position: absolute;
  top: 10px;
  right: 10px;
  background: rgba(255, 255, 255, 0.3);
  padding: 5px 15px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: bold;
}

.device-info h3 {
  margin: 0 0 10px 0;
  font-size: 24px;
}

.device-info .brand {
  margin: 0 0 15px 0;
  opacity: 0.9;
  font-size: 14px;
}

.device-info .stats {
  margin: 15px 0;
}

.device-info .stats .el-tag {
  display: block;
  text-align: center;
}

.device-info .repair-time {
  margin: 10px 0 0 0;
  font-size: 13px;
  opacity: 0.9;
}

@media (max-width: 768px) {
  .top-failure-card {
    margin-bottom: 10px;
  }
  
  .device-info h3 {
    font-size: 20px;
  }
}
</style>
