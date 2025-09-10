import axios from 'axios'

const api = axios.create({
  baseURL: '/api',
  timeout: 10000
})

// 請求攔截器
api.interceptors.request.use(
  config => {
    return config
  },
  error => {
    return Promise.reject(error)
  }
)

// 響應攔截器
api.interceptors.response.use(
  response => {
    return response.data
  },
  error => {
    console.error('API Error:', error)
    return Promise.reject(error)
  }
)

// 用户相关API
export const userApi = {
  // 获取所有用户
  getAllUsers() {
    return api.get('/users')
  },
  
  // 创建用户
  createUser(name) {
    return api.post('/users', { name })
  },
  
  // 更新用户
  updateUser(id, name) {
    return api.put(`/users/${id}`, { name })
  },
  
  // 删除用户
  deleteUser(id) {
    return api.delete(`/users/${id}`)
  }
}

// 费用相关API
export const expenseApi = {
  // 获取所有费用记录
  getAllExpenses() {
    return api.get('/expenses')
  },
  
  // 添加费用记录
  addExpense(expenseData) {
    return api.post('/expenses', expenseData)
  },
  
  // 获取费用记录详情
  getExpenseById(id) {
    return api.get(`/expenses/${id}`)
  },
  
  // 更新费用记录
  updateExpense(id, expenseData) {
    return api.put(`/expenses/${id}`, expenseData)
  },
  
  // 标记分摊为已支付
  markShareAsPaid(shareId) {
    return api.put(`/expenses/shares/${shareId}/pay`)
  },
  
  // 获取用户余额统计
  getUserBalances() {
    return api.get('/expenses/balances')
  }
}

// 费用分类相关API
export const categoryApi = {
  // 获取所有费用分类
  getAllCategories() {
    return api.get('/categories')
  },
  
  // 创建费用分类
  createCategory(name, description, isDefault = false) {
    return api.post('/categories', { name, description, isDefault })
  },
  
  // 更新费用分类
  updateCategory(id, name, description) {
    return api.put(`/categories/${id}`, { name, description })
  },
  
  // 删除费用分类
  deleteCategory(id) {
    return api.delete(`/categories/${id}`)
  },
  
  // 初始化默认分类
  initDefaultCategories() {
    return api.post('/categories/init')
  }
}

export default api
