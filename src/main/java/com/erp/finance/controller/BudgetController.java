package com.erp.finance.controller;

import com.erp.finance.domain.Budget;
import com.erp.finance.domain.BudgetLine;
import com.erp.finance.service.BudgetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/budgets")
@CrossOrigin(origins = "http://localhost:4200", allowCredentials = "true")
public class BudgetController {
    
    @Autowired
    private BudgetService budgetService;
    
    @GetMapping
    public ResponseEntity<List<Budget>> getAllBudgets() {
        return ResponseEntity.ok(budgetService.getAllBudgets());
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Budget> getBudgetById(@PathVariable Long id) {
        return budgetService.getBudgetById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/year/{year}")
    public ResponseEntity<List<Budget>> getBudgetsByYear(@PathVariable Integer year) {
        return ResponseEntity.ok(budgetService.getBudgetsByYear(year));
    }
    
    @GetMapping("/status/{status}")
    public ResponseEntity<List<Budget>> getBudgetsByStatus(@PathVariable Budget.BudgetStatus status) {
        return ResponseEntity.ok(budgetService.getBudgetsByStatus(status));
    }
    
    @PostMapping
    public ResponseEntity<Budget> createBudget(@RequestBody Budget budget) {
        try {
            Budget created = budgetService.createBudget(budget);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<Budget> updateBudget(@PathVariable Long id, @RequestBody Budget budget) {
        try {
            Budget updated = budgetService.updateBudget(id, budget);
            return ResponseEntity.ok(updated);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBudget(@PathVariable Long id) {
        try {
            budgetService.deleteBudget(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @PostMapping("/{id}/submit")
    public ResponseEntity<Budget> submitBudget(@PathVariable Long id) {
        try {
            Budget submitted = budgetService.submitBudget(id);
            return ResponseEntity.ok(submitted);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }
    
    @PostMapping("/{id}/approve")
    public ResponseEntity<Budget> approveBudget(@PathVariable Long id, @RequestBody Map<String, String> request) {
        try {
            String approvedBy = request.get("approvedBy");
            Budget approved = budgetService.approveBudget(id, approvedBy);
            return ResponseEntity.ok(approved);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }
    
    @PostMapping("/{id}/activate")
    public ResponseEntity<Budget> activateBudget(@PathVariable Long id) {
        try {
            Budget activated = budgetService.activateBudget(id);
            return ResponseEntity.ok(activated);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }
    
    @GetMapping("/{id}/lines")
    public ResponseEntity<List<BudgetLine>> getBudgetLines(@PathVariable Long id) {
        return ResponseEntity.ok(budgetService.getBudgetLines(id));
    }
    
    @PostMapping("/{id}/lines")
    public ResponseEntity<BudgetLine> addBudgetLine(@PathVariable Long id, @RequestBody BudgetLine budgetLine) {
        try {
            BudgetLine created = budgetService.addBudgetLine(id, budgetLine);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PutMapping("/lines/{lineId}")
    public ResponseEntity<BudgetLine> updateBudgetLine(@PathVariable Long lineId, @RequestBody BudgetLine budgetLine) {
        try {
            BudgetLine updated = budgetService.updateBudgetLine(lineId, budgetLine);
            return ResponseEntity.ok(updated);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @DeleteMapping("/lines/{lineId}")
    public ResponseEntity<Void> deleteBudgetLine(@PathVariable Long lineId) {
        try {
            budgetService.deleteBudgetLine(lineId);
            return ResponseEntity.noContent().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @GetMapping("/{id}/variance-analysis")
    public ResponseEntity<Map<String, Object>> getVarianceAnalysis(@PathVariable Long id) {
        try {
            Map<String, Object> analysis = budgetService.calculateVarianceAnalysis(id);
            return ResponseEntity.ok(analysis);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
