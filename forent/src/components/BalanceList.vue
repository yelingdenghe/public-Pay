<template>
  <div class="balance-list">
    <div class="header">
      <h2>余额统计</h2>
      <el-button @click="fetchBalances" :loading="loading">
        <el-icon><Refresh /></el-icon>
        刷新
      </el-button>
    </div>
    
    <!-- 余额卡片 -->
    <div class="balance-cards">
      <el-card
        v-for="balance in balances"
        :key="balance.userId"
        class="balance-card"
        :class="{ 'positive': balance.balance > 0, 'negative': balance.balance < 0 }"
      >
        <div class="card-header">
          <h3>{{ balance.userName }}</h3>
          <div class="balance-amount" :class="{ 'positive': balance.balance > 0, 'negative': balance.balance < 0 }">
            {{ balance.balance > 0 ? '+' : '' }}¥{{ balance.balance.toFixed(2) }}
          </div>
        </div>
        
        <div class="card-content">
          <div class="stat-item">
            <span class="label">总支付：</span>
            <span class="value paid">¥{{ balance.totalPaid.toFixed(2) }}</span>
          </div>
          <div class="stat-item">
            <span class="label">总欠款：</span>
            <span class="value owed">¥{{ balance.totalOwed.toFixed(2) }}</span>
          </div>
        </div>
        
        <!-- 详细信息 -->
        <el-collapse v-if="balance.details && balance.details.length > 0">
          <el-collapse-item title="详细信息" name="details">
            <div class="details">
              <div
                v-for="detail in balance.details"
                :key="detail.type + detail.relatedUserName"
                class="detail-item"
                :class="detail.type"
              >
                <span class="detail-label">{{ detail.description }}：</span>
                <span class="detail-value">
                  {{ detail.relatedUserName }} - ¥{{ detail.amount.toFixed(2) }}
                </span>
              </div>
            </div>
          </el-collapse-item>
        </el-collapse>
      </el-card>
    </div>
    
    <!-- 总结信息 -->
    <el-card class="summary-card" v-if="balances.length > 0">
      <h3>总结</h3>
      <div class="summary-content">
        <div class="summary-item">
          <span class="label">总费用：</span>
          <span class="value">¥{{ totalExpenses.toFixed(2) }}</span>
        </div>
        <div class="summary-item">
          <span class="label">平均每人：</span>
          <span class="value">¥{{ averagePerPerson.toFixed(2) }}</span>
        </div>
        <div class="summary-item">
          <span class="label">室友数量：</span>
          <span class="value">{{ balances.length }}人</span>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { Refresh } from '@element-plus/icons-vue'
import { expenseApi } from '../api'

const balances = ref([])
const loading = ref(false)

// 计算总费用
const totalExpenses = computed(() => {
  return balances.value.reduce((sum, balance) => sum + balance.totalPaid, 0)
})

// 计算平均每人费用
const averagePerPerson = computed(() => {
  if (balances.value.length === 0) return 0
  return totalExpenses.value / balances.value.length
})

// 获取余额统计
const fetchBalances = async () => {
  loading.value = true
  try {
    balances.value = await expenseApi.getUserBalances()
  } catch (error) {
    ElMessage.error('获取余额统计失败：' + error.message)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchBalances()
})
</script>

<style scoped>
.balance-list {
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

.balance-cards {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 20px;
  margin-bottom: 20px;
}

.balance-card {
  border-radius: 8px;
  transition: all 0.3s ease;
}

.balance-card.positive {
  border-left: 4px solid #67c23a;
}

.balance-card.negative {
  border-left: 4px solid #f56c6c;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.card-header h3 {
  margin: 0;
  color: #303133;
  font-size: 18px;
}

.balance-amount {
  font-size: 24px;
  font-weight: bold;
}

.balance-amount.positive {
  color: #67c23a;
}

.balance-amount.negative {
  color: #f56c6c;
}

.card-content {
  margin-bottom: 15px;
}

.stat-item {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8px;
}

.label {
  color: #606266;
  font-size: 14px;
}

.value {
  font-weight: bold;
}

.value.paid {
  color: #67c23a;
}

.value.owed {
  color: #f56c6c;
}

.details {
  padding: 10px 0;
}

.detail-item {
  display: flex;
  justify-content: space-between;
  padding: 5px 0;
  border-bottom: 1px solid #f0f0f0;
}

.detail-item:last-child {
  border-bottom: none;
}

.detail-item.should_receive {
  background-color: #f0f9ff;
  padding: 8px;
  border-radius: 4px;
  margin-bottom: 5px;
}

.detail-item.owed {
  background-color: #fef0f0;
  padding: 8px;
  border-radius: 4px;
  margin-bottom: 5px;
}

.detail-label {
  color: #606266;
  font-size: 13px;
}

.detail-value {
  font-weight: bold;
  font-size: 13px;
}

.summary-card {
  margin-top: 20px;
}

.summary-card h3 {
  margin: 0 0 15px 0;
  color: #303133;
}

.summary-content {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 15px;
}

.summary-item {
  display: flex;
  justify-content: space-between;
  padding: 10px;
  background-color: #f8f9fa;
  border-radius: 4px;
}

.summary-item .label {
  font-weight: bold;
  color: #303133;
}

.summary-item .value {
  color: #409eff;
  font-weight: bold;
}
</style>
