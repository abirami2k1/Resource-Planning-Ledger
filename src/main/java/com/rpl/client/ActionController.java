package com.rpl.client;

import com.rpl.client.dto.AllocationRequest;
import com.rpl.client.dto.SuspendRequest;
import com.rpl.client.dto.SuspensionResponse;
import com.rpl.domain.ProposedAction;
import com.rpl.domain.ResourceAllocation;
import com.rpl.manager.ActionManager;
import com.rpl.manager.ResourceAllocationManager;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/actions")
public class ActionController {
    private final ActionManager actionManager;
    private final ResourceAllocationManager allocationManager;

    public ActionController(ActionManager actionManager, ResourceAllocationManager allocationManager) {
        this.actionManager = actionManager;
        this.allocationManager = allocationManager;
    }

    @PostMapping("/{id}/suspend")
    public ProposedAction suspend(@PathVariable Long id, @RequestBody(required = false) SuspendRequest body) {
        String reason = body != null && body.reason() != null ? body.reason() : "";
        return actionManager.suspendWithReason(id, reason);
    }

    @GetMapping("/{id}/suspensions")
    public List<SuspensionResponse> suspensions(@PathVariable Long id) {
        return actionManager.getSuspensions(id);
    }

    @PostMapping("/{id}/{event}")
    public ProposedAction transition(@PathVariable Long id, @PathVariable String event) {
        return actionManager.transition(id, event);
    }

    @PostMapping("/{id}/allocations")
    public ResourceAllocation allocate(@PathVariable Long id, @RequestBody AllocationRequest request) {
        ResourceAllocation allocation = new ResourceAllocation();
        allocation.setQuantity(request.quantity());
        allocation.setKind(request.kind());
        allocation.setAssetId(request.assetId());
        return allocationManager.attach(id, request.resourceTypeId(), allocation);
    }
}
