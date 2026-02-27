package com.erp.finance.web;

import com.erp.finance.domain.APInvoice;
import com.erp.finance.domain.APPayment;
import com.erp.finance.domain.Vendor;
import com.erp.finance.service.APService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/ap")
public class APController {
    private final APService apService;

    public APController(APService apService) {
        this.apService = apService;
    }

    // Invoice endpoints
    @GetMapping("/invoices")
    public List<APInvoice> getAllInvoices() {
        return apService.getAllInvoices();
    }

    @GetMapping("/invoices/{id}")
    public ResponseEntity<APInvoice> getInvoiceById(@PathVariable("id") Long id) {
        try {
            return ResponseEntity.ok(apService.getInvoiceById(id));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/invoices")
    public ResponseEntity<APInvoice> createInvoice(@RequestBody APInvoice invoice) {
        try {
            APInvoice created = apService.createInvoice(invoice);
            return ResponseEntity.created(URI.create("/api/ap/invoices/" + created.getId())).body(created);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/invoices/{id}")
    public ResponseEntity<APInvoice> updateInvoice(@PathVariable("id") Long id, @RequestBody APInvoice invoice) {
        try {
            APInvoice updated = apService.updateInvoice(id, invoice);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/invoices/{id}")
    public ResponseEntity<Void> deleteInvoice(@PathVariable("id") Long id) {
        apService.deleteInvoice(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/invoices/{id}/pay")
    public ResponseEntity<Void> payInvoice(@PathVariable("id") Long id) {
        try {
            apService.payInvoice(id);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Payment endpoints
    @GetMapping("/payments")
    public List<APPayment> getPaymentHistory() {
        return apService.getPaymentHistory();
    }

    // Vendor endpoints
    @GetMapping("/vendors")
    public List<Vendor> getAllVendors() {
        return apService.getAllVendors();
    }

    @GetMapping("/vendors/{id}")
    public ResponseEntity<Vendor> getVendorById(@PathVariable("id") Long id) {
        try {
            return ResponseEntity.ok(apService.getVendorById(id));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/vendors")
    public ResponseEntity<Vendor> createVendor(@RequestBody Vendor vendor) {
        Vendor created = apService.createVendor(vendor);
        return ResponseEntity.created(URI.create("/api/ap/vendors/" + created.getId())).body(created);
    }

    @PutMapping("/vendors/{id}")
    public ResponseEntity<Vendor> updateVendor(@PathVariable("id") Long id, @RequestBody Vendor vendor) {
        try {
            Vendor updated = apService.updateVendor(id, vendor);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/vendors/{id}")
    public ResponseEntity<Void> deleteVendor(@PathVariable("id") Long id) {
        apService.deleteVendor(id);
        return ResponseEntity.noContent().build();
    }
}
