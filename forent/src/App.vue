<template>
  <div id="app">
    <el-container>
      <el-header class="header">
        <h1>合租记账系统</h1>
        <el-button type="primary" @click="showAddUserDialog = true">
          <el-icon><Plus /></el-icon>
          添加室友
        </el-button>
      </el-header>
      
      <el-container>
        <el-aside width="200px" class="sidebar">
          <el-menu
            :default-active="activeMenu"
            @select="handleMenuSelect"
            class="menu"
          >
            <el-menu-item index="expenses">
              <el-icon><Money /></el-icon>
              <span>费用记录</span>
            </el-menu-item>
            <el-menu-item index="balances">
              <el-icon><Calendar /></el-icon>
              <span>余额统计</span>
            </el-menu-item>
            <el-menu-item index="users">
              <el-icon><User /></el-icon>
              <span>室友管理</span>
            </el-menu-item>
            <el-menu-item index="categories">
              <el-icon><Collection /></el-icon>
              <span>分类管理</span>
            </el-menu-item>
          </el-menu>
        </el-aside>
        
        <el-main class="main-content">
          <!-- 费用记录页面 -->
          <div v-if="activeMenu === 'expenses'">
            <ExpenseList />
          </div>
          
          <!-- 余额统计页面 -->
          <div v-if="activeMenu === 'balances'">
            <BalanceList />
          </div>
          
          <!-- 室友管理页面 -->
          <div v-if="activeMenu === 'users'">
            <UserList />
          </div>
          
          <!-- 分类管理页面 -->
          <div v-if="activeMenu === 'categories'">
            <CategoryList />
          </div>
        </el-main>
      </el-container>
    </el-container>
    
    <!-- 添加室友对话框 -->
    <el-dialog
      v-model="showAddUserDialog"
      title="添加室友"
      width="400px"
    >
      <el-form :model="newUser" label-width="80px">
        <el-form-item label="姓名" required>
          <el-input v-model="newUser.name" placeholder="请输入室友姓名" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showAddUserDialog = false">取消</el-button>
        <el-button type="primary" @click="addUser">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import {Plus, Money, User, Collection, Calendar} from '@element-plus/icons-vue'
import ExpenseList from './components/ExpenseList.vue'
import BalanceList from './components/BalanceList.vue'
import UserList from './components/UserList.vue'
import CategoryList from './components/CategoryList.vue'
import { userApi } from './api'

const activeMenu = ref('expenses')
const showAddUserDialog = ref(false)
const newUser = ref({ name: '' })

const handleMenuSelect = (index) => {
  activeMenu.value = index
}

const addUser = async () => {
  if (!newUser.value.name.trim()) {
    ElMessage.warning('请输入室友姓名')
    return
  }
  
  try {
    await userApi.createUser(newUser.value.name.trim())
    ElMessage.success('添加成功')
    showAddUserDialog.value = false
    newUser.value.name = ''
  } catch (error) {
    ElMessage.error('添加失败：' + (error.response?.data?.message || error.message))
  }
}

onMounted(() => {
  // 初始化数据
})
</script>

<style scoped>
.header {
  background-color: #409eff;
  color: white;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 20px;
}

.header h1 {
  margin: 0;
  font-size: 24px;
}

.sidebar {
  background-color: #f5f5f5;
  border-right: 1px solid #e6e6e6;
}

.menu {
  border: none;
}

.main-content {
  padding: 20px;
  background-color: #fafafa;
  min-height: calc(100vh - 60px);
}

#app {
  height: 100vh;
}
</style>
