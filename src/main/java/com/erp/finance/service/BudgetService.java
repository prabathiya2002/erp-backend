package com.erp.finance.service;

import com.erp.finance.domain.*;
import com.erp.finance.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class BudgetService {
    
    @Autowired
    private BudgetRepository budgetRepository;
    
    @Autowired
    private BudgetLineRepository budgetLineRepository;
    
    @Autowired
    private AccountRepository accountRepository;
    
    @Autowired
    private JournalRepository journalRepository;
    
    public List<Budget> getAllBudgets() {
        return budgetRepository.findAll();
    }
    
    public Optional<Budget> getBudgetById(Long id) {
        return budgetRepository.findById(id);
    }
    
    public List<Budget> getBudgetsByYear(Integer year) {
        return budgetRepository.findByFiscalYear(year);
    }
    
    public List<Budget> getBudgetsByStatus(Budget.BudgetStatus status) {
        return budgetRepository.findByStatus(status);
    }
    
    public Budget createBudget(Budget budget) {
        // Validate dates
        if (budget.getEndDate().isBefore(budget.getStartDate())) {
            throw new IllegalArgumentException("End date cannot be before start date");
        }
        
        budget.setCreatedDate(LocalDate.now());
        budget.setStatus(Budget.BudgetStatus.DRAFT);
        
        Budget savedBudget = budgetRepository.save(budget);
        
        // Save budget lines if present
        if (budget.getBudgetLines() != null && !budget.getBudgetLines().isEmpty()) {
            for (BudgetLine line : budget.getBudgetLines()) {
                line.setBudget(savedBudget);
                
                // Determine line type from account
                Optional<Account> accountOpt = accountRepository.findById(line.getAccountId());
                if (accountOpt.isPresent()) {
                    Account account = accountOpt.get();
                    line.setLineType(mapAccountTypeToLineType(account.getType()));
                }
            }
            budgetLineRepository.saveAll(budget.getBudgetLines());
        }
        
        return savedBudget;
    }
    
    public Budget updateBudget(Long id, Budget budgetDetails) {
        Budget budget = budgetRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Budget not found with id: " + id));
        
        // Only allow updates if budget is in DRAFT or SUBMITTED status
        if (budget.getStatus() == Budget.BudgetStatus.APPROVED || 
            budget.getStatus() == Budget.BudgetStatus.ACTIVE) {
            throw new IllegalStateException("Cannot update approved or active budget");
        }
        
        budget.setName(budgetDetails.getName());
        budget.setDescription(budgetDetails.getDescription());
        budget.setFiscalYear(budgetDetails.getFiscalYear());
        budget.setPeriod(budgetDetails.getPeriod());
        budget.setType(budgetDetails.getType());
        budget.setStartDate(budgetDetails.getStartDate());
        budget.setEndDate(budgetDetails.getEndDate());
        budget.setNotes(budgetDetails.getNotes());
        
        return budgetRepository.save(budget);
    }
    
    public void deleteBudget(Long id) {
        Budget budget = budgetRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Budget not found with id: " + id));
        
        // Only allow deletion of draft budgets
        if (budget.getStatus() != Budget.BudgetStatus.DRAFT) {
            throw new IllegalStateException("Can only delete draft budgets");
        }
        
        budgetRepository.delete(budget);
    }
    
    public Budget approveBudget(Long id, String approvedBy) {
        Budget budget = budgetRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Budget not found with id: " + id));
        
        if (budget.getStatus() != Budget.BudgetStatus.SUBMITTED && 
            budget.getStatus() != Budget.BudgetStatus.DRAFT) {
            throw new IllegalStateException("Budget must be in DRAFT or SUBMITTED status to approve");
        }
        
        budget.setStatus(Budget.BudgetStatus.APPROVED);
        budget.setApprovedDate(LocalDate.now());
        budget.setApprovedBy(approvedBy);
        
        return budgetRepository.save(budget);
    }
    
    public Budget submitBudget(Long id) {
        Budget budget = budgetRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Budget not found with id: " + id));
        
        if (budget.getStatus() != Budget.BudgetStatus.DRAFT) {
            throw new IllegalStateException("Only draft budgets can be submitted");
        }
        
        // Validate that budget has at least one line item
        List<BudgetLine> lines = budgetLineRepository.findByBudgetId(id);
        if (lines.isEmpty()) {
            throw new IllegalStateException("Budget must have at least one line item");
        }
        
        budget.setStatus(Budget.BudgetStatus.SUBMITTED);
        return budgetRepository.save(budget);
    }
    
    public Budget activateBudget(Long id) {
        Budget budget = budgetRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Budget not found with id: " + id));
        
        if (budget.getStatus() != Budget.BudgetStatus.APPROVED) {
            throw new IllegalStateException("Only approved budgets can be activated");
        }
        
        budget.setStatus(Budget.BudgetStatus.ACTIVE);
        return budgetRepository.save(budget);
    }
    
    public List<BudgetLine> getBudgetLines(Long budgetId) {
        return budgetLineRepository.findByBudgetId(budgetId);
    }
    
    public BudgetLine addBudgetLine(Long budgetId, BudgetLine budgetLine) {
        Budget budget = budgetRepository.findById(budgetId)
            .orElseThrow(() -> new RuntimeException("Budget not found with id: " + budgetId));
        
        if (budget.getStatus() == Budget.BudgetStatus.APPROVED || 
            budget.getStatus() == Budget.BudgetStatus.ACTIVE) {
            throw new IllegalStateException("Cannot add lines to approved or active budget");
        }
        
        // Validate account exists
        Account account = accountRepository.findById(budgetLine.getAccountId())
            .orElseThrow(() -> new RuntimeException("Account not found with id: " + budgetLine.getAccountId()));
        
        budgetLine.setBudget(budget);
        budgetLine.setLineType(mapAccountTypeToLineType(account.getType()));
        
        return budgetLineRepository.save(budgetLine);
    }
    
    public BudgetLine updateBudgetLine(Long lineId, BudgetLine lineDetails) {
        BudgetLine line = budgetLineRepository.findById(lineId)
            .orElseThrow(() -> new RuntimeException("Budget line not found with id: " + lineId));
        
        Budget budget = line.getBudget();
        if (budget.getStatus() == Budget.BudgetStatus.APPROVED || 
            budget.getStatus() == Budget.BudgetStatus.ACTIVE) {
            throw new IllegalStateException("Cannot update lines in approved or active budget");
        }
        
        line.setAmount(lineDetails.getAmount());
        line.setNotes(lineDetails.getNotes());
        
        return budgetLineRepository.save(line);
    }
    
    public void deleteBudgetLine(Long lineId) {
        BudgetLine line = budgetLineRepository.findById(lineId)
            .orElseThrow(() -> new RuntimeException("Budget line not found with id: " + lineId));
        
        Budget budget = line.getBudget();
        if (budget.getStatus() == Budget.BudgetStatus.APPROVED || 
            budget.getStatus() == Budget.BudgetStatus.ACTIVE) {
            throw new IllegalStateException("Cannot delete lines from approved or active budget");
        }
        
        budgetLineRepository.delete(line);
    }
    
    /**
     * Calculate variance analysis comparing budget to actual for a given period
     */
    public Map<String, Object> calculateVarianceAnalysis(Long budgetId) {
        Budget budget = budgetRepository.findById(budgetId)
            .orElseThrow(() -> new RuntimeException("Budget not found with id: " + budgetId));
        
        List<BudgetLine> budgetLines = budgetLineRepository.findByBudgetId(budgetId);
        
        Map<String, Object> analysis = new HashMap<>();
        List<Map<String, Object>> lineAnalysis = new ArrayList<>();
        
        BigDecimal totalBudgetRevenue = BigDecimal.ZERO;
        BigDecimal totalActualRevenue = BigDecimal.ZERO;
        BigDecimal totalBudgetExpense = BigDecimal.ZERO;
        BigDecimal totalActualExpense = BigDecimal.ZERO;
        
        for (BudgetLine line : budgetLines) {
            Map<String, Object> lineData = new HashMap<>();
            
            Account account = accountRepository.findById(line.getAccountId()).orElse(null);
            if (account == null) continue;
            
            // Get actual amount from account activity within budget period
            BigDecimal actualAmount = calculateActualAmount(line.getAccountId(), budget.getStartDate(), budget.getEndDate());
            
            BigDecimal budgetAmount = line.getAmount();
            BigDecimal variance = actualAmount.subtract(budgetAmount);
            BigDecimal variancePercent = budgetAmount.compareTo(BigDecimal.ZERO) != 0 
                ? variance.divide(budgetAmount, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100))
                : BigDecimal.ZERO;
            
            lineData.put("accountId", line.getAccountId());
            lineData.put("accountCode", account.getCode());
            lineData.put("accountName", account.getName());
            lineData.put("lineType", line.getLineType());
            lineData.put("budgetAmount", budgetAmount);
            lineData.put("actualAmount", actualAmount);
            lineData.put("variance", variance);
            lineData.put("variancePercent", variancePercent);
            lineData.put("status", determineVarianceStatus(line.getLineType(), variance));
            
            lineAnalysis.add(lineData);
            
            // Accumulate totals
            if (line.getLineType() == BudgetLine.LineType.REVENUE) {
                totalBudgetRevenue = totalBudgetRevenue.add(budgetAmount);
                totalActualRevenue = totalActualRevenue.add(actualAmount);
            } else if (line.getLineType() == BudgetLine.LineType.EXPENSE) {
                totalBudgetExpense = totalBudgetExpense.add(budgetAmount);
                totalActualExpense = totalActualExpense.add(actualAmount);
            }
        }
        
        analysis.put("budgetId", budgetId);
        analysis.put("budgetName", budget.getName());
        analysis.put("period", budget.getPeriod());
        analysis.put("startDate", budget.getStartDate());
        analysis.put("endDate", budget.getEndDate());
        analysis.put("lines", lineAnalysis);
        
        // Summary totals
        Map<String, Object> summary = new HashMap<>();
        summary.put("totalBudgetRevenue", totalBudgetRevenue);
        summary.put("totalActualRevenue", totalActualRevenue);
        summary.put("revenueVariance", totalActualRevenue.subtract(totalBudgetRevenue));
        summary.put("totalBudgetExpense", totalBudgetExpense);
        summary.put("totalActualExpense", totalActualExpense);
        summary.put("expenseVariance", totalActualExpense.subtract(totalBudgetExpense));
        summary.put("budgetNetIncome", totalBudgetRevenue.subtract(totalBudgetExpense));
        summary.put("actualNetIncome", totalActualRevenue.subtract(totalActualExpense));
        summary.put("netIncomeVariance", totalActualRevenue.subtract(totalActualExpense).subtract(totalBudgetRevenue.subtract(totalBudgetExpense)));
        
        analysis.put("summary", summary);
        
        return analysis;
    }
    
    /**
     * Calculate actual amount for an account within a date range from journal entries
     */
    private BigDecimal calculateActualAmount(Long accountId, LocalDate startDate, LocalDate endDate) {
        // Get all posted journals within the date range
        List<JournalEntry> journals = journalRepository.findByDateBetweenAndStatus(startDate, endDate, JournalStatus.POSTED);
        
        BigDecimal total = BigDecimal.ZERO;
        
        for (JournalEntry journal : journals) {
            List<JournalLine> lines = journal.getLines();
            
            for (JournalLine line : lines) {
                if (line.getAccountId().equals(accountId)) {
                    // For revenue accounts, credits increase (positive)
                    // For expense accounts, debits increase (positive)
                    Account account = accountRepository.findById(accountId).orElse(null);
                    if (account != null) {
                        if (account.getType() == AccountType.REVENUE) {
                            total = total.add(line.getCredit()).subtract(line.getDebit());
                        } else if (account.getType() == AccountType.EXPENSE) {
                            total = total.add(line.getDebit()).subtract(line.getCredit());
                        } else {
                            // For assets, debits are positive
                            total = total.add(line.getDebit()).subtract(line.getCredit());
                        }
                    }
                }
            }
        }
        
        return total;
    }
    
    /**
     * Determine if variance is favorable or unfavorable
     */
    private String determineVarianceStatus(BudgetLine.LineType lineType, BigDecimal variance) {
        if (variance.compareTo(BigDecimal.ZERO) == 0) {
            return "ON_TARGET";
        }
        
        // For revenue: positive variance is favorable (more revenue than budgeted)
        // For expense: negative variance is favorable (less expense than budgeted)
        if (lineType == BudgetLine.LineType.REVENUE) {
            return variance.compareTo(BigDecimal.ZERO) > 0 ? "FAVORABLE" : "UNFAVORABLE";
        } else if (lineType == BudgetLine.LineType.EXPENSE) {
            return variance.compareTo(BigDecimal.ZERO) < 0 ? "FAVORABLE" : "UNFAVORABLE";
        }
        
        return "NEUTRAL";
    }
    
    private BudgetLine.LineType mapAccountTypeToLineType(AccountType accountType) {
        switch (accountType) {
            case REVENUE:
                return BudgetLine.LineType.REVENUE;
            case EXPENSE:
                return BudgetLine.LineType.EXPENSE;
            case ASSET:
                return BudgetLine.LineType.ASSET;
            case LIABILITY:
                return BudgetLine.LineType.LIABILITY;
            case EQUITY:
                return BudgetLine.LineType.EQUITY;
            default:
                throw new IllegalArgumentException("Unknown account type: " + accountType);
        }
    }
}
