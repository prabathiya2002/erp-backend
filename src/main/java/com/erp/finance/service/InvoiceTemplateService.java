package com.erp.finance.service;

import com.erp.finance.domain.InvoiceTemplate;
import com.erp.finance.repository.InvoiceTemplateRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class InvoiceTemplateService {
    
    @Autowired
    private InvoiceTemplateRepository invoiceTemplateRepository;
    
    public List<InvoiceTemplate> getAllTemplates() {
        return invoiceTemplateRepository.findAll();
    }
    
    public List<InvoiceTemplate> getActiveTemplates() {
        return invoiceTemplateRepository.findByIsActiveTrue();
    }
    
    public List<InvoiceTemplate> getTemplatesByType(InvoiceTemplate.TemplateType type) {
        return invoiceTemplateRepository.findByTemplateType(type);
    }
    
    public InvoiceTemplate getDefaultTemplate(InvoiceTemplate.TemplateType type) {
        return invoiceTemplateRepository.findByTemplateTypeAndIsDefaultTrue(type)
            .orElse(null);
    }
    
    public InvoiceTemplate getTemplateById(Long id) {
        return invoiceTemplateRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Template not found with id: " + id));
    }
    
    public InvoiceTemplate createTemplate(InvoiceTemplate template) {
        template.setCreatedDate(LocalDateTime.now());
        template.setLastModified(LocalDateTime.now());
        
        // If this is set as default, remove default from other templates of same type
        if (Boolean.TRUE.equals(template.getIsDefault())) {
            removeDefaultFromOtherTemplates(template.getTemplateType(), null);
        }
        
        return invoiceTemplateRepository.save(template);
    }
    
    public InvoiceTemplate updateTemplate(Long id, InvoiceTemplate template) {
        InvoiceTemplate existing = getTemplateById(id);
        
        template.setId(existing.getId());
        template.setCreatedDate(existing.getCreatedDate());
        template.setLastModified(LocalDateTime.now());
        
        // If this is set as default, remove default from other templates of same type
        if (Boolean.TRUE.equals(template.getIsDefault())) {
            removeDefaultFromOtherTemplates(template.getTemplateType(), id);
        }
        
        return invoiceTemplateRepository.save(template);
    }
    
    public void deleteTemplate(Long id) {
        invoiceTemplateRepository.deleteById(id);
    }
    
    private void removeDefaultFromOtherTemplates(InvoiceTemplate.TemplateType type, Long excludeId) {
        List<InvoiceTemplate> templates = invoiceTemplateRepository.findByTemplateType(type);
        for (InvoiceTemplate t : templates) {
            if (!t.getId().equals(excludeId) && Boolean.TRUE.equals(t.getIsDefault())) {
                t.setIsDefault(false);
                t.setLastModified(LocalDateTime.now());
                invoiceTemplateRepository.save(t);
            }
        }
    }
    
    public void initializeDefaultTemplates() {
        // Check if templates already exist
        List<InvoiceTemplate> existing = invoiceTemplateRepository.findAll();
        if (!existing.isEmpty()) {
            return;
        }
        
        // Create default product template
        InvoiceTemplate productTemplate = new InvoiceTemplate();
        productTemplate.setTemplateName("Default Product Invoice");
        productTemplate.setTemplateType(InvoiceTemplate.TemplateType.PRODUCT);
        productTemplate.setIsDefault(true);
        productTemplate.setIsActive(true);
        productTemplate.setCustomFooterMessage("Thank you for your purchase! Come again soon!");
        productTemplate.setCreatedDate(LocalDateTime.now());
        productTemplate.setLastModified(LocalDateTime.now());
        invoiceTemplateRepository.save(productTemplate);
        
        // Create default service template
        InvoiceTemplate serviceTemplate = new InvoiceTemplate();
        serviceTemplate.setTemplateName("Default Service Invoice");
        serviceTemplate.setTemplateType(InvoiceTemplate.TemplateType.SERVICE);
        serviceTemplate.setIsDefault(true);
        serviceTemplate.setIsActive(true);
        serviceTemplate.setCustomFooterMessage("Thank you for choosing our services! We look forward to serving you again.");
        serviceTemplate.setCreatedDate(LocalDateTime.now());
        serviceTemplate.setLastModified(LocalDateTime.now());
        invoiceTemplateRepository.save(serviceTemplate);
    }
}
