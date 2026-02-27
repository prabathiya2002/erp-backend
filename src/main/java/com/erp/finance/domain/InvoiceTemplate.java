package com.erp.finance.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "invoice_templates")
public class InvoiceTemplate {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String templateName;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TemplateType templateType; // PRODUCT, SERVICE
    
    @Column(length = 2000)
    private String headerTemplate; // HTML template for header section
    
    @Column(length = 5000)
    private String bodyTemplate; // HTML template for items section
    
    @Column(length = 2000)
    private String footerTemplate; // HTML template for footer section
    
    private Boolean showLogo = true;
    private Boolean showCompanyAddress = true;
    private Boolean showCompanyPhone = true;
    private Boolean showCompanyEmail = true;
    private Boolean showTaxId = true;
    
    private Boolean showItemDescription = true;
    private Boolean showItemQuantity = true;
    private Boolean showItemPrice = true;
    private Boolean showItemTotal = true;
    
    private Boolean showSubtotal = true;
    private Boolean showTax = true;
    private Boolean showDiscount = false;
    private Boolean showTotal = true;
    
    @Column(length = 1000)
    private String customFooterMessage; // "Thank you, come again!"
    
    private Boolean isDefault = false;
    private Boolean isActive = true;
    
    private LocalDateTime createdDate;
    private LocalDateTime lastModified;
    
    // Enums
    public enum TemplateType {
        PRODUCT,
        SERVICE
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getTemplateName() {
        return templateName;
    }
    
    public void setTemplateName(String templateName) {
        this.templateName = templateName;
    }
    
    public TemplateType getTemplateType() {
        return templateType;
    }
    
    public void setTemplateType(TemplateType templateType) {
        this.templateType = templateType;
    }
    
    public String getHeaderTemplate() {
        return headerTemplate;
    }
    
    public void setHeaderTemplate(String headerTemplate) {
        this.headerTemplate = headerTemplate;
    }
    
    public String getBodyTemplate() {
        return bodyTemplate;
    }
    
    public void setBodyTemplate(String bodyTemplate) {
        this.bodyTemplate = bodyTemplate;
    }
    
    public String getFooterTemplate() {
        return footerTemplate;
    }
    
    public void setFooterTemplate(String footerTemplate) {
        this.footerTemplate = footerTemplate;
    }
    
    public Boolean getShowLogo() {
        return showLogo;
    }
    
    public void setShowLogo(Boolean showLogo) {
        this.showLogo = showLogo;
    }
    
    public Boolean getShowCompanyAddress() {
        return showCompanyAddress;
    }
    
    public void setShowCompanyAddress(Boolean showCompanyAddress) {
        this.showCompanyAddress = showCompanyAddress;
    }
    
    public Boolean getShowCompanyPhone() {
        return showCompanyPhone;
    }
    
    public void setShowCompanyPhone(Boolean showCompanyPhone) {
        this.showCompanyPhone = showCompanyPhone;
    }
    
    public Boolean getShowCompanyEmail() {
        return showCompanyEmail;
    }
    
    public void setShowCompanyEmail(Boolean showCompanyEmail) {
        this.showCompanyEmail = showCompanyEmail;
    }
    
    public Boolean getShowTaxId() {
        return showTaxId;
    }
    
    public void setShowTaxId(Boolean showTaxId) {
        this.showTaxId = showTaxId;
    }
    
    public Boolean getShowItemDescription() {
        return showItemDescription;
    }
    
    public void setShowItemDescription(Boolean showItemDescription) {
        this.showItemDescription = showItemDescription;
    }
    
    public Boolean getShowItemQuantity() {
        return showItemQuantity;
    }
    
    public void setShowItemQuantity(Boolean showItemQuantity) {
        this.showItemQuantity = showItemQuantity;
    }
    
    public Boolean getShowItemPrice() {
        return showItemPrice;
    }
    
    public void setShowItemPrice(Boolean showItemPrice) {
        this.showItemPrice = showItemPrice;
    }
    
    public Boolean getShowItemTotal() {
        return showItemTotal;
    }
    
    public void setShowItemTotal(Boolean showItemTotal) {
        this.showItemTotal = showItemTotal;
    }
    
    public Boolean getShowSubtotal() {
        return showSubtotal;
    }
    
    public void setShowSubtotal(Boolean showSubtotal) {
        this.showSubtotal = showSubtotal;
    }
    
    public Boolean getShowTax() {
        return showTax;
    }
    
    public void setShowTax(Boolean showTax) {
        this.showTax = showTax;
    }
    
    public Boolean getShowDiscount() {
        return showDiscount;
    }
    
    public void setShowDiscount(Boolean showDiscount) {
        this.showDiscount = showDiscount;
    }
    
    public Boolean getShowTotal() {
        return showTotal;
    }
    
    public void setShowTotal(Boolean showTotal) {
        this.showTotal = showTotal;
    }
    
    public String getCustomFooterMessage() {
        return customFooterMessage;
    }
    
    public void setCustomFooterMessage(String customFooterMessage) {
        this.customFooterMessage = customFooterMessage;
    }
    
    public Boolean getIsDefault() {
        return isDefault;
    }
    
    public void setIsDefault(Boolean isDefault) {
        this.isDefault = isDefault;
    }
    
    public Boolean getIsActive() {
        return isActive;
    }
    
    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }
    
    public LocalDateTime getCreatedDate() {
        return createdDate;
    }
    
    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }
    
    public LocalDateTime getLastModified() {
        return lastModified;
    }
    
    public void setLastModified(LocalDateTime lastModified) {
        this.lastModified = lastModified;
    }
}
