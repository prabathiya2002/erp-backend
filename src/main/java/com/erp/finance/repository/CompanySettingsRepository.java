package com.erp.finance.repository;

import com.erp.finance.domain.CompanySettings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CompanySettingsRepository extends JpaRepository<CompanySettings, Long> {
    // Only one company settings record should exist
    // The first record will be the active company settings
}
