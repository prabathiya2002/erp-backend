package com.erp.finance.domain;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "budgets")
public class Budget {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String name;
    
    @Column(length = 1000)
    private String description;
    
    @Column(nullable = false)
    private Integer fiscalYear;
    
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private BudgetPeriod period;
    
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private BudgetType type;
    
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private BudgetStatus status;
    
    @Column(nullable = false)
    private LocalDate startDate;
    
    @Column(nullable = false)
    private LocalDate endDate;
    
    private LocalDate createdDate;
    
    private LocalDate approvedDate;
    
    private String approvedBy;
    
    private String notes;
    
    @OneToMany(mappedBy = "budget", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BudgetLine> budgetLines = new ArrayList<>();
    
    public enum BudgetPeriod {
        MONTHLY,
        QUARTERLY,
        ANNUALLY
    }
    
    public enum BudgetType {
        OPERATING,
        CAPITAL,
        CASH_FLOW,
        MASTER
    }
    
    public enum BudgetStatus {
        DRAFT,
        SUBMITTED,
        APPROVED,
        REJECTED,
        ACTIVE,
        CLOSED
    }
    
    public Budget() {
        this.createdDate = LocalDate.now();
        this.status = BudgetStatus.DRAFT;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public Integer getFiscalYear() {
        return fiscalYear;
    }
    
    public void setFiscalYear(Integer fiscalYear) {
        this.fiscalYear = fiscalYear;
    }
    
    public BudgetPeriod getPeriod() {
        return period;
    }
    
    public void setPeriod(BudgetPeriod period) {
        this.period = period;
    }
    
    public BudgetType getType() {
        return type;
    }
    
    public void setType(BudgetType type) {
        this.type = type;
    }
    
    public BudgetStatus getStatus() {
        return status;
    }
    
    public void setStatus(BudgetStatus status) {
        this.status = status;
    }
    
    public LocalDate getStartDate() {
        return startDate;
    }
    
    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }
    
    public LocalDate getEndDate() {
        return endDate;
    }
    
    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }
    
    public LocalDate getCreatedDate() {
        return createdDate;
    }
    
    public void setCreatedDate(LocalDate createdDate) {
        this.createdDate = createdDate;
    }
    
    public LocalDate getApprovedDate() {
        return approvedDate;
    }
    
    public void setApprovedDate(LocalDate approvedDate) {
        this.approvedDate = approvedDate;
    }
    
    public String getApprovedBy() {
        return approvedBy;
    }
    
    public void setApprovedBy(String approvedBy) {
        this.approvedBy = approvedBy;
    }
    
    public String getNotes() {
        return notes;
    }
    
    public void setNotes(String notes) {
        this.notes = notes;
    }
    
    public List<BudgetLine> getBudgetLines() {
        return budgetLines;
    }
    
    public void setBudgetLines(List<BudgetLine> budgetLines) {
        this.budgetLines = budgetLines;
    }
    
    public void addBudgetLine(BudgetLine budgetLine) {
        budgetLines.add(budgetLine);
        budgetLine.setBudget(this);
    }
    
    public void removeBudgetLine(BudgetLine budgetLine) {
        budgetLines.remove(budgetLine);
        budgetLine.setBudget(null);
    }
}
