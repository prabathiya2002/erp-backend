package com.erp.finance.repository;

import com.erp.finance.domain.InvoiceTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InvoiceTemplateRepository extends JpaRepository<InvoiceTemplate, Long> {
    
    List<InvoiceTemplate> findByTemplateType(InvoiceTemplate.TemplateType templateType);
    
    Optional<InvoiceTemplate> findByTemplateTypeAndIsDefaultTrue(InvoiceTemplate.TemplateType templateType);
    
    List<InvoiceTemplate> findByIsActiveTrue();
}
