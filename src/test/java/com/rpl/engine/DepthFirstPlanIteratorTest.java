package com.rpl.engine;

import com.rpl.domain.Plan;
import com.rpl.domain.ProposedAction;
import com.rpl.domain.composite.PlanNode;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class DepthFirstPlanIteratorTest {
    @Test
    void iteratesRootThenChildrenInOrder() {
        Plan plan = new Plan();
        plan.setName("Root");
        ProposedAction a = new ProposedAction();
        a.setName("A");
        a.setPlan(plan);
        ProposedAction b = new ProposedAction();
        b.setName("B");
        b.setPlan(plan);
        plan.getActions().add(a);
        plan.getActions().add(b);

        DepthFirstPlanIterator iterator = new DepthFirstPlanIterator(plan);
        List<String> names = new ArrayList<>();
        while (iterator.hasNext()) {
            PlanNode node = iterator.next();
            names.add(node.getName());
        }
        Assertions.assertEquals(List.of("Root", "A", "B"), names);
    }
}
