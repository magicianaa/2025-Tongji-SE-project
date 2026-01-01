<template>
  <div class="points-shop">
    <!-- 积分余额卡片 -->
    <el-card>
      <template #header>
        <span>积分余额</span>
      </template>
      <div class="balance-info">
        <el-statistic title="可用积分" :value="currentPoints" suffix="分" />
      </div>
    </el-card>

    <!-- 商品列表 -->
    <el-card style="margin-top: 20px;">
      <template #header>
        <span>积分商城</span>
      </template>
      <el-row :gutter="20">
        <el-col :span="6" v-for="product in products" :key="product.pointsProductId">
          <el-card shadow="hover" class="product-card">
            <div class="product-icon">
              <el-icon :size="80" color="#409EFF"><ShoppingBag /></el-icon>
            </div>
            <div class="product-info">
              <h3>{{ product.productName }}</h3>
              <p class="product-description">{{ product.description }}</p>
              <div class="product-footer">
                <el-tag type="warning" size="large">{{ product.pointsRequired }}分</el-tag>
                <div class="stock-info">
                  <span :class="{ 'out-of-stock': product.stock === 0 }">
                    库存: {{ product.stock }}
                  </span>
                </div>
              </div>
              <el-button 
                type="primary" 
                @click="redeemProductConfirm(product)" 
                :disabled="product.stock === 0 || currentPoints < product.pointsRequired"
                style="width: 100%; margin-top: 10px;"
              >
                {{ product.stock === 0 ? '已售罄' : (currentPoints < product.pointsRequired ? '积分不足' : '立即兑换') }}
              </el-button>
            </div>
          </el-card>
        </el-col>
      </el-row>
      <el-empty description="暂无可兑换商品" v-if="products.length === 0" />
    </el-card>

    <!-- 我的兑换记录 -->
    <el-card style="margin-top: 20px;">
      <template #header>
        <div class="card-header">
          <span>我的兑换记录</span>
          <el-button @click="loadMyRedemptions">
            <el-icon><Refresh /></el-icon>
            刷新
          </el-button>
        </div>
      </template>
      <el-table :data="myRedemptions" border v-loading="redemptionsLoading">
        <el-table-column prop="redemptionId" label="订单号" width="100" />
        <el-table-column prop="productName" label="商品名称" width="180" />
        <el-table-column prop="pointsCost" label="消费积分" width="120">
          <template #default="{ row }">
            <el-tag type="warning">{{ row.pointsCost }}分</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="redemptionTime" label="兑换时间" width="160">
          <template #default="{ row }">
            {{ formatTime(row.redemptionTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="订单状态" width="120">
          <template #default="{ row }">
            <el-tag v-if="row.status === 'PENDING'" type="warning">待配送</el-tag>
            <el-tag v-else-if="row.status === 'FULFILLED'" type="success">已配送</el-tag>
            <el-tag v-else type="danger">已取消</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="fulfillmentNotes" label="备注" min-width="200">
          <template #default="{ row }">
            {{ row.fulfillmentNotes || '-' }}
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Refresh, ShoppingBag } from '@element-plus/icons-vue'
import { getPointsProducts, redeemProduct, getMyRedemptions } from '@/api/pointsShop'
import { getPointsBalance } from '@/api/points'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()

// 积分余额
const currentPoints = ref(0)

// 商品列表
const products = ref([])
const loading = ref(false)

// 兑换记录
const myRedemptions = ref([])
const redemptionsLoading = ref(false)

// 加载积分余额
const loadPointsBalance = async () => {
  try {
    const data = await getPointsBalance()
    currentPoints.value = data.currentPoints || 0
  } catch (error) {
    ElMessage.error('加载积分信息失败')
    console.error(error)
  }
}

// 加载商品列表
const loadProducts = async () => {
  loading.value = true
  try {
    products.value = await getPointsProducts(true)  // 仅显示上架商品
  } catch (error) {
    ElMessage.error('加载商品列表失败')
    console.error(error)
  } finally {
    loading.value = false
  }
}

// 加载我的兑换记录
const loadMyRedemptions = async () => {
  redemptionsLoading.value = true
  try {
    myRedemptions.value = await getMyRedemptions()
  } catch (error) {
    ElMessage.error('加载兑换记录失败')
    console.error(error)
  } finally {
    redemptionsLoading.value = false
  }
}

// 兑换商品确认
const redeemProductConfirm = async (product) => {
  try {
    await ElMessageBox.confirm(
      `确定要花费 ${product.pointsRequired} 积分兑换"${product.productName}"吗？`,
      '确认兑换',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    await redeemProduct(product.pointsProductId)
    ElMessage.success('兑换成功！请等待配送')
    
    // 刷新数据
    loadPointsBalance()
    loadProducts()
    loadMyRedemptions()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '兑换失败')
      console.error(error)
    }
  }
}

// 图片加载失败处理
const handleImageError = (e) => {
  e.target.src = '/placeholder.jpg'
}

// 格式化时间
const formatTime = (time) => {
  if (!time) return '-'
  return new Date(time).toLocaleString('zh-CN')
}

onMounted(() => {
  loadPointsBalance()
  loadProducts()
  loadMyRedemptions()
})
</script>

<style scoped>
.points-shop {
  overflow-y: auto;
  max-height: calc(100vh - 120px);
}

.shop-container {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.balance-info {
  padding: 20px 0;
  text-align: center;
}

.product-card {
  margin-bottom: 20px;
  height: 420px;
  display: flex;
  flex-direction: column;
}

.product-icon {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 120px;
  margin-bottom: 15px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 8px;
}

.product-info {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.product-info h3 {
  margin: 0 0 10px 0;
  font-size: 16px;
  font-weight: bold;
}

.product-description {
  flex: 1;
  color: #606266;
  font-size: 14px;
  line-height: 1.5;
  margin-bottom: 15px;
}

.product-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.stock-info {
  font-size: 14px;
  color: #606266;
}

.out-of-stock {
  color: #f56c6c;
}
</style>
