<template>
  <div class="shopping-mall">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>商城点餐</span>
          <el-badge :value="cartCount" class="cart-badge">
            <el-button type="primary" @click="showCart = true">
              购物车
            </el-button>
          </el-badge>
        </div>
      </template>

      <!-- 商品分类 -->
      <el-tabs v-model="activeTab" @tab-click="handleTabClick">
        <el-tab-pane label="全部" name="ALL" />
        <el-tab-pane label="零食" name="SNACK" />
        <el-tab-pane label="饮料" name="BEVERAGE" />
        <el-tab-pane label="外设租赁" name="PERIPHERAL" />
      </el-tabs>

      <!-- 商品列表 -->
      <div class="product-grid">
        <el-card
          v-for="product in filteredProducts"
          :key="product.productId"
          class="product-card"
          shadow="hover"
        >
          <div class="product-info">
            <h3>{{ product.productName }}</h3>
            <p class="category">{{ product.category }}</p>
            <p class="description">{{ product.description }}</p>
            <div class="price-row">
              <span class="price">¥{{ product.price }}</span>
              <el-tag v-if="product.productType === 'PERIPHERAL'" size="small" type="warning">
                租赁
              </el-tag>
            </div>
            <div class="stock-row">
              <span :class="{ 'low-stock': product.isLowStock }">
                库存：{{ product.stockQuantity }}
              </span>
            </div>
            <el-button
              type="primary"
              :disabled="product.stockQuantity === 0"
              @click="addToCart(product)"
              class="add-btn"
            >
              {{ product.stockQuantity === 0 ? '缺货' : '加入购物车' }}
            </el-button>
          </div>
        </el-card>
      </div>
    </el-card>

    <!-- 购物车抽屉 -->
    <el-drawer v-model="showCart" title="购物车" size="40%">
      <div class="cart-content">
        <el-empty v-if="cart.length === 0" description="购物车是空的" />
        <div v-else>
          <div v-for="item in cart" :key="item.productId" class="cart-item">
            <div class="item-info">
              <h4>{{ item.productName }}</h4>
              <p class="item-price">¥{{ item.price }}</p>
            </div>
            <div class="item-actions">
              <el-input-number
                v-model="item.quantity"
                :min="1"
                :max="item.stockQuantity"
                size="small"
              />
              <el-button
                link
                type="danger"
                @click="removeFromCart(item.productId)"
              >
                删除
              </el-button>
            </div>
          </div>

          <el-divider />

          <div class="cart-summary">
            <div class="summary-row">
              <span>商品总额：</span>
              <span class="total-amount">¥{{ totalAmount }}</span>
            </div>
            <el-button
              type="primary"
              size="large"
              @click="handleCheckout"
              :loading="submitting"
              class="checkout-btn"
            >
              提交订单
            </el-button>
          </div>
        </div>
      </div>
    </el-drawer>

    <!-- 我的订单 -->
    <el-card class="my-orders" v-if="myOrders.length > 0">
      <template #header>
        <span>我的订单</span>
      </template>
      <el-timeline>
        <el-timeline-item
          v-for="order in myOrders"
          :key="order.orderId"
          :timestamp="formatTime(order.createTime)"
          placement="top"
        >
          <el-card>
            <div class="order-info">
              <div>
                <el-tag v-if="order.status === 'PENDING'" type="warning">待配送</el-tag>
                <el-tag v-else-if="order.status === 'DELIVERED'" type="success">已配送</el-tag>
                <el-tag v-else-if="order.status === 'RETURNED'" type="info">已归还</el-tag>
                <span class="order-no">{{ order.orderNo }}</span>
              </div>
              <div class="order-amount">¥{{ order.totalAmount }}</div>
            </div>
            <div class="order-items">
              <span v-for="item in order.items" :key="item.itemId" class="item-tag">
                {{ item.productName }} x{{ item.quantity }}
              </span>
            </div>
          </el-card>
        </el-timeline-item>
      </el-timeline>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getAvailableProducts } from '@/api/product'
import { createOrder, getMyOrders } from '@/api/order'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()

// 商品列表
const products = ref([])
const activeTab = ref('ALL')

// 购物车
const cart = ref([])
const showCart = ref(false)
const submitting = ref(false)

// 我的订单
const myOrders = ref([])

// 过滤商品
const filteredProducts = computed(() => {
  if (activeTab.value === 'ALL') {
    return products.value
  }
  return products.value.filter(p => p.productType === activeTab.value)
})

// 购物车数量
const cartCount = computed(() => {
  return cart.value.reduce((sum, item) => sum + item.quantity, 0)
})

// 总金额
const totalAmount = computed(() => {
  return cart.value.reduce((sum, item) => sum + item.price * item.quantity, 0).toFixed(2)
})

// 加载商品
const loadProducts = async () => {
  try {
    const res = await getAvailableProducts()
    products.value = res || []
  } catch (error) {
    ElMessage.error('加载商品失败')
  }
}

// 加载我的订单
const loadMyOrders = async () => {
  try {
    if (!userStore.checkInInfo || !userStore.checkInInfo.recordId) {
      return
    }
    const res = await getMyOrders(userStore.checkInInfo.recordId)
    myOrders.value = res || []
  } catch (error) {
    console.error('加载订单失败', error)
  }
}

// 添加到购物车
const addToCart = (product) => {
  const existingItem = cart.value.find(item => item.productId === product.productId)
  if (existingItem) {
    if (existingItem.quantity < product.stockQuantity) {
      existingItem.quantity++
    } else {
      ElMessage.warning('库存不足')
    }
  } else {
    cart.value.push({
      productId: product.productId,
      productName: product.productName,
      productType: product.productType,
      price: product.price,
      stockQuantity: product.stockQuantity,
      quantity: 1
    })
  }
  ElMessage.success('已加入购物车')
}

// 从购物车移除
const removeFromCart = (productId) => {
  const index = cart.value.findIndex(item => item.productId === productId)
  if (index > -1) {
    cart.value.splice(index, 1)
  }
}

// 提交订单
const handleCheckout = async () => {
  if (!userStore.checkInInfo || !userStore.checkInInfo.recordId) {
    ElMessage.error('请先入住')
    return
  }

  if (cart.value.length === 0) {
    ElMessage.warning('购物车是空的')
    return
  }

  // 判断订单类型：如果有外设则为租赁订单
  const hasPeripheral = cart.value.some(item => item.productType === 'PERIPHERAL')
  const orderType = hasPeripheral ? 'RENTAL' : 'PURCHASE'

  try {
    submitting.value = true
    const orderData = {
      recordId: userStore.checkInInfo.recordId,
      orderType: orderType,
      items: cart.value.map(item => ({
        productId: item.productId,
        quantity: item.quantity
      }))
    }

    await createOrder(orderData)
    ElMessage.success('下单成功')
    cart.value = []
    showCart.value = false
    loadMyOrders()
    loadProducts()  // 刷新商品库存
  } catch (error) {
    ElMessage.error(error.message || '下单失败')
  } finally {
    submitting.value = false
  }
}

// 切换标签
const handleTabClick = () => {
  // 标签切换时的处理
}

// 格式化时间
const formatTime = (time) => {
  if (!time) return ''
  return new Date(time).toLocaleString('zh-CN')
}

onMounted(() => {
  loadProducts()
  loadMyOrders()
})
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.cart-badge {
  margin-right: 0;
}

.product-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
  gap: 20px;
  margin-top: 20px;
}

.product-card {
  transition: transform 0.2s;
}

.product-card:hover {
  transform: translateY(-5px);
}

.product-info h3 {
  margin: 0 0 10px 0;
  font-size: 16px;
}

.product-info .category {
  color: #999;
  font-size: 12px;
  margin: 5px 0;
}

.product-info .description {
  color: #666;
  font-size: 14px;
  margin: 10px 0;
  min-height: 40px;
}

.price-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin: 15px 0 10px 0;
}

.price {
  font-size: 24px;
  color: #f56c6c;
  font-weight: bold;
}

.stock-row {
  font-size: 14px;
  color: #666;
  margin-bottom: 15px;
}

.low-stock {
  color: #f56c6c;
}

.add-btn {
  width: 100%;
}

.cart-content {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.cart-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px 0;
  border-bottom: 1px solid #eee;
}

.item-info h4 {
  margin: 0 0 5px 0;
}

.item-price {
  color: #f56c6c;
  font-weight: bold;
}

.item-actions {
  display: flex;
  align-items: center;
  gap: 10px;
}

.cart-summary {
  margin-top: auto;
  padding-top: 20px;
}

.summary-row {
  display: flex;
  justify-content: space-between;
  font-size: 16px;
  margin-bottom: 20px;
}

.total-amount {
  font-size: 24px;
  color: #f56c6c;
  font-weight: bold;
}

.checkout-btn {
  width: 100%;
}

.my-orders {
  margin-top: 30px;
}

.order-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.order-no {
  margin-left: 10px;
  color: #666;
}

.order-amount {
  font-size: 18px;
  color: #f56c6c;
  font-weight: bold;
}

.order-items {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.item-tag {
  background: #f5f5f5;
  padding: 5px 10px;
  border-radius: 4px;
  font-size: 12px;
}
</style>
