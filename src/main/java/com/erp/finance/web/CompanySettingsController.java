package com.erp.finance.web;

import com.erp.finance.domain.CompanySettings;
import com.erp.finance.service.CompanySettingsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/company-settings")
@CrossOrigin(origins = "http://localhost:4200", allowCredentials = "true")
public class CompanySettingsController {
    
    @Autowired
    private CompanySettingsService companySettingsService;
    
    @GetMapping
    public ResponseEntity<CompanySettings> getCompanySettings() {
        CompanySettings settings = companySettingsService.getCompanySettings();
        return ResponseEntity.ok(settings);
    }
    
    @PutMapping
    public ResponseEntity<CompanySettings> updateCompanySettings(@RequestBody CompanySettings settings) {
        try {
            CompanySettings updated = companySettingsService.saveCompanySettings(settings);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
