package com.rpl.engine;

import com.rpl.domain.Plan;
import com.rpl.domain.ProposedAction;
import com.rpl.domain.composite.PlanNode;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class LazySubtreeIteratorTest {

    @Test
    void depthLimit1_doesNotExpandChildSubPlans() {
        // Arrange: Root -> SubPlan -> Leaf
        Plan root = new Plan();
        root.setName("Root");

        Plan sub = new Plan();
        sub.setName("SubPlan");
        sub.setParentPlan(root);
        root.getSubPlans().add(sub);

        ProposedAction leaf = new ProposedAction();
        leaf.setName("Leaf");
        sub.getActions().add(leaf);

        // Act — depth limit 1: should yield Root and SubPlan but NOT Leaf
        LazySubtreeIterator it = new LazySubtreeIterator(root, 1);
        List<String> names = new ArrayList<>();
        while (it.hasNext()) names.add(it.next().getName());

        // Assert
        assertTrue(names.contains("Root"));
        assertTrue(names.contains("SubPlan"));
        assertFalse(names.contains("Leaf"), "Leaf should be collapsed at depth limit 1");
    }

    @Test
    void depthLimit0_yieldsOnlyRoot() {
        // Arrange
        Plan root = new Plan();
        root.setName("Root");
        ProposedAction a = new ProposedAction();
        a.setName("A");
        root.getActions().add(a);

        // Act
        LazySubtreeIterator it = new LazySubtreeIterator(root, 0);
        List<String> names = new ArrayList<>();
        while (it.hasNext()) names.add(it.next().getName());

        // Assert — only root, no children
        assertEquals(List.of("Root"), names);
    }

    @Test
    void depthLimitHigh_yieldsAllNodes() {
        // Arrange
        Plan root = new Plan();
        root.setName("Root");
        ProposedAction a = new ProposedAction();
        a.setName("A");
        root.getActions().add(a);

        // Act — large depth limit
        LazySubtreeIterator it = new LazySubtreeIterator(root, 99);
        List<String> names = new ArrayList<>();
        while (it.hasNext()) names.add(it.next().getName());

        // Assert
        assertEquals(List.of("Root", "A"), names);
    }
}
