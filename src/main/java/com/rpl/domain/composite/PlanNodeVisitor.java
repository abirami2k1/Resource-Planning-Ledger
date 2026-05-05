package com.rpl.domain.composite;

import com.rpl.domain.Plan;
import com.rpl.domain.ProposedAction;

public interface PlanNodeVisitor {
    void visit(ProposedAction leaf);

    void visit(Plan composite);
}
