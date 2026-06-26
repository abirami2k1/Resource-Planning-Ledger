package com.rpl.domain.state;

import com.rpl.domain.ActionStatus;
import com.rpl.exception.IllegalStateTransitionException;
import org.springframework.stereotype.Component;

/**
 * REOPENED state (Week 2).
 * Entered from COMPLETED via reopen() — reversal ledger entries are created.
 * From here the action may complete() again or abandon().
 */
@Component
public class ReopenedState implements ActionState {

    @Override
    public String name() { return "REOPENED"; }

    @Override
    public void implement(ActionContext ctx) {
        throw new IllegalStateTransitionException("Cannot implement from REOPENED");
    }

    @Override
    public void submitForApproval(ActionContext ctx) {
        throw new IllegalStateTransitionException("Cannot submit for approval from REOPENED");
    }

    @Override
    public void suspend(ActionContext ctx, String reason) {
        throw new IllegalStateTransitionException("Cannot suspend from REOPENED");
    }

    @Override
    public void resume(ActionContext ctx) {
        throw new IllegalStateTransitionException("Cannot resume from REOPENED");
    }

    @Override
    public void complete(ActionContext ctx) {
        ctx.getAction().setStatus(ActionStatus.COMPLETED);
        ctx.getCallbacks().onComplete(ctx.getAction());
    }

    @Override
    public void abandon(ActionContext ctx) {
        ctx.getAction().setStatus(ActionStatus.ABANDONED);
        ctx.getCallbacks().onAbandon(ctx.getAction());
    }

    @Override
    public void approve(ActionContext ctx) {
        throw new IllegalStateTransitionException("Cannot approve from REOPENED");
    }

    @Override
    public void reject(ActionContext ctx) {
        throw new IllegalStateTransitionException("Cannot reject from REOPENED");
    }

    @Override
    public void reopen(ActionContext ctx) {
        throw new IllegalStateTransitionException("Cannot reopen from REOPENED");
    }
}
