package com.rpl.domain.composite;

import com.rpl.domain.Plan;
import com.rpl.domain.ProposedAction;
import com.rpl.domain.ResourceAllocation;
import com.rpl.domain.ResourceType;
import java.math.BigDecimal;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class PlanNodeQuantityTest {

    @Test
    void proposedAction_twoAllocationsSameType_sumsQuantities() {
        // Arrange
        ResourceType typeA = type(1L, "A");
        ResourceType typeB = type(2L, "B");
        ProposedAction pa = new ProposedAction();
        pa.getAllocations().add(alloc(typeA, 3));
        pa.getAllocations().add(alloc(typeA, 2));
        pa.getAllocations().add(alloc(typeB, 10));

        // Act + Assert
        Assertions.assertEquals(0, pa.getTotalAllocatedQuantity(typeA).compareTo(BigDecimal.valueOf(5)));
        Assertions.assertEquals(0, pa.getTotalAllocatedQuantity(typeB).compareTo(BigDecimal.valueOf(10)));
    }

    @Test
    void plan_flatLeaves_accumulatesViaDepthFirstIterator() {
        // Arrange
        ResourceType typeA = type(1L, "A");
        Plan plan = new Plan();
        ProposedAction one = new ProposedAction();
        one.getAllocations().add(alloc(typeA, 3));
        plan.addLeaf(one);
        ProposedAction two = new ProposedAction();
        two.getAllocations().add(alloc(typeA, 2));
        plan.addLeaf(two);

        // Act + Assert
        Assertions.assertEquals(0, plan.getTotalAllocatedQuantity(typeA).compareTo(BigDecimal.valueOf(5)));
    }

    @Test
    void plan_nestedSubPlan_includesDescendantAllocations() {
        // Arrange
        ResourceType typeA = type(1L, "A");
        Plan root = new Plan();
        Plan sub = new Plan();
        ProposedAction direct = new ProposedAction();
        direct.getAllocations().add(alloc(typeA, 2));
        root.addLeaf(direct);
        root.addSubPlan(sub);
        ProposedAction nested = new ProposedAction();
        nested.getAllocations().add(alloc(typeA, 1));
        sub.addLeaf(nested);

        // Act + Assert
        Assertions.assertEquals(0, root.getTotalAllocatedQuantity(typeA).compareTo(BigDecimal.valueOf(3)));
    }

    private static ResourceType type(long id, String name) {
        ResourceType rt = new ResourceType();
        rt.setId(id);
        rt.setName(name);
        return rt;
    }

    private static ResourceAllocation alloc(ResourceType rt, double qty) {
        ResourceAllocation a = new ResourceAllocation();
        a.setResourceType(rt);
        a.setQuantity(qty);
        return a;
    }
}
