package com.rpl.engine;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.*;

import com.rpl.domain.*;
import com.rpl.manager.AuditLogManager;
import com.rpl.resourceaccess.*;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AbstractLedgerEntryGeneratorPostingRuleTest {

    @Mock LedgerTransactionRepository transactionRepository;
    @Mock EntryRepository entryRepository;
    @Mock AccountRepository accountRepository;
    @Mock PostingRuleEngine postingRuleEngine;
    @Mock AuditLogManager auditLogManager;

    Clock fixedClock = Clock.fixed(Instant.parse("2026-01-01T00:00:00Z"), ZoneId.of("UTC"));

    /** Minimal concrete subclass to expose protected postEntries(). */
    private final class ExposingGenerator extends AbstractLedgerEntryGenerator {
        ExposingGenerator() {
            super(transactionRepository, entryRepository, accountRepository,
                  postingRuleEngine, auditLogManager, fixedClock);
        }

        @Override
        protected List<ResourceAllocation> selectAllocations(ImplementedAction action) {
            return List.of();
        }

        @Override
        protected void validate(List<ResourceAllocation> allocations) {}

        void exposePostEntries(ImplementedAction action, Entry withdrawal, Entry deposit) {
            postEntries(action, withdrawal, deposit);
        }
    }

    @Test
    void postEntries_savesWithdrawalAndDeposit_thenFiresPostingRule() {
        // Arrange
        ExposingGenerator gen = new ExposingGenerator();

        ProposedAction pa = new ProposedAction();
        pa.setId(42L);
        ImplementedAction ia = new ImplementedAction();
        ia.setProposedAction(pa);

        Account pool = new Account();
        pool.setId(7L);
        Entry withdrawal = new Entry();
        withdrawal.setAccount(pool);
        Entry deposit = new Entry();

        when(entryRepository.save(withdrawal)).thenReturn(withdrawal);
        when(entryRepository.save(deposit)).thenReturn(deposit);

        // Act
        gen.exposePostEntries(ia, withdrawal, deposit);

        // Assert — withdrawal saved, then deposit saved, then posting rule fires
        InOrder order = inOrder(entryRepository, postingRuleEngine);
        order.verify(entryRepository).save(withdrawal);
        order.verify(entryRepository).save(deposit);
        order.verify(postingRuleEngine).evaluate(withdrawal);
        verify(auditLogManager).record(eq("LEDGER_ENTRIES_POSTED"), eq(7L), isNull(), eq(42L));
    }

    @Test
    void postEntries_clockUsedForTimestamps_notInstantNow() {
        // Arrange — verify that the generator uses injected clock (fixed time)
        ExposingGenerator gen = new ExposingGenerator();
        ProposedAction pa = new ProposedAction();
        pa.setId(1L);
        ImplementedAction ia = new ImplementedAction();
        ia.setProposedAction(pa);

        Account pool = new Account();
        pool.setId(1L);

        // Build entries using buildWithdrawal/buildDeposit via a thin allocation
        ResourceAllocation alloc = new ResourceAllocation();
        alloc.setQuantity(5.0);
        ResourceType rt = new ResourceType();
        rt.setId(1L);
        rt.setName("T");
        alloc.setResourceType(rt);

        LedgerTransaction tx = new LedgerTransaction();
        Entry w = gen.buildWithdrawal(tx, pool, alloc);
        Entry d = gen.buildDeposit(tx, pool, alloc);

        // Assert — timestamps come from fixed clock
        assertEquals(Instant.parse("2026-01-01T00:00:00Z"), w.getChargedAt());
        assertEquals(Instant.parse("2026-01-01T00:00:00Z"), d.getBookedAt());
    }
}
