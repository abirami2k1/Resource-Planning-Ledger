package com.rpl.engine;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.rpl.domain.Account;
import com.rpl.domain.Entry;
import com.rpl.domain.ImplementedAction;
import com.rpl.domain.ProposedAction;
import com.rpl.domain.ResourceAllocation;
import com.rpl.manager.AuditLogManager;
import com.rpl.resourceaccess.AccountRepository;
import com.rpl.resourceaccess.EntryRepository;
import com.rpl.resourceaccess.LedgerTransactionRepository;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AbstractLedgerEntryGeneratorPostingRuleTest {

    @Mock
    private LedgerTransactionRepository transactionRepository;

    @Mock
    private EntryRepository entryRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private PostingRuleEngine postingRuleEngine;

    @Mock
    private AuditLogManager auditLogManager;

    /** Minimal generator to expose {@link AbstractLedgerEntryGenerator#postEntries}. */
    private static final class ExposingGenerator extends AbstractLedgerEntryGenerator {
        private ExposingGenerator(
                LedgerTransactionRepository transactionRepository,
                EntryRepository entryRepository,
                AccountRepository accountRepository,
                PostingRuleEngine postingRuleEngine,
                AuditLogManager auditLogManager) {
            super(transactionRepository, entryRepository, accountRepository, postingRuleEngine, auditLogManager);
        }

        @Override
        protected List<ResourceAllocation> selectAllocations(ImplementedAction action) {
            return List.of();
        }

        @Override
        protected void validate(List<ResourceAllocation> allocations) {
            // no-op
        }

        void exposePostEntries(ImplementedAction action, Entry withdrawal, Entry deposit) {
            postEntries(action, withdrawal, deposit);
        }
    }

    @Test
    void postEntries_afterBothSaves_callsPostingRuleEvaluateWithWithdrawal() {
        // Arrange
        ExposingGenerator gen = new ExposingGenerator(
                transactionRepository, entryRepository, accountRepository, postingRuleEngine, auditLogManager);

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

        // Assert — posting rule runs after persistence of withdrawal (and deposit)
        InOrder order = inOrder(entryRepository, postingRuleEngine);
        order.verify(entryRepository).save(withdrawal);
        order.verify(entryRepository).save(deposit);
        order.verify(postingRuleEngine).evaluate(withdrawal);
        verify(auditLogManager).record(eq("LEDGER_ENTRIES_POSTED"), eq(7L), isNull(), eq(42L));
    }
}
