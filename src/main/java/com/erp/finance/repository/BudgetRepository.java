package com.erp.finance.repository;

import com.erp.finance.domain.Budget;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface BudgetRepository extends JpaRepository<Budget, Long> {
    
    List<Budget> findByFiscalYear(Integer fiscalYear);
    
    List<Budget> findByStatus(Budget.BudgetStatus status);
    
    List<Budget> findByFiscalYearAndStatus(Integer fiscalYear, Budget.BudgetStatus status);
    
    List<Budget> findByType(Budget.BudgetType type);
    
    List<Budget> findByStartDateLessThanEqualAndEndDateGreaterThanEqual(LocalDate date1, LocalDate date2);
}
