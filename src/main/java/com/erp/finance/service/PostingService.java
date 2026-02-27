package com.erp.finance.service;

import com.erp.finance.domain.*;
import com.erp.finance.repository.AccountRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Optional;

@Service
public class PostingService {
    private final AccountRepository accounts;

    public PostingService(AccountRepository accounts) {
        this.accounts = accounts;
    }

    public void applyLineToAccount(JournalLine line) {
        Optional<Account> opt = accounts.findById(line.getAccountId());
        if (opt.isEmpty()) return;
        Account acct = opt.get();
        AccountType type = acct.getType();
        BigDecimal balance = acct.getBalance();
        BigDecimal newBalance;
        if (type == AccountType.ASSET || type == AccountType.EXPENSE) {
            newBalance = balance.add(line.getDebit()).subtract(line.getCredit());
        } else {
            newBalance = balance.subtract(line.getDebit()).add(line.getCredit());
        }
        acct.setBalance(newBalance);
        accounts.save(acct);
    }
}
