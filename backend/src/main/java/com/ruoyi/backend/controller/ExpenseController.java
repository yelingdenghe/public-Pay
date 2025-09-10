package com.ruoyi.backend.controller;

import com.ruoyi.backend.dto.ExpenseRequest;
import com.ruoyi.backend.dto.ExpenseResponse;
import com.ruoyi.backend.dto.UserBalanceResponse;
import com.ruoyi.backend.service.ExpenseService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 费用控制器
 */
@RestController
@RequestMapping("/api/expenses")
@CrossOrigin(origins = "*")
public class ExpenseController {
    
    @Autowired
    private ExpenseService expenseService;
    
    /**
     * 添加费用记录
     */
    @PostMapping
    public ResponseEntity<?> addExpense(@Valid @RequestBody ExpenseRequest request) {
        try {
            ExpenseResponse response = expenseService.addExpense(request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }
    
    /**
     * 获取所有费用记录
     */
    @GetMapping
    public ResponseEntity<List<ExpenseResponse>> getAllExpenses() {
        List<ExpenseResponse> expenses = expenseService.getAllExpenses();
        return ResponseEntity.ok(expenses);
    }
    
    /**
     * 根据ID获取费用记录
     */
    @GetMapping("/{id}")
    public ResponseEntity<ExpenseResponse> getExpenseById(@PathVariable Long id) {
        try {
            ExpenseResponse response = expenseService.getExpenseById(id);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * 更新费用记录
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateExpense(@PathVariable Long id, @Valid @RequestBody ExpenseRequest request) {
        try {
            ExpenseResponse response = expenseService.updateExpense(id, request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }
    
    /**
     * 标记分摊为已支付
     */
    @PutMapping("/shares/{shareId}/pay")
    public ResponseEntity<Void> markShareAsPaid(@PathVariable Long shareId) {
        try {
            expenseService.markShareAsPaid(shareId);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * 获取用户余额统计
     */
    @GetMapping("/balances")
    public ResponseEntity<List<UserBalanceResponse>> getUserBalances() {
        List<UserBalanceResponse> balances = expenseService.getUserBalances();
        return ResponseEntity.ok(balances);
    }
}
