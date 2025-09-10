<template>
  <div class="expense-list">
    <div class="header">
      <h2>费用记录</h2>
      <el-button type="primary" @click="showAddExpenseDialog">
        <el-icon><Plus /></el-icon>
        添加费用
      </el-button>
    </div>
    
    <!-- 费用列表 -->
    <el-table :data="expenses" style="width: 100%" v-loading="loading">
      <el-table-column prop="description" label="描述" width="180" />
      <el-table-column prop="amount" label="金额" width="100">
        <template #default="scope">
          <span class="amount">¥{{ scope.row.amount }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="categoryName" label="分类" width="100">
        <template #default="scope">
          <el-tag v-if="scope.row.categoryName" size="small">{{ scope.row.categoryName }}</el-tag>
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column prop="splitType" label="分摊方式" width="100">
        <template #default="scope">
          <el-tag :type="scope.row.splitType === '平均分摊' ? 'success' : 'warning'" size="small">
            {{ scope.row.splitType }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="payments" label="付款人" width="150">
        <template #default="scope">
          <div class="payments">
            <el-tag
              v-for="payment in scope.row.payments"
              :key="payment.id"
              type="info"
              size="small"
              class="payment-tag"
            >
              {{ payment.payerName }}: ¥{{ payment.amount }}
            </el-tag>
          </div>
        </template>
      </el-table-column>
      <el-table-column prop="expenseDate" label="日期" width="140">
        <template #default="scope">
          {{ formatDate(scope.row.expenseDate) }}
        </template>
      </el-table-column>
      <el-table-column label="分摊情况" min-width="200">
        <template #default="scope">
          <div class="shares">
            <el-tag
              v-for="share in scope.row.shares"
              :key="share.id"
              :type="share.isPaid ? 'success' : 'warning'"
              size="small"
              class="share-tag"
            >
              {{ share.userName }}: ¥{{ share.amount }}
              <el-icon v-if="share.isPaid" class="paid-icon"><Check /></el-icon>
            </el-tag>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="180">
        <template #default="scope">
          <div class="action-buttons">
            <el-button
              type="primary"
              size="small"
              @click="editExpense(scope.row)"
            >
              编辑
            </el-button>
            <template v-for="share in scope.row.shares || []" :key="share.id">
              <el-button
                  v-if="!share.isPaid"
                  type="success"
                  size="small"
                  @click="markAsPaid(share.id)"
              >
                标记已付
              </el-button>
            </template>
          </div>
        </template>
      </el-table-column>
    </el-table>
    
    <!-- 添加/编辑费用对话框 -->
    <el-dialog
      v-model="showAddDialog"
      :title="isEditMode ? '编辑费用记录' : '添加费用记录'"
      width="600px"
    >
      <el-form :model="newExpense" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="费用描述" prop="description">
          <el-input v-model="newExpense.description" placeholder="请输入费用描述" />
        </el-form-item>
        
        <el-form-item label="费用金额" prop="amount">
          <el-input-number
            v-model="newExpense.amount"
            :min="0.01"
            :precision="2"
            placeholder="请输入费用金额"
            style="width: 100%"
          />
        </el-form-item>
        
        <el-form-item label="付款人" prop="payments">
          <div class="payments-input">
            <div
              v-for="(payment, index) in newExpense.payments"
              :key="index"
              class="payment-item"
            >
              <el-select
                v-model="payment.payerId"
                placeholder="选择付款人"
                style="width: 200px; margin-right: 10px"
                @change="updatePaymentTotal"
              >
                <el-option
                  v-for="user in users"
                  :key="user.id"
                  :label="user.name"
                  :value="user.id"
                />
              </el-select>
              <el-input-number
                v-model="payment.amount"
                :min="0.01"
                :precision="2"
                placeholder="付款金额"
                style="width: 150px; margin-right: 10px"
                @change="updatePaymentTotal"
              />
              <el-button
                type="danger"
                size="small"
                @click="removePayment(index)"
                :disabled="newExpense.payments.length <= 1"
              >
                删除
              </el-button>
            </div>
            <el-button
              type="primary"
              size="small"
              @click="addPayment"
              style="margin-top: 10px"
            >
              添加付款人
            </el-button>
            <div class="payment-total" style="margin-top: 10px">
              <strong>付款总额: ¥{{ paymentTotal }}</strong>
            </div>
          </div>
        </el-form-item>
        
        <el-form-item label="费用分类">
          <el-select v-model="newExpense.categoryId" placeholder="请选择费用分类" style="width: 100%">
            <el-option
              v-for="category in categories"
              :key="category.id"
              :label="category.name"
              :value="category.id"
            />
          </el-select>
        </el-form-item>
        
        <el-form-item label="分摊方式" prop="splitType">
          <el-radio-group v-model="newExpense.splitType">
            <el-radio label="EQUAL">平均分摊</el-radio>
            <el-radio label="CUSTOM">自定义分摊</el-radio>
          </el-radio-group>
        </el-form-item>
        
        <el-form-item label="分摊室友" prop="shareUserIds">
          <el-checkbox-group v-model="newExpense.shareUserIds">
            <el-checkbox
              v-for="user in users"
              :key="user.id"
              :label="user.id"
            >
              {{ user.name }}
            </el-checkbox>
          </el-checkbox-group>
        </el-form-item>
        
        <!-- 自定义分摊金额 -->
        <el-form-item v-if="newExpense.splitType === 'CUSTOM'" label="自定义金额">
          <div class="custom-shares">
            <div
              v-for="userId in newExpense.shareUserIds"
              :key="userId"
              class="custom-share-item"
            >
              <span class="user-name">{{ users.find(u => u.id === userId)?.name }}</span>
              <el-input-number
                  v-model="newExpense.customSharesMap[userId]"
                :min="0"
                :precision="2"
                placeholder="输入金额"
                style="width: 150px; margin-left: 10px"
                @change="updateCustomAmount(userId, $event)"
              />
            </div>
          </div>
        </el-form-item>
        
        <el-form-item label="费用日期">
          <el-date-picker
            v-model="newExpense.expenseDate"
            type="datetime"
            placeholder="选择费用日期"
            style="width: 100%"
          />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="showAddDialog = false">取消</el-button>
        <el-button type="primary" @click="isEditMode ? updateExpense() : addExpense()" :loading="submitting">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import {ref, onMounted, reactive, watch, computed} from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Check } from '@element-plus/icons-vue'
import { expenseApi, userApi, categoryApi } from '../api'

const expenses = ref([])
const users = ref([])
const categories = ref([])
const loading = ref(false)
const submitting = ref(false)
const showAddDialog = ref(false)
const isEditMode = ref(false)
const editingExpenseId = ref(null)
const formRef = ref()

// 計算付款總額
const paymentTotal = computed(() => {
  return newExpense.payments.reduce((total, payment) => {
    return total + (payment.amount || 0)
  }, 0)
})


const newExpense = reactive({
  description: '',
  amount: null,
  payments: [{ payerId: null, amount: null }], // 多付款人支持
  categoryId: null,
  splitType: 'EQUAL',
  shareUserIds: [],
  customShares: [],          // 原来的数组
  customSharesMap: {},       // 新增 Map 用于 v-model 双向绑定
  expenseDate: null
})

const rules = {
  description: [
    { required: true, message: '请输入费用描述', trigger: 'blur' }
  ],
  amount: [
    { required: true, message: '请输入费用金额', trigger: 'blur' }
  ],
  payments: [
    { required: true, message: '请添加付款人', trigger: 'change' }
  ],
  splitType: [
    { required: true, message: '请选择分摊方式', trigger: 'change' }
  ],
  shareUserIds: [
    { required: true, message: '请选择分摊室友', trigger: 'change' }
  ]
}

// 显示添加费用对话框
const showAddExpenseDialog = () => {
  resetForm()
  showAddDialog.value = true
}

// 添加付款人
const addPayment = () => {
  newExpense.payments.push({ payerId: null, amount: null })
}

// 删除付款人
const removePayment = (index) => {
  if (newExpense.payments.length > 1) {
    newExpense.payments.splice(index, 1)
    updatePaymentTotal()
  }
}

// 更新付款总额
const updatePaymentTotal = () => {
  // 这里可以添加额外的验证逻辑
}

// 获取费用列表
const fetchExpenses = async () => {
  loading.value = true
  try {
    expenses.value = await expenseApi.getAllExpenses()
  } catch (error) {
    ElMessage.error('获取费用列表失败：' + error.message)
  } finally {
    loading.value = false
  }
}

// 获取用户列表
const fetchUsers = async () => {
  try {
    users.value = await userApi.getAllUsers()
  } catch (error) {
    ElMessage.error('获取用户列表失败：' + error.message)
  }
}

// 获取费用分类列表
const fetchCategories = async () => {
  try {
    categories.value = await categoryApi.getAllCategories()
  } catch (error) {
    ElMessage.error('获取费用分类失败：' + error.message)
  }
}

// 添加费用
const addExpense = async () => {
  if (!formRef.value) {
    console.error('表单引用不存在')
    return
  }
  
  try {
    console.log('开始验证表单...')
    await formRef.value.validate()
    console.log('表单验证通过')
    
    submitting.value = true
    console.log('开始构建请求数据...')
    
    // 構建請求數據
    const expenseData = {
      description: newExpense.description,
      amount: newExpense.amount,
      payments: newExpense.payments.filter(p => p.payerId && p.amount), // 過濾掉空的付款人
      categoryId: newExpense.categoryId,
      splitType: newExpense.splitType,
      shareUserIds: newExpense.shareUserIds,
      expenseDate: newExpense.expenseDate ? newExpense.expenseDate.toISOString() : null,
      customShares: newExpense.splitType === 'CUSTOM' ? newExpense.customShares : null
    }
    
    console.log('请求数据:', expenseData)
    console.log('发送请求到后端...')
    
    await expenseApi.addExpense(expenseData)
    
    console.log('请求成功')
    ElMessage.success('添加成功')
    showAddDialog.value = false
    resetForm()
    fetchExpenses()
  } catch (error) {
    console.error('添加费用失败:', error)
    let errorMessage = '添加失败'
    
    if (error.response && error.response.data) {
      if (error.response.data.error) {
        errorMessage = error.response.data.error
      } else if (error.response.data.details) {
        // 处理验证错误
        const details = error.response.data.details
        const errorMessages = Object.values(details).join(', ')
        errorMessage = `验证失败：${errorMessages}`
      }
    } else if (error.message) {
      errorMessage = error.message
    }
    
    ElMessage.error(errorMessage)
  } finally {
    submitting.value = false
  }
}

// 编辑费用记录
const editExpense = (expense) => {
  isEditMode.value = true
  editingExpenseId.value = expense.id
  
  // 填充表单数据
  newExpense.description = expense.description
  newExpense.amount = expense.amount
  newExpense.payments = expense.payments.map(payment => ({
    payerId: users.value.find(u => u.name === payment.payerName)?.id,
    amount: payment.amount
  }))
  newExpense.categoryId = categories.value.find(c => c.name === expense.categoryName)?.id
  newExpense.splitType = expense.splitType === '平均分摊' ? 'EQUAL' : 'CUSTOM'
  newExpense.shareUserIds = expense.shares.map(share => 
    users.value.find(u => u.name === share.userName)?.id
  ).filter(id => id !== undefined)
  
  // 处理自定义分摊
  if (newExpense.splitType === 'CUSTOM') {
    newExpense.customShares = expense.shares.map(share => ({
      userId: users.value.find(u => u.name === share.userName)?.id,
      amount: share.customAmount || share.amount
    })).filter(share => share.userId !== undefined)
    
    // 初始化customSharesMap
    newExpense.customSharesMap = {}
    newExpense.customShares.forEach(share => {
      newExpense.customSharesMap[share.userId] = share.amount
    })
  }
  
  newExpense.expenseDate = expense.expenseDate ? new Date(expense.expenseDate) : null
  
  showAddDialog.value = true
}

// 更新费用记录
const updateExpense = async () => {
  if (!formRef.value) {
    console.error('表单引用不存在')
    return
  }
  
  try {
    console.log('开始验证表单...')
    await formRef.value.validate()
    console.log('表单验证通过')
    
    submitting.value = true
    console.log('开始构建请求数据...')
    
    // 構建請求數據
    const expenseData = {
      description: newExpense.description,
      amount: newExpense.amount,
      payments: newExpense.payments.filter(p => p.payerId && p.amount), // 過濾掉空的付款人
      categoryId: newExpense.categoryId,
      splitType: newExpense.splitType,
      shareUserIds: newExpense.shareUserIds,
      expenseDate: newExpense.expenseDate ? newExpense.expenseDate.toISOString() : null,
      customShares: newExpense.splitType === 'CUSTOM' ? newExpense.customShares : null
    }
    
    console.log('请求数据:', expenseData)
    console.log('发送请求到后端...')
    
    await expenseApi.updateExpense(editingExpenseId.value, expenseData)
    
    console.log('请求成功')
    ElMessage.success('更新成功')
    showAddDialog.value = false
    resetForm()
    fetchExpenses()
  } catch (error) {
    console.error('更新费用失败:', error)
    let errorMessage = '更新失败'
    
    if (error.response && error.response.data) {
      if (error.response.data.error) {
        errorMessage = error.response.data.error
      } else if (error.response.data.details) {
        // 处理验证错误
        const details = error.response.data.details
        const errorMessages = Object.values(details).join(', ')
        errorMessage = `验证失败：${errorMessages}`
      }
    } else if (error.message) {
      errorMessage = error.message
    }
    
    ElMessage.error(errorMessage)
  } finally {
    submitting.value = false
  }
}

// 标记为已支付
const markAsPaid = async (shareId) => {
  try {
    await ElMessageBox.confirm('确认标记为已支付？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await expenseApi.markShareAsPaid(shareId)
    ElMessage.success('标记成功')
    fetchExpenses()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('标记失败：' + error.message)
    }
  }
}

// 重置表单
const resetForm = () => {
  isEditMode.value = false
  editingExpenseId.value = null
  newExpense.description = ''
  newExpense.amount = null
  newExpense.payments = [{ payerId: null, amount: null }]
  newExpense.categoryId = null
  newExpense.splitType = 'EQUAL'
  newExpense.shareUserIds = []
  newExpense.customShares = []
  newExpense.customSharesMap = {}
  newExpense.expenseDate = null
  if (formRef.value) {
    formRef.value.resetFields()
  }
}

// 获取自定义金额
const getCustomAmount = (userId) => {
  const customShare = newExpense.customShares.find(share => share.userId === userId)
  return customShare ? customShare.amount : null
}

// 更新自定义金额
const updateCustomAmount = (userId, amount) => {
  if (!amount) amount = 0
  newExpense.customSharesMap[userId] = amount

  const existingIndex = newExpense.customShares.findIndex(share => share.userId === userId)
  if (existingIndex >= 0) {
    newExpense.customShares[existingIndex].amount = amount
  } else {
    newExpense.customShares.push({ userId, amount })
  }
}

// 格式化日期
const formatDate = (dateString) => {
  if (!dateString) return ''
  const date = new Date(dateString)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

onMounted(() => {
  fetchExpenses()
  fetchUsers()
  fetchCategories()
})

watch(() => newExpense.shareUserIds, (val) => {
  val.forEach(userId => {
    if (!newExpense.customSharesMap[userId]) {
      newExpense.customSharesMap[userId] = getCustomAmount(userId) || 0
    }
  })
})
</script>

<style scoped>
.expense-list {
  background: white;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.header h2 {
  margin: 0;
  color: #303133;
}

.amount {
  font-weight: bold;
  color: #e6a23c;
}

.shares {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
}

.share-tag {
  margin: 2px;
}

.paid-icon {
  margin-left: 4px;
}

.custom-shares {
  border: 1px solid #e6e6e6;
  border-radius: 4px;
  padding: 10px;
  background-color: #fafafa;
}

.custom-share-item {
  display: flex;
  align-items: center;
  margin-bottom: 8px;
}

.custom-share-item:last-child {
  margin-bottom: 0;
}

.user-name {
  min-width: 80px;
  font-weight: bold;
  color: #303133;
}

.action-buttons {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
}

.payments {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
}

.payment-tag {
  margin: 2px;
}

.payments-input {
  border: 1px solid #e6e6e6;
  border-radius: 4px;
  padding: 10px;
  background-color: #fafafa;
}

.payment-item {
  display: flex;
  align-items: center;
  margin-bottom: 8px;
}

.payment-item:last-child {
  margin-bottom: 0;
}

.payment-total {
  color: #e6a23c;
  font-size: 14px;
}
</style>
