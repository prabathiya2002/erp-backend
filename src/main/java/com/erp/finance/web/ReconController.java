package com.erp.finance.web;

import com.erp.finance.domain.ReconItem;
import com.erp.finance.domain.ReconStatus;
import com.erp.finance.repository.ReconItemRepository;
import com.erp.finance.service.ReconService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/recon")
public class ReconController {
    private final ReconItemRepository reconItems;
    private final ReconService reconService;

    public ReconController(ReconItemRepository reconItems, ReconService reconService) {
        this.reconItems = reconItems;
        this.reconService = reconService;
    }

    @GetMapping
    public Iterable<ReconItem> list() {
        return reconItems.findAll();
    }

    @PostMapping("/import")
    public ResponseEntity<List<ReconItem>> importItems(@Valid @RequestBody List<ReconItem> items) {
        items.forEach(i -> i.setStatus(ReconStatus.UNMATCHED));
        List<ReconItem> saved = (List<ReconItem>) reconItems.saveAll(items);
        return ResponseEntity.created(URI.create("/api/recon")).body(saved);
    }

    @GetMapping("/matches")
    public List<ReconService.MatchSuggestion> matches(@RequestParam("period") String period) {
        return reconService.suggestMatches(period);
    }

    @PostMapping("/{id}/resolve")
    public ResponseEntity<ReconItem> resolve(@PathVariable("id") Long id,
                                             @RequestParam("journalId") Long journalId,
                                             @RequestParam("journalLineId") Long journalLineId,
                                             @RequestParam(value = "variance", required = false) java.math.BigDecimal variance) {
        java.util.Optional<ReconItem> opt = reconItems.findById(id);
        if (opt.isEmpty()) return ResponseEntity.notFound().build();
        ReconItem item = opt.get();
        item.setStatus(ReconStatus.MATCHED);
        item.setMatchedJournalId(journalId);
        item.setMatchedJournalLineId(journalLineId);
        if (variance != null) item.setVariance(variance);
        reconItems.save(item);
        return ResponseEntity.ok(item);
    }
}
