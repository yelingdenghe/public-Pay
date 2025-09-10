package com.ruoyi.backend.repository;

import com.ruoyi.backend.entity.ExpenseShare;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 费用分摊数据访问接口
 */
@Repository
public interface ExpenseShareRepository extends JpaRepository<ExpenseShare, Long> {
    
    List<ExpenseShare> findByUserId(Long userId);
    
    List<ExpenseShare> findByExpenseId(Long expenseId);
    
    @Query("SELECT es FROM ExpenseShare es WHERE es.user.id = :userId AND es.isPaid = false")
    List<ExpenseShare> findUnpaidByUserId(@Param("userId") Long userId);
    
    // 移除此方法，因為現在需要通過 ExpensePayment 來查找付款人
    // @Query("SELECT es FROM ExpenseShare es WHERE es.expense.payer.id = :payerId AND es.isPaid = false")
    // List<ExpenseShare> findUnpaidByPayerId(@Param("payerId") Long payerId);
    
    void deleteByExpenseId(Long expenseId);
}
