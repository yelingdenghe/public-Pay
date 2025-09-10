package com.ruoyi.backend.repository;

import com.ruoyi.backend.entity.ExpenseCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 费用分类数据访问接口
 */
@Repository
public interface ExpenseCategoryRepository extends JpaRepository<ExpenseCategory, Long> {
    
    Optional<ExpenseCategory> findByName(String name);
    
    boolean existsByName(String name);
    
    List<ExpenseCategory> findByIsDefaultTrue();
    
    List<ExpenseCategory> findAllByOrderByCreatedAtAsc();
}
