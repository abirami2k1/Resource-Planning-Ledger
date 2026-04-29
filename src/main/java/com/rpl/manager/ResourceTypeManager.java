package com.rpl.manager;

import com.rpl.domain.Account;
import com.rpl.domain.AccountKind;
import com.rpl.domain.ResourceType;
import com.rpl.exception.ConflictException;
import com.rpl.exception.NotFoundException;
import com.rpl.resourceaccess.AccountRepository;
import com.rpl.resourceaccess.ResourceAllocationRepository;
import com.rpl.resourceaccess.ResourceTypeRepository;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class ResourceTypeManager {
    private final ResourceTypeRepository resourceTypeRepository;
    private final AccountRepository accountRepository;
    private final ResourceAllocationRepository allocationRepository;

    public ResourceTypeManager(
            ResourceTypeRepository resourceTypeRepository,
            AccountRepository accountRepository,
            ResourceAllocationRepository allocationRepository) {
        this.resourceTypeRepository = resourceTypeRepository;
        this.accountRepository = accountRepository;
        this.allocationRepository = allocationRepository;
    }

    public List<ResourceType> list() { return resourceTypeRepository.findAll(); }

    public ResourceType create(ResourceType resourceType) {
        ResourceType saved = resourceTypeRepository.save(resourceType);
        Account account = new Account();
        account.setName("Pool:" + saved.getName());
        account.setKind(AccountKind.POOL);
        account.setResourceType(saved);
        accountRepository.save(account);
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
