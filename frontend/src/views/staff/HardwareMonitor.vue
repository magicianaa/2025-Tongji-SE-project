<template>
  <div class="hardware-monitor">
    <el-card class="control-card">
      <div class="control-bar">
        <div class="connection-status">
          <el-icon :class="{ connected: isConnected, disconnected: !isConnected }">
            <Connection />
          </el-icon>
          <span>{{ isConnected ? 'WebSocket 已连接' : 'WebSocket 未连接' }}</span>
        </div>
        
        <div class="control-buttons">
          <el-button
            :type="autoRefresh ? 'success' : 'info'"
            @click="toggleAutoRefresh"
          >
            <el-icon><RefreshRight /></el-icon>
            {{ autoRefresh ? '自动刷新中' : '自动刷新' }}
          </el-button>
          
          <el-button type="primary" @click="connectWebSocket">
            <el-icon><Link /></el-icon>
            重新连接
          </el-button>
          
          <el-button type="warning" @click="showAlertDialog">
            <el-icon><Warning /></el-icon>
            告警记录 ({{ unhandledAlerts }})
          </el-button>
        </div>
      </div>
    </el-card>
    
    <el-card class="monitor-card">
      <div class="rooms-grid">
        <div
          v-for="status in hardwareStatusList"
          :key="status.roomId"
          class="hardware-card"
          :class="getHealthClass(status.healthLevel)"
        >
          <div class="card-header">
            <div class="room-info">
              <div class="room-number">房间 {{ status.roomNo || status.roomId }}</div>
              <el-tag :type="getHealthTagType(status.healthLevel)" size="small">
                {{ getHealthText(status.healthLevel) }}
              </el-tag>
            </div>
            <div class="last-update">
              {{ formatTime(status.timestamp) }}
            </div>
          </div>
          
          <div class="card-body">
            <div class="metric-item">
              <div class="metric-label">
                <el-icon><Cpu /></el-icon>
                CPU温度
              </div>
              <div class="metric-value" :class="getTempClass(status.cpuTemp)">
                {{ status.cpuTemp.toFixed(1) }}°C
              </div>
            </div>
            
            <div class="metric-item">
              <div class="metric-label">
                <el-icon><VideoCamera /></el-icon>
                GPU温度
              </div>
              <div class="metric-value" :class="getTempClass(status.gpuTemp)">
                {{ status.gpuTemp.toFixed(1) }}°C
              </div>
            </div>
            
            <div class="metric-item">
              <div class="metric-label">
                <el-icon><Connection /></el-icon>
                网络延迟
              </div>
              <div class="metric-value" :class="getLatencyClass(status.networkLatency)">
                {{ status.networkLatency.toFixed(0) }}ms
              </div>
            </div>
            
          </div>
          
          <div class="card-footer">
            <el-button
              size="small"
              type="danger"
              @click="triggerFailure(status.roomId)"
            >
              触发故障
            </el-button>
          </div>
        </div>
      </div>
      
      <el-empty v-if="hardwareStatusList.length === 0" description="暂无硬件监控数据" />
    </el-card>
    
    <!-- 告警对话框 -->
    <el-dialog
      v-model="alertDialogVisible"
      title="告警记录"
      width="800px"
    >
      <el-table :data="alertLogs" stripe>
        <el-table-column prop="roomId" label="房间ID" width="100" />
        <el-table-column label="告警类型" width="120">
          <template #default="{ row }">
            <el-tag :type="getAlertTypeTag(row.alertType)">
              {{ row.alertType }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="message" label="告警信息" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.isHandled ? 'success' : 'danger'">
              {{ row.isHandled ? '已处理' : '未处理' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="alertTime" label="告警时间" width="180" />
        <el-table-column label="操作" width="100">
          <template #default="{ row }">
            <el-button
              v-if="!row.isHandled"
              size="small"
              type="primary"
              @click="handleAlert(row.alertId)"
            >
              处理
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted } from 'vue'
import { ElMessage } from 'element-plus'
import { RefreshRight, Link, Warning, Cpu, VideoCamera, Connection, TrendCharts } from '@element-plus/icons-vue'
import SockJS from 'sockjs-client'
import Stomp from 'stompjs'
import { getHardwareStatus, triggerFailure as apiTriggerFailure, getAlertLogs, handleAlert as apiHandleAlert } from '@/api/hardware'

// WebSocket 连接
let stompClient = null
const isConnected = ref(false)
const autoRefresh = ref(true)

// 硬件状态列表
const hardwareStatusList = ref([])

// 告警相关
const alertDialogVisible = ref(false)
const alertLogs = ref([])
const unhandledAlerts = ref(0)

// 连接 WebSocket
const connectWebSocket = () => {
  try {
    const socket = new SockJS('/api/ws')
    stompClient = Stomp.over(socket)
    
    // 设置心跳
    stompClient.heartbeat.outgoing = 20000
    stompClient.heartbeat.incoming = 20000
    
    stompClient.connect(
      {},
      () => {
        console.log('WebSocket 连接成功')
        isConnected.value = true
        ElMessage.success('WebSocket 连接成功')
        
        // 订阅硬件状态主题
        stompClient.subscribe('/topic/hardware', (message) => {
          const data = JSON.parse(message.body)
          updateHardwareStatus(data)
        })
        
        // 订阅告警主题
        stompClient.subscribe('/topic/alerts', (message) => {
          const alert = JSON.parse(message.body)
          handleNewAlert(alert)
        })
      },
      (error) => {
        console.error('WebSocket 连接失败：', error)
        isConnected.value = false
        ElMessage.error('WebSocket 连接失败')
        
        // 5秒后重连
        if (autoRefresh.value) {
          setTimeout(() => {
            connectWebSocket()
          }, 5000)
        }
      }
    )
  } catch (error) {
    console.error('WebSocket 初始化失败：', error)
    isConnected.value = false
  }
}

// 断开 WebSocket
const disconnectWebSocket = () => {
  if (stompClient !== null) {
    stompClient.disconnect()
    isConnected.value = false
    console.log('WebSocket 已断开')
  }
}

// 更新硬件状态
const updateHardwareStatus = (data) => {
  if (Array.isArray(data)) {
    hardwareStatusList.value = data
  } else {
    // 单个房间更新
    const index = hardwareStatusList.value.findIndex(s => s.roomId === data.roomId)
    if (index !== -1) {
      hardwareStatusList.value[index] = data
    } else {
      hardwareStatusList.value.push(data)
    }
  }
}

// 处理新告警
const handleNewAlert = (alert) => {
  ElMessage.warning({
    message: `房间 ${alert.roomId} 发生告警：${alert.message}`,
    duration: 5000
  })
  
  // 添加到告警列表
  alertLogs.value.unshift(alert)
  unhandledAlerts.value++
}

// 加载硬件状态
const loadHardwareStatus = async () => {
  try {
    const data = await getHardwareStatus()
    hardwareStatusList.value = data
  } catch (error) {
    console.error('加载硬件状态失败：', error)
  }
}

// 加载告警日志
const loadAlertLogs = async () => {
  try {
    const data = await getAlertLogs({ limit: 50 })
    alertLogs.value = data
    unhandledAlerts.value = data.filter(a => !a.isHandled).length
  } catch (error) {
    console.error('加载告警日志失败：', error)
  }
}

// 触发故障
const triggerFailure = async (roomId) => {
  try {
    await apiTriggerFailure(roomId)
    ElMessage.success('故障已触发')
  } catch (error) {
    console.error('触发故障失败：', error)
  }
}

// 处理告警
const handleAlert = async (alertId) => {
  try {
    await apiHandleAlert(alertId)
    ElMessage.success('告警已处理')
    loadAlertLogs()
  } catch (error) {
    console.error('处理告警失败：', error)
  }
}

// 切换自动刷新
const toggleAutoRefresh = () => {
  autoRefresh.value = !autoRefresh.value
  if (autoRefresh.value && !isConnected.value) {
    connectWebSocket()
  }
}

// 显示告警对话框
const showAlertDialog = () => {
  loadAlertLogs()
  alertDialogVisible.value = true
}

// 获取健康状态样式类
const getHealthClass = (healthLevel) => {
  return {
    'health-green': healthLevel === 'GREEN',
    'health-yellow': healthLevel === 'YELLOW',
    'health-red': healthLevel === 'RED',
    'health-offline': healthLevel === 'OFFLINE'
  }
}

// 获取健康状态标签类型
const getHealthTagType = (healthLevel) => {
  const typeMap = {
    GREEN: 'success',
    YELLOW: 'warning',
    RED: 'danger',
    OFFLINE: 'info'
  }
  return typeMap[healthLevel] || ''
}

// 获取健康状态文本
const getHealthText = (healthLevel) => {
  const textMap = {
    GREEN: '正常',
    YELLOW: '预警',
    RED: '告警',
    OFFLINE: '未开机'
  }
  return textMap[healthLevel] || healthLevel
}

// 获取温度样式类
const getTempClass = (temp) => {
  if (temp === 0) return 'temp-offline'
  if (temp >= 95) return 'temp-danger'
  if (temp >= 85) return 'temp-warning'
  return 'temp-normal'
}

// 获取延迟样式类
const getLatencyClass = (latency) => {
  if (latency === 0) return 'temp-offline'
  if (latency >= 100) return 'temp-danger'
  if (latency >= 50) return 'temp-warning'
  return 'temp-normal'
}

// 获取告警类型标签
const getAlertTypeTag = (alertType) => {
  const typeMap = {
    HARDWARE_FAILURE: 'danger',
    HIGH_TEMPERATURE: 'warning',
    NETWORK_ISSUE: 'info'
  }
  return typeMap[alertType] || ''
}

// 格式化时间
const formatTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  return date.toLocaleString('zh-CN', {
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit'
  })
}

onMounted(() => {
  loadHardwareStatus()
  loadAlertLogs()
  if (autoRefresh.value) {
    connectWebSocket()
  }
})

onUnmounted(() => {
  disconnectWebSocket()
})
</script>

<style scoped>
.hardware-monitor {
  width: 100%;
  min-height: 100%;
}

.control-card {
  margin-bottom: 20px;
}

.control-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 15px;
}

.connection-status {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
}

.connection-status .el-icon {
  font-size: 20px;
}

.connection-status .connected {
  color: #67c23a;
  animation: pulse 2s infinite;
}

.connection-status .disconnected {
  color: #909399;
}

@keyframes pulse {
  0%, 100% {
    opacity: 1;
  }
  50% {
    opacity: 0.5;
  }
}

.control-buttons {
  display: flex;
  gap: 10px;
}

.monitor-card {
  height: calc(100% - 100px);
  overflow-y: auto;
}

.rooms-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 20px;
}

.hardware-card {
  border: 3px solid;
  border-radius: 12px;
  padding: 16px;
  transition: all 0.3s;
}

.hardware-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.15);
}

.hardware-card.health-green {
  border-color: #67c23a;
  background: linear-gradient(135deg, #f0f9ff 0%, #dcfce7 100%);
}

.hardware-card.health-yellow {
  border-color: #e6a23c;
  background: linear-gradient(135deg, #fffbeb 0%, #fef3c7 100%);
}

.hardware-card.health-red {
  border-color: #f56c6c;
  background: linear-gradient(135deg, #fef2f2 0%, #fee2e2 100%);
  animation: redPulse 1.5s infinite;
}

.hardware-card.health-offline {
  border-color: #909399;
  background: linear-gradient(135deg, #f4f4f5 0%, #e5e7eb 100%);
  opacity: 0.75;
}

@keyframes redPulse {
  0%, 100% {
    box-shadow: 0 0 10px rgba(245, 108, 108, 0.5);
  }
  50% {
    box-shadow: 0 0 20px rgba(245, 108, 108, 0.8);
  }
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 2px solid rgba(0, 0, 0, 0.1);
}

.room-info {
  display: flex;
  align-items: center;
  gap: 10px;
}

.room-number {
  font-size: 24px;
  font-weight: bold;
  color: #333;
}

.last-update {
  font-size: 12px;
  color: #999;
}

.card-body {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
  margin-bottom: 16px;
}

.metric-item {
  padding: 10px;
  background: rgba(255, 255, 255, 0.7);
  border-radius: 8px;
}

.metric-label {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: #666;
  margin-bottom: 6px;
}

.metric-value {
  font-size: 20px;
  font-weight: bold;
  color: #333;
}

.metric-value.temp-normal {
  color: #67c23a;
}

.metric-value.temp-warning {
  color: #e6a23c;
}

.metric-value.temp-danger {
  color: #f56c6c;
}

.metric-value.temp-offline {
  color: #909399;
}

.card-footer {
  display: flex;
  justify-content: center;
}

.card-footer .el-button {
  width: 100%;
}
</style>
