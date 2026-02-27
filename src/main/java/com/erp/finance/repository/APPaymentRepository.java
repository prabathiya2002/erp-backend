package com.erp.finance.repository;

import com.erp.finance.domain.APPayment;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface APPaymentRepository extends CrudRepository<APPayment, Long> {
    List<APPayment> findByInvoiceId(Long invoiceId);
}
