package com.erp.finance.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "fixed_assets")
public class FixedAsset {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String assetName;
    
    @Column(nullable = false)
    private String assetCode;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AssetCategory category;
    
    @Column(nullable = false)
    private LocalDate purchaseDate;
    
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal purchaseCost;
    
    @Column(precision = 15, scale = 2)
    private BigDecimal salvageValue = BigDecimal.ZERO;
    
    @Column(nullable = false)
    private Integer usefulLifeYears;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DepreciationMethod depreciationMethod;
    
    @Column(precision = 15, scale = 2)
    private BigDecimal accumulatedDepreciation = BigDecimal.ZERO;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AssetStatus status = AssetStatus.ACTIVE;
    
    private LocalDate disposalDate;
    
    @Column(precision = 15, scale = 2)
    private BigDecimal disposalAmount;
    
    private String location;
    
    private String description;
    
    @Column(name = "account_id")
    private Long accountId;  // Link to GL account
    
    public enum AssetCategory {
        BUILDING,
        EQUIPMENT,
        FURNITURE,
        VEHICLE,
        COMPUTER,
        LAND,
        MACHINERY,
        OTHER
    }
    
    public enum DepreciationMethod {
        STRAIGHT_LINE,
        DECLINING_BALANCE,
        UNITS_OF_PRODUCTION
    }
    
    public enum AssetStatus {
        ACTIVE,
        DISPOSED,
        FULLY_DEPRECIATED
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAssetName() {
        return assetName;
    }

    public void setAssetName(String assetName) {
        this.assetName = assetName;
    }

    public String getAssetCode() {
        return assetCode;
    }

    public void setAssetCode(String assetCode) {
        this.assetCode = assetCode;
    }

    public AssetCategory getCategory() {
        return category;
    }

    public void setCategory(AssetCategory category) {
        this.category = category;
    }

    public LocalDate getPurchaseDate() {
        return purchaseDate;
    }

    public void setPurchaseDate(LocalDate purchaseDate) {
        this.purchaseDate = purchaseDate;
    }

    public BigDecimal getPurchaseCost() {
        return purchaseCost;
    }

    public void setPurchaseCost(BigDecimal purchaseCost) {
        this.purchaseCost = purchaseCost;
    }

    public BigDecimal getSalvageValue() {
        return salvageValue;
    }

    public void setSalvageValue(BigDecimal salvageValue) {
        this.salvageValue = salvageValue;
    }

    public Integer getUsefulLifeYears() {
        return usefulLifeYears;
    }

    public void setUsefulLifeYears(Integer usefulLifeYears) {
        this.usefulLifeYears = usefulLifeYears;
    }

    public DepreciationMethod getDepreciationMethod() {
        return depreciationMethod;
    }

    public void setDepreciationMethod(DepreciationMethod depreciationMethod) {
        this.depreciationMethod = depreciationMethod;
    }

    public BigDecimal getAccumulatedDepreciation() {
        return accumulatedDepreciation;
    }

    public void setAccumulatedDepreciation(BigDecimal accumulatedDepreciation) {
        this.accumulatedDepreciation = accumulatedDepreciation;
    }

    public AssetStatus getStatus() {
        return status;
    }

    public void setStatus(AssetStatus status) {
        this.status = status;
    }

    public LocalDate getDisposalDate() {
        return disposalDate;
    }

    public void setDisposalDate(LocalDate disposalDate) {
        this.disposalDate = disposalDate;
    }

    public BigDecimal getDisposalAmount() {
        return disposalAmount;
    }

    public void setDisposalAmount(BigDecimal disposalAmount) {
        this.disposalAmount = disposalAmount;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getAccountId() {
        return accountId;
    }

    public void setAccountId(Long accountId) {
        this.accountId = accountId;
    }
}
