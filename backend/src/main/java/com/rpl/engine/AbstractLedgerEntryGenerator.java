package com.rpl.engine;

import com.rpl.domain.Account;
import com.rpl.domain.AccountKind;
import com.rpl.domain.Entry;
import com.rpl.domain.ImplementedAction;
import com.rpl.domain.LedgerTransaction;
import com.rpl.domain.ResourceAllocation;
import com.rpl.exception.NotFoundException;
import com.rpl.manager.AuditLogManager;
import com.rpl.resourceaccess.AccountRepository;
import com.rpl.resourceaccess.EntryRepository;
import com.rpl.resourceaccess.LedgerTransactionRepository;
import java.time.Instant;
import java.util.List;

public abstract class AbstractLedgerEntryGenerator {
    private final LedgerTransactionRepository transactionRepository;
    private final EntryRepository entryRepository;
    private final AccountRepository accountRepository;
    private final PostingRuleEngine postingRuleEngine;
    private final AuditLogManager auditLogManager;

    protected AbstractLedgerEntryGenerator(
            LedgerTransactionRepository transactionRepository,
            EntryRepository entryRepository,
            AccountRepository accountRepository,
            PostingRuleEngine postingRuleEngine,
            AuditLogManager auditLogManager) {
        this.transactionRepository = transactionRepository;
        this.entryRepository = entryRepository;
        this.accountRepository = accountRepository;
        this.postingRuleEngine = postingRuleEngine;
        this.auditLogManager = auditLogManager;
    }

    public final void generateEntries(ImplementedAction action) {
        List<ResourceAllocation> allocations = selectAllocations(action);
        validate(allocations);
        LedgerTransaction lastTransaction = null;
        for (ResourceAllocation allocation : allocations) {
            Account pool = accountRepository.findPoolAccounts().stream()
                    .filter(a -> a.getResourceType().getId().equals(allocation.getResourceType().getId()))
                    .findFirst()
                    .orElseThrow(() -> new NotFoundException("Pool account not found"));
            Account usage = new Account();
            usage.setName("Usage:" + allocation.getResourceType().getName());
            usage.setKind(AccountKind.USAGE);
            usage.setResourceType(allocation.getResourceType());
            usage = accountRepository.save(usage);
            LedgerTransaction transaction = createTransaction(action);
            lastTransaction = transaction;
            postEntries(
                    action,
                    buildWithdrawal(transaction, pool, allocation),
                    buildDeposit(transaction, usage, allocation));
        }
        if (lastTransaction != null) {
            afterPost(lastTransaction);
        }
    }

    protected abstract List<ResourceAllocation> selectAllocations(ImplementedAction action);

    protected abstract void validate(List<ResourceAllocation> allocations);

    protected Entry buildWithdrawal(LedgerTransaction tx, Account account, ResourceAllocation allocation) {
        Entry e = new Entry();
        e.setTransaction(tx);
        e.setAccount(account);
        e.setAmount(-allocation.getQuantity());
        e.setChargedAt(Instant.now());
        e.setBookedAt(Instant.now());
        return e;
    }

    protected Entry buildDeposit(LedgerTransaction tx, Account account, ResourceAllocation allocation) {
        Entry e = new Entry();
        e.setTransaction(tx);
        e.setAccount(account);
        e.setAmount(allocation.getQuantity());
        e.setChargedAt(Instant.now());
        e.setBookedAt(Instant.now());
        return e;
    }

    protected final LedgerTransaction createTransaction(ImplementedAction action) {
        LedgerTransaction tx = new LedgerTransaction();
        tx.setDescription("Complete action #" + action.getId());
        return transactionRepository.save(tx);
    }

    protected void afterPost(LedgerTransaction tx) {
        // Hook for subclasses / Week 2 extensions
    }

    protected final void postEntries(ImplementedAction action, Entry withdrawal, Entry deposit) {
        Entry savedWithdrawal = entryRepository.save(withdrawal);
        entryRepository.save(deposit);
        postingRuleEngine.evaluate(savedWithdrawal);
        auditLogManager.record(
                "LEDGER_ENTRIES_POSTED",
                savedWithdrawal.getAccount() != null ? savedWithdrawal.getAccount().getId() : null,
                savedWithdrawal.getId(),
                action.getProposedAction().getId());
    }
}
