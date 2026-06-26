package com.rpl.domain.composite;

import com.rpl.domain.Plan;
import com.rpl.domain.ProposedAction;

/**
 * Visitor interface for the Composite PlanNode tree.
 * visitLeaf() is called on ProposedAction leaf nodes.
 * visitComposite() is called on Plan composite nodes.
 */
public interface PlanNodeVisitor {
    void visitLeaf(ProposedAction leaf);
    void visitComposite(Plan plan);
}
