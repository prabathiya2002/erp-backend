package com.erp.finance.service;

import com.erp.finance.domain.CompanySettings;
import com.erp.finance.repository.CompanySettingsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class CompanySettingsService {
    
    @Autowired
    private CompanySettingsRepository companySettingsRepository;
    
    public CompanySettings getCompanySettings() {
        List<CompanySettings> settings = companySettingsRepository.findAll();
        if (settings.isEmpty()) {
            // Create default settings if none exist
            return createDefaultSettings();
        }
        return settings.get(0); // Return the first (and should be only) record
    }
    
    public CompanySettings saveCompanySettings(CompanySettings settings) {
        List<CompanySettings> existing = companySettingsRepository.findAll();
        
        if (!existing.isEmpty()) {
            // Update existing settings
            CompanySettings existingSettings = existing.get(0);
            settings.setId(existingSettings.getId());
            settings.setCreatedDate(existingSettings.getCreatedDate());
        } else {
            settings.setCreatedDate(LocalDateTime.now());
        }
        
        settings.setLastModified(LocalDateTime.now());
        return companySettingsRepository.save(settings);
    }
    
    private CompanySettings createDefaultSettings() {
        CompanySettings settings = new CompanySettings();
        settings.setCompanyName("Your Company Name");
        settings.setAddress("123 Business Street");
        settings.setCity("City");
        settings.setState("State");
        settings.setZipCode("12345");
        settings.setCountry("Country");
        settings.setPhone("+1 (555) 123-4567");
        settings.setEmail("info@yourcompany.com");
        settings.setWebsite("www.yourcompany.com");
        settings.setCurrency("USD");
        settings.setCurrencySymbol("$");
        settings.setInvoiceFooterMessage("Thank you for your business! We appreciate your trust.");
        settings.setCreatedDate(LocalDateTime.now());
        settings.setLastModified(LocalDateTime.now());
        
        return companySettingsRepository.save(settings);
    }
}
