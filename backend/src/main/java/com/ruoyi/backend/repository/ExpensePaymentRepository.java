package com.ruoyi.backend.repository;

import com.ruoyi.backend.entity.ExpensePayment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 费用付款数据访问接口
 */
@Repository
public interface ExpensePaymentRepository extends JpaRepository<ExpensePayment, Long> {
    
    List<ExpensePayment> findByExpenseId(Long expenseId);
    
    List<ExpensePayment> findByPayerId(Long payerId);
    
    void deleteByExpenseId(Long expenseId);
}
