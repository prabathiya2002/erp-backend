package com.erp.finance.service;

import com.erp.finance.domain.*;
import com.erp.finance.repository.*;
import com.erp.finance.repo.ARInvoiceItemRepository;
import com.erp.finance.web.ARInvoiceDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class ARService {
    private final ARInvoiceRepository arInvoiceRepository;
    private final ARPaymentRepository arPaymentRepository;
    private final CustomerRepository customerRepository;
    private final JournalRepository journalRepository;
    private final AccountRepository accountRepository;
    private final ARInvoiceItemRepository arInvoiceItemRepository;
    private final PostingService postingService;

    public ARService(ARInvoiceRepository arInvoiceRepository,
                     ARPaymentRepository arPaymentRepository,
                     CustomerRepository customerRepository,
                     JournalRepository journalRepository,
                     AccountRepository accountRepository,
                     ARInvoiceItemRepository arInvoiceItemRepository,
                     PostingService postingService) {
        this.arInvoiceRepository = arInvoiceRepository;
        this.arPaymentRepository = arPaymentRepository;
        this.customerRepository = customerRepository;
        this.journalRepository = journalRepository;
        this.accountRepository = accountRepository;
        this.arInvoiceItemRepository = arInvoiceItemRepository;
        this.postingService = postingService;
    }

    public List<ARInvoice> getAllInvoices() {
        List<ARInvoice> invoices = new ArrayList<>();
        arInvoiceRepository.findAll().forEach(invoice -> {
            // Set customer name
            customerRepository.findById(invoice.getCustomerId()).ifPresent(customer -> {
                invoice.setCustomerName(customer.getName());
            });
            invoices.add(invoice);
        });
        return invoices;
    }

    public ARInvoice getInvoiceById(Long id) {
        ARInvoice invoice = arInvoiceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Invoice not found"));
        customerRepository.findById(invoice.getCustomerId()).ifPresent(customer -> {
            invoice.setCustomerName(customer.getName());
        });
        return invoice;
    }

    @Transactional
    public ARInvoice createInvoice(ARInvoice invoice) {
        if (arInvoiceRepository.existsByInvoiceNumber(invoice.getInvoiceNumber())) {
            throw new RuntimeException("Invoice number already exists");
        }
        // Set initial balance equal to amount
        if (invoice.getBalance() == null) {
            invoice.setBalance(invoice.getAmount());
        }
        
        // Save invoice first
        ARInvoice saved = arInvoiceRepository.save(invoice);
        
        // Create journal entry for AR invoice
        // Debit: Accounts Receivable
        // Credit: Revenue
        createARInvoiceJournalEntry(saved);
        
        return saved;
    }
    
    private void createARInvoiceJournalEntry(ARInvoice invoice) {
        // Get customer-specific AR account
        Customer customer = customerRepository.findById(invoice.getCustomerId())
                .orElseThrow(() -> new RuntimeException("Customer not found"));
        
        Account customerAccount;
        if (customer.getAccountId() != null) {
            customerAccount = accountRepository.findById(customer.getAccountId())
                    .orElseThrow(() -> new RuntimeException("Customer account not found"));
        } else {
            // Fallback to control account
            customerAccount = findOrCreateAccount("1200", "Accounts Receivable - Control", AccountType.ASSET);
        }
        
        Account revenueAccount = findOrCreateAccount("4000", "Sales Revenue", AccountType.REVENUE);
        
        // Create journal entry
        JournalEntry journal = new JournalEntry();
        journal.setDate(invoice.getInvoiceDate());
        journal.setPeriod(invoice.getInvoiceDate().format(DateTimeFormatter.ofPattern("yyyy-MM")));
        journal.setStatus(JournalStatus.POSTED);
        
        // Debit customer-specific AR (increase asset)
        JournalLine arLine = new JournalLine();
        arLine.setJournal(journal);
        arLine.setAccountId(customerAccount.getId());
        arLine.setDebit(invoice.getAmount());
        arLine.setCredit(BigDecimal.ZERO);
        arLine.setDescription("AR Invoice: " + invoice.getInvoiceNumber() + " - " + customer.getName());
        
        // Credit Revenue (increase revenue)
        JournalLine revenueLine = new JournalLine();
        revenueLine.setJournal(journal);
        revenueLine.setAccountId(revenueAccount.getId());
        revenueLine.setDebit(BigDecimal.ZERO);
        revenueLine.setCredit(invoice.getAmount());
        revenueLine.setDescription("AR Invoice: " + invoice.getInvoiceNumber() + " - " + customer.getName());
        
        journal.getLines().add(arLine);
        journal.getLines().add(revenueLine);
        
        journalRepository.save(journal);
        
        // Post to GL
        postingService.applyLineToAccount(arLine);
        postingService.applyLineToAccount(revenueLine);
    }

    @Transactional
    public ARInvoice updateInvoice(Long id, ARInvoice updatedInvoice) {
        ARInvoice existing = arInvoiceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Invoice not found"));
        
        existing.setCustomerId(updatedInvoice.getCustomerId());
        existing.setInvoiceNumber(updatedInvoice.getInvoiceNumber());
        existing.setInvoiceDate(updatedInvoice.getInvoiceDate());
        existing.setDueDate(updatedInvoice.getDueDate());
        existing.setAmount(updatedInvoice.getAmount());
        existing.setBalance(updatedInvoice.getBalance());
        existing.setDescription(updatedInvoice.getDescription());
        existing.setStatus(updatedInvoice.getStatus());
        
        return arInvoiceRepository.save(existing);
    }

    @Transactional
    public void deleteInvoice(Long id) {
        arInvoiceItemRepository.deleteByInvoiceId(id);
        arInvoiceRepository.deleteById(id);
    }

    // Invoice with items methods
    public ARInvoiceDTO getInvoiceWithItems(Long id) {
        ARInvoice invoice = getInvoiceById(id);
        List<ARInvoiceItem> items = arInvoiceItemRepository.findByInvoiceId(id);
        return new ARInvoiceDTO(invoice, items);
    }

    @Transactional
    public ARInvoiceDTO createInvoiceWithItems(ARInvoiceDTO dto) {
        ARInvoice invoice = dto.getInvoice();
        List<ARInvoiceItem> items = dto.getItems();

        // Calculate totals from items
        BigDecimal subtotal = BigDecimal.ZERO;
        if (items != null && !items.isEmpty()) {
            for (ARInvoiceItem item : items) {
                item.setTotal(item.getQuantity().multiply(item.getUnitPrice()));
                subtotal = subtotal.add(item.getTotal());
            }
        }

        invoice.setSubtotal(subtotal);
        
        // Calculate tax and discount if not set
        if (invoice.getTaxAmount() == null) {
            invoice.setTaxAmount(BigDecimal.ZERO);
        }
        if (invoice.getDiscountAmount() == null) {
            invoice.setDiscountAmount(BigDecimal.ZERO);
        }

        // Calculate final amount
        BigDecimal finalAmount = subtotal
                .add(invoice.getTaxAmount())
                .subtract(invoice.getDiscountAmount());
        invoice.setAmount(finalAmount);
        invoice.setBalance(finalAmount);

        // Save invoice
        ARInvoice savedInvoice = createInvoice(invoice);

        // Save items
        List<ARInvoiceItem> savedItems = new ArrayList<>();
        if (items != null) {
            for (ARInvoiceItem item : items) {
                item.setInvoiceId(savedInvoice.getId());
                savedItems.add(arInvoiceItemRepository.save(item));
            }
        }

        return new ARInvoiceDTO(savedInvoice, savedItems);
    }

    @Transactional
    public ARInvoiceDTO updateInvoiceWithItems(Long id, ARInvoiceDTO dto) {
        // Delete existing items
        arInvoiceItemRepository.deleteByInvoiceId(id);

        ARInvoice invoice = dto.getInvoice();
        List<ARInvoiceItem> items = dto.getItems();

        // Calculate totals from items
        BigDecimal subtotal = BigDecimal.ZERO;
        if (items != null && !items.isEmpty()) {
            for (ARInvoiceItem item : items) {
                item.setTotal(item.getQuantity().multiply(item.getUnitPrice()));
                subtotal = subtotal.add(item.getTotal());
            }
        }

        invoice.setSubtotal(subtotal);
        
        // Calculate final amount
        BigDecimal finalAmount = subtotal
                .add(invoice.getTaxAmount() != null ? invoice.getTaxAmount() : BigDecimal.ZERO)
                .subtract(invoice.getDiscountAmount() != null ? invoice.getDiscountAmount() : BigDecimal.ZERO);
        invoice.setAmount(finalAmount);

        // Update invoice
        ARInvoice updatedInvoice = updateInvoice(id, invoice);

        // Save new items
        List<ARInvoiceItem> savedItems = new ArrayList<>();
        if (items != null) {
            for (ARInvoiceItem item : items) {
                item.setInvoiceId(updatedInvoice.getId());
                savedItems.add(arInvoiceItemRepository.save(item));
            }
        }

        return new ARInvoiceDTO(updatedInvoice, savedItems);
    }

    @Transactional
    public void recordPayment(ARPayment payment) {
        ARInvoice invoice = arInvoiceRepository.findById(payment.getInvoiceId())
                .orElseThrow(() -> new RuntimeException("Invoice not found"));
        
        // Update invoice balance
        BigDecimal newBalance = invoice.getBalance().subtract(payment.getAmount());
        invoice.setBalance(newBalance);
        
        // Update invoice status
        if (newBalance.compareTo(BigDecimal.ZERO) == 0) {
            invoice.setStatus(ARInvoice.InvoiceStatus.PAID);
        } else if (newBalance.compareTo(invoice.getAmount()) < 0) {
            invoice.setStatus(ARInvoice.InvoiceStatus.PARTIALLY_PAID);
        }
        
        arInvoiceRepository.save(invoice);
        
        // Get customer-specific AR account
        Customer customer = customerRepository.findById(invoice.getCustomerId())
                .orElseThrow(() -> new RuntimeException("Customer not found"));
        
        Account customerAccount;
        if (customer.getAccountId() != null) {
            customerAccount = accountRepository.findById(customer.getAccountId())
                    .orElseThrow(() -> new RuntimeException("Customer account not found"));
        } else {
            customerAccount = findOrCreateAccount("1200", "Accounts Receivable - Control", AccountType.ASSET);
        }
        
        Account cashAccount = findOrCreateAccount("1000", "Cash and Bank", AccountType.ASSET);
        
        // Create journal entry
        JournalEntry journal = new JournalEntry();
        journal.setDate(payment.getPaymentDate());
        journal.setPeriod(payment.getPaymentDate().format(DateTimeFormatter.ofPattern("yyyy-MM")));
        journal.setStatus(JournalStatus.POSTED);
        
        // Debit Cash (increase asset)
        JournalLine cashLine = new JournalLine();
        cashLine.setJournal(journal);
        cashLine.setAccountId(cashAccount.getId());
        cashLine.setDebit(payment.getAmount());
        cashLine.setCredit(BigDecimal.ZERO);
        cashLine.setDescription("AR Payment: " + invoice.getInvoiceNumber() + " - " + customer.getName());
        
        // Credit customer-specific AR (reduce asset)
        JournalLine arLine = new JournalLine();
        arLine.setJournal(journal);
        arLine.setAccountId(customerAccount.getId());
        arLine.setDebit(BigDecimal.ZERO);
        arLine.setCredit(payment.getAmount());
        arLine.setDescription("AR Payment: " + invoice.getInvoiceNumber() + " - " + customer.getName());
        
        journal.getLines().add(cashLine);
        journal.getLines().add(arLine);
        
        journalRepository.save(journal);
        
        // Post to GL
        postingService.applyLineToAccount(cashLine);
        postingService.applyLineToAccount(arLine);
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

    public List<ARPayment> getPaymentHistory() {
        List<ARPayment> payments = new ArrayList<>();
        arPaymentRepository.findAll().forEach(payment -> {
            // Set invoice and customer details
            arInvoiceRepository.findById(payment.getInvoiceId()).ifPresent(invoice -> {
                payment.setInvoiceNumber(invoice.getInvoiceNumber());
                customerRepository.findById(invoice.getCustomerId()).ifPresent(customer -> {
                    payment.setCustomerName(customer.getName());
                });
            });
            payments.add(payment);
        });
        return payments;
    }

    // Customer methods
    public List<Customer> getAllCustomers() {
        List<Customer> customers = new ArrayList<>();
        customerRepository.findAll().forEach(customer -> {
            // Calculate customer balance from their account
            if (customer.getAccountId() != null) {
                accountRepository.findById(customer.getAccountId()).ifPresent(account -> {
                    customer.setBalance(account.getBalance());
                });
            }
            customers.add(customer);
        });
        return customers;
    }

    public Customer getCustomerById(Long id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Customer not found"));
    }

    @Transactional
    public Customer createCustomer(Customer customer) {
        // Create customer account in Chart of Accounts
        Account customerAccount = new Account();
        customerAccount.setCode("1200-" + System.currentTimeMillis()); // Unique code
        customerAccount.setName("AR - " + customer.getName());
        customerAccount.setType(AccountType.ASSET);
        customerAccount.setSubType("Accounts Receivable");
        customerAccount.setBalance(BigDecimal.ZERO);
        customerAccount.setStatus("Active");
        
        // Find or create parent AR account
        Account parentAR = findOrCreateAccount("1200", "Accounts Receivable - Control", AccountType.ASSET);
        customerAccount.setParentId(parentAR.getId());
        
        Account savedAccount = accountRepository.save(customerAccount);
        customer.setAccountId(savedAccount.getId());
        
        return customerRepository.save(customer);
    }

    @Transactional
    public Customer updateCustomer(Long id, Customer updatedCustomer) {
        Customer existing = customerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Customer not found"));
        
        existing.setName(updatedCustomer.getName());
        existing.setContactPerson(updatedCustomer.getContactPerson());
        existing.setEmail(updatedCustomer.getEmail());
        existing.setPhone(updatedCustomer.getPhone());
        existing.setAddress(updatedCustomer.getAddress());
        existing.setStatus(updatedCustomer.getStatus());
        
        return customerRepository.save(existing);
    }

    @Transactional
    public void deleteCustomer(Long id) {
        customerRepository.deleteById(id);
    }
}
