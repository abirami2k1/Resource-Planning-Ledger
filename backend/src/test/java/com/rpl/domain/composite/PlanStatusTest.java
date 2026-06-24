package com.rpl.domain.composite;

import com.rpl.domain.ActionStatus;
import com.rpl.domain.Plan;
import com.rpl.domain.ProposedAction;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class PlanStatusTest {
    @Test
    void completedWhenAllChildrenComplete() {
        Plan plan = new Plan();
        ProposedAction one = new ProposedAction();
        one.setStatus(ActionStatus.COMPLETED);
        one.setPlan(plan);
        ProposedAction two = new ProposedAction();
        two.setStatus(ActionStatus.COMPLETED);
        two.setPlan(plan);
        plan.getActions().add(one);
        plan.getActions().add(two);
        Assertions.assertEquals(ActionStatus.COMPLETED, plan.getStatus());
    }
}
