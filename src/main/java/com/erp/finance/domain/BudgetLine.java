package com.erp.finance.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "budget_lines")
public class BudgetLine {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "budget_id", nullable = false)
    private Budget budget;
    
    @Column(nullable = false)
    private Long accountId;
    
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;
    
    @Column(length = 500)
    private String notes;
    
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private LineType lineType;
    
    public enum LineType {
        REVENUE,
        EXPENSE,
        ASSET,
        LIABILITY,
        EQUITY
    }
    
    public BudgetLine() {
        this.amount = BigDecimal.ZERO;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Budget getBudget() {
        return budget;
    }
    
    public void setBudget(Budget budget) {
        this.budget = budget;
    }
    
    public Long getAccountId() {
        return accountId;
    }
    
    public void setAccountId(Long accountId) {
        this.accountId = accountId;
    }
    
    public BigDecimal getAmount() {
        return amount;
    }
    
    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
    
    public String getNotes() {
        return notes;
    }
    
    public void setNotes(String notes) {
        this.notes = notes;
    }
    
    public LineType getLineType() {
        return lineType;
    }
    
    public void setLineType(LineType lineType) {
        this.lineType = lineType;
    }
}
