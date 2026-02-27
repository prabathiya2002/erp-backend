package com.erp.finance.web;

import com.erp.finance.domain.ARInvoice;
import com.erp.finance.domain.ARPayment;
import com.erp.finance.domain.Customer;
import com.erp.finance.service.ARService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/ar")
public class ARController {
    private final ARService arService;

    public ARController(ARService arService) {
        this.arService = arService;
    }

    // Invoice endpoints
    @GetMapping("/invoices")
    public List<ARInvoice> getAllInvoices() {
        return arService.getAllInvoices();
    }

    @GetMapping("/invoices/{id}")
    public ResponseEntity<ARInvoice> getInvoiceById(@PathVariable("id") Long id) {
        try {
            return ResponseEntity.ok(arService.getInvoiceById(id));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/invoices")
    public ResponseEntity<ARInvoice> createInvoice(@RequestBody ARInvoice invoice) {
        try {
            ARInvoice created = arService.createInvoice(invoice);
            return ResponseEntity.created(URI.create("/api/ar/invoices/" + created.getId())).body(created);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/invoices/{id}")
    public ResponseEntity<ARInvoice> updateInvoice(@PathVariable("id") Long id, @RequestBody ARInvoice invoice) {
        try {
            ARInvoice updated = arService.updateInvoice(id, invoice);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/invoices/{id}")
    public ResponseEntity<Void> deleteInvoice(@PathVariable("id") Long id) {
        arService.deleteInvoice(id);
        return ResponseEntity.noContent().build();
    }

    // Invoice with items endpoints
    @GetMapping("/invoices/{id}/with-items")
    public ResponseEntity<ARInvoiceDTO> getInvoiceWithItems(@PathVariable("id") Long id) {
        try {
            return ResponseEntity.ok(arService.getInvoiceWithItems(id));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/invoices/with-items")
    public ResponseEntity<ARInvoiceDTO> createInvoiceWithItems(@RequestBody ARInvoiceDTO invoiceDTO) {
        try {
            ARInvoiceDTO created = arService.createInvoiceWithItems(invoiceDTO);
            return ResponseEntity.created(URI.create("/api/ar/invoices/" + created.getInvoice().getId())).body(created);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/invoices/{id}/with-items")
    public ResponseEntity<ARInvoiceDTO> updateInvoiceWithItems(@PathVariable("id") Long id, @RequestBody ARInvoiceDTO invoiceDTO) {
        try {
            ARInvoiceDTO updated = arService.updateInvoiceWithItems(id, invoiceDTO);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Payment endpoints
    @PostMapping("/payments")
    public ResponseEntity<Void> recordPayment(@RequestBody ARPayment payment) {
        try {
            arService.recordPayment(payment);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/payments")
    public List<ARPayment> getPaymentHistory() {
        return arService.getPaymentHistory();
    }

    // Customer endpoints
    @GetMapping("/customers")
    public List<Customer> getAllCustomers() {
        return arService.getAllCustomers();
    }

    @GetMapping("/customers/{id}")
    public ResponseEntity<Customer> getCustomerById(@PathVariable("id") Long id) {
        try {
            return ResponseEntity.ok(arService.getCustomerById(id));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/customers")
    public ResponseEntity<Customer> createCustomer(@RequestBody Customer customer) {
        Customer created = arService.createCustomer(customer);
        return ResponseEntity.created(URI.create("/api/ar/customers/" + created.getId())).body(created);
    }

    @PutMapping("/customers/{id}")
    public ResponseEntity<Customer> updateCustomer(@PathVariable("id") Long id, @RequestBody Customer customer) {
        try {
            Customer updated = arService.updateCustomer(id, customer);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/customers/{id}")
    public ResponseEntity<Void> deleteCustomer(@PathVariable("id") Long id) {
        arService.deleteCustomer(id);
        return ResponseEntity.noContent().build();
    }
}
