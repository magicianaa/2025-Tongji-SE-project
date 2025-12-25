<template>
  <div class="product-management">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>商品管理</span>
          <el-button type="primary" @click="handleAdd">添加商品</el-button>
        </div>
      </template>

      <!-- 搜索栏 -->
      <el-form :inline="true" class="search-form">
        <el-form-item label="商品类型">
          <el-select v-model="searchForm.productType" clearable placeholder="全部">
            <el-option label="全部" value="" />
            <el-option label="零食" value="SNACK" />
            <el-option label="饮料" value="BEVERAGE" />
            <el-option label="外设" value="PERIPHERAL" />
            <el-option label="其他" value="OTHER" />
          </el-select>
        </el-form-item>
        <el-form-item label="关键词">
          <el-input v-model="searchForm.keyword" placeholder="商品名称" clearable />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadProducts">查询</el-button>
        </el-form-item>
      </el-form>

      <!-- 商品表格 -->
      <el-table :data="products" border>
        <el-table-column prop="productId" label="ID" width="80" />
        <el-table-column prop="productName" label="商品名称" />
        <el-table-column prop="productType" label="类型" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.productType === 'SNACK'" type="warning">零食</el-tag>
            <el-tag v-else-if="row.productType === 'BEVERAGE'" type="success">饮料</el-tag>
            <el-tag v-else-if="row.productType === 'PERIPHERAL'" type="info">外设</el-tag>
            <el-tag v-else-if="row.productType === 'OTHER'" type="">其他</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="category" label="分类" width="120" />
        <el-table-column prop="price" label="价格" width="100">
          <template #default="{ row }">
            ¥{{ row.price }}
          </template>
        </el-table-column>
        <el-table-column prop="stockQuantity" label="库存" width="100">
          <template #default="{ row }">
            <el-badge
              :value="row.isLowStock ? '低库存' : ''"
              :type="row.isLowStock ? 'danger' : 'success'"
            >
              <span :style="{ color: row.isLowStock ? 'red' : '' }">
                {{ row.stockQuantity }}
              </span>
            </el-badge>
          </template>
        </el-table-column>
        <el-table-column prop="isAvailable" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.isAvailable ? 'success' : 'danger'">
              {{ row.isAvailable ? '上架' : '下架' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="250" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleEdit(row)">编辑</el-button>
            <el-button link type="primary" @click="handleStock(row)">调整库存</el-button>
            <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <el-pagination
        v-model:current-page="pagination.pageNum"
        v-model:page-size="pagination.pageSize"
        :total="pagination.total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="loadProducts"
        @current-change="loadProducts"
        class="pagination"
      />
    </el-card>

    <!-- 添加/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="600px"
    >
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="商品名称" prop="productName">
          <el-input v-model="form.productName" />
        </el-form-item>
        <el-form-item label="商品类型" prop="productType">
          <el-select v-model="form.productType">
            <el-option label="零食" value="SNACK" />
            <el-option label="饮料" value="BEVERAGE" />
            <el-option label="外设" value="PERIPHERAL" />
          </el-select>
        </el-form-item>
        <el-form-item label="分类" prop="category">
          <el-input v-model="form.category" />
        </el-form-item>
        <el-form-item label="价格" prop="price">
          <el-input-number v-model="form.price" :min="0" :precision="2" />
        </el-form-item>
        <el-form-item label="库存数量" prop="stockQuantity">
          <el-input-number v-model="form.stockQuantity" :min="0" />
        </el-form-item>
        <el-form-item label="库存阈值" prop="stockThreshold">
          <el-input-number v-model="form.stockThreshold" :min="0" />
        </el-form-item>
        <el-form-item label="租赁单位" prop="rentalUnit" v-if="form.productType === 'PERIPHERAL'">
          <el-select v-model="form.rentalUnit">
            <el-option label="不可租赁" value="NONE" />
            <el-option label="按次计费" value="PER_TIME" />
          </el-select>
        </el-form-item>
        <el-form-item label="商品描述" prop="description">
          <el-input type="textarea" v-model="form.description" :rows="3" />
        </el-form-item>
        <el-form-item label="状态" prop="isAvailable">
          <el-switch v-model="form.isAvailable" active-text="上架" inactive-text="下架" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <!-- 调整库存对话框 -->
    <el-dialog v-model="stockDialogVisible" title="调整库存" width="400px">
      <el-form :model="stockForm" label-width="100px">
        <el-form-item label="当前库存">
          <span>{{ stockForm.currentStock }}</span>
        </el-form-item>
        <el-form-item label="调整数量">
          <el-input-number v-model="stockForm.quantity" :min="-stockForm.currentStock" />
          <div class="tip">正数为增加，负数为减少</div>
        </el-form-item>
        <el-form-item label="调整后">
          <span>{{ stockForm.currentStock + stockForm.quantity }}</span>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="stockDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleStockSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getProducts, addProduct, updateProduct, deleteProduct, updateStock } from '@/api/product'

// 搜索表单
const searchForm = reactive({
  productType: '',
  keyword: ''
})

// 分页
const pagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
})

// 商品列表
const products = ref([])

// 对话框
const dialogVisible = ref(false)
const dialogTitle = ref('')
const formRef = ref()

// 表单
const form = reactive({
  productId: null,
  productName: '',
  productType: 'SNACK',
  category: '',
  price: 0,
  stockQuantity: 0,
  stockThreshold: 5,
  rentalUnit: 'NONE',
  description: '',
  isAvailable: true
})

// 表单验证
const rules = {
  productName: [{ required: true, message: '请输入商品名称', trigger: 'blur' }],
  productType: [{ required: true, message: '请选择商品类型', trigger: 'change' }],
  price: [{ required: true, message: '请输入价格', trigger: 'blur' }]
}

// 库存对话框
const stockDialogVisible = ref(false)
const stockForm = reactive({
  productId: null,
  currentStock: 0,
  quantity: 0
})

// 加载商品列表
const loadProducts = async () => {
  try {
    const params = {
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize,
      productType: searchForm.productType,
      keyword: searchForm.keyword
    }
    const res = await getProducts(params)
    products.value = res.records || []
    pagination.total = res.total || 0
  } catch (error) {
    ElMessage.error('加载商品列表失败')
  }
}

// 添加商品
const handleAdd = () => {
  dialogTitle.value = '添加商品'
  Object.assign(form, {
    productId: null,
    productName: '',
    productType: 'SNACK',
    category: '',
    price: 0,
    stockQuantity: 0,
    stockThreshold: 5,
    rentalUnit: 'NONE',
    description: '',
    isAvailable: true
  })
  dialogVisible.value = true
}

// 编辑商品
const handleEdit = (row) => {
  dialogTitle.value = '编辑商品'
  Object.assign(form, row)
  dialogVisible.value = true
}

// 提交表单
const handleSubmit = async () => {
  try {
    await formRef.value.validate()
    if (form.productId) {
      await updateProduct(form.productId, form)
      ElMessage.success('更新成功')
    } else {
      await addProduct(form)
      ElMessage.success('添加成功')
    }
    dialogVisible.value = false
    loadProducts()
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

// 调整库存
const handleStock = (row) => {
  stockForm.productId = row.productId
  stockForm.currentStock = row.stockQuantity
  stockForm.quantity = 0
  stockDialogVisible.value = true
}

// 提交库存调整
const handleStockSubmit = async () => {
  try {
    await updateStock(stockForm.productId, stockForm.quantity)
    ElMessage.success('库存调整成功')
    stockDialogVisible.value = false
    loadProducts()
  } catch (error) {
    ElMessage.error('库存调整失败')
  }
}

// 删除商品
const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除该商品吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await deleteProduct(row.productId)
    ElMessage.success('删除成功')
    loadProducts()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

onMounted(() => {
  loadProducts()
})
</script>

<style scoped>
.product-management {
  padding: 20px;
  height: calc(100vh - 120px);
  overflow-y: auto;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.search-form {
  margin-bottom: 20px;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.tip {
  font-size: 12px;
  color: #999;
  margin-top: 5px;
}
</style>
