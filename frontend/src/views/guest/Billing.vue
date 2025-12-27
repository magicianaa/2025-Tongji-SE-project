<template>
  <div class="billing">
    <el-card>
      <template #header>
        <span>退房结算</span>
      </template>

      <div v-if="loading" class="loading">
        <el-skeleton :rows="8" animated />
      </div>

      <div v-else-if="billDetail" class="bill-detail">
        <!-- 房间信息 -->
        <el-descriptions title="房间信息" :column="2" border>
          <el-descriptions-item label="房间号">{{ billDetail.roomNo }}</el-descriptions-item>
          <el-descriptions-item label="房型">
            <el-tag v-if="billDetail.roomType === 'SINGLE'">单人房</el-tag>
            <el-tag v-else-if="billDetail.roomType === 'DOUBLE'" type="success">双人房</el-tag>
            <el-tag v-else-if="billDetail.roomType === 'FIVE_PLAYER'" type="warning">五黑房</el-tag>
            <el-tag v-else-if="billDetail.roomType === 'VIP'" type="danger">VIP房</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="入住时间">{{ formatTime(billDetail.checkInTime) }}</el-descriptions-item>
          <el-descriptions-item label="当前时间">{{ formatTime(billDetail.currentTime) }}</el-descriptions-item>
          <el-descriptions-item label="入住天数">{{ billDetail.stayDays }} 天</el-descriptions-item>
          <el-descriptions-item label="每天价格">¥{{ billDetail.pricePerDay }}</el-descriptions-item>
        </el-descriptions>

        <!-- 费用明细 -->
        <el-divider content-position="left"><b>费用明细</b></el-divider>
        
        <el-table :data="[billDetail]" border show-summary :summary-method="getSummaries">
          <el-table-column prop="roomFee" label="房费" width="200">
            <template #default="{ row }">
              <div>
                <div>¥{{ row.pricePerDay }} × {{ row.stayDays }} 天</div>
                <div class="amount">¥{{ row.roomFee }}</div>
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="posTotal" label="商城消费" width="200">
            <template #default="{ row }">
              <el-popover
                v-if="row.posOrders && row.posOrders.length > 0"
                placement="top"
                :width="600"
                trigger="hover"
              >
                <template #reference>
                  <div class="amount-link">¥{{ row.posTotal }}</div>
                </template>
                <el-table :data="row.posOrders" border size="small">
                  <el-table-column prop="orderNo" label="订单号" width="180" />
                  <el-table-column prop="orderType" label="类型" width="80">
                    <template #default="{ row: order }">
                      <el-tag v-if="order.orderType === 'PURCHASE'" size="small">购买</el-tag>
                      <el-tag v-else-if="order.orderType === 'RENTAL'" type="warning" size="small">租赁</el-tag>
                    </template>
                  </el-table-column>
                  <el-table-column prop="guestName" label="下单人" width="120" />
                  <el-table-column prop="totalAmount" label="金额" width="100">
                    <template #default="{ row: order }">
                      ¥{{ order.totalAmount }}
                    </template>
                  </el-table-column>
                  <el-table-column prop="status" label="状态" width="80">
                    <template #default="{ row: order }">
                      <el-tag v-if="order.status === 'PENDING'" type="warning" size="small">待配送</el-tag>
                      <el-tag v-else-if="order.status === 'DELIVERED'" type="success" size="small">已配送</el-tag>
                      <el-tag v-else-if="order.status === 'RETURNED'" type="info" size="small">已归还</el-tag>
                    </template>
                  </el-table-column>
                </el-table>
              </el-popover>
              <div v-else class="amount">¥{{ row.posTotal }}</div>
            </template>
          </el-table-column>
          <el-table-column prop="totalAmount" label="总计" width="200">
            <template #default="{ row }">
              <div class="total-amount">¥{{ row.totalAmount }}</div>
            </template>
          </el-table-column>
          <el-table-column label="备注">
            <template #default>
              <div class="remark">
                <p>* 房费按天计算，不满1天按1天计算</p>
                <p>* 商城消费包含当前房间所有住客的订单</p>
              </div>
            </template>
          </el-table-column>
        </el-table>

        <!-- 支付信息 -->
        <el-divider content-position="left"><b>支付信息</b></el-divider>
        
        <el-descriptions :column="3" border>
          <el-descriptions-item label="待支付金额">
            <span class="unpaid-amount">¥{{ billDetail.unpaidAmount }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="已支付金额">
            <span>¥{{ billDetail.paidAmount }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="总金额">
            <span class="total-amount">¥{{ billDetail.totalAmount }}</span>
          </el-descriptions-item>
        </el-descriptions>

        <!-- 操作按钮 -->
        <div class="actions">
          <el-button type="primary" size="large" @click="showSettleDialog = true" :disabled="billDetail.unpaidAmount <= 0">
            账单清付
          </el-button>
          <el-button size="large" @click="loadBill">
            刷新账单
          </el-button>
        </div>
      </div>

      <el-empty v-else description="暂无账单信息" />
    </el-card>

    <!-- 支付方式对话框 -->
    <el-dialog v-model="showSettleDialog" title="选择支付方式" width="450px">
      <el-form :model="settleForm" label-width="120px">
        <el-form-item label="支付金额">
          <span class="settle-amount">¥{{ billDetail?.unpaidAmount }}</span>
        </el-form-item>
        <el-form-item label="支付方式">
          <el-radio-group v-model="settleForm.paymentMethod">
            <el-radio value="CASH">现金</el-radio>
            <el-radio value="WECHAT">微信支付</el-radio>
            <el-radio value="ALIPAY">支付宝</el-radio>
            <el-radio value="CARD">银行卡</el-radio>
          </el-radio-group>
        </el-form-item>

      </el-form>
      <template #footer>
        <el-button @click="showSettleDialog = false">取消</el-button>
        <el-button type="primary" @click="handleSettle" :loading="settling">确认支付</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getBillDetail, settleBill } from '@/api/billing'
import { createBillAlipay, queryAlipayStatus } from '@/api/payment'
import { useUserStore } from '@/stores/user'
import { useRouter } from 'vue-router'

const userStore = useUserStore()
const router = useRouter()

const loading = ref(false)
const billDetail = ref(null)
const showSettleDialog = ref(false)
const settling = ref(false)

// 支付轮询相关
const pollingTimer = ref(null)
const currentOutTradeNo = ref(null)
const pollingCount = ref(0)
const MAX_POLLING_COUNT = 60  // 最多轮询60次（5分钟）

const settleForm = ref({
  paymentMethod: 'ALIPAY'
})

// 加载账单
const loadBill = async () => {
  if (!userStore.checkInInfo || !userStore.checkInInfo.recordId) {
    ElMessage.error('您还未入住')
    return
  }

  try {
    loading.value = true
    const res = await getBillDetail(userStore.checkInInfo.recordId)
    billDetail.value = res
  } catch (error) {
    ElMessage.error('加载账单失败')
  } finally {
    loading.value = false
  }
}

// 账单清付
const handleSettle = async () => {
  try {
    settling.value = true
    
    // 如果选择支付宝支付，跳转到支付宝页面
    if (settleForm.value.paymentMethod === 'ALIPAY') {
      const payResponse = await createBillAlipay(userStore.checkInInfo.recordId)
      const { outTradeNo, formHtml } = payResponse
      
      // 保存订单号用于轮询
      currentOutTradeNo.value = outTradeNo
      pollingCount.value = 0
      
      // 打开新窗口显示支付宝支付页面
      const payWindow = window.open('', '_blank')
      if (payWindow) {
        payWindow.document.write(formHtml)
        payWindow.document.close()
        
        ElMessage.info('支付页面已打开，请在新窗口完成支付。系统将自动检测支付结果...')
        showSettleDialog.value = false
        
        // 开始轮询检测支付状态
        startPolling()
      } else {
        ElMessage.error('无法打开支付窗口，请检查浏览器弹窗设置')
      }
    } else {
      // 其他支付方式直接调用原有接口
      await settleBill(userStore.checkInInfo.recordId, settleForm.value.paymentMethod)
      ElMessage.success('支付成功')
      showSettleDialog.value = false
      loadBill() // 刷新账单
    }
  } catch (error) {
    ElMessage.error('支付失败：' + (error.message || '未知错误'))
  } finally {
    settling.value = false
  }
}

// 开始轮询检测支付状态
const startPolling = () => {
  stopPolling() // 先停止之前的轮询
  
  pollingTimer.value = setInterval(async () => {
    pollingCount.value++
    
    if (pollingCount.value > MAX_POLLING_COUNT) {
      stopPolling()
      ElMessage.warning('支付状态检测超时，请手动刷新页面查看支付结果')
      return
    }
    
    try {
      const res = await queryAlipayStatus(currentOutTradeNo.value)
      const { tradeStatus, success } = res
      
      if (success) {
        stopPolling()
        ElMessage.success('支付成功！')
        loadBill() // 刷新账单
      } else if (tradeStatus === 'TRADE_CLOSED') {
        stopPolling()
        ElMessage.error('交易已关闭')
      }
      // WAIT_BUYER_PAY 或 null 继续轮询
    } catch (error) {
      console.error('轮询支付状态失败:', error)
    }
  }, 5000) // 每5秒查询一次
}

// 停止轮询
const stopPolling = () => {
  if (pollingTimer.value) {
    clearInterval(pollingTimer.value)
    pollingTimer.value = null
  }
}

// 格式化时间
const formatTime = (time) => {
  if (!time) return '-'
  const date = new Date(time)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit',
    hour12: false
  })
}

// 合计行
const getSummaries = () => {
  return ['', '', '', '合计']
}

onMounted(() => {
  loadBill()
})

onUnmounted(() => {
  stopPolling() // 组件销毁时停止轮询
})
</script>

<style scoped>
.billing {
  padding: 20px;
}

.loading {
  padding: 20px;
}

.bill-detail {
  margin-top: 20px;
}

.amount {
  font-size: 16px;
  font-weight: bold;
  color: #333;
  margin-top: 5px;
}

.amount-link {
  font-size: 16px;
  font-weight: bold;
  color: #409eff;
  cursor: pointer;
  margin-top: 5px;
}

.amount-link:hover {
  text-decoration: underline;
}

.total-amount {
  font-size: 20px;
  font-weight: bold;
  color: #f56c6c;
}

.unpaid-amount {
  font-size: 18px;
  font-weight: bold;
  color: #e6a23c;
}

.remark {
  color: #909399;
  font-size: 12px;
  line-height: 1.8;
}

.remark p {
  margin: 0;
}

.actions {
  margin-top: 30px;
  text-align: center;
}

.actions .el-button {
  min-width: 150px;
}

.settle-amount {
  font-size: 24px;
  font-weight: bold;
  color: #f56c6c;
}
</style>
