<template>
  <div class="category-list">
    <div class="header">
      <h2>费用分类管理</h2>
      <el-button type="primary" @click="showAddDialog = true">
        <el-icon><Plus /></el-icon>
        添加分类
      </el-button>
    </div>
    
    <!-- 分类列表 -->
    <el-table :data="categories" style="width: 100%" v-loading="loading">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="name" label="分类名称" />
      <el-table-column prop="description" label="描述" />
      <el-table-column prop="isDefault" label="默认分类" width="100">
        <template #default="scope">
          <el-tag :type="scope.row.isDefault ? 'success' : 'info'" size="small">
            {{ scope.row.isDefault ? '是' : '否' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createdAt" label="创建时间" width="180">
        <template #default="scope">
          {{ formatDate(scope.row.createdAt) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="150">
        <template #default="scope">
          <el-button
            type="primary"
            size="small"
            @click="editCategory(scope.row)"
          >
            编辑
          </el-button>
          <el-button
            v-if="!scope.row.isDefault"
            type="danger"
            size="small"
            @click="deleteCategory(scope.row)"
          >
            删除
          </el-button>
        </template>
      </el-table-column>
    </el-table>
    
    <!-- 添加/编辑分类对话框 -->
    <el-dialog
      v-model="showAddDialog"
      :title="isEdit ? '编辑分类' : '添加分类'"
      width="400px"
    >
      <el-form :model="categoryForm" :rules="rules" ref="formRef" label-width="80px">
        <el-form-item label="分类名称" prop="name">
          <el-input v-model="categoryForm.name" placeholder="请输入分类名称" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="categoryForm.description" type="textarea" placeholder="请输入分类描述" />
        </el-form-item>
        <el-form-item label="默认分类">
          <el-switch v-model="categoryForm.isDefault" />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="cancelEdit">取消</el-button>
        <el-button type="primary" @click="saveCategory" :loading="submitting">
          {{ isEdit ? '更新' : '添加' }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, reactive } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { categoryApi } from '../api'

const categories = ref([])
const loading = ref(false)
const submitting = ref(false)
const showAddDialog = ref(false)
const isEdit = ref(false)
const formRef = ref()

const categoryForm = reactive({
  id: null,
  name: '',
  description: '',
  isDefault: false
})

const rules = {
  name: [
    { required: true, message: '请输入分类名称', trigger: 'blur' },
    { min: 1, max: 20, message: '分类名称长度在1到20个字符', trigger: 'blur' }
  ]
}

// 获取分类列表
const fetchCategories = async () => {
  loading.value = true
  try {
    categories.value = await categoryApi.getAllCategories()
  } catch (error) {
    ElMessage.error('获取分类列表失败：' + error.message)
  } finally {
    loading.value = false
  }
}

// 编辑分类
const editCategory = (category) => {
  isEdit.value = true
  categoryForm.id = category.id
  categoryForm.name = category.name
  categoryForm.description = category.description || ''
  categoryForm.isDefault = category.isDefault
  showAddDialog.value = true
}

// 删除分类
const deleteCategory = async (category) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除分类 "${category.name}" 吗？此操作不可恢复！`,
      '警告',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    await categoryApi.deleteCategory(category.id)
    ElMessage.success('删除成功')
    fetchCategories()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败：' + error.message)
    }
  }
}

// 保存分类
const saveCategory = async () => {
  if (!formRef.value) return
  
  try {
    await formRef.value.validate()
    
    submitting.value = true
    
    if (isEdit.value) {
      await categoryApi.updateCategory(categoryForm.id, categoryForm.name, categoryForm.description)
      ElMessage.success('更新成功')
    } else {
      await categoryApi.createCategory(categoryForm.name, categoryForm.description, categoryForm.isDefault)
      ElMessage.success('添加成功')
    }
    
    showAddDialog.value = false
    resetForm()
    fetchCategories()
  } catch (error) {
    ElMessage.error((isEdit.value ? '更新' : '添加') + '失败：' + error.message)
  } finally {
    submitting.value = false
  }
}

// 取消编辑
const cancelEdit = () => {
  showAddDialog.value = false
  resetForm()
}

// 重置表单
const resetForm = () => {
  isEdit.value = false
  categoryForm.id = null
  categoryForm.name = ''
  categoryForm.description = ''
  categoryForm.isDefault = false
  if (formRef.value) {
    formRef.value.resetFields()
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
  fetchCategories()
})
</script>

<style scoped>
.category-list {
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
</style>
