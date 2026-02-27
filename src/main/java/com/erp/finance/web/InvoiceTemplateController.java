package com.erp.finance.web;

import com.erp.finance.domain.InvoiceTemplate;
import com.erp.finance.service.InvoiceTemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/invoice-templates")
@CrossOrigin(origins = "http://localhost:4200", allowCredentials = "true")
public class InvoiceTemplateController {
    
    @Autowired
    private InvoiceTemplateService invoiceTemplateService;
    
    @GetMapping
    public ResponseEntity<List<InvoiceTemplate>> getAllTemplates() {
        List<InvoiceTemplate> templates = invoiceTemplateService.getAllTemplates();
        return ResponseEntity.ok(templates);
    }
    
    @GetMapping("/active")
    public ResponseEntity<List<InvoiceTemplate>> getActiveTemplates() {
        List<InvoiceTemplate> templates = invoiceTemplateService.getActiveTemplates();
        return ResponseEntity.ok(templates);
    }
    
    @GetMapping("/type/{type}")
    public ResponseEntity<List<InvoiceTemplate>> getTemplatesByType(@PathVariable("type") String type) {
        try {
            InvoiceTemplate.TemplateType templateType = InvoiceTemplate.TemplateType.valueOf(type.toUpperCase());
            List<InvoiceTemplate> templates = invoiceTemplateService.getTemplatesByType(templateType);
            return ResponseEntity.ok(templates);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @GetMapping("/default/{type}")
    public ResponseEntity<InvoiceTemplate> getDefaultTemplate(@PathVariable("type") String type) {
        try {
            InvoiceTemplate.TemplateType templateType = InvoiceTemplate.TemplateType.valueOf(type.toUpperCase());
            InvoiceTemplate template = invoiceTemplateService.getDefaultTemplate(templateType);
            if (template == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(template);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<InvoiceTemplate> getTemplateById(@PathVariable("id") Long id) {
        try {
            InvoiceTemplate template = invoiceTemplateService.getTemplateById(id);
            return ResponseEntity.ok(template);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @PostMapping
    public ResponseEntity<InvoiceTemplate> createTemplate(@RequestBody InvoiceTemplate template) {
        try {
            InvoiceTemplate created = invoiceTemplateService.createTemplate(template);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<InvoiceTemplate> updateTemplate(
            @PathVariable("id") Long id, 
            @RequestBody InvoiceTemplate template) {
        try {
            InvoiceTemplate updated = invoiceTemplateService.updateTemplate(id, template);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTemplate(@PathVariable("id") Long id) {
        try {
            invoiceTemplateService.deleteTemplate(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @PostMapping("/initialize")
    public ResponseEntity<Void> initializeDefaultTemplates() {
        invoiceTemplateService.initializeDefaultTemplates();
        return ResponseEntity.ok().build();
    }
}
