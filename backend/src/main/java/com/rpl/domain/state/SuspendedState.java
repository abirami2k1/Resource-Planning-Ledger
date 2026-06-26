package com.rpl.domain.state;

import com.rpl.domain.ActionStatus;
import com.rpl.exception.IllegalStateTransitionException;
import org.springframework.stereotype.Component;

@Component
public class SuspendedState implements ActionState {

    @Override
    public String name() { return "SUSPENDED"; }

    @Override
    public void implement(ActionContext ctx) {
        throw new IllegalStateTransitionException("Cannot implement from SUSPENDED");
    }

    @Override
    public void submitForApproval(ActionContext ctx) {
        throw new IllegalStateTransitionException("Cannot submit for approval from SUSPENDED");
    }

    @Override
    public void suspend(ActionContext ctx, String reason) {
        throw new IllegalStateTransitionException("Cannot suspend from SUSPENDED");
    }

    @Override
    public void resume(ActionContext ctx) {
        ctx.getAction().setStatus(ActionStatus.PROPOSED);
        ctx.getCallbacks().onResume(ctx.getAction());
    }

    @Override
    public void complete(ActionContext ctx) {
        throw new IllegalStateTransitionException("Cannot complete from SUSPENDED");
    }

    @Override
    public void abandon(ActionContext ctx) {
        ctx.getAction().setStatus(ActionStatus.ABANDONED);
        ctx.getCallbacks().onAbandon(ctx.getAction());
    }

    @Override
    public void approve(ActionContext ctx) {
        throw new IllegalStateTransitionException("Cannot approve from SUSPENDED");
    }

    @Override
    public void reject(ActionContext ctx) {
        throw new IllegalStateTransitionException("Cannot reject from SUSPENDED");
    }

    @Override
    public void reopen(ActionContext ctx) {
        throw new IllegalStateTransitionException("Cannot reopen from SUSPENDED");
    }
}
