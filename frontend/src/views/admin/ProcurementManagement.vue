<template>
  <div class="procurement-management">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>进货管理</span>
          <el-button type="primary" @click="showAddDialog">新增进货</el-button>
        </div>
      </template>

      <!-- 查询表单 -->
      <el-form :inline="true" :model="queryForm" class="query-form">
        <el-form-item label="商品名称">
          <el-input v-model="queryForm.productName" placeholder="请输入商品名称" clearable />
        </el-form-item>
        <el-form-item label="供应商">
          <el-input v-model="queryForm.supplier" placeholder="请输入供应商" clearable />
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
          <el-button type="primary" @click="loadProcurementList">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 进货列表 -->
      <el-table :data="procurementList" border stripe v-loading="loading">
        <el-table-column type="index" label="序号" width="60" />
        <el-table-column prop="productName" label="商品名称" min-width="120" />
        <el-table-column prop="quantity" label="进货数量" width="100" align="center" />
        <el-table-column prop="unitPrice" label="单价" width="100" align="center">
          <template #default="{ row }">
            ¥{{ row.unitPrice.toFixed(2) }}
          </template>
        </el-table-column>
        <el-table-column prop="totalCost" label="总成本" width="120" align="center">
          <template #default="{ row }">
            ¥{{ row.totalCost.toFixed(2) }}
          </template>
        </el-table-column>
        <el-table-column prop="supplier" label="供应商" min-width="150" />
        <el-table-column prop="operatorName" label="操作员" width="100" />
        <el-table-column prop="procurementTime" label="进货时间" width="160">
          <template #default="{ row }">
            {{ formatDateTime(row.procurementTime) }}
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
        @size-change="loadProcurementList"
        @current-change="loadProcurementList"
        class="pagination"
      />
    </el-card>

    <!-- 新增进货对话框 -->
    <el-dialog
      v-model="dialogVisible"
      title="新增进货"
      width="600px"
      @close="resetForm"
    >
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="商品" prop="productId">
          <el-select
            v-model="form.productId"
            placeholder="请选择商品"
            filterable
            style="width: 100%"
            @change="handleProductChange"
          >
            <el-option
              v-for="product in productList"
              :key="product.productId"
              :label="`${product.productName} (库存: ${product.stockQuantity})`"
              :value="product.productId"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="进货数量" prop="quantity">
          <el-input-number
            v-model="form.quantity"
            :min="1"
            :max="10000"
            style="width: 100%"
            placeholder="请输入进货数量"
          />
        </el-form-item>
        <el-form-item label="单价" prop="unitPrice">
          <el-input-number
            v-model="form.unitPrice"
            :min="0.01"
            :precision="2"
            style="width: 100%"
            placeholder="请输入单价"
          />
        </el-form-item>
        <el-form-item label="总成本">
          <el-input
            :model-value="totalCost"
            disabled
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="供应商" prop="supplier">
          <el-input v-model="form.supplier" placeholder="请输入供应商名称" />
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
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getProcurementList, createProcurement } from '@/api/procurement'
import { getProducts } from '@/api/product'

// 查询表单
const queryForm = reactive({
  pageNum: 1,
  pageSize: 10,
  productName: '',
  supplier: '',
  dateRange: []
})

// 数据列表
const procurementList = ref([])
const total = ref(0)
const loading = ref(false)

// 商品列表
const productList = ref([])

// 对话框
const dialogVisible = ref(false)
const formRef = ref(null)
const submitting = ref(false)

// 表单数据
const form = reactive({
  productId: null,
  quantity: 1,
  unitPrice: 0,
  supplier: '',
  notes: ''
})

// 计算总成本
const totalCost = computed(() => {
  return `¥${(form.quantity * form.unitPrice).toFixed(2)}`
})

// 表单验证规则
const rules = {
  productId: [{ required: true, message: '请选择商品', trigger: 'change' }],
  quantity: [{ required: true, message: '请输入进货数量', trigger: 'blur' }],
  unitPrice: [{ required: true, message: '请输入单价', trigger: 'blur' }],
  supplier: [{ required: true, message: '请输入供应商名称', trigger: 'blur' }]
}

// 加载进货列表
const loadProcurementList = async () => {
  loading.value = true
  try {
    const params = {
      pageNum: queryForm.pageNum,
      pageSize: queryForm.pageSize
    }
    
    if (queryForm.productName) {
      params.productName = queryForm.productName
    }
    if (queryForm.supplier) {
      params.supplier = queryForm.supplier
    }
    if (queryForm.dateRange && queryForm.dateRange.length === 2) {
      params.startDate = queryForm.dateRange[0]
      params.endDate = queryForm.dateRange[1]
    }
    
    const response = await getProcurementList(params)
    procurementList.value = response.records
    total.value = response.total
  } catch (error) {
    console.error('加载进货列表失败:', error)
    ElMessage.error('加载进货列表失败')
  } finally {
    loading.value = false
  }
}

// 加载商品列表
const loadProductList = async () => {
  try {
    const response = await getProducts({ pageSize: 1000 })
    productList.value = response.records || []
  } catch (error) {
    console.error('加载商品列表失败:', error)
    ElMessage.error('加载商品列表失败')
  }
}

// 商品选择变化
const handleProductChange = (productId) => {
  const product = productList.value.find(p => p.productId === productId)
  if (product && product.price) {
    form.unitPrice = product.price
  }
}

// 显示新增对话框
const showAddDialog = () => {
  dialogVisible.value = true
  loadProductList()
}

// 重置表单
const resetForm = () => {
  formRef.value?.resetFields()
  form.productId = null
  form.quantity = 1
  form.unitPrice = 0
  form.supplier = ''
  form.notes = ''
}

// 提交表单
const handleSubmit = async () => {
  try {
    await formRef.value.validate()
    
    submitting.value = true
    
    const data = {
      productId: form.productId,
      quantity: form.quantity,
      unitCost: form.unitPrice,  // 后端接收的字段名是 unitCost
      supplier: form.supplier,
      notes: form.notes
    }
    
    await createProcurement(data)
    ElMessage.success('进货记录添加成功')
    dialogVisible.value = false
    loadProcurementList()
  } catch (error) {
    if (error !== false) { // 排除表单验证失败的情况
      console.error('添加进货记录失败:', error)
      ElMessage.error(error.response?.data?.message || '添加进货记录失败')
    }
  } finally {
    submitting.value = false
  }
}

// 重置查询
const resetQuery = () => {
  queryForm.pageNum = 1
  queryForm.pageSize = 10
  queryForm.productName = ''
  queryForm.supplier = ''
  queryForm.dateRange = []
  loadProcurementList()
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
  loadProcurementList()
})
</script>

<style scoped>
.procurement-management {
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
</style>
