package com.rpl.engine;

import com.rpl.domain.Plan;
import com.rpl.domain.ProposedAction;
import com.rpl.domain.composite.PlanNode;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class DepthFirstPlanIterator implements Iterator<PlanNode> {
    private final Deque<PlanNode> stack = new ArrayDeque<>();

    public DepthFirstPlanIterator(Plan plan) {
        stack.push(plan);
    }

    @Override
    public boolean hasNext() {
        return !stack.isEmpty();
    }

    @Override
    public PlanNode next() {
        if (!hasNext()) {
            throw new NoSuchElementException("No more plan nodes");
        }
        PlanNode next = stack.pop();
        if (next instanceof Plan p) {
            for (int i = p.getActions().size() - 1; i >= 0; i--) {
                ProposedAction action = p.getActions().get(i);
                stack.push(action);
            }
        }
        return next;
    }
}
