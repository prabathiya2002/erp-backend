package com.erp.finance.service;

import com.erp.finance.domain.*;
import com.erp.finance.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class FixedAssetService {
    
    private final FixedAssetRepository fixedAssetRepository;
    private final AccountRepository accountRepository;
    private final JournalRepository journalRepository;
    private final PostingService postingService;
    
    public FixedAssetService(FixedAssetRepository fixedAssetRepository,
                            AccountRepository accountRepository,
                            JournalRepository journalRepository,
                            PostingService postingService) {
        this.fixedAssetRepository = fixedAssetRepository;
        this.accountRepository = accountRepository;
        this.journalRepository = journalRepository;
        this.postingService = postingService;
    }
    
    public List<FixedAsset> getAllAssets() {
        return fixedAssetRepository.findAll();
    }
    
    public FixedAsset getAssetById(Long id) {
        return fixedAssetRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Asset not found"));
    }
    
    @Transactional
    public FixedAsset createAsset(FixedAsset asset) {
        // Generate asset code if not provided
        if (asset.getAssetCode() == null || asset.getAssetCode().isEmpty()) {
            asset.setAssetCode(generateAssetCode(asset.getCategory()));
        }
        
        // Save asset first
        FixedAsset saved = fixedAssetRepository.save(asset);
        
        // Create GL account for the asset
        createAssetAccount(saved);
        
        // Create journal entry for asset purchase
        createPurchaseJournalEntry(saved);
        
        return saved;
    }
    
    @Transactional
    public FixedAsset updateAsset(Long id, FixedAsset updatedAsset) {
        FixedAsset existing = getAssetById(id);
        
        existing.setAssetName(updatedAsset.getAssetName());
        existing.setCategory(updatedAsset.getCategory());
        existing.setLocation(updatedAsset.getLocation());
        existing.setDescription(updatedAsset.getDescription());
        existing.setUsefulLifeYears(updatedAsset.getUsefulLifeYears());
        existing.setSalvageValue(updatedAsset.getSalvageValue());
        
        return fixedAssetRepository.save(existing);
    }
    
    @Transactional
    public void deleteAsset(Long id) {
        fixedAssetRepository.deleteById(id);
    }
    
    @Transactional
    public void recordDepreciation(Long assetId, LocalDate depreciationDate) {
        FixedAsset asset = getAssetById(assetId);
        
        if (asset.getStatus() != FixedAsset.AssetStatus.ACTIVE) {
            throw new RuntimeException("Cannot depreciate non-active asset");
        }
        
        BigDecimal depreciationAmount = calculateDepreciation(asset, depreciationDate);
        
        // Update accumulated depreciation
        asset.setAccumulatedDepreciation(
            asset.getAccumulatedDepreciation().add(depreciationAmount)
        );
        
        // Check if fully depreciated
        BigDecimal netBookValue = asset.getPurchaseCost()
                .subtract(asset.getAccumulatedDepreciation());
        
        if (netBookValue.compareTo(asset.getSalvageValue()) <= 0) {
            asset.setStatus(FixedAsset.AssetStatus.FULLY_DEPRECIATED);
        }
        
        fixedAssetRepository.save(asset);
        
        // Create depreciation journal entry
        createDepreciationJournalEntry(asset, depreciationAmount, depreciationDate);
    }
    
    @Transactional
    public void disposeAsset(Long assetId, LocalDate disposalDate, BigDecimal disposalAmount) {
        FixedAsset asset = getAssetById(assetId);
        
        asset.setStatus(FixedAsset.AssetStatus.DISPOSED);
        asset.setDisposalDate(disposalDate);
        asset.setDisposalAmount(disposalAmount);
        
        fixedAssetRepository.save(asset);
        
        // Create disposal journal entry
        createDisposalJournalEntry(asset);
    }
    
    private void createAssetAccount(FixedAsset asset) {
        String accountCode = "1500-" + System.currentTimeMillis();
        String accountName = "Fixed Asset - " + asset.getAssetName();
        
        Account assetAccount = findOrCreateAccount(accountCode, accountName, AccountType.ASSET);
        asset.setAccountId(assetAccount.getId());
        fixedAssetRepository.save(asset);
    }
    
    private void createPurchaseJournalEntry(FixedAsset asset) {
        // Debit: Fixed Asset Account (increase asset)
        // Credit: Cash/Bank (decrease asset)
        
        Account assetAccount = accountRepository.findById(asset.getAccountId())
                .orElseThrow(() -> new RuntimeException("Asset account not found"));
        Account cashAccount = findOrCreateAccount("1000", "Cash and Bank", AccountType.ASSET);
        
        JournalEntry journal = new JournalEntry();
        journal.setDate(asset.getPurchaseDate());
        journal.setPeriod(asset.getPurchaseDate().format(DateTimeFormatter.ofPattern("yyyy-MM")));
        journal.setStatus(JournalStatus.POSTED);
        
        // Debit Fixed Asset
        JournalLine assetLine = new JournalLine();
        assetLine.setJournal(journal);
        assetLine.setAccountId(assetAccount.getId());
        assetLine.setDebit(asset.getPurchaseCost());
        assetLine.setCredit(BigDecimal.ZERO);
        assetLine.setDescription("Purchase of " + asset.getAssetName());
        
        // Credit Cash
        JournalLine cashLine = new JournalLine();
        cashLine.setJournal(journal);
        cashLine.setAccountId(cashAccount.getId());
        cashLine.setDebit(BigDecimal.ZERO);
        cashLine.setCredit(asset.getPurchaseCost());
        cashLine.setDescription("Payment for " + asset.getAssetName());
        
        journal.getLines().add(assetLine);
        journal.getLines().add(cashLine);
        
        journalRepository.save(journal);
        
        // Post to GL
        postingService.applyLineToAccount(assetLine);
        postingService.applyLineToAccount(cashLine);
    }
    
    private void createDepreciationJournalEntry(FixedAsset asset, BigDecimal amount, LocalDate date) {
        // Debit: Depreciation Expense (increase expense)
        // Credit: Accumulated Depreciation (contra-asset, reduces fixed asset value)
        
        Account expenseAccount = findOrCreateAccount("6000", "Depreciation Expense", AccountType.EXPENSE);
        Account accumDepAccount = findOrCreateAccount("1590", "Accumulated Depreciation", AccountType.ASSET);
        
        JournalEntry journal = new JournalEntry();
        journal.setDate(date);
        journal.setPeriod(date.format(DateTimeFormatter.ofPattern("yyyy-MM")));
        journal.setStatus(JournalStatus.POSTED);
        
        // Debit Depreciation Expense
        JournalLine expenseLine = new JournalLine();
        expenseLine.setJournal(journal);
        expenseLine.setAccountId(expenseAccount.getId());
        expenseLine.setDebit(amount);
        expenseLine.setCredit(BigDecimal.ZERO);
        expenseLine.setDescription("Depreciation: " + asset.getAssetName());
        
        // Credit Accumulated Depreciation
        JournalLine accumLine = new JournalLine();
        accumLine.setJournal(journal);
        accumLine.setAccountId(accumDepAccount.getId());
        accumLine.setDebit(BigDecimal.ZERO);
        accumLine.setCredit(amount);
        accumLine.setDescription("Accumulated Depreciation: " + asset.getAssetName());
        
        journal.getLines().add(expenseLine);
        journal.getLines().add(accumLine);
        
        journalRepository.save(journal);
        
        // Post to GL
        postingService.applyLineToAccount(expenseLine);
        postingService.applyLineToAccount(accumLine);
    }
    
    private void createDisposalJournalEntry(FixedAsset asset) {
        // Calculate gain/loss on disposal
        BigDecimal netBookValue = asset.getPurchaseCost()
                .subtract(asset.getAccumulatedDepreciation());
        BigDecimal gainLoss = asset.getDisposalAmount().subtract(netBookValue);
        
        Account assetAccount = accountRepository.findById(asset.getAccountId())
                .orElseThrow(() -> new RuntimeException("Asset account not found"));
        Account accumDepAccount = findOrCreateAccount("1590", "Accumulated Depreciation", AccountType.ASSET);
        Account cashAccount = findOrCreateAccount("1000", "Cash and Bank", AccountType.ASSET);
        
        JournalEntry journal = new JournalEntry();
        journal.setDate(asset.getDisposalDate());
        journal.setPeriod(asset.getDisposalDate().format(DateTimeFormatter.ofPattern("yyyy-MM")));
        journal.setStatus(JournalStatus.POSTED);
        
        // Debit: Cash (if sold)
        JournalLine cashLine = new JournalLine();
        cashLine.setJournal(journal);
        cashLine.setAccountId(cashAccount.getId());
        cashLine.setDebit(asset.getDisposalAmount());
        cashLine.setCredit(BigDecimal.ZERO);
        cashLine.setDescription("Disposal proceeds: " + asset.getAssetName());
        
        // Debit: Accumulated Depreciation (remove accumulated depreciation)
        JournalLine accumLine = new JournalLine();
        accumLine.setJournal(journal);
        accumLine.setAccountId(accumDepAccount.getId());
        accumLine.setDebit(asset.getAccumulatedDepreciation());
        accumLine.setCredit(BigDecimal.ZERO);
        accumLine.setDescription("Remove accumulated depreciation: " + asset.getAssetName());
        
        // Credit: Fixed Asset (remove asset from books)
        JournalLine assetLine = new JournalLine();
        assetLine.setJournal(journal);
        assetLine.setAccountId(assetAccount.getId());
        assetLine.setDebit(BigDecimal.ZERO);
        assetLine.setCredit(asset.getPurchaseCost());
        assetLine.setDescription("Disposal of " + asset.getAssetName());
        
        journal.getLines().add(cashLine);
        journal.getLines().add(accumLine);
        journal.getLines().add(assetLine);
        
        // Handle gain/loss
        if (gainLoss.compareTo(BigDecimal.ZERO) != 0) {
            Account gainLossAccount;
            JournalLine gainLossLine = new JournalLine();
            gainLossLine.setJournal(journal);
            
            if (gainLoss.compareTo(BigDecimal.ZERO) > 0) {
                // Gain on disposal
                gainLossAccount = findOrCreateAccount("7000", "Gain on Asset Disposal", AccountType.REVENUE);
                gainLossLine.setDebit(BigDecimal.ZERO);
                gainLossLine.setCredit(gainLoss);
                gainLossLine.setDescription("Gain on disposal: " + asset.getAssetName());
            } else {
                // Loss on disposal
                gainLossAccount = findOrCreateAccount("6500", "Loss on Asset Disposal", AccountType.EXPENSE);
                gainLossLine.setDebit(gainLoss.abs());
                gainLossLine.setCredit(BigDecimal.ZERO);
                gainLossLine.setDescription("Loss on disposal: " + asset.getAssetName());
            }
            
            gainLossLine.setAccountId(gainLossAccount.getId());
            journal.getLines().add(gainLossLine);
            postingService.applyLineToAccount(gainLossLine);
        }
        
        journalRepository.save(journal);
        
        // Post to GL
        postingService.applyLineToAccount(cashLine);
        postingService.applyLineToAccount(accumLine);
        postingService.applyLineToAccount(assetLine);
    }
    
    private BigDecimal calculateDepreciation(FixedAsset asset, LocalDate depreciationDate) {
        if (asset.getDepreciationMethod() == FixedAsset.DepreciationMethod.STRAIGHT_LINE) {
            return calculateStraightLineDepreciation(asset);
        } else if (asset.getDepreciationMethod() == FixedAsset.DepreciationMethod.DECLINING_BALANCE) {
            return calculateDecliningBalanceDepreciation(asset);
        }
        
        return BigDecimal.ZERO;
    }
    
    private BigDecimal calculateStraightLineDepreciation(FixedAsset asset) {
        // (Cost - Salvage Value) / Useful Life
        BigDecimal depreciableAmount = asset.getPurchaseCost().subtract(asset.getSalvageValue());
        BigDecimal annualDepreciation = depreciableAmount.divide(
            new BigDecimal(asset.getUsefulLifeYears()), 2, RoundingMode.HALF_UP
        );
        
        // Return monthly depreciation
        return annualDepreciation.divide(new BigDecimal(12), 2, RoundingMode.HALF_UP);
    }
    
    private BigDecimal calculateDecliningBalanceDepreciation(FixedAsset asset) {
        // Double declining balance: (2 / Useful Life) * Book Value
        BigDecimal rate = new BigDecimal(2).divide(
            new BigDecimal(asset.getUsefulLifeYears()), 4, RoundingMode.HALF_UP
        );
        
        BigDecimal bookValue = asset.getPurchaseCost().subtract(asset.getAccumulatedDepreciation());
        BigDecimal annualDepreciation = bookValue.multiply(rate);
        
        // Return monthly depreciation
        return annualDepreciation.divide(new BigDecimal(12), 2, RoundingMode.HALF_UP);
    }
    
    private String generateAssetCode(FixedAsset.AssetCategory category) {
        String prefix = category.name().substring(0, 3);
        long timestamp = System.currentTimeMillis();
        return prefix + "-" + timestamp;
    }
    
    private Account findOrCreateAccount(String code, String name, AccountType type) {
        for (Account account : accountRepository.findAll()) {
            if (account.getCode().equals(code)) {
                return account;
            }
        }
        
        Account newAccount = new Account();
        newAccount.setCode(code);
        newAccount.setName(name);
        newAccount.setType(type);
        newAccount.setBalance(BigDecimal.ZERO);
        newAccount.setStatus("Active");
        return accountRepository.save(newAccount);
    }
}
