package com.erp.finance.web;

import com.erp.finance.domain.Account;
import com.erp.finance.repository.AccountRepository;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {
    private final AccountRepository accounts;

    public AccountController(AccountRepository accounts) {
        this.accounts = accounts;
    }

    @GetMapping
    public Iterable<Account> list() {
        return accounts.findAll();
    }

    @PostMapping
    public ResponseEntity<Account> create(@Valid @RequestBody Account account) {
        if (accounts.existsByCode(account.getCode())) {
            return ResponseEntity.badRequest().build();
        }
        Account saved = accounts.save(account);
        return ResponseEntity.created(URI.create("/api/accounts/" + saved.getId())).body(saved);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Account> getById(@PathVariable("id") Long id) {
        return accounts.findById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Account> update(@PathVariable("id") Long id, @Valid @RequestBody Account account) {
        if (!accounts.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        account.setId(id);
        Account saved = accounts.save(account);
        return ResponseEntity.ok(saved);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") Long id) {
        if (!accounts.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        accounts.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
