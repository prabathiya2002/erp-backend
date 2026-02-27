package com.erp.finance.service;

import com.erp.finance.domain.*;
import com.erp.finance.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class APService {
    private final APInvoiceRepository apInvoiceRepository;
    private final APPaymentRepository apPaymentRepository;
    private final VendorRepository vendorRepository;
    private final JournalRepository journalRepository;
    private final AccountRepository accountRepository;
    private final PostingService postingService;

    public APService(APInvoiceRepository apInvoiceRepository,
                     APPaymentRepository apPaymentRepository,
                     VendorRepository vendorRepository,
                     JournalRepository journalRepository,
                     AccountRepository accountRepository,
                     PostingService postingService) {
        this.apInvoiceRepository = apInvoiceRepository;
        this.apPaymentRepository = apPaymentRepository;
        this.vendorRepository = vendorRepository;
        this.journalRepository = journalRepository;
        this.accountRepository = accountRepository;
        this.postingService = postingService;
    }

    public List<APInvoice> getAllInvoices() {
        List<APInvoice> invoices = new ArrayList<>();
        apInvoiceRepository.findAll().forEach(invoice -> {
            // Set vendor name
            vendorRepository.findById(invoice.getVendorId()).ifPresent(vendor -> {
                invoice.setVendorName(vendor.getName());
            });
            invoices.add(invoice);
        });
        return invoices;
    }

    public APInvoice getInvoiceById(Long id) {
        APInvoice invoice = apInvoiceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Invoice not found"));
        vendorRepository.findById(invoice.getVendorId()).ifPresent(vendor -> {
            invoice.setVendorName(vendor.getName());
        });
        return invoice;
    }

    @Transactional
    public APInvoice createInvoice(APInvoice invoice) {
        if (apInvoiceRepository.existsByInvoiceNumber(invoice.getInvoiceNumber())) {
            throw new RuntimeException("Invoice number already exists");
        }
        
        // Save invoice first
        APInvoice saved = apInvoiceRepository.save(invoice);
        
        // Create journal entry for AP invoice
        // Debit: Expense Account (or asset if inventory)
        // Credit: Accounts Payable
        createAPInvoiceJournalEntry(saved);
        
        return saved;
    }
    
    private void createAPInvoiceJournalEntry(APInvoice invoice) {
        // Find accounts
        Account expenseAccount = findOrCreateAccount("5000", "Operating Expenses", AccountType.EXPENSE);
        
        // Get vendor-specific AP account
        Vendor vendor = vendorRepository.findById(invoice.getVendorId())
                .orElseThrow(() -> new RuntimeException("Vendor not found"));
        
        Account vendorAccount;
        if (vendor.getAccountId() != null) {
            vendorAccount = accountRepository.findById(vendor.getAccountId())
                    .orElseThrow(() -> new RuntimeException("Vendor account not found"));
        } else {
            // Fallback to control account
            vendorAccount = findOrCreateAccount("2100", "Accounts Payable - Control", AccountType.LIABILITY);
        }
        
        // Create journal entry
        JournalEntry journal = new JournalEntry();
        journal.setDate(invoice.getInvoiceDate());
        journal.setPeriod(invoice.getInvoiceDate().format(DateTimeFormatter.ofPattern("yyyy-MM")));
        journal.setStatus(JournalStatus.POSTED);
        
        // Debit expense
        JournalLine expenseLine = new JournalLine();
        expenseLine.setJournal(journal);
        expenseLine.setAccountId(expenseAccount.getId());
        expenseLine.setDebit(invoice.getAmount());
        expenseLine.setCredit(BigDecimal.ZERO);
        expenseLine.setDescription("AP Invoice: " + invoice.getInvoiceNumber() + " - " + vendor.getName());
        
        // Credit vendor-specific AP account
        JournalLine apLine = new JournalLine();
        apLine.setJournal(journal);
        apLine.setAccountId(vendorAccount.getId());
        apLine.setDebit(BigDecimal.ZERO);
        apLine.setCredit(invoice.getAmount());
        apLine.setDescription("AP Invoice: " + invoice.getInvoiceNumber() + " - " + vendor.getName());
        
        journal.getLines().add(expenseLine);
        journal.getLines().add(apLine);
        
        journalRepository.save(journal);
        
        // Post to GL
        postingService.applyLineToAccount(expenseLine);
        postingService.applyLineToAccount(apLine);
    }
    
    private void createAPPaymentJournalEntry(APPayment payment, APInvoice invoice) {
        // Get vendor-specific AP account
        Vendor vendor = vendorRepository.findById(invoice.getVendorId())
                .orElseThrow(() -> new RuntimeException("Vendor not found"));
        
        Account vendorAccount;
        if (vendor.getAccountId() != null) {
            vendorAccount = accountRepository.findById(vendor.getAccountId())
                    .orElseThrow(() -> new RuntimeException("Vendor account not found"));
        } else {
            vendorAccount = findOrCreateAccount("2100", "Accounts Payable - Control", AccountType.LIABILITY);
        }
        
        Account cashAccount = findOrCreateAccount("1000", "Cash and Bank", AccountType.ASSET);
        
        // Create journal entry
        JournalEntry journal = new JournalEntry();
        journal.setDate(payment.getPaymentDate());
        journal.setPeriod(payment.getPaymentDate().format(DateTimeFormatter.ofPattern("yyyy-MM")));
        journal.setStatus(JournalStatus.POSTED);
        
        // Debit vendor-specific AP (reduce liability)
        JournalLine apLine = new JournalLine();
        apLine.setJournal(journal);
        apLine.setAccountId(vendorAccount.getId());
        apLine.setDebit(payment.getAmount());
        apLine.setCredit(BigDecimal.ZERO);
        apLine.setDescription("AP Payment: " + invoice.getInvoiceNumber() + " - " + vendor.getName());
        
        // Credit Cash (reduce asset)
        JournalLine cashLine = new JournalLine();
        cashLine.setJournal(journal);
        cashLine.setAccountId(cashAccount.getId());
        cashLine.setDebit(BigDecimal.ZERO);
        cashLine.setCredit(payment.getAmount());
        cashLine.setDescription("AP Payment: " + invoice.getInvoiceNumber() + " - " + vendor.getName());
        
        journal.getLines().add(apLine);
        journal.getLines().add(cashLine);
        
        journalRepository.save(journal);
        
        // Post to GL
        postingService.applyLineToAccount(apLine);
        postingService.applyLineToAccount(cashLine);
    }
    
    private Account findOrCreateAccount(String code, String name, AccountType type) {
        for (Account account : accountRepository.findAll()) {
            if (account.getCode().equals(code)) {
                return account;
            }
        }
        
        // Create account if not found
        Account newAccount = new Account();
        newAccount.setCode(code);
        newAccount.setName(name);
        newAccount.setType(type);
        newAccount.setBalance(BigDecimal.ZERO);
        newAccount.setStatus("Active");
        return accountRepository.save(newAccount);
    }

    @Transactional
    public APInvoice updateInvoice(Long id, APInvoice updatedInvoice) {
        APInvoice existing = apInvoiceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Invoice not found"));
        
        existing.setVendorId(updatedInvoice.getVendorId());
        existing.setInvoiceNumber(updatedInvoice.getInvoiceNumber());
        existing.setInvoiceDate(updatedInvoice.getInvoiceDate());
        existing.setDueDate(updatedInvoice.getDueDate());
        existing.setAmount(updatedInvoice.getAmount());
        existing.setDescription(updatedInvoice.getDescription());
        existing.setStatus(updatedInvoice.getStatus());
        
        return apInvoiceRepository.save(existing);
    }

    @Transactional
    public void deleteInvoice(Long id) {
        apInvoiceRepository.deleteById(id);
    }

    @Transactional
    public void payInvoice(Long id) {
        APInvoice invoice = apInvoiceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Invoice not found"));
        
        invoice.setStatus(APInvoice.InvoiceStatus.PAID);
        apInvoiceRepository.save(invoice);
        
        // Create payment record
        APPayment payment = new APPayment();
        payment.setInvoiceId(id);
        payment.setPaymentDate(LocalDate.now());
        payment.setAmount(invoice.getAmount());
        payment.setPaymentMethod("System");
        payment.setReference("Payment-" + id);
        apPaymentRepository.save(payment);
    }

    public List<APPayment> getPaymentHistory() {
        List<APPayment> payments = new ArrayList<>();
        apPaymentRepository.findAll().forEach(payment -> {
            // Set invoice and vendor details
            apInvoiceRepository.findById(payment.getInvoiceId()).ifPresent(invoice -> {
                payment.setInvoiceNumber(invoice.getInvoiceNumber());
                vendorRepository.findById(invoice.getVendorId()).ifPresent(vendor -> {
                    payment.setVendorName(vendor.getName());
                });
            });
            payments.add(payment);
        });
        return payments;
    }

    // Vendor methods
    public List<Vendor> getAllVendors() {
        List<Vendor> vendors = new ArrayList<>();
        vendorRepository.findAll().forEach(vendor -> {
            // Calculate vendor balance from their account
            if (vendor.getAccountId() != null) {
                accountRepository.findById(vendor.getAccountId()).ifPresent(account -> {
                    vendor.setBalance(account.getBalance());
                });
            }
            vendors.add(vendor);
        });
        return vendors;
    }

    public Vendor getVendorById(Long id) {
        return vendorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Vendor not found"));
    }

    @Transactional
    public Vendor createVendor(Vendor vendor) {
        // Create vendor account in Chart of Accounts
        Account vendorAccount = new Account();
        vendorAccount.setCode("2100-" + System.currentTimeMillis()); // Unique code
        vendorAccount.setName("AP - " + vendor.getName());
        vendorAccount.setType(AccountType.LIABILITY);
        vendorAccount.setSubType("Accounts Payable");
        vendorAccount.setBalance(BigDecimal.ZERO);
        vendorAccount.setStatus("Active");
        
        // Find or create parent AP account
        Account parentAP = findOrCreateAccount("2100", "Accounts Payable - Control", AccountType.LIABILITY);
        vendorAccount.setParentId(parentAP.getId());
        
        Account savedAccount = accountRepository.save(vendorAccount);
        vendor.setAccountId(savedAccount.getId());
        
        return vendorRepository.save(vendor);
    }

    @Transactional
    public Vendor updateVendor(Long id, Vendor updatedVendor) {
        Vendor existing = vendorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Vendor not found"));
        
        existing.setName(updatedVendor.getName());
        existing.setContactPerson(updatedVendor.getContactPerson());
        existing.setEmail(updatedVendor.getEmail());
        existing.setPhone(updatedVendor.getPhone());
        existing.setAddress(updatedVendor.getAddress());
        existing.setStatus(updatedVendor.getStatus());
        
        return vendorRepository.save(existing);
    }

    @Transactional
    public void deleteVendor(Long id) {
        vendorRepository.deleteById(id);
    }
}
