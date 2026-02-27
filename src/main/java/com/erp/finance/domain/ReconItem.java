package com.erp.finance.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "recon_items")
public class ReconItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate date;

    @Column(nullable = false)
    private BigDecimal amount;

    private String reference;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReconStatus status = ReconStatus.UNMATCHED;

    private Long matchedJournalId;
    private Long matchedJournalLineId;

    private BigDecimal variance = BigDecimal.ZERO;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public String getReference() { return reference; }
    public void setReference(String reference) { this.reference = reference; }
    public ReconStatus getStatus() { return status; }
    public void setStatus(ReconStatus status) { this.status = status; }
    public Long getMatchedJournalId() { return matchedJournalId; }
    public void setMatchedJournalId(Long matchedJournalId) { this.matchedJournalId = matchedJournalId; }
    public Long getMatchedJournalLineId() { return matchedJournalLineId; }
    public void setMatchedJournalLineId(Long matchedJournalLineId) { this.matchedJournalLineId = matchedJournalLineId; }
    public BigDecimal getVariance() { return variance; }
    public void setVariance(BigDecimal variance) { this.variance = variance; }
}
