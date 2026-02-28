package com.erp.finance.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "company_settings")
public class CompanySettings {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String companyName;
    
    @Column(columnDefinition = "TEXT")
    private String companyLogo; // URL or base64 encoded image
    
    @Column(length = 500)
    private String address;
    
    private String city;
    private String state;
    private String zipCode;
    private String country;
    
    private String phone;
    private String fax;
    private String email;
    private String website;
    
    private String taxId; // Tax ID / VAT number
    private String registrationNumber;
    
    @Column(columnDefinition = "TEXT")
    private String invoiceFooterMessage; // "Thank you for your business!"
    
    @Column(columnDefinition = "TEXT")
    private String termsAndConditions;
    
    private String currency; // USD, EUR, etc.
    private String currencySymbol; // $, €, etc.
    
    private LocalDateTime createdDate;
    private LocalDateTime lastModified;
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getCompanyName() {
        return companyName;
    }
    
    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }
    
    public String getCompanyLogo() {
        return companyLogo;
    }
    
    public void setCompanyLogo(String companyLogo) {
        this.companyLogo = companyLogo;
    }
    
    public String getAddress() {
        return address;
    }
    
    public void setAddress(String address) {
        this.address = address;
    }
    
    public String getCity() {
        return city;
    }
    
    public void setCity(String city) {
        this.city = city;
    }
    
    public String getState() {
        return state;
    }
    
    public void setState(String state) {
        this.state = state;
    }
    
    public String getZipCode() {
        return zipCode;
    }
    
    public void setZipCode(String zipCode) {
        this.zipCode = zipCode;
    }
    
    public String getCountry() {
        return country;
    }
    
    public void setCountry(String country) {
        this.country = country;
    }
    
    public String getPhone() {
        return phone;
    }
    
    public void setPhone(String phone) {
        this.phone = phone;
    }
    
    public String getFax() {
        return fax;
    }
    
    public void setFax(String fax) {
        this.fax = fax;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getWebsite() {
        return website;
    }
    
    public void setWebsite(String website) {
        this.website = website;
    }
    
    public String getTaxId() {
        return taxId;
    }
    
    public void setTaxId(String taxId) {
        this.taxId = taxId;
    }
    
    public String getRegistrationNumber() {
        return registrationNumber;
    }
    
    public void setRegistrationNumber(String registrationNumber) {
        this.registrationNumber = registrationNumber;
    }
    
    public String getInvoiceFooterMessage() {
        return invoiceFooterMessage;
    }
    
    public void setInvoiceFooterMessage(String invoiceFooterMessage) {
        this.invoiceFooterMessage = invoiceFooterMessage;
    }
    
    public String getTermsAndConditions() {
        return termsAndConditions;
    }
    
    public void setTermsAndConditions(String termsAndConditions) {
        this.termsAndConditions = termsAndConditions;
    }
    
    public String getCurrency() {
        return currency;
    }
    
    public void setCurrency(String currency) {
        this.currency = currency;
    }
    
    public String getCurrencySymbol() {
        return currencySymbol;
    }
    
    public void setCurrencySymbol(String currencySymbol) {
        this.currencySymbol = currencySymbol;
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
