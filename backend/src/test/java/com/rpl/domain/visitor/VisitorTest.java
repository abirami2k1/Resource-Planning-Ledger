package com.rpl.domain.visitor;

import com.rpl.domain.*;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for CompletionRatioVisitor, ResourceCostVisitor, RiskScoreVisitor.
 */
class VisitorTest {

    // ── CompletionRatioVisitor ───────────────────────────────────────────────

    @Test
    void completionRatio_allCompleted_returnsOne() {
        // Arrange
        Plan root = buildPlan("Root", ActionStatus.COMPLETED, ActionStatus.COMPLETED);
        CompletionRatioVisitor v = new CompletionRatioVisitor();

        // Act
        root.accept(v);

        // Assert
        assertEquals(1.0, v.getRatio(), 0.001);
        assertEquals(2, v.getTotalLeaves());
        assertEquals(2, v.getCompletedLeaves());
    }

    @Test
    void completionRatio_noneCompleted_returnsZero() {
        // Arrange
        Plan root = buildPlan("Root", ActionStatus.PROPOSED, ActionStatus.IN_PROGRESS);
        CompletionRatioVisitor v = new CompletionRatioVisitor();

        // Act
        root.accept(v);

        // Assert
        assertEquals(0.0, v.getRatio(), 0.001);
    }

    @Test
    void completionRatio_halfCompleted_returnsHalf() {
        // Arrange
        Plan root = buildPlan("Root", ActionStatus.COMPLETED, ActionStatus.PROPOSED);
        CompletionRatioVisitor v = new CompletionRatioVisitor();

        // Act
        root.accept(v);

        // Assert
        assertEquals(0.5, v.getRatio(), 0.001);
    }

    @Test
    void completionRatio_emptyPlan_returnsZero() {
        // Arrange — plan with no leaves
        Plan root = new Plan();
        root.setName("Empty");
        CompletionRatioVisitor v = new CompletionRatioVisitor();

        // Act
        root.accept(v);

        // Assert
        assertEquals(0.0, v.getRatio(), 0.001);
    }

    // ── ResourceCostVisitor ─────────────────────────────────────────────────

    @Test
    void resourceCost_sumsQuantityTimesUnitCost() {
        // Arrange
        ResourceType rt = new ResourceType();
        rt.setUnitCost(10.0);

        ProposedAction a = new ProposedAction();
        a.setName("A");
        a.setStatus(ActionStatus.PROPOSED);
        ResourceAllocation alloc = new ResourceAllocation();
        alloc.setQuantity(3.0);
        alloc.setResourceType(rt);
        a.getAllocations().add(alloc);

        Plan root = new Plan();
        root.setName("Root");
        root.getActions().add(a);

        ResourceCostVisitor v = new ResourceCostVisitor();

        // Act
        root.accept(v);

        // Assert: 3 * 10 = 30
        assertEquals(30.0, v.getTotalCost(), 0.001);
    }

    @Test
    void resourceCost_noAllocations_returnsZero() {
        // Arrange
        Plan root = buildPlan("Root", ActionStatus.PROPOSED);
        ResourceCostVisitor v = new ResourceCostVisitor();

        // Act
        root.accept(v);

        // Assert
        assertEquals(0.0, v.getTotalCost(), 0.001);
    }

    // ── RiskScoreVisitor ────────────────────────────────────────────────────

    @Test
    void riskScore_suspendedAndAbandoned_counted() {
        // Arrange
        Plan root = buildPlan("Root",
                ActionStatus.SUSPENDED, ActionStatus.ABANDONED, ActionStatus.PROPOSED);
        RiskScoreVisitor v = new RiskScoreVisitor();

        // Act
        root.accept(v);

        // Assert: 2 risky leaves
        assertEquals(2, v.getScore());
    }

    @Test
    void riskScore_noRiskyLeaves_returnsZero() {
        // Arrange
        Plan root = buildPlan("Root", ActionStatus.PROPOSED, ActionStatus.COMPLETED);
        RiskScoreVisitor v = new RiskScoreVisitor();

        // Act
        root.accept(v);

        // Assert
        assertEquals(0, v.getScore());
    }

    // ── helpers ─────────────────────────────────────────────────────────────

    private static Plan buildPlan(String name, ActionStatus... statuses) {
        Plan plan = new Plan();
        plan.setName(name);
        for (int i = 0; i < statuses.length; i++) {
            ProposedAction a = new ProposedAction();
            a.setName("Action-" + i);
            a.setStatus(statuses[i]);
            plan.getActions().add(a);
        }
        return plan;
    }
}
