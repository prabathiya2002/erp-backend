package com.erp.finance.service;

import com.erp.finance.domain.JournalEntry;
import com.erp.finance.domain.JournalLine;
import com.erp.finance.domain.JournalStatus;
import com.erp.finance.domain.ReconItem;
import com.erp.finance.repository.JournalRepository;
import com.erp.finance.repository.ReconItemRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Service
public class ReconService {
    private final ReconItemRepository reconItems;
    private final JournalRepository journals;

    public ReconService(ReconItemRepository reconItems, JournalRepository journals) {
        this.reconItems = reconItems;
        this.journals = journals;
    }

    public List<MatchSuggestion> suggestMatches(String period) {
        List<ReconItem> candidates = reconItems.findByStatus(com.erp.finance.domain.ReconStatus.UNMATCHED);
        List<JournalEntry> posted = journals.findByPeriodAndStatus(period, JournalStatus.POSTED);
        List<MatchSuggestion> suggestions = new ArrayList<>();
        for (ReconItem item : candidates) {
            for (JournalEntry je : posted) {
                for (JournalLine line : je.getLines()) {
                    BigDecimal lineAmount = line.getDebit().compareTo(BigDecimal.ZERO) != 0 ? line.getDebit() : line.getCredit();
                    if (lineAmount.compareTo(item.getAmount()) == 0) {
                        long days = Math.abs(ChronoUnit.DAYS.between(item.getDate(), je.getDate()));
                        // Simple score: closer date gets higher score
                        int score = (int) Math.max(0, 100 - days);
                        suggestions.add(new MatchSuggestion(item.getId(), je.getId(), line.getId(), score));
                    }
                }
            }
        }
        return suggestions;
    }

    public static class MatchSuggestion {
        private final Long reconItemId;
        private final Long journalId;
        private final Long journalLineId;
        private final int score;
        public MatchSuggestion(Long reconItemId, Long journalId, Long journalLineId, int score) {
            this.reconItemId = reconItemId; this.journalId = journalId; this.journalLineId = journalLineId; this.score = score;
        }
        public Long getReconItemId() { return reconItemId; }
        public Long getJournalId() { return journalId; }
        public Long getJournalLineId() { return journalLineId; }
        public int getScore() { return score; }
    }
}
