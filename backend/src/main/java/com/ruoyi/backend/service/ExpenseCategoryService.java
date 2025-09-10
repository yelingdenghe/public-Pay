package com.ruoyi.backend.service;

import com.ruoyi.backend.dto.ExpenseCategoryResponse;
import com.ruoyi.backend.entity.ExpenseCategory;
import com.ruoyi.backend.repository.ExpenseCategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 费用分类服务类
 */
@Service
public class ExpenseCategoryService {
    
    @Autowired
    private ExpenseCategoryRepository categoryRepository;
    
    /**
     * 创建费用分类
     */
    public ExpenseCategoryResponse createCategory(String name, String description, Boolean isDefault) {
        if (categoryRepository.existsByName(name)) {
            throw new RuntimeException("费用分类名称已存在: " + name);
        }
        
        ExpenseCategory category = new ExpenseCategory();
        category.setName(name);
        category.setDescription(description);
        category.setIsDefault(isDefault != null ? isDefault : false);
        
        ExpenseCategory savedCategory = categoryRepository.save(category);
        return convertToResponse(savedCategory);
    }
    
    /**
     * 获取所有费用分类
     */
    public List<ExpenseCategoryResponse> getAllCategories() {
        return categoryRepository.findAllByOrderByCreatedAtAsc()
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * 根据ID获取费用分类
     */
    public Optional<ExpenseCategory> getCategoryById(Long id) {
        return categoryRepository.findById(id);
    }
    
    /**
     * 更新费用分类
     */
    public ExpenseCategoryResponse updateCategory(Long id, String name, String description) {
        ExpenseCategory category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("费用分类不存在: " + id));
        
        if (!category.getName().equals(name) && categoryRepository.existsByName(name)) {
            throw new RuntimeException("费用分类名称已存在: " + name);
        }
        
        category.setName(name);
        category.setDescription(description);
        
        ExpenseCategory savedCategory = categoryRepository.save(category);
        return convertToResponse(savedCategory);
    }
    
    /**
     * 删除费用分类
     */
    public void deleteCategory(Long id) {
        if (!categoryRepository.existsById(id)) {
            throw new RuntimeException("费用分类不存在: " + id);
        }
        categoryRepository.deleteById(id);
    }
    
    /**
     * 初始化默认分类
     */
    public void initDefaultCategories() {
        if (categoryRepository.count() == 0) {
            createCategory("水电费", "水费、电费、燃气费等", true);
            createCategory("物业费", "物业管理费、垃圾处理费等", true);
            createCategory("生活费", "日用品、食材等生活费用", true);
            createCategory("网络费", "宽带、手机话费等", true);
            createCategory("其他", "其他杂项费用", true);
        }
    }
    
    /**
     * 转换为响应DTO
     */
    private ExpenseCategoryResponse convertToResponse(ExpenseCategory category) {
        ExpenseCategoryResponse response = new ExpenseCategoryResponse();
        response.setId(category.getId());
        response.setName(category.getName());
        response.setDescription(category.getDescription());
        response.setIsDefault(category.getIsDefault());
        response.setCreatedAt(category.getCreatedAt());
        return response;
    }
}
