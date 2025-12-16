<template>
  <div class="guest-booking">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>房间预订</span>
        </div>
      </template>

      <el-form :model="bookingForm" label-width="100px">
        <el-form-item label="入住日期">
          <el-date-picker
            v-model="bookingForm.checkInDate"
            type="date"
            placeholder="选择入住日期"
            :disabled-date="disabledBeforeToday"
            style="width: 100%;"
          />
        </el-form-item>

        <el-form-item label="退房日期">
          <el-date-picker
            v-model="bookingForm.checkOutDate"
            type="date"
            placeholder="选择退房日期"
            :disabled-date="disabledBeforeCheckIn"
            style="width: 100%;"
          />
        </el-form-item>

        <el-form-item label="房型">
          <el-select v-model="bookingForm.roomType" placeholder="选择房型" style="width: 100%;">
            <el-option label="单人电竞房" value="SINGLE" />
            <el-option label="双人电竞房" value="DOUBLE" />
            <el-option label="豪华电竞套房" value="SUITE" />
          </el-select>
        </el-form-item>
      </el-form>

      <el-divider />

      <div class="room-list">
        <h3>可预订房间</h3>
        <el-empty description="请先选择入住和退房日期" v-if="!bookingForm.checkInDate || !bookingForm.checkOutDate" />
        
        <el-row :gutter="20" v-else>
          <el-col :span="8" v-for="room in availableRooms" :key="room.roomId">
            <el-card shadow="hover" class="room-card">
              <div class="room-info">
                <div class="room-no">{{ room.roomNo }}</div>
                <div class="room-type">{{ room.roomTypeName }}</div>
                <div class="room-price">¥{{ room.pricePerNight }}/晚</div>
                <el-button type="primary" @click="handleBook(room)" style="width: 100%; margin-top: 10px;">
                  立即预订
                </el-button>
              </div>
            </el-card>
          </el-col>
        </el-row>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'

const bookingForm = reactive({
  checkInDate: null,
  checkOutDate: null,
  roomType: 'SINGLE'
})

const availableRooms = ref([
  { roomId: 1, roomNo: '101', roomTypeName: '单人电竞房', pricePerNight: 299 },
  { roomId: 2, roomNo: '102', roomTypeName: '单人电竞房', pricePerNight: 299 },
  { roomId: 3, roomNo: '201', roomTypeName: '双人电竞房', pricePerNight: 399 }
])

const disabledBeforeToday = (date) => {
  return date < new Date(new Date().setHours(0, 0, 0, 0))
}

const disabledBeforeCheckIn = (date) => {
  if (!bookingForm.checkInDate) return true
  return date <= bookingForm.checkInDate
}

const handleBook = (room) => {
  ElMessage.success(`预订房间 ${room.roomNo} 成功！`)
  // TODO: 调用预订API
}
</script>

<style scoped>
.guest-booking {
  padding: 20px;
}

.room-list {
  margin-top: 20px;
}

.room-card {
  margin-bottom: 20px;
  transition: transform 0.3s;
}

.room-card:hover {
  transform: translateY(-5px);
}

.room-info {
  text-align: center;
}

.room-no {
  font-size: 24px;
  font-weight: bold;
  color: #409eff;
  margin-bottom: 5px;
}

.room-type {
  font-size: 14px;
  color: #909399;
  margin-bottom: 10px;
}

.room-price {
  font-size: 20px;
  color: #f56c6c;
  font-weight: bold;
}
</style>
