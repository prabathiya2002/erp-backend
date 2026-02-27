package com.erp.finance.repo;

import com.erp.finance.domain.ARInvoiceItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ARInvoiceItemRepository extends JpaRepository<ARInvoiceItem, Long> {
    List<ARInvoiceItem> findByInvoiceId(Long invoiceId);
    void deleteByInvoiceId(Long invoiceId);
}
