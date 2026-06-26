package com.rpl.engine;

import com.rpl.domain.ImplementedAction;
import com.rpl.domain.ResourceAllocation;
import com.rpl.domain.ResourceKind;
import com.rpl.exception.ValidationException;
import com.rpl.manager.AuditLogManager;
import com.rpl.resourceaccess.AccountRepository;
import com.rpl.resourceaccess.EntryRepository;
import com.rpl.resourceaccess.LedgerTransactionRepository;
import com.rpl.resourceaccess.ResourceAllocationRepository;
import java.time.Clock;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class ConsumableLedgerEntryGenerator extends AbstractLedgerEntryGenerator {
    private final ResourceAllocationRepository allocationRepository;

    public ConsumableLedgerEntryGenerator(
            LedgerTransactionRepository transactionRepository,
            EntryRepository entryRepository,
            AccountRepository accountRepository,
            PostingRuleEngine postingRuleEngine,
            AuditLogManager auditLogManager,
            Clock clock,
            ResourceAllocationRepository allocationRepository) {
        super(transactionRepository, entryRepository, accountRepository, postingRuleEngine, auditLogManager, clock);
        this.allocationRepository = allocationRepository;
    }

    @Override
    protected List<ResourceAllocation> selectAllocations(ImplementedAction action) {
        return allocationRepository.findByActionId(action.getProposedAction().getId()).stream()
                .filter(a -> a.getResourceType().getKind() == ResourceKind.CONSUMABLE)
                .toList();
    }

    @Override
    protected void validate(List<ResourceAllocation> allocations) {
        for (ResourceAllocation a : allocations) {
            if (a.getQuantity() <= 0) {
                throw new ValidationException("Quantity must be positive for consumable allocation id=" + a.getId());
            }
        }
    }
}
