package com.rpl.engine;

import com.rpl.domain.ActionStatus;
import com.rpl.domain.Plan;
import com.rpl.domain.ProposedAction;
import com.rpl.domain.composite.PlanNode;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FilteredPlanIteratorTest {

    @Test
    void filter_proposedOnly_returnsOnlyProposedLeaves() {
        // Arrange
        Plan root = new Plan();
        root.setName("Root");

        ProposedAction proposed = new ProposedAction();
        proposed.setName("Proposed Action");
        proposed.setStatus(ActionStatus.PROPOSED);
        root.getActions().add(proposed);

        ProposedAction inProgress = new ProposedAction();
        inProgress.setName("In-Progress Action");
        inProgress.setStatus(ActionStatus.IN_PROGRESS);
        root.getActions().add(inProgress);

        // Act — filter for PROPOSED leaves only
        FilteredPlanIterator it = new FilteredPlanIterator(root,
                node -> node instanceof ProposedAction pa && pa.getStatus() == ActionStatus.PROPOSED);

        List<String> names = new ArrayList<>();
        while (it.hasNext()) names.add(it.next().getName());

        // Assert
        assertTrue(names.contains("Proposed Action"));
        assertFalse(names.contains("In-Progress Action"));
    }

    @Test
    void filter_noMatch_returnsEmpty() {
        // Arrange
        Plan root = new Plan();
        root.setName("Empty Plan");
        ProposedAction a = new ProposedAction();
        a.setName("A");
        a.setStatus(ActionStatus.PROPOSED);
        root.getActions().add(a);

        // Act — filter for COMPLETED — none match
        FilteredPlanIterator it = new FilteredPlanIterator(root,
                node -> node instanceof ProposedAction pa && pa.getStatus() == ActionStatus.COMPLETED);

        List<PlanNode> results = new ArrayList<>();
        while (it.hasNext()) results.add(it.next());

        // Assert
        assertTrue(results.isEmpty());
    }

    @Test
    void filter_includesCompositeNodes_whenPredicateAllows() {
        // Arrange
        Plan root = new Plan();
        root.setName("Root");
        ProposedAction a = new ProposedAction();
        a.setName("A");
        a.setStatus(ActionStatus.PROPOSED);
        root.getActions().add(a);

        // Act — match everything
        FilteredPlanIterator it = new FilteredPlanIterator(root, node -> true);
        List<String> names = new ArrayList<>();
        while (it.hasNext()) names.add(it.next().getName());

        // Assert — Root composite + leaf A
        assertEquals(List.of("Root", "A"), names);
    }
}
