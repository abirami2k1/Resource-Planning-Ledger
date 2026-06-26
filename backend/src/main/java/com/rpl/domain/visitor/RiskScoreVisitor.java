package com.rpl.domain.visitor;

import com.rpl.domain.ActionStatus;
import com.rpl.domain.Plan;
import com.rpl.domain.ProposedAction;
import com.rpl.domain.composite.PlanNodeVisitor;

/**
 * Increments a counter for each leaf in SUSPENDED or ABANDONED state.
 */
public class RiskScoreVisitor implements PlanNodeVisitor {
    private int score = 0;

    @Override
    public void visitLeaf(ProposedAction leaf) {
        if (leaf.getStatus() == ActionStatus.SUSPENDED
                || leaf.getStatus() == ActionStatus.ABANDONED) {
            score++;
        }
    }

    @Override
    public void visitComposite(Plan plan) {
        // risk is measured at the leaf level
    }

    public int getScore() { return score; }
}
