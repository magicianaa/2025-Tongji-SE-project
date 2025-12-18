<template>
  <div class="pos-orders">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>POS订单管理</span>
        </div>
      </template>

      <!-- 筛选栏 -->
      <el-form :inline="true" class="search-form">
        <el-form-item label="订单状态">
          <el-select v-model="searchForm.status" clearable placeholder="全部">
            <el-option label="待配送" value="PENDING" />
            <el-option label="已配送" value="DELIVERED" />
            <el-option label="已归还" value="RETURNED" />
            <el-option label="已取消" value="CANCELLED" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadOrders">查询</el-button>
          <el-button @click="loadPendingOrders">仅看待配送</el-button>
        </el-form-item>
      </el-form>

      <!-- 订单表格 -->
      <el-table :data="orders" border>
        <el-table-column prop="orderNo" label="订单号" width="180" />
        <el-table-column prop="roomNo" label="房间" width="100" />
        <el-table-column prop="guestName" label="住客" width="120" />
        <el-table-column prop="orderType" label="订单类型" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.orderType === 'PURCHASE'" type="success">购买</el-tag>
            <el-tag v-else-if="row.orderType === 'RENTAL'" type="warning">租赁</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.status === 'PENDING'" type="warning">待配送</el-tag>
            <el-tag v-else-if="row.status === 'DELIVERED'" type="success">已配送</el-tag>
            <el-tag v-else-if="row.status === 'RETURNED'" type="info">已归还</el-tag>
            <el-tag v-else-if="row.status === 'CANCELLED'" type="danger">已取消</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="totalAmount" label="金额" width="100">
          <template #default="{ row }">
            ¥{{ row.totalAmount }}
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="下单时间" width="180">
          <template #default="{ row }">
            {{ formatTime(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button
              v-if="row.status === 'PENDING'"
              link
              type="primary"
              @click="handleDeliver(row)"
            >
              已配送
            </el-button>
            <el-button
              v-if="row.orderType === 'RENTAL' && row.status === 'DELIVERED'"
              link
              type="success"
              @click="handleReturn(row)"
            >
              已归还
            </el-button>
            <el-button link @click="handleViewDetail(row)">查看详情</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 订单详情对话框 -->
    <el-dialog v-model="detailDialogVisible" title="订单详情" width="800px">
      <div v-if="currentOrder" class="order-detail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="订单号">{{ currentOrder.orderNo }}</el-descriptions-item>
          <el-descriptions-item label="房间号">{{ currentOrder.roomNo }}</el-descriptions-item>
          <el-descriptions-item label="住客姓名">{{ currentOrder.guestName }}</el-descriptions-item>
          <el-descriptions-item label="订单类型">
            <el-tag v-if="currentOrder.orderType === 'PURCHASE'" type="success">购买</el-tag>
            <el-tag v-else-if="currentOrder.orderType === 'RENTAL'" type="warning">租赁</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="订单状态">
            <el-tag v-if="currentOrder.status === 'PENDING'" type="warning">待配送</el-tag>
            <el-tag v-else-if="currentOrder.status === 'DELIVERED'" type="success">已配送</el-tag>
            <el-tag v-else-if="currentOrder.status === 'RETURNED'" type="info">已归还</el-tag>
            <el-tag v-else-if="currentOrder.status === 'CANCELLED'" type="danger">已取消</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="订单金额">¥{{ currentOrder.totalAmount }}</el-descriptions-item>
          <el-descriptions-item label="下单时间">{{ formatTime(currentOrder.createTime) }}</el-descriptions-item>
          <el-descriptions-item label="配送时间" v-if="currentOrder.deliveryTime">
            {{ formatTime(currentOrder.deliveryTime) }}
          </el-descriptions-item>
          <el-descriptions-item label="配送人员" v-if="currentOrder.operatorName">
            {{ currentOrder.operatorName }}
          </el-descriptions-item>
          <el-descriptions-item label="归还时间" v-if="currentOrder.returnTime">
            {{ formatTime(currentOrder.returnTime) }}
          </el-descriptions-item>
        </el-descriptions>

        <div class="order-items">
          <h4>订单明细</h4>
          <el-table :data="currentOrder.items" border>
            <el-table-column prop="productName" label="商品名称" />
            <el-table-column prop="productType" label="类型" width="100">
              <template #default="{ row }">
                <el-tag v-if="row.productType === 'SNACK'" type="warning">零食</el-tag>
                <el-tag v-else-if="row.productType === 'BEVERAGE'" type="success">饮料</el-tag>
                <el-tag v-else-if="row.productType === 'PERIPHERAL'" type="info">外设</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="quantity" label="数量" width="80" />
            <el-table-column prop="unitPrice" label="单价" width="100">
              <template #default="{ row }">
                ¥{{ row.unitPrice }}
              </template>
            </el-table-column>
            <el-table-column prop="subtotal" label="小计" width="100">
              <template #default="{ row }">
                ¥{{ row.subtotal }}
              </template>
            </el-table-column>
          </el-table>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getAllOrders, getPendingOrders, deliverOrder, returnRental } from '@/api/order'

// 搜索表单
const searchForm = reactive({
  status: ''
})

// 订单列表
const orders = ref([])

// 详情对话框
const detailDialogVisible = ref(false)
const currentOrder = ref(null)

// 加载订单列表
const loadOrders = async () => {
  try {
    const params = {
      status: searchForm.status
    }
    const res = await getAllOrders(params)
    orders.value = res || []
  } catch (error) {
    ElMessage.error('加载订单列表失败')
  }
}

// 仅加载待配送订单
const loadPendingOrders = async () => {
  try {
    const res = await getPendingOrders()
    orders.value = res || []
  } catch (error) {
    ElMessage.error('加载待配送订单失败')
  }
}

// 标记为已配送
const handleDeliver = async (row) => {
  try {
    await ElMessageBox.confirm('确认已配送该订单？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'info'
    })
    await deliverOrder(row.orderId)
    ElMessage.success('操作成功')
    loadOrders()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('操作失败')
    }
  }
}

// 标记为已归还
const handleReturn = async (row) => {
  try {
    await ElMessageBox.confirm('确认客人已归还租赁设备？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'info'
    })
    await returnRental(row.orderId)
    ElMessage.success('操作成功')
    loadOrders()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('操作失败')
    }
  }
}

// 查看详情
const handleViewDetail = (row) => {
  currentOrder.value = row
  detailDialogVisible.value = true
}

// 格式化时间
const formatTime = (time) => {
  if (!time) return '-'
  return new Date(time).toLocaleString('zh-CN')
}

onMounted(() => {
  loadOrders()
})
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.search-form {
  margin-bottom: 20px;
}

.order-detail {
  padding: 20px 0;
}

.order-items {
  margin-top: 30px;
}

.order-items h4 {
  margin-bottom: 10px;
}
</style>
