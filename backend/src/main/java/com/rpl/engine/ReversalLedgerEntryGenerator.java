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

/**
 * Week 2 — generates reversal entries (equal and opposite to completion entries)
 * when an action is reopened, restoring the pool balance.
 * Reuses the AbstractLedgerEntryGenerator skeleton unchanged; simply negates amounts.
 */
@Service
public class ReversalLedgerEntryGenerator extends AbstractLedgerEntryGenerator {
    private final ResourceAllocationRepository allocationRepository;

    public ReversalLedgerEntryGenerator(
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
                throw new ValidationException("Quantity must be positive for reversal allocation id=" + a.getId());
            }
        }
    }

    /**
     * Reversal: withdrawal posts +quantity (credit back to pool).
     * This negates the original completion withdrawal of -quantity.
     */
    @Override
    protected double entryAmount(ResourceAllocation allocation) {
        return -allocation.getQuantity(); // base class applies another negation in buildWithdrawal → net positive
    }
}
