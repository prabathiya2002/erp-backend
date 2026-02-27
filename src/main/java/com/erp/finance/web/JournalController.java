package com.erp.finance.web;

import com.erp.finance.domain.JournalEntry;
import com.erp.finance.domain.JournalLine;
import com.erp.finance.domain.JournalStatus;
import com.erp.finance.repository.JournalRepository;
import com.erp.finance.service.PostingService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.net.URI;

@RestController
@RequestMapping("/api/journals")
@Transactional
public class JournalController {
    private final JournalRepository journals;
    private final PostingService posting;

    public JournalController(JournalRepository journals, PostingService posting) {
        this.journals = journals;
        this.posting = posting;
    }

    @GetMapping
    public Iterable<JournalEntry> list() {
        return journals.findAll();
    }

    @PostMapping
    public ResponseEntity<JournalEntry> create(@Valid @RequestBody JournalEntry entry) {
        // lines cascade via JPA; ensure bidirectional linkage
        if (entry.getLines() != null) {
            for (JournalLine line : entry.getLines()) {
                line.setJournal(entry);
            }
        }
        JournalEntry saved = journals.save(entry);
        return ResponseEntity.created(URI.create("/api/journals/" + saved.getId())).body(saved);
    }

    @PostMapping("/{id}/approve")
    public ResponseEntity<?> approve(@PathVariable("id") Long id) {
        try {
            System.out.println("=== APPROVE CALLED FOR JOURNAL ID: " + id + " ===");
            
            java.util.Optional<JournalEntry> opt = journals.findById(id);
            if (opt.isEmpty()) {
                System.out.println("Journal not found: " + id);
                return ResponseEntity.notFound().build();
            }
            
            JournalEntry je = opt.get();
            System.out.println("Journal loaded: " + je.getId() + ", Status: " + je.getStatus());
            System.out.println("Journal has " + je.getLines().size() + " lines");
            
            je.setStatus(JournalStatus.APPROVAL);
            System.out.println("Status changed to APPROVAL");
            
            JournalEntry saved = journals.save(je);
            System.out.println("Journal saved successfully");
            
            saved.getLines().size(); // force load
            System.out.println("Lines loaded: " + saved.getLines().size());
            
            System.out.println("Returning response...");
            return ResponseEntity.ok(saved);
        } catch (Exception e) {
            System.err.println("========================================");
            System.err.println("ERROR IN APPROVE ENDPOINT:");
            System.err.println("Exception: " + e.getClass().getName());
            System.err.println("Message: " + e.getMessage());
            System.err.println("========================================");
            e.printStackTrace();
            return ResponseEntity.status(500).body(java.util.Map.of("error", e.getMessage() != null ? e.getMessage() : "Unknown error"));
        }
    }

    @PostMapping("/{id}/post")
    public ResponseEntity<JournalEntry> post(@PathVariable("id") Long id) {
        java.util.Optional<JournalEntry> opt = journals.findById(id);
        if (opt.isEmpty()) return ResponseEntity.notFound().build();
        JournalEntry je = opt.get();

        BigDecimal totalDebit = BigDecimal.ZERO;
        BigDecimal totalCredit = BigDecimal.ZERO;
        for (JournalLine line : je.getLines()) {
            totalDebit = totalDebit.add(line.getDebit());
            totalCredit = totalCredit.add(line.getCredit());
        }
        if (totalDebit.compareTo(totalCredit) != 0) {
            return ResponseEntity.badRequest().body(je);
        }
        // apply posting to account balances
        for (JournalLine line : je.getLines()) {
            posting.applyLineToAccount(line);
        }
        je.setStatus(JournalStatus.POSTED);
        journals.save(je);
        return ResponseEntity.ok(je);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") Long id) {
        if (!journals.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        journals.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
