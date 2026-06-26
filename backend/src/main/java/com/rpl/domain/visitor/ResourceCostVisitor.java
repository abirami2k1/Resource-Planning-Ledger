package com.rpl.domain.visitor;

import com.rpl.domain.Plan;
import com.rpl.domain.ProposedAction;
import com.rpl.domain.ResourceAllocation;
import com.rpl.domain.composite.PlanNodeVisitor;

/**
 * Sums quantity × unitCost across all leaf allocations.
 * unitCost is stored on ResourceType.
 */
public class ResourceCostVisitor implements PlanNodeVisitor {
    private double totalCost = 0.0;

    @Override
    public void visitLeaf(ProposedAction leaf) {
        for (ResourceAllocation alloc : leaf.getAllocations()) {
            if (alloc.getResourceType() != null) {
                double unitCost = alloc.getResourceType().getUnitCost();
                totalCost += alloc.getQuantity() * unitCost;
            }
        }
    }

    @Override
    public void visitComposite(Plan plan) {
        // cost is accumulated from leaf allocations only
    }

    public double getTotalCost() { return totalCost; }
}
