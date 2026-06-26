package com.rpl.engine;

import com.rpl.domain.Plan;
import com.rpl.domain.composite.PlanNode;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Week 2 — depth-first traversal with a depth limit.
 * Stops descending into a sub-plan once the depth limit is reached,
 * yielding the sub-plan node itself rather than its children.
 * Supports the "collapsed view" in the UI.
 */
public class LazySubtreeIterator implements Iterator<PlanNode> {
    private final Deque<NodeWithDepth> stack = new ArrayDeque<>();
    private final int depthLimit;

    private record NodeWithDepth(PlanNode node, int depth) {}

    public LazySubtreeIterator(Plan root, int depthLimit) {
        this.depthLimit = depthLimit;
        stack.push(new NodeWithDepth(root, 0));
    }

    @Override
    public boolean hasNext() {
        return !stack.isEmpty();
    }

    @Override
    public PlanNode next() {
        if (!hasNext()) throw new NoSuchElementException("No more plan nodes");
        NodeWithDepth current = stack.pop();
        PlanNode node = current.node();
        int depth = current.depth();
        // Only expand children if we haven't hit the depth limit
        if (node instanceof Plan plan && depth < depthLimit) {
            java.util.List<PlanNode> children = plan.getChildren();
            for (int i = children.size() - 1; i >= 0; i--) {
                stack.push(new NodeWithDepth(children.get(i), depth + 1));
            }
        }
        return node;
    }
}
