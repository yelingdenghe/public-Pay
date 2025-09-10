package com.ruoyi.backend.controller;

import com.ruoyi.backend.dto.ExpenseCategoryResponse;
import com.ruoyi.backend.service.ExpenseCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 费用分类控制器
 */
@RestController
@RequestMapping("/api/categories")
@CrossOrigin(origins = "*")
public class ExpenseCategoryController {
    
    @Autowired
    private ExpenseCategoryService categoryService;
    
    /**
     * 创建费用分类
     */
    @PostMapping
    public ResponseEntity<ExpenseCategoryResponse> createCategory(@RequestBody Map<String, String> request) {
        String name = request.get("name");
        String description = request.get("description");
        Boolean isDefault = Boolean.valueOf(request.getOrDefault("isDefault", "false"));
        
        if (name == null || name.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        
        try {
            ExpenseCategoryResponse response = categoryService.createCategory(name.trim(), description, isDefault);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * 获取所有费用分类
     */
    @GetMapping
    public ResponseEntity<List<ExpenseCategoryResponse>> getAllCategories() {
        List<ExpenseCategoryResponse> categories = categoryService.getAllCategories();
        return ResponseEntity.ok(categories);
    }
    
    /**
     * 更新费用分类
     */
    @PutMapping("/{id}")
    public ResponseEntity<ExpenseCategoryResponse> updateCategory(@PathVariable Long id, @RequestBody Map<String, String> request) {
        String name = request.get("name");
        String description = request.get("description");
        
        if (name == null || name.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        
        try {
            ExpenseCategoryResponse response = categoryService.updateCategory(id, name.trim(), description);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * 删除费用分类
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        try {
            categoryService.deleteCategory(id);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * 初始化默认分类
     */
    @PostMapping("/init")
    public ResponseEntity<Void> initDefaultCategories() {
        categoryService.initDefaultCategories();
        return ResponseEntity.ok().build();
    }
}
