package com.rpl.domain.visitor;

import com.rpl.domain.ActionStatus;
import com.rpl.domain.Plan;
import com.rpl.domain.ProposedAction;
import com.rpl.domain.composite.PlanNodeVisitor;

/**
 * Counts total leaf actions and completed leaves; exposes getRatio().
 */
public class CompletionRatioVisitor implements PlanNodeVisitor {
    private int totalLeaves = 0;
    private int completedLeaves = 0;

    @Override
    public void visitLeaf(ProposedAction leaf) {
        totalLeaves++;
        if (leaf.getStatus() == ActionStatus.COMPLETED) completedLeaves++;
    }

    @Override
    public void visitComposite(Plan plan) {
        // composite nodes do not count as leaves
    }

    public double getRatio() {
        return totalLeaves == 0 ? 0.0 : (double) completedLeaves / totalLeaves;
    }

    public int getTotalLeaves() { return totalLeaves; }
    public int getCompletedLeaves() { return completedLeaves; }
}
