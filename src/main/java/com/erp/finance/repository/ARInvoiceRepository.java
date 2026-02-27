package com.erp.finance.repository;

import com.erp.finance.domain.ARInvoice;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ARInvoiceRepository extends CrudRepository<ARInvoice, Long> {
    List<ARInvoice> findByCustomerId(Long customerId);
    boolean existsByInvoiceNumber(String invoiceNumber);
}
