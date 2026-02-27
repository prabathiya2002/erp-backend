package com.erp.finance.repository;

import com.erp.finance.domain.BudgetLine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BudgetLineRepository extends JpaRepository<BudgetLine, Long> {
    
    List<BudgetLine> findByBudgetId(Long budgetId);
    
    List<BudgetLine> findByAccountId(Long accountId);
    
    @Query("SELECT bl FROM BudgetLine bl WHERE bl.budget.id = :budgetId AND bl.lineType = :lineType")
    List<BudgetLine> findByBudgetIdAndLineType(@Param("budgetId") Long budgetId, @Param("lineType") BudgetLine.LineType lineType);
}
