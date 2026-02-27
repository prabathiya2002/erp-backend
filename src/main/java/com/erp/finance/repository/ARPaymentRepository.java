package com.erp.finance.repository;

import com.erp.finance.domain.ARPayment;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ARPaymentRepository extends CrudRepository<ARPayment, Long> {
    List<ARPayment> findByInvoiceId(Long invoiceId);
}
