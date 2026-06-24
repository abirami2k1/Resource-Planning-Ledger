package com.rpl.engine;

import com.rpl.domain.Account;
import com.rpl.domain.Entry;
import com.rpl.domain.PostingRule;
import com.rpl.resourceaccess.AccountRepository;
import com.rpl.resourceaccess.EntryRepository;
import com.rpl.resourceaccess.PostingRuleRepository;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class PostingRuleEngine {
    private static final Logger log = LoggerFactory.getLogger(PostingRuleEngine.class);

    private final AccountRepository accountRepository;
    private final EntryRepository entryRepository;
    private final PostingRuleRepository postingRuleRepository;
    private final Clock clock;

    public PostingRuleEngine(
            AccountRepository accountRepository,
            EntryRepository entryRepository,
            PostingRuleRepository postingRuleRepository,
            Clock clock) {
        this.accountRepository = accountRepository;
        this.entryRepository = entryRepository;
        this.postingRuleRepository = postingRuleRepository;
        this.clock = clock;
    }

    /**
     * Fire-and-forget posting evaluation: never throws.
     */
    public void evaluate(Entry withdrawalEntry) {
        try {
            Account poolAccount = withdrawalEntry.getAccount();
            if (poolAccount == null || poolAccount.getId() == null) {
                return;
            }
            List<PostingRule> rules = postingRuleRepository.findByTriggerAccount_Id(poolAccount.getId());
            if (rules.isEmpty()) {
                return;
            }
            PostingRule rule = rules.get(0);
            BigDecimal balance = accountRepository.getBalance(poolAccount.getId());
            if (balance.compareTo(BigDecimal.ZERO) >= 0) {
                return;
            }
            Account memoAccount = rule.getOutputAccount();
            if (memoAccount == null) {
                log.warn("Alert memo account not found for pool {}", poolAccount.getId());
                return;
            }
            Instant now = clock.instant();
            Entry alert = new Entry();
            alert.setTransaction(withdrawalEntry.getTransaction());
            alert.setAccount(memoAccount);
            alert.setAmount(balance.abs().doubleValue());
            alert.setChargedAt(now);
            alert.setBookedAt(now);
            entryRepository.save(alert);
        } catch (Exception e) {
            log.warn("Posting rule evaluation failed: {}", e.getMessage(), e);
        }
    }
}
