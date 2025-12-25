<template>
  <div class="cleaning-management">
    <!-- 清扫记录 -->
    <el-card>
      <template #header>
        <div class="card-header">
          <span>清扫记录</span>
          <el-button type="primary" @click="showCleaningDialog">完成清扫</el-button>
        </div>
      </template>

      <!-- 查询表单 -->
      <el-form :inline="true" :model="queryForm" class="query-form">
        <el-form-item label="房间号">
          <el-input v-model="queryForm.roomNumber" placeholder="请输入房间号" clearable />
        </el-form-item>
        <el-form-item label="清扫类型">
          <el-select v-model="queryForm.cleaningType" placeholder="请选择清扫类型" clearable>
            <el-option label="退房清扫" value="CHECKOUT" />
            <el-option label="日常清扫" value="DAILY" />
            <el-option label="深度清扫" value="DEEP" />
          </el-select>
        </el-form-item>
        <el-form-item label="时间范围">
          <el-date-picker
            v-model="queryForm.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadCleaningRecords">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 清扫记录列表 -->
      <el-table :data="cleaningList" border stripe v-loading="loading">
        <el-table-column type="index" label="序号" width="60" />
        <el-table-column prop="roomNumber" label="房间号" width="100" align="center" />
        <el-table-column prop="cleaningType" label="清扫类型" width="120" align="center">
          <template #default="{ row }">
            <el-tag :type="getCleaningTypeTag(row.cleaningType)">
              {{ getCleaningTypeText(row.cleaningType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="staffName" label="清扫员工" width="100" />
        <el-table-column prop="cleaningTime" label="清扫时间" width="160">
          <template #default="{ row }">
            {{ formatDateTime(row.cleaningTime) }}
          </template>
        </el-table-column>
        <el-table-column label="使用物品" min-width="200">
          <template #default="{ row }">
            <div v-if="row.supplyUsages && row.supplyUsages.length > 0">
              <el-tag
                v-for="(supply, index) in row.supplyUsages"
                :key="index"
                size="small"
                class="supply-tag"
              >
                {{ supply.productName }} × {{ supply.quantity }}
              </el-tag>
            </div>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="notes" label="备注" min-width="150" show-overflow-tooltip />
      </el-table>

      <!-- 分页 -->
      <el-pagination
        v-model:current-page="queryForm.pageNum"
        v-model:page-size="queryForm.pageSize"
        :total="total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="loadCleaningRecords"
        @current-change="loadCleaningRecords"
        class="pagination"
      />
    </el-card>

    <!-- 完成清扫对话框 -->
    <el-dialog
      v-model="dialogVisible"
      title="完成清扫"
      width="700px"
      @close="resetForm"
    >
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="房间" prop="roomId">
          <el-select
            v-model="form.roomId"
            placeholder="请选择房间"
            filterable
            style="width: 100%"
          >
            <el-option
              v-for="room in roomList"
              :key="room.roomId"
              :label="`${room.roomNo} - ${room.roomType}`"
              :value="room.roomId"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="清扫类型" prop="cleaningType">
          <el-radio-group v-model="form.cleaningType">
            <el-radio label="CHECKOUT">退房清扫</el-radio>
            <el-radio label="DAILY">日常清扫</el-radio>
            <el-radio label="DEEP">深度清扫</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="使用物品" prop="supplyUsages">
          <el-button type="primary" size="small" @click="addSupplyItem">添加物品</el-button>
          <el-table :data="form.supplyUsages" border stripe style="margin-top: 10px;">
            <el-table-column label="商品名称" min-width="200">
              <template #default="{ row, $index }">
                <el-select
                  v-model="row.productId"
                  placeholder="请选择商品"
                  filterable
                  style="width: 100%"
                  @change="handleSupplyChange($index)"
                >
                  <el-option
                    v-for="product in supplyList"
                    :key="product.productId"
                    :label="`${product.productName} (库存: ${product.stockQuantity})`"
                    :value="product.productId"
                  />
                </el-select>
              </template>
            </el-table-column>
            <el-table-column label="使用数量" width="150">
              <template #default="{ row }">
                <el-input-number
                  v-model="row.quantity"
                  :min="1"
                  :max="100"
                  size="small"
                  style="width: 100%"
                />
              </template>
            </el-table-column>
            <el-table-column label="操作" width="80" align="center">
              <template #default="{ $index }">
                <el-button
                  type="danger"
                  size="small"
                  @click="removeSupplyItem($index)"
                  link
                >
                  删除
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-form-item>
        <el-form-item label="备注">
          <el-input
            v-model="form.notes"
            type="textarea"
            :rows="3"
            placeholder="请输入备注信息"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getCleaningRecords, completeCleaning } from '@/api/cleaning'
import { getRoomList } from '@/api/room'
import { getProducts } from '@/api/product'

// 查询表单
const queryForm = reactive({
  pageNum: 1,
  pageSize: 10,
  roomNumber: '',
  cleaningType: '',
  dateRange: []
})

// 数据列表
const cleaningList = ref([])
const total = ref(0)
const loading = ref(false)

// 房间列表
const roomList = ref([])

// 物品列表（仅"其他"类型）
const supplyList = ref([])

// 对话框
const dialogVisible = ref(false)
const formRef = ref(null)
const submitting = ref(false)

// 表单数据
const form = reactive({
  roomId: null,
  cleaningType: 'DAILY',
  supplyUsages: [],
  notes: ''
})

// 表单验证规则
const rules = {
  roomId: [{ required: true, message: '请选择房间', trigger: 'change' }],
  cleaningType: [{ required: true, message: '请选择清扫类型', trigger: 'change' }],
  supplyUsages: [
    {
      validator: (rule, value, callback) => {
        if (!value || value.length === 0) {
          callback(new Error('请至少添加一项使用物品'))
        } else {
          const hasInvalid = value.some(item => !item.productId || !item.quantity)
          if (hasInvalid) {
            callback(new Error('请完整填写所有物品信息'))
          } else {
            callback()
          }
        }
      },
      trigger: 'change'
    }
  ]
}

// 加载清扫记录
const loadCleaningRecords = async () => {
  loading.value = true
  try {
    const params = {
      pageNum: queryForm.pageNum,
      pageSize: queryForm.pageSize
    }
    
    if (queryForm.roomNumber) {
      params.roomNumber = queryForm.roomNumber
    }
    if (queryForm.cleaningType) {
      params.cleaningType = queryForm.cleaningType
    }
    if (queryForm.dateRange && queryForm.dateRange.length === 2) {
      params.startDate = queryForm.dateRange[0]
      params.endDate = queryForm.dateRange[1]
    }
    
    const data = await getCleaningRecords(params)
    cleaningList.value = data.records || []
    total.value = data.total || 0
  } catch (error) {
    console.error('加载清扫记录失败:', error)
    ElMessage.error('加载清扫记录失败')
  } finally {
    loading.value = false
  }
}

// 加载房间列表
const loadRoomList = async () => {
  try {
    const data = await getRoomList()
    roomList.value = data || []
  } catch (error) {
    console.error('加载房间列表失败:', error)
    ElMessage.error('加载房间列表失败')
  }
}

// 加载物品列表（仅"其他"类型）
const loadSupplyList = async () => {
  try {
    const data = await getProducts({ category: 'OTHER', pageSize: 1000 })
    supplyList.value = data.records || []
  } catch (error) {
    console.error('加载物品列表失败:', error)
    ElMessage.error('加载物品列表失败')
  }
}

// 显示清扫对话框
const showCleaningDialog = () => {
  dialogVisible.value = true
  loadRoomList()
  loadSupplyList()
}

// 添加物品项
const addSupplyItem = () => {
  form.supplyUsages.push({
    productId: null,
    quantity: 1
  })
}

// 删除物品项
const removeSupplyItem = (index) => {
  form.supplyUsages.splice(index, 1)
}

// 物品选择变化
const handleSupplyChange = (index) => {
  // 可以在这里添加额外逻辑，如检查库存等
}

// 重置表单
const resetForm = () => {
  formRef.value?.resetFields()
  form.roomId = null
  form.cleaningType = 'DAILY'
  form.supplyUsages = []
  form.notes = ''
}

// 提交表单
const handleSubmit = async () => {
  try {
    await formRef.value.validate()
    
    submitting.value = true
    
    // 根据 roomId 查找对应的 roomNo
    const selectedRoom = roomList.value.find(room => room.roomId === form.roomId)
    if (!selectedRoom) {
      ElMessage.error('请选择有效的房间')
      return
    }
    
    const data = {
      roomNo: selectedRoom.roomNo,
      cleaningType: form.cleaningType,
      supplies: form.supplyUsages,
      notes: form.notes
    }
    
    await completeCleaning(data)
    ElMessage.success('清扫完成，物品已自动消耗')
    dialogVisible.value = false
    loadCleaningRecords()
  } catch (error) {
    if (error !== false) { // 排除表单验证失败的情况
      console.error('完成清扫失败:', error)
      ElMessage.error(error.message || '完成清扫失败')
    }
  } finally {
    submitting.value = false
  }
}

// 重置查询
const resetQuery = () => {
  queryForm.pageNum = 1
  queryForm.pageSize = 10
  queryForm.roomNumber = ''
  queryForm.cleaningType = ''
  queryForm.dateRange = []
  loadCleaningRecords()
}

// 获取清扫类型标签颜色
const getCleaningTypeTag = (type) => {
  const tags = {
    CHECKOUT: 'danger',
    DAILY: 'success',
    DEEP: 'warning'
  }
  return tags[type] || ''
}

// 获取清扫类型文本
const getCleaningTypeText = (type) => {
  const texts = {
    CHECKOUT: '退房清扫',
    DAILY: '日常清扫',
    DEEP: '深度清扫'
  }
  return texts[type] || type
}

// 格式化日期时间
const formatDateTime = (dateTime) => {
  if (!dateTime) return '-'
  return new Date(dateTime).toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit'
  })
}

// 组件挂载
onMounted(() => {
  loadCleaningRecords()
})
</script>

<style scoped>
.cleaning-management {
  padding: 20px;
  height: calc(100vh - 120px);
  overflow-y: auto;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.query-form {
  margin-bottom: 20px;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.supply-tag {
  margin-right: 5px;
  margin-bottom: 5px;
}
</style>
