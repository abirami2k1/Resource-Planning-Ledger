package com.rpl.engine;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.rpl.domain.Account;
import com.rpl.domain.Entry;
import com.rpl.domain.LedgerTransaction;
import com.rpl.domain.PostingRule;
import com.rpl.resourceaccess.AccountRepository;
import com.rpl.resourceaccess.EntryRepository;
import com.rpl.resourceaccess.PostingRuleRepository;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PostingRuleEngineTest {

    private static final Instant FIXED = Instant.parse("2026-05-01T10:15:30Z");

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private EntryRepository entryRepository;

    @Mock
    private PostingRuleRepository postingRuleRepository;

    private Clock clock;

    private PostingRuleEngine engine;

    @BeforeEach
    void setUp() {
        clock = Clock.fixed(FIXED, ZoneOffset.UTC);
        engine = new PostingRuleEngine(accountRepository, entryRepository, postingRuleRepository, clock);
    }

    @Test
    void evaluate_whenBalanceNonNegative_noAlertEntryCreated() {
        // Arrange
        Account pool = accountWithId(10L);
        Entry withdrawal = withdrawalOn(pool);
        PostingRule rule = rule(pool, memoAccount(99L));
        when(postingRuleRepository.findByTriggerAccount_Id(10L)).thenReturn(List.of(rule));
        when(accountRepository.getBalance(10L)).thenReturn(BigDecimal.ZERO);

        // Act
        engine.evaluate(withdrawal);

        // Assert
        verify(entryRepository, never()).save(any(Entry.class));
    }

    @Test
    void evaluate_whenBalanceNegative_createsOneAlertEntryOnMemoAccount() {
        // Arrange
        Account pool = accountWithId(10L);
        Account memo = memoAccount(99L);
        LedgerTransaction tx = new LedgerTransaction();
        Entry withdrawal = withdrawalOn(pool);
        withdrawal.setTransaction(tx);
        PostingRule rule = rule(pool, memo);
        when(postingRuleRepository.findByTriggerAccount_Id(10L)).thenReturn(List.of(rule));
        when(accountRepository.getBalance(10L)).thenReturn(BigDecimal.valueOf(-12.5));

        // Act
        engine.evaluate(withdrawal);

        // Assert
        ArgumentCaptor<Entry> captor = ArgumentCaptor.forClass(Entry.class);
        verify(entryRepository).save(captor.capture());
        Entry alert = captor.getValue();
        org.junit.jupiter.api.Assertions.assertEquals(memo, alert.getAccount());
        org.junit.jupiter.api.Assertions.assertEquals(12.5, alert.getAmount(), 0.0001);
        org.junit.jupiter.api.Assertions.assertEquals(tx, alert.getTransaction());
    }

    @Test
    void evaluate_whenBalanceNegative_alertAmountEqualsDeficitAbs() {
        // Arrange
        Account pool = accountWithId(10L);
        Entry withdrawal = withdrawalOn(pool);
        PostingRule rule = rule(pool, memoAccount(99L));
        when(postingRuleRepository.findByTriggerAccount_Id(10L)).thenReturn(List.of(rule));
        when(accountRepository.getBalance(10L)).thenReturn(BigDecimal.valueOf(-3.25));

        // Act
        engine.evaluate(withdrawal);

        // Assert
        ArgumentCaptor<Entry> captor = ArgumentCaptor.forClass(Entry.class);
        verify(entryRepository).save(captor.capture());
        org.junit.jupiter.api.Assertions.assertEquals(3.25, captor.getValue().getAmount(), 0.0001);
    }

    @Test
    void evaluate_whenOutputAccountMissing_logsAndDoesNotThrow() {
        // Arrange
        Account pool = accountWithId(10L);
        Entry withdrawal = withdrawalOn(pool);
        PostingRule rule = new PostingRule();
        rule.setTriggerAccount(pool);
        rule.setOutputAccount(null);
        rule.setStrategyType("UNDER_BALANCE_ALERT");
        when(postingRuleRepository.findByTriggerAccount_Id(10L)).thenReturn(List.of(rule));
        when(accountRepository.getBalance(10L)).thenReturn(BigDecimal.valueOf(-1));

        // Act + Assert
        assertDoesNotThrow(() -> engine.evaluate(withdrawal));
        verify(entryRepository, never()).save(any(Entry.class));
    }

    @Test
    void evaluate_alertEntry_timestampsFromClock() {
        // Arrange
        Account pool = accountWithId(10L);
        Entry withdrawal = withdrawalOn(pool);
        PostingRule rule = rule(pool, memoAccount(99L));
        when(postingRuleRepository.findByTriggerAccount_Id(10L)).thenReturn(List.of(rule));
        when(accountRepository.getBalance(10L)).thenReturn(BigDecimal.valueOf(-2));

        // Act
        engine.evaluate(withdrawal);

        // Assert
        ArgumentCaptor<Entry> captor = ArgumentCaptor.forClass(Entry.class);
        verify(entryRepository).save(captor.capture());
        org.junit.jupiter.api.Assertions.assertEquals(FIXED, captor.getValue().getChargedAt());
        org.junit.jupiter.api.Assertions.assertEquals(FIXED, captor.getValue().getBookedAt());
    }

    @Test
    void evaluate_whenSaveThrows_doesNotPropagate() {
        // Arrange
        Account pool = accountWithId(10L);
        Entry withdrawal = withdrawalOn(pool);
        PostingRule rule = rule(pool, memoAccount(99L));
        when(postingRuleRepository.findByTriggerAccount_Id(10L)).thenReturn(List.of(rule));
        when(accountRepository.getBalance(10L)).thenReturn(BigDecimal.valueOf(-1));
        when(entryRepository.save(any(Entry.class))).thenThrow(new IllegalStateException("db"));

        // Act + Assert
        assertDoesNotThrow(() -> engine.evaluate(withdrawal));
    }

    @Test
    void evaluate_whenNoRulesReturnsImmediately() {
        // Arrange
        Entry withdrawal = withdrawalOn(accountWithId(1L));
        when(postingRuleRepository.findByTriggerAccount_Id(1L)).thenReturn(Collections.emptyList());

        // Act
        engine.evaluate(withdrawal);

        // Assert
        verify(accountRepository, never()).getBalance(any());
        verify(entryRepository, never()).save(any(Entry.class));
    }

    private static Account accountWithId(long id) {
        Account a = new Account();
        a.setId(id);
        return a;
    }

    private static Account memoAccount(long id) {
        Account a = new Account();
        a.setId(id);
        return a;
    }

    private static Entry withdrawalOn(Account pool) {
        Entry e = new Entry();
        e.setAccount(pool);
        return e;
    }

    private static PostingRule rule(Account trigger, Account output) {
        PostingRule r = new PostingRule();
        r.setTriggerAccount(trigger);
        r.setOutputAccount(output);
        r.setStrategyType("UNDER_BALANCE_ALERT");
        return r;
    }
}
