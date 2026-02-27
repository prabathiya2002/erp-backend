package com.erp.finance.repository;

import com.erp.finance.domain.JournalEntry;
import com.erp.finance.domain.JournalStatus;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface JournalRepository extends CrudRepository<JournalEntry, Long> {
	List<JournalEntry> findByPeriodAndStatus(String period, JournalStatus status);
	List<JournalEntry> findByDateBetweenAndStatus(LocalDate startDate, LocalDate endDate, JournalStatus status);
}
