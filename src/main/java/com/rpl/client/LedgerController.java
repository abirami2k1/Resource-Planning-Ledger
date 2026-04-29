package com.rpl.client;

import com.rpl.domain.Account;
import com.rpl.domain.Entry;
import com.rpl.manager.LedgerManager;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/accounts")
public class LedgerController {
    private final LedgerManager ledgerManager;
    public LedgerController(LedgerManager ledgerManager) { this.ledgerManager = ledgerManager; }
    @GetMapping public List<Account> accounts() { return ledgerManager.allAccounts(); }
    @GetMapping("/{id}/entries") public List<Entry> entries(@PathVariable Long id) { return ledgerManager.entriesForAccount(id); }
}
