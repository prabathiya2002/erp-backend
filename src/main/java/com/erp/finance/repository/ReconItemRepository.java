package com.erp.finance.repository;

import com.erp.finance.domain.ReconItem;
import com.erp.finance.domain.ReconStatus;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReconItemRepository extends CrudRepository<ReconItem, Long> {
    List<ReconItem> findByStatus(ReconStatus status);
}
