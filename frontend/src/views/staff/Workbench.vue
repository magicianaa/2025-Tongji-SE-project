<template>
  <div class="home-container">
    <div class="stats-grid">
      <el-card class="stat-card">
        <div class="stat-icon vacant">
          <el-icon><House /></el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-value">{{ stats.vacant }}</div>
          <div class="stat-label">空闲房间</div>
        </div>
      </el-card>
      
      <el-card class="stat-card">
        <div class="stat-icon occupied">
          <el-icon><UserFilled /></el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-value">{{ stats.occupied }}</div>
          <div class="stat-label">已入住</div>
        </div>
      </el-card>
      
      <el-card class="stat-card">
        <div class="stat-icon dirty">
          <el-icon><Tools /></el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-value">{{ stats.dirty }}</div>
          <div class="stat-label">待清洁</div>
        </div>
      </el-card>
      
      <el-card class="stat-card">
        <div class="stat-icon maintenance">
          <el-icon><Warning /></el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-value">{{ stats.maintenance }}</div>
          <div class="stat-label">维修中</div>
        </div>
      </el-card>
    </div>
    
    <el-card class="welcome-card">
      <h3>欢迎使用智慧电竞酒店管理系统</h3>
      <p>本系统提供全面的酒店管理功能，包括：</p>
      <ul>
        <li>房态实时管理：查看所有房间状态，办理入住/退房</li>
        <li>硬件实时监控：监测房间设备运行状况，自动告警</li>
        <li>二次鉴权机制：确保入住客人才能访问房间控制功能</li>
      </ul>
      <div class="quick-actions">
        <el-button type="primary" @click="router.push('/staff/rooms')">
          <el-icon><OfficeBuilding /></el-icon>
          查看房态
        </el-button>
        <el-button type="success" @click="router.push('/staff/hardware')">
          <el-icon><Monitor /></el-icon>
          硬件监控
        </el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getRoomList } from '@/api/room'
import { 
  House, 
  UserFilled, 
  Tools, 
  Warning,
  OfficeBuilding,
  Monitor
} from '@element-plus/icons-vue'

const router = useRouter()

// 统计数据
const stats = reactive({
  vacant: 0,
  occupied: 0,
  dirty: 0,
  maintenance: 0
})

// 加载统计数据
const loadStats = async () => {
  try {
    const rooms = await getRoomList()
    
    // 统计各状态房间数量
    stats.vacant = rooms.filter(r => r.status === 'VACANT').length
    stats.occupied = rooms.filter(r => r.status === 'OCCUPIED').length
    stats.dirty = rooms.filter(r => r.status === 'DIRTY').length
    stats.maintenance = rooms.filter(r => r.status === 'MAINTENANCE').length
  } catch (error) {
    console.error('加载统计数据失败：', error)
  }
}

onMounted(() => {
  loadStats()
})
</script>

<style scoped>
.home-container {
  width: 100%;
  height: 100%;
  overflow-y: auto;
  max-height: calc(100vh - 120px);
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 20px;
  margin-bottom: 20px;
}

.stat-card {
  display: flex;
  align-items: center;
  padding: 20px;
}

.stat-icon {
  width: 60px;
  height: 60px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 15px;
  flex-shrink: 0;
  font-size: 28px;
  color: white;
}

.stat-icon.vacant {
  background: linear-gradient(135deg, #67b26f 0%, #4ca2cd 100%);
}

.stat-icon.occupied {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
}

.stat-icon.dirty {
  background: linear-gradient(135deg, #ffd89b 0%, #19547b 100%);
}

.stat-icon.maintenance {
  background: linear-gradient(135deg, #fa709a 0%, #fee140 100%);
}

.stat-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
}

.stat-value {
  font-size: 28px;
  font-weight: bold;
  color: #333;
  margin-bottom: 5px;
  line-height: 1;
}

.stat-label {
  font-size: 14px;
  color: #999;
  line-height: 1;
}

.welcome-card {
  margin-top: 20px;
}

.welcome-card h3 {
  font-size: 20px;
  color: #333;
  margin-bottom: 15px;
}

.welcome-card p {
  color: #666;
  margin-bottom: 10px;
}

.welcome-card ul {
  list-style: none;
  padding-left: 0;
  margin-bottom: 20px;
}

.welcome-card li {
  color: #666;
  padding: 8px 0;
  padding-left: 20px;
  position: relative;
}

.welcome-card li::before {
  content: '✓';
  position: absolute;
  left: 0;
  color: #67c23a;
  font-weight: bold;
}

.quick-actions {
  display: flex;
  gap: 15px;
}
</style>
