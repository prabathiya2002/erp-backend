package com.erp.finance.service;

import com.erp.finance.domain.*;
import com.erp.finance.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

@Service
@Transactional(readOnly = true)
public class FinancialReportService {
    
    @Autowired
    private AccountRepository accountRepository;
    
    @Autowired
    private JournalRepository journalRepository;
    
    /**
     * Generate Income Statement (Profit & Loss Statement)
     * Shows revenues and expenses for a period, resulting in net income/loss
     */
    public Map<String, Object> generateIncomeStatement(LocalDate startDate, LocalDate endDate) {
        Map<String, Object> report = new HashMap<>();
        
        List<Account> allAccounts = new ArrayList<>();
        accountRepository.findAll().forEach(allAccounts::add);
        List<JournalEntry> journals = journalRepository.findByDateBetweenAndStatus(
            startDate, endDate, JournalStatus.POSTED);
        
        // Calculate account balances for the period
        Map<Long, BigDecimal> accountBalances = calculateAccountBalances(journals);
        
        // Revenue section
        List<Map<String, Object>> revenues = new ArrayList<>();
        BigDecimal totalRevenue = BigDecimal.ZERO;
        
        for (Account account : allAccounts) {
            if (account.getType() == AccountType.REVENUE) {
                BigDecimal balance = accountBalances.getOrDefault(account.getId(), BigDecimal.ZERO);
                if (balance.compareTo(BigDecimal.ZERO) != 0) {
                    Map<String, Object> item = new HashMap<>();
                    item.put("accountCode", account.getCode());
                    item.put("accountName", account.getName());
                    item.put("amount", balance.abs());
                    revenues.add(item);
                    totalRevenue = totalRevenue.add(balance.abs());
                }
            }
        }
        
        // Expense section
        List<Map<String, Object>> expenses = new ArrayList<>();
        BigDecimal totalExpense = BigDecimal.ZERO;
        
        for (Account account : allAccounts) {
            if (account.getType() == AccountType.EXPENSE) {
                BigDecimal balance = accountBalances.getOrDefault(account.getId(), BigDecimal.ZERO);
                if (balance.compareTo(BigDecimal.ZERO) != 0) {
                    Map<String, Object> item = new HashMap<>();
                    item.put("accountCode", account.getCode());
                    item.put("accountName", account.getName());
                    item.put("amount", balance.abs());
                    expenses.add(item);
                    totalExpense = totalExpense.add(balance.abs());
                }
            }
        }
        
        BigDecimal netIncome = totalRevenue.subtract(totalExpense);
        
        report.put("reportType", "Income Statement");
        report.put("startDate", startDate);
        report.put("endDate", endDate);
        report.put("revenues", revenues);
        report.put("totalRevenue", totalRevenue);
        report.put("expenses", expenses);
        report.put("totalExpense", totalExpense);
        report.put("netIncome", netIncome);
        
        return report;
    }
    
    /**
     * Generate Balance Sheet (Statement of Financial Position)
     * Shows assets, liabilities, and equity at a specific date
     */
    public Map<String, Object> generateBalanceSheet(LocalDate asOfDate) {
        Map<String, Object> report = new HashMap<>();
        
        List<Account> allAccounts = new ArrayList<>();
        accountRepository.findAll().forEach(allAccounts::add);
        List<JournalEntry> journals = journalRepository.findByDateBetweenAndStatus(
            LocalDate.of(2000, 1, 1), asOfDate, JournalStatus.POSTED);
        
        // Calculate cumulative account balances up to the date
        Map<Long, BigDecimal> accountBalances = calculateAccountBalances(journals);
        
        // Assets section
        List<Map<String, Object>> assets = new ArrayList<>();
        BigDecimal totalAssets = BigDecimal.ZERO;
        
        for (Account account : allAccounts) {
            if (account.getType() == AccountType.ASSET) {
                BigDecimal balance = accountBalances.getOrDefault(account.getId(), BigDecimal.ZERO);
                if (balance.compareTo(BigDecimal.ZERO) != 0) {
                    Map<String, Object> item = new HashMap<>();
                    item.put("accountCode", account.getCode());
                    item.put("accountName", account.getName());
                    item.put("amount", balance.abs());
                    assets.add(item);
                    totalAssets = totalAssets.add(balance.abs());
                }
            }
        }
        
        // Liabilities section
        List<Map<String, Object>> liabilities = new ArrayList<>();
        BigDecimal totalLiabilities = BigDecimal.ZERO;
        
        for (Account account : allAccounts) {
            if (account.getType() == AccountType.LIABILITY) {
                BigDecimal balance = accountBalances.getOrDefault(account.getId(), BigDecimal.ZERO);
                if (balance.compareTo(BigDecimal.ZERO) != 0) {
                    Map<String, Object> item = new HashMap<>();
                    item.put("accountCode", account.getCode());
                    item.put("accountName", account.getName());
                    item.put("amount", balance.abs());
                    liabilities.add(item);
                    totalLiabilities = totalLiabilities.add(balance.abs());
                }
            }
        }
        
        // Equity section
        List<Map<String, Object>> equity = new ArrayList<>();
        BigDecimal totalEquity = BigDecimal.ZERO;
        
        for (Account account : allAccounts) {
            if (account.getType() == AccountType.EQUITY) {
                BigDecimal balance = accountBalances.getOrDefault(account.getId(), BigDecimal.ZERO);
                if (balance.compareTo(BigDecimal.ZERO) != 0) {
                    Map<String, Object> item = new HashMap<>();
                    item.put("accountCode", account.getCode());
                    item.put("accountName", account.getName());
                    item.put("amount", balance.abs());
                    equity.add(item);
                    totalEquity = totalEquity.add(balance.abs());
                }
            }
        }
        
        // Calculate retained earnings (Net Income from all periods)
        BigDecimal retainedEarnings = calculateRetainedEarnings(asOfDate, accountBalances);
        if (retainedEarnings.compareTo(BigDecimal.ZERO) != 0) {
            Map<String, Object> item = new HashMap<>();
            item.put("accountCode", "RE");
            item.put("accountName", "Retained Earnings");
            item.put("amount", retainedEarnings.abs());
            equity.add(item);
            totalEquity = totalEquity.add(retainedEarnings.abs());
        }
        
        report.put("reportType", "Balance Sheet");
        report.put("asOfDate", asOfDate);
        report.put("assets", assets);
        report.put("totalAssets", totalAssets);
        report.put("liabilities", liabilities);
        report.put("totalLiabilities", totalLiabilities);
        report.put("equity", equity);
        report.put("totalEquity", totalEquity);
        report.put("totalLiabilitiesAndEquity", totalLiabilities.add(totalEquity));
        
        return report;
    }
    
    /**
     * Generate Trial Balance
     * Lists all accounts with their debit and credit balances
     */
    public Map<String, Object> generateTrialBalance(LocalDate asOfDate) {
        Map<String, Object> report = new HashMap<>();
        
        List<Account> allAccounts = new ArrayList<>();
        accountRepository.findAll().forEach(allAccounts::add);
        List<JournalEntry> journals = journalRepository.findByDateBetweenAndStatus(
            LocalDate.of(2000, 1, 1), asOfDate, JournalStatus.POSTED);
        
        Map<Long, BigDecimal> accountBalances = calculateAccountBalances(journals);
        
        List<Map<String, Object>> accounts = new ArrayList<>();
        BigDecimal totalDebit = BigDecimal.ZERO;
        BigDecimal totalCredit = BigDecimal.ZERO;
        
        for (Account account : allAccounts) {
            BigDecimal balance = accountBalances.getOrDefault(account.getId(), BigDecimal.ZERO);
            
            if (balance.compareTo(BigDecimal.ZERO) != 0) {
                Map<String, Object> item = new HashMap<>();
                item.put("accountCode", account.getCode());
                item.put("accountName", account.getName());
                item.put("accountType", account.getType());
                
                // Determine if balance is debit or credit based on account type
                boolean isDebitBalance = isDebitNormal(account.getType());
                
                if ((isDebitBalance && balance.compareTo(BigDecimal.ZERO) > 0) ||
                    (!isDebitBalance && balance.compareTo(BigDecimal.ZERO) < 0)) {
                    item.put("debit", balance.abs());
                    item.put("credit", BigDecimal.ZERO);
                    totalDebit = totalDebit.add(balance.abs());
                } else {
                    item.put("debit", BigDecimal.ZERO);
                    item.put("credit", balance.abs());
                    totalCredit = totalCredit.add(balance.abs());
                }
                
                accounts.add(item);
            }
        }
        
        report.put("reportType", "Trial Balance");
        report.put("asOfDate", asOfDate);
        report.put("accounts", accounts);
        report.put("totalDebit", totalDebit);
        report.put("totalCredit", totalCredit);
        report.put("isBalanced", totalDebit.compareTo(totalCredit) == 0);
        
        return report;
    }
    
    /**
     * Generate Cash Flow Statement
     * Shows cash inflows and outflows categorized by operating, investing, and financing activities
     */
    public Map<String, Object> generateCashFlowStatement(LocalDate startDate, LocalDate endDate) {
        Map<String, Object> report = new HashMap<>();
        
        List<JournalEntry> journals = journalRepository.findByDateBetweenAndStatus(
            startDate, endDate, JournalStatus.POSTED);
        
        BigDecimal operatingCashFlow = BigDecimal.ZERO;
        BigDecimal investingCashFlow = BigDecimal.ZERO;
        BigDecimal financingCashFlow = BigDecimal.ZERO;
        
        List<Map<String, Object>> operatingActivities = new ArrayList<>();
        List<Map<String, Object>> investingActivities = new ArrayList<>();
        List<Map<String, Object>> financingActivities = new ArrayList<>();
        
        // Get cash account
        List<Account> allAccounts = new ArrayList<>();
        accountRepository.findAll().forEach(allAccounts::add);
        Optional<Account> cashAccountOpt = allAccounts.stream()
            .filter(a -> a.getCode().equals("1000") || a.getName().toLowerCase().contains("cash"))
            .findFirst();
        
        if (cashAccountOpt.isPresent()) {
            Long cashAccountId = cashAccountOpt.get().getId();
            
            for (JournalEntry journal : journals) {
                for (JournalLine line : journal.getLines()) {
                    if (line.getAccountId().equals(cashAccountId)) {
                        BigDecimal cashChange = line.getDebit().subtract(line.getCredit());
                        
                        Map<String, Object> activity = new HashMap<>();
                        activity.put("date", journal.getDate());
                        activity.put("description", line.getDescription());
                        activity.put("amount", cashChange);
                        
                        // Categorize based on description or account involved
                        String desc = line.getDescription().toLowerCase();
                        if (desc.contains("sales") || desc.contains("revenue") || desc.contains("expense")) {
                            operatingActivities.add(activity);
                            operatingCashFlow = operatingCashFlow.add(cashChange);
                        } else if (desc.contains("asset") || desc.contains("investment") || desc.contains("equipment")) {
                            investingActivities.add(activity);
                            investingCashFlow = investingCashFlow.add(cashChange);
                        } else {
                            financingActivities.add(activity);
                            financingCashFlow = financingCashFlow.add(cashChange);
                        }
                    }
                }
            }
        }
        
        BigDecimal netCashFlow = operatingCashFlow.add(investingCashFlow).add(financingCashFlow);
        
        report.put("reportType", "Cash Flow Statement");
        report.put("startDate", startDate);
        report.put("endDate", endDate);
        report.put("operatingActivities", operatingActivities);
        report.put("operatingCashFlow", operatingCashFlow);
        report.put("investingActivities", investingActivities);
        report.put("investingCashFlow", investingCashFlow);
        report.put("financingActivities", financingActivities);
        report.put("financingCashFlow", financingCashFlow);
        report.put("netCashFlow", netCashFlow);
        
        return report;
    }
    
    /**
     * Generate General Ledger Report for a specific account
     */
    public Map<String, Object> generateAccountLedger(Long accountId, LocalDate startDate, LocalDate endDate) {
        Map<String, Object> report = new HashMap<>();
        
        Account account = accountRepository.findById(accountId)
            .orElseThrow(() -> new RuntimeException("Account not found"));
        
        List<JournalEntry> journals = journalRepository.findByDateBetweenAndStatus(
            startDate, endDate, JournalStatus.POSTED);
        
        List<Map<String, Object>> transactions = new ArrayList<>();
        BigDecimal runningBalance = BigDecimal.ZERO;
        
        for (JournalEntry journal : journals) {
            for (JournalLine line : journal.getLines()) {
                if (line.getAccountId().equals(accountId)) {
                    Map<String, Object> transaction = new HashMap<>();
                    transaction.put("date", journal.getDate());
                    transaction.put("description", line.getDescription());
                    transaction.put("debit", line.getDebit());
                    transaction.put("credit", line.getCredit());
                    
                    BigDecimal change = line.getDebit().subtract(line.getCredit());
                    runningBalance = runningBalance.add(change);
                    transaction.put("balance", runningBalance);
                    
                    transactions.add(transaction);
                }
            }
        }
        
        report.put("reportType", "Account Ledger");
        report.put("accountCode", account.getCode());
        report.put("accountName", account.getName());
        report.put("accountType", account.getType());
        report.put("startDate", startDate);
        report.put("endDate", endDate);
        report.put("transactions", transactions);
        report.put("endingBalance", runningBalance);
        
        return report;
    }
    
    /**
     * Helper method to calculate account balances from journal entries
     */
    private Map<Long, BigDecimal> calculateAccountBalances(List<JournalEntry> journals) {
        Map<Long, BigDecimal> balances = new HashMap<>();
        
        for (JournalEntry journal : journals) {
            for (JournalLine line : journal.getLines()) {
                Long accountId = line.getAccountId();
                BigDecimal change = line.getDebit().subtract(line.getCredit());
                balances.merge(accountId, change, BigDecimal::add);
            }
        }
        
        return balances;
    }
    
    /**
     * Calculate retained earnings (accumulated net income)
     */
    private BigDecimal calculateRetainedEarnings(LocalDate asOfDate, Map<Long, BigDecimal> accountBalances) {
        List<Account> allAccounts = new ArrayList<>();
        accountRepository.findAll().forEach(allAccounts::add);
        
        BigDecimal totalRevenue = BigDecimal.ZERO;
        BigDecimal totalExpense = BigDecimal.ZERO;
        
        for (Account account : allAccounts) {
            BigDecimal balance = accountBalances.getOrDefault(account.getId(), BigDecimal.ZERO);
            
            if (account.getType() == AccountType.REVENUE) {
                totalRevenue = totalRevenue.add(balance.abs());
            } else if (account.getType() == AccountType.EXPENSE) {
                totalExpense = totalExpense.add(balance.abs());
            }
        }
        
        return totalRevenue.subtract(totalExpense);
    }
    
    /**
     * Determine if an account type has a normal debit balance
     */
    private boolean isDebitNormal(AccountType type) {
        return type == AccountType.ASSET || type == AccountType.EXPENSE;
    }
}
