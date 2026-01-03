<template>
  <div class="points-shop-management">
    <!-- 商品管理卡片 -->
    <el-card>
      <template #header>
        <div class="card-header">
          <span>积分商品管理</span>
          <el-button type="primary" @click="showCreateDialog">
            <el-icon><Plus /></el-icon>
            从商品库上架
          </el-button>
        </div>
      </template>

      <el-table :data="products" border v-loading="loading">
        <el-table-column prop="pointsProductId" label="商品ID" width="80" />
        <el-table-column prop="productName" label="商品名称" width="200" />
        <el-table-column prop="description" label="商品描述" min-width="250" show-overflow-tooltip />
        <el-table-column prop="pointsRequired" label="所需积分" width="120">
          <template #default="{ row }">
            <el-tag type="warning">{{ row.pointsRequired }}分</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="stock" label="库存" width="100">
          <template #default="{ row }">
            <el-tag :type="row.stock > 5 ? 'success' : row.stock > 0 ? 'warning' : 'danger'">
              {{ row.stock }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="isAvailable" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.isAvailable ? 'success' : 'danger'">
              {{ row.isAvailable ? '上架' : '下架' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="relatedProductId" label="关联商品" width="120">
          <template #default="{ row }">
            <el-tag v-if="row.relatedProductId" type="info">ID:{{ row.relatedProductId }}</el-tag>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="editProduct(row)">编辑</el-button>
            <el-button link type="warning" @click="toggleAvailability(row)" v-if="row.isAvailable">下架</el-button>
            <el-button link type="success" @click="toggleAvailability(row)" v-else>上架</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 兑换订单管理卡片 -->
    <el-card style="margin-top: 20px;">
      <template #header>
        <div class="card-header">
          <span>兑换订单管理</span>
          <el-select v-model="redemptionStatusFilter" @change="loadRedemptions" placeholder="筛选状态" clearable>
            <el-option label="待配送" value="PENDING" />
            <el-option label="已配送" value="FULFILLED" />
            <el-option label="已取消" value="CANCELLED" />
          </el-select>
        </div>
      </template>

      <el-table :data="redemptions" border v-loading="redemptionsLoading">
        <el-table-column prop="redemptionId" label="订单ID" width="80" />
        <el-table-column prop="guestUsername" label="兑换人" width="120" />
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
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.status === 'PENDING'" type="warning">待配送</el-tag>
            <el-tag v-else-if="row.status === 'FULFILLED'" type="success">已配送</el-tag>
            <el-tag v-else type="danger">已取消</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button 
              link 
              type="success" 
              @click="updateRedemptionStatusConfirm(row, 'FULFILLED')" 
              v-if="row.status === 'PENDING'"
            >
              标记已配送
            </el-button>
            <el-button 
              link 
              type="danger" 
              @click="updateRedemptionStatusConfirm(row, 'CANCELLED')" 
              v-if="row.status === 'PENDING'"
            >
              取消订单
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 创建/编辑商品对话框 -->
    <el-dialog v-model="productDialogVisible" :title="isEditMode ? '编辑商品' : '上架商品到积分商城'" width="700px">
      <el-form :model="productForm" label-width="120px">
        <!-- 选择商品（仅新建时） -->
        <el-form-item label="选择商品" v-if="!isEditMode">
          <el-select 
            v-model="productForm.relatedProductId" 
            placeholder="从商品库选择" 
            filterable
            @change="handleProductSelect"
            style="width: 100%"
          >
            <el-option 
              v-for="product in availableProducts" 
              :key="product.productId" 
              :label="`${product.productName} (库存:${product.stockQuantity})`" 
              :value="product.productId"
            />
          </el-select>
          <div class="tip">从普通商品库中选择商品上架到积分商城</div>
        </el-form-item>
        
        <!-- 商品名称 -->
        <el-form-item label="商品名称">
          <el-input v-model="productForm.productName" :disabled="isEditMode || productForm.relatedProductId" />
        </el-form-item>
        
        <!-- 商品描述 -->
        <el-form-item label="商品描述">
          <el-input v-model="productForm.description" type="textarea" :rows="3" placeholder="商品详细描述" />
        </el-form-item>
        
        <!-- 所需积分 -->
        <el-form-item label="所需积分">
          <el-input-number v-model="productForm.pointsRequired" :min="1" :step="100" />
        </el-form-item>
        
        <!-- 库存数量 -->
        <el-form-item label="库存数量">
          <el-input-number v-model="productForm.stock" :min="0" :step="1" />
          <div class="tip" v-if="productForm.relatedProductId">关联商品库存将同步到积分商城</div>
        </el-form-item>
        
        <!-- 商品图片URL -->
        <el-form-item label="商品图片URL">
          <el-input v-model="productForm.imageUrl" placeholder="https://..." />
        </el-form-item>
        
        <!-- 商品类型 -->
        <el-form-item label="商品类型">
          <el-radio-group v-model="productForm.productType">
            <el-radio label="PHYSICAL">实物商品</el-radio>
            <el-radio label="SERVICE_PRIVILEGE">服务特权</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="productDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveProduct">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { 
  getPointsProducts, 
  createPointsProduct, 
  updatePointsProduct, 
  deletePointsProduct,
  getAllRedemptions,
  updateRedemptionStatus
} from '@/api/pointsShop'
import { getAvailableProducts } from '@/api/product'

// 商品列表
const products = ref([])
const loading = ref(false)

// 可用商品列表（用于选择）
const availableProducts = ref([])

// 兑换订单列表
const redemptions = ref([])
const redemptionsLoading = ref(false)
const redemptionStatusFilter = ref('')

// 商品对话框
const productDialogVisible = ref(false)
const isEditMode = ref(false)
const productForm = ref({
  productName: '',
  description: '',
  pointsRequired: 500,
  stock: 10,
  imageUrl: '',
  productType: 'PHYSICAL',
  relatedProductId: null
})

// 加载商品列表
const loadProducts = async () => {
  loading.value = true
  try {
    products.value = await getPointsProducts(false)  // 显示所有商品（包括已下架）
  } catch (error) {
    ElMessage.error('加载商品列表失败')
    console.error(error)
  } finally {
    loading.value = false
  }
}

// 加载可用商品（用于选择）
const loadAvailableProducts = async () => {
  try {
    availableProducts.value = await getAvailableProducts()
  } catch (error) {
    ElMessage.error('加载可用商品失败')
    console.error(error)
  }
}

// 加载兑换订单
const loadRedemptions = async () => {
  redemptionsLoading.value = true
  try {
    redemptions.value = await getAllRedemptions(redemptionStatusFilter.value || null)
  } catch (error) {
    ElMessage.error('加载兑换订单失败')
    console.error(error)
  } finally {
    redemptionsLoading.value = false
  }
}

// 显示创建对话框
const showCreateDialog = async () => {
  await loadAvailableProducts()
  isEditMode.value = false
  productForm.value = {
    productName: '',
    description: '',
    pointsRequired: 500,
    stock: 10,
    imageUrl: '',
    productType: 'PHYSICAL',
    relatedProductId: null
  }
  productDialogVisible.value = true
}

// 处理商品选择
const handleProductSelect = (productId) => {
  const selected = availableProducts.value.find(p => p.productId === productId)
  if (selected) {
    productForm.value.productName = selected.productName
    productForm.value.description = selected.description || ''
    productForm.value.stock = selected.stockQuantity
    productForm.value.imageUrl = selected.imageUrl || ''
  }
}

// 编辑商品
const editProduct = (product) => {
  isEditMode.value = true
  productForm.value = { ...product }
  productDialogVisible.value = true
}

// 保存商品
const saveProduct = async () => {
  try {
    if (!productForm.value.productName) {
      ElMessage.warning('请输入商品名称')
      return
    }
    
    if (isEditMode.value) {
      await updatePointsProduct(productForm.value.pointsProductId, productForm.value)
      ElMessage.success('商品更新成功')
    } else {
      await createPointsProduct(productForm.value)
      ElMessage.success('商品上架成功')
    }
    productDialogVisible.value = false
    loadProducts()
  } catch (error) {
    ElMessage.error(error.message || '操作失败')
    console.error(error)
  }
}

// 切换上架/下架状态
const toggleAvailability = async (product) => {
  const action = product.isAvailable ? '下架' : '上架'
  try {
    await ElMessageBox.confirm(`确定要${action}此商品吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const updatedProduct = { ...product, isAvailable: !product.isAvailable }
    await updatePointsProduct(product.pointsProductId, updatedProduct)
    ElMessage.success(`商品已${action}`)
    loadProducts()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('操作失败')
      console.error(error)
    }
  }
}

// 更新兑换订单状态确认
const updateRedemptionStatusConfirm = async (redemption, status) => {
  const statusText = status === 'FULFILLED' ? '已配送' : '已取消'
  try {
    await ElMessageBox.confirm(`确定要标记此订单为"${statusText}"吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await updateRedemptionStatus(redemption.redemptionId, status, null)
    ElMessage.success('订单状态已更新')
    loadRedemptions()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('操作失败')
      console.error(error)
    }
  }
}

// 格式化时间
const formatTime = (time) => {
  if (!time) return '-'
  return new Date(time).toLocaleString('zh-CN')
}

onMounted(() => {
  loadProducts()
  loadRedemptions()
})
</script>

<style scoped>
.points-shop-management {
  padding: 20px;
  overflow-y: auto;
  max-height: calc(100vh - 120px);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.tip {
  font-size: 12px;
  color: #999;
  margin-top: 5px;
}
</style>
