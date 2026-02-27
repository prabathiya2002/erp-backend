package com.erp.finance.controller;

import com.erp.finance.domain.FixedAsset;
import com.erp.finance.service.FixedAssetService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/fixed-assets")
public class FixedAssetController {
    
    private final FixedAssetService fixedAssetService;
    
    public FixedAssetController(FixedAssetService fixedAssetService) {
        this.fixedAssetService = fixedAssetService;
    }
    
    @GetMapping
    public List<FixedAsset> getAllAssets() {
        return fixedAssetService.getAllAssets();
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<FixedAsset> getAssetById(@PathVariable Long id) {
        return ResponseEntity.ok(fixedAssetService.getAssetById(id));
    }
    
    @PostMapping
    public ResponseEntity<FixedAsset> createAsset(@RequestBody FixedAsset asset) {
        return ResponseEntity.ok(fixedAssetService.createAsset(asset));
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<FixedAsset> updateAsset(@PathVariable Long id, @RequestBody FixedAsset asset) {
        return ResponseEntity.ok(fixedAssetService.updateAsset(id, asset));
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAsset(@PathVariable Long id) {
        fixedAssetService.deleteAsset(id);
        return ResponseEntity.ok().build();
    }
    
    @PostMapping("/{id}/depreciate")
    public ResponseEntity<Void> recordDepreciation(@PathVariable Long id, @RequestBody Map<String, String> request) {
        LocalDate date = LocalDate.parse(request.get("date"));
        fixedAssetService.recordDepreciation(id, date);
        return ResponseEntity.ok().build();
    }
    
    @PostMapping("/{id}/dispose")
    public ResponseEntity<Void> disposeAsset(@PathVariable Long id, @RequestBody Map<String, String> request) {
        LocalDate date = LocalDate.parse(request.get("date"));
        BigDecimal amount = new BigDecimal(request.get("amount"));
        fixedAssetService.disposeAsset(id, date, amount);
        return ResponseEntity.ok().build();
    }
}
