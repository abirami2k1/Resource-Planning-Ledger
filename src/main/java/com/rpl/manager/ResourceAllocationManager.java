package com.rpl.manager;

import com.rpl.domain.ActionStatus;
import com.rpl.domain.AllocationKind;
import com.rpl.domain.ProposedAction;
import com.rpl.domain.ResourceAllocation;
import com.rpl.domain.ResourceType;
import com.rpl.exception.ConflictException;
import com.rpl.exception.NotFoundException;
import com.rpl.exception.ValidationException;
import com.rpl.resourceaccess.ProposedActionRepository;
import com.rpl.resourceaccess.ResourceAllocationRepository;
import com.rpl.resourceaccess.ResourceTypeRepository;
import org.springframework.stereotype.Service;

@Service
public class ResourceAllocationManager {
    private final ResourceAllocationRepository allocationRepository;
    private final ProposedActionRepository actionRepository;
    private final ResourceTypeRepository resourceTypeRepository;

    public ResourceAllocationManager(
            ResourceAllocationRepository allocationRepository,
            ProposedActionRepository actionRepository,
            ResourceTypeRepository resourceTypeRepository) {
        this.allocationRepository = allocationRepository;
        this.actionRepository = actionRepository;
        this.resourceTypeRepository = resourceTypeRepository;
    }

    public ResourceAllocation attach(Long actionId, Long resourceTypeId, ResourceAllocation allocationRequest) {
        ProposedAction action = actionRepository.findById(actionId)
                .orElseThrow(() -> new NotFoundException("Action not found"));
        if (action.getStatus() == ActionStatus.COMPLETED || action.getStatus() == ActionStatus.ABANDONED) {
            throw new ConflictException("Cannot allocate resources to terminal state actions");
        }
        ResourceType resourceType = resourceTypeRepository.findById(resourceTypeId)
                .orElseThrow(() -> new NotFoundException("Resource type not found"));
        if (allocationRequest.getQuantity() <= 0) {
            throw new ValidationException("Quantity must be positive");
        }
        if (allocationRequest.getKind() == AllocationKind.SPECIFIC && allocationRequest.getAssetId() == null) {
            throw new ValidationException("SPECIFIC allocation requires assetId");
        }
        allocationRequest.setAction(action);
        allocationRequest.setResourceType(resourceType);
        return allocationRepository.save(allocationRequest);
    }
}
