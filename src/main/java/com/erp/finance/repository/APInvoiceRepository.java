package com.erp.finance.repository;

import com.erp.finance.domain.APInvoice;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface APInvoiceRepository extends CrudRepository<APInvoice, Long> {
    List<APInvoice> findByVendorId(Long vendorId);
    boolean existsByInvoiceNumber(String invoiceNumber);
}
