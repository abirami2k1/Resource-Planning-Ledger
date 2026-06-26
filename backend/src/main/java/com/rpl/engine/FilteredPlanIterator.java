package com.rpl.engine;

import com.rpl.domain.Plan;
import com.rpl.domain.composite.PlanNode;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.Predicate;

/**
 * Week 2 — wraps DepthFirstPlanIterator and skips nodes that do not satisfy
 * the supplied predicate. The underlying depth-first traversal is unchanged.
 * Implements Iterator<PlanNode> so any consumer works with all three iterators.
 */
public class FilteredPlanIterator implements Iterator<PlanNode> {
    private final DepthFirstPlanIterator delegate;
    private final Predicate<PlanNode> predicate;
    private PlanNode next;

    public FilteredPlanIterator(Plan root, Predicate<PlanNode> predicate) {
        this.delegate = new DepthFirstPlanIterator(root);
        this.predicate = predicate;
        advance();
    }

    private void advance() {
        next = null;
        while (delegate.hasNext()) {
            PlanNode candidate = delegate.next();
            if (predicate.test(candidate)) {
                next = candidate;
                break;
            }
        }
    }

    @Override
    public boolean hasNext() {
        return next != null;
    }

    @Override
    public PlanNode next() {
        if (!hasNext()) throw new NoSuchElementException("No more matching plan nodes");
        PlanNode result = next;
        advance();
        return result;
    }
}
