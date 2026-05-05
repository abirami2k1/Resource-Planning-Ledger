package com.rpl.manager;

import com.rpl.domain.Account;
import com.rpl.domain.AccountKind;
import com.rpl.domain.PostingRule;
import com.rpl.domain.ResourceType;
import com.rpl.exception.ConflictException;
import com.rpl.exception.NotFoundException;
import com.rpl.resourceaccess.AccountRepository;
import com.rpl.resourceaccess.PostingRuleRepository;
import com.rpl.resourceaccess.ResourceAllocationRepository;
import com.rpl.resourceaccess.ResourceTypeRepository;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class ResourceTypeManager {
    private final ResourceTypeRepository resourceTypeRepository;
    private final AccountRepository accountRepository;
    private final ResourceAllocationRepository allocationRepository;
    private final PostingRuleRepository postingRuleRepository;

    public ResourceTypeManager(
            ResourceTypeRepository resourceTypeRepository,
            AccountRepository accountRepository,
            ResourceAllocationRepository allocationRepository,
            PostingRuleRepository postingRuleRepository) {
        this.resourceTypeRepository = resourceTypeRepository;
        this.accountRepository = accountRepository;
        this.allocationRepository = allocationRepository;
        this.postingRuleRepository = postingRuleRepository;
    }

    public List<ResourceType> list() {
        return resourceTypeRepository.findAll();
    }

    public ResourceType create(ResourceType resourceType) {
        ResourceType saved = resourceTypeRepository.save(resourceType);
        Account pool = new Account();
        pool.setName("Pool:" + saved.getName());
        pool.setKind(AccountKind.POOL);
        pool.setResourceType(saved);
        Account savedPool = accountRepository.save(pool);

        Account alertMemo = new Account();
        alertMemo.setName("AlertMemo:" + saved.getName());
        alertMemo.setKind(AccountKind.ALERT_MEMO);
        alertMemo.setResourceType(saved);
        Account savedMemo = accountRepository.save(alertMemo);

        PostingRule rule = new PostingRule();
        rule.setTriggerAccount(savedPool);
        rule.setOutputAccount(savedMemo);
        rule.setStrategyType("UNDER_BALANCE_ALERT");
        postingRuleRepository.save(rule);

        return saved;
    }

    public void delete(Long id) {
        ResourceType existing = resourceTypeRepository.findById(id).orElseThrow(() -> new NotFoundException("Resource type not found"));
        if (allocationRepository.countByResourceTypeId(id) > 0) {
            throw new ConflictException("Resource type has active allocations");
        }
        resourceTypeRepository.delete(existing);
    }
}
