package com.rpl.domain.composite;

import com.rpl.domain.ActionStatus;
import com.rpl.domain.Plan;
import com.rpl.domain.ProposedAction;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class PlanNestedCompositeStatusTest {

    @Test
    void threeLevelTree_derivesStatusFromAllLeaves() {
        // Arrange — root → leaf1 (COMPLETED) + sub → leaf2 (IN_PROGRESS), leaf3 (PROPOSED)
        Plan root = new Plan();
        ProposedAction leaf1 = new ProposedAction();
        leaf1.setStatus(ActionStatus.COMPLETED);
        root.addLeaf(leaf1);

        Plan sub = new Plan();
        ProposedAction leaf2 = new ProposedAction();
        leaf2.setStatus(ActionStatus.IN_PROGRESS);
        ProposedAction leaf3 = new ProposedAction();
        leaf3.setStatus(ActionStatus.PROPOSED);
        sub.addLeaf(leaf2);
        sub.addLeaf(leaf3);
        root.addSubPlan(sub);

        // Assert initial rollup
        Assertions.assertEquals(ActionStatus.IN_PROGRESS, root.getStatus());
        Assertions.assertEquals(ActionStatus.IN_PROGRESS, sub.getStatus());

        // Complete remaining work under sub
        leaf2.setStatus(ActionStatus.COMPLETED);
        leaf3.setStatus(ActionStatus.COMPLETED);
        Assertions.assertEquals(ActionStatus.COMPLETED, sub.getStatus());

        Assertions.assertEquals(ActionStatus.COMPLETED, root.getStatus());
    }
}
