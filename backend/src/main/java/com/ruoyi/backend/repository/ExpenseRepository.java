package com.ruoyi.backend.repository;

import com.ruoyi.backend.entity.Expense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 费用记录数据访问接口
 */
@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long> {
    
    // 移除此方法，因為現在使用 ExpensePaymentRepository 來查詢付款記錄
    // @Query("SELECT e FROM Expense e LEFT JOIN FETCH e.shares s LEFT JOIN FETCH s.user WHERE e.payer.id = :payerId ORDER BY e.expenseDate DESC")
    // List<Expense> findByPayerIdOrderByExpenseDateDesc(@Param("payerId") Long payerId);
    
    @Query("SELECT e FROM Expense e LEFT JOIN FETCH e.shares s LEFT JOIN FETCH s.user ORDER BY e.expenseDate DESC")
    List<Expense> findAllOrderByExpenseDateDesc();
    
    @Query("SELECT e FROM Expense e WHERE e.expenseDate BETWEEN :startDate AND :endDate ORDER BY e.expenseDate DESC")
    List<Expense> findByDateRange(@Param("startDate") LocalDateTime startDate, 
                                 @Param("endDate") LocalDateTime endDate);
}
