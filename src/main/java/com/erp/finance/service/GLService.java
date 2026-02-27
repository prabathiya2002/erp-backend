package com.erp.finance.service;

import com.erp.finance.domain.*;
import com.erp.finance.repository.AccountRepository;
import com.erp.finance.repository.JournalRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class GLService {
    private final JournalRepository journals;
    private final AccountRepository accounts;

    public GLService(JournalRepository journals, AccountRepository accounts) {
        this.journals = journals;
        this.accounts = accounts;
    }

    public TrialBalance computeTrialBalance(String period) {
        BigDecimal assets = BigDecimal.ZERO;
        BigDecimal liabilities = BigDecimal.ZERO;
        BigDecimal equity = BigDecimal.ZERO;
        BigDecimal revenue = BigDecimal.ZERO;
        BigDecimal expenses = BigDecimal.ZERO;

        List<JournalEntry> posted = journals.findByPeriodAndStatus(period, JournalStatus.POSTED);
        for (JournalEntry je : posted) {
            for (JournalLine line : je.getLines()) {
                Optional<Account> opt = accounts.findById(line.getAccountId());
                if (opt.isEmpty()) continue;
                Account acct = opt.get();
                AccountType type = acct.getType();
                BigDecimal effect;
                // For assets & expenses: debit increases balance; credit decreases
                // For liabilities, equity, revenue: credit increases balance; debit decreases
                if (type == AccountType.ASSET) {
                    effect = line.getDebit().subtract(line.getCredit());
                    assets = assets.add(effect);
                } else if (type == AccountType.EXPENSE) {
                    effect = line.getDebit().subtract(line.getCredit());
                    expenses = expenses.add(effect);
                } else if (type == AccountType.LIABILITY) {
                    effect = line.getCredit().subtract(line.getDebit());
                    liabilities = liabilities.add(effect);
                } else if (type == AccountType.EQUITY) {
                    effect = line.getCredit().subtract(line.getDebit());
                    equity = equity.add(effect);
                } else if (type == AccountType.REVENUE) {
                    effect = line.getCredit().subtract(line.getDebit());
                    revenue = revenue.add(effect);
                }
            }
        }
        // Revenue and Expenses are part of Equity (Retained Earnings)
        // Accounting Equation: Assets = Liabilities + Equity + (Revenue - Expenses)
        // Or: Assets + Expenses = Liabilities + Equity + Revenue
        BigDecimal totalEquity = equity.add(revenue).subtract(expenses);
        boolean equationOk = assets.compareTo(liabilities.add(totalEquity)) == 0;
        return new TrialBalance(assets, liabilities, totalEquity, revenue, expenses, equationOk);
    }

    public TrialBalance computeTrialBalanceByDateRange(LocalDate startDate, LocalDate endDate) {
        BigDecimal assets = BigDecimal.ZERO;
        BigDecimal liabilities = BigDecimal.ZERO;
        BigDecimal equity = BigDecimal.ZERO;
        BigDecimal revenue = BigDecimal.ZERO;
        BigDecimal expenses = BigDecimal.ZERO;

        List<JournalEntry> posted = journals.findByDateBetweenAndStatus(startDate, endDate, JournalStatus.POSTED);
        for (JournalEntry je : posted) {
            for (JournalLine line : je.getLines()) {
                Optional<Account> opt = accounts.findById(line.getAccountId());
                if (opt.isEmpty()) continue;
                Account acct = opt.get();
                AccountType type = acct.getType();
                BigDecimal effect;
                // For assets & expenses: debit increases balance; credit decreases
                // For liabilities, equity, revenue: credit increases balance; debit decreases
                if (type == AccountType.ASSET) {
                    effect = line.getDebit().subtract(line.getCredit());
                    assets = assets.add(effect);
                } else if (type == AccountType.EXPENSE) {
                    effect = line.getDebit().subtract(line.getCredit());
                    expenses = expenses.add(effect);
                } else if (type == AccountType.LIABILITY) {
                    effect = line.getCredit().subtract(line.getDebit());
                    liabilities = liabilities.add(effect);
                } else if (type == AccountType.EQUITY) {
                    effect = line.getCredit().subtract(line.getDebit());
                    equity = equity.add(effect);
                } else if (type == AccountType.REVENUE) {
                    effect = line.getCredit().subtract(line.getDebit());
                    revenue = revenue.add(effect);
                }
            }
        }
        BigDecimal totalEquity = equity.add(revenue).subtract(expenses);
        boolean equationOk = assets.compareTo(liabilities.add(totalEquity)) == 0;
        return new TrialBalance(assets, liabilities, totalEquity, revenue, expenses, equationOk);
    }

    public static class TrialBalance {
        private final BigDecimal assets;
        private final BigDecimal liabilities;
        private final BigDecimal equity;
        private final BigDecimal revenue;
        private final BigDecimal expenses;
        private final boolean equationOk;

        public TrialBalance(BigDecimal assets, BigDecimal liabilities, BigDecimal equity,
                            BigDecimal revenue, BigDecimal expenses, boolean equationOk) {
            this.assets = assets; this.liabilities = liabilities; this.equity = equity;
            this.revenue = revenue; this.expenses = expenses; this.equationOk = equationOk;
        }
        public BigDecimal getAssets() { return assets; }
        public BigDecimal getLiabilities() { return liabilities; }
        public BigDecimal getEquity() { return equity; }
        public BigDecimal getRevenue() { return revenue; }
        public BigDecimal getExpenses() { return expenses; }
        public boolean isEquationOk() { return equationOk; }
    }
}
