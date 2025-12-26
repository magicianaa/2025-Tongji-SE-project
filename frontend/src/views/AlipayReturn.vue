<template>
  <div class="alipay-return">
    <el-result
      :icon="paymentStatus"
      :title="title"
      :sub-title="subTitle"
    >
      <template #extra>
        <el-space direction="vertical" :size="20">
          <el-descriptions :column="1" border v-if="orderInfo.out_trade_no">
            <el-descriptions-item label="商户订单号">
              {{ orderInfo.out_trade_no }}
            </el-descriptions-item>
            <el-descriptions-item label="支付宝交易号" v-if="orderInfo.trade_no">
              {{ orderInfo.trade_no }}
            </el-descriptions-item>
            <el-descriptions-item label="支付金额" v-if="orderInfo.total_amount">
              ¥{{ orderInfo.total_amount }}
            </el-descriptions-item>
            <el-descriptions-item label="支付时间" v-if="orderInfo.timestamp">
              {{ formatTime(orderInfo.timestamp) }}
            </el-descriptions-item>
          </el-descriptions>

          <el-space>
            <el-button type="primary" size="large" @click="closeWindow">
              关闭此窗口
            </el-button>
            <el-button size="large" @click="returnToOrigin">
              返回原页面
            </el-button>
          </el-space>

          <el-alert
            type="info"
            :closable="false"
            show-icon
          >
            <template #default>
              <p>请关闭此窗口，返回原页面刷新查看最新订单状态</p>
              <p v-if="paymentStatus === 'success'">支付成功后，订单状态会在几秒内自动更新</p>
            </template>
          </el-alert>
        </el-space>
      </template>
    </el-result>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()

const paymentStatus = ref('info')
const title = ref('处理中...')
const subTitle = ref('正在处理您的支付请求')
const orderInfo = ref({})

onMounted(() => {
  // 获取支付宝返回的所有参数
  const params = route.query
  orderInfo.value = params

  // 根据支付宝返回参数判断支付状态
  // 注意：这里只是前端展示，真实支付状态以后端异步通知为准
  if (params.out_trade_no) {
    // 有订单号说明支付流程已完成
    paymentStatus.value = 'success'
    title.value = '支付成功'
    subTitle.value = '您的支付已提交，订单状态正在更新中...'
  } else {
    paymentStatus.value = 'warning'
    title.value = '支付状态未知'
    subTitle.value = '未能获取支付结果，请返回原页面查看订单状态'
  }
})

const formatTime = (timestamp) => {
  if (!timestamp) return '-'
  const date = new Date(timestamp)
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

const closeWindow = () => {
  window.close()
}

const returnToOrigin = () => {
  // 尝试返回上一页
  if (window.opener) {
    // 如果是弹窗打开的，刷新父窗口
    window.opener.location.reload()
    window.close()
  } else {
    // 否则返回首页或登录页
    window.location.href = '/'
  }
}
</script>

<style scoped>
.alipay-return {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 20px;
}

.el-result {
  background: white;
  border-radius: 8px;
  padding: 40px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
  max-width: 600px;
}

.el-alert {
  margin-top: 20px;
}

.el-alert p {
  margin: 5px 0;
  line-height: 1.6;
}
</style>
