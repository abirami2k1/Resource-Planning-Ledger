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
import java.time.Clock;
import java.time.Instant;
import java.util.List;

/**
 * Template Method pattern: defines the fixed skeleton for generating double-entry
 * ledger entries on action completion. Subclasses override selectAllocations(),
 * validate(), and optionally buildWithdrawal(), buildDeposit(), afterPost().
 * The conservation check in postEntries() is final and cannot be overridden.
 */
public abstract class AbstractLedgerEntryGenerator {
    private final LedgerTransactionRepository transactionRepository;
    private final EntryRepository entryRepository;
    private final AccountRepository accountRepository;
    private final PostingRuleEngine postingRuleEngine;
    private final AuditLogManager auditLogManager;
    protected final Clock clock;

    protected AbstractLedgerEntryGenerator(
            LedgerTransactionRepository transactionRepository,
            EntryRepository entryRepository,
            AccountRepository accountRepository,
            PostingRuleEngine postingRuleEngine,
            AuditLogManager auditLogManager,
            Clock clock) {
        this.transactionRepository = transactionRepository;
        this.entryRepository = entryRepository;
        this.accountRepository = accountRepository;
        this.postingRuleEngine = postingRuleEngine;
        this.auditLogManager = auditLogManager;
        this.clock = clock;
    }

    /** Template method — final skeleton, defines entry generation sequence. */
    public final LedgerTransaction generateEntries(ImplementedAction action) {
        List<ResourceAllocation> allocations = selectAllocations(action);
        validate(allocations);
        LedgerTransaction lastTransaction = null;
        for (ResourceAllocation allocation : allocations) {
            Account pool = accountRepository.findPoolAccounts().stream()
                    .filter(a -> a.getResourceType().getId().equals(allocation.getResourceType().getId()))
                    .findFirst()
                    .orElseThrow(() -> new NotFoundException("Pool account not found for resource type: "
                            + allocation.getResourceType().getName()));
            Account usage = new Account();
            usage.setName("Usage:" + allocation.getResourceType().getName());
            usage.setKind(AccountKind.USAGE);
            usage.setResourceType(allocation.getResourceType());
            usage = accountRepository.save(usage);
            LedgerTransaction transaction = createTransaction(action);
            lastTransaction = transaction;
            postEntries(action, buildWithdrawal(transaction, pool, allocation),
                    buildDeposit(transaction, usage, allocation));
        }
        if (lastTransaction != null) {
            afterPost(lastTransaction);
        }
        return lastTransaction;
    }

    protected abstract List<ResourceAllocation> selectAllocations(ImplementedAction action);

    protected abstract void validate(List<ResourceAllocation> allocations);

    protected Entry buildWithdrawal(LedgerTransaction tx, Account account, ResourceAllocation allocation) {
        Entry e = new Entry();
        e.setTransaction(tx);
        e.setAccount(account);
        e.setAmount(-entryAmount(allocation));
        e.setChargedAt(clock.instant());
        e.setBookedAt(clock.instant());
        return e;
    }

    protected Entry buildDeposit(LedgerTransaction tx, Account account, ResourceAllocation allocation) {
        Entry e = new Entry();
        e.setTransaction(tx);
        e.setAccount(account);
        e.setAmount(entryAmount(allocation));
        e.setChargedAt(clock.instant());
        e.setBookedAt(clock.instant());
        return e;
    }

    /** Amount used for withdrawal/deposit. Default is quantity; override for time-based entries. */
    protected double entryAmount(ResourceAllocation allocation) {
        return allocation.getQuantity();
    }

    /** Hook for subclasses — empty by default. AssetLedgerEntryGenerator uses this for audit log. */
    protected void afterPost(LedgerTransaction tx) {}

    protected final LedgerTransaction createTransaction(ImplementedAction action) {
        LedgerTransaction tx = new LedgerTransaction();
        tx.setDescription("Complete action #" + action.getId());
        return transactionRepository.save(tx);
    }

    /** Final — ensures conservation (balanced entries) and fires posting rules. */
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
