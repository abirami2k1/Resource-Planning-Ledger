package com.rpl.domain.state;

import com.rpl.domain.ActionStatus;
import com.rpl.exception.IllegalStateTransitionException;
import org.springframework.stereotype.Component;

@Component
public class InProgressState implements ActionState {

    @Override
    public String name() { return "IN_PROGRESS"; }

    @Override
    public void implement(ActionContext ctx) {
        throw new IllegalStateTransitionException("Cannot implement from IN_PROGRESS");
    }

    @Override
    public void submitForApproval(ActionContext ctx) {
        throw new IllegalStateTransitionException("Cannot submit for approval from IN_PROGRESS");
    }

    @Override
    public void suspend(ActionContext ctx, String reason) {
        ctx.getAction().setStatus(ActionStatus.SUSPENDED);
        ctx.getCallbacks().onSuspend(ctx.getAction(), reason);
    }

    @Override
    public void resume(ActionContext ctx) {
        throw new IllegalStateTransitionException("Cannot resume from IN_PROGRESS");
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
        throw new IllegalStateTransitionException("Cannot approve from IN_PROGRESS");
    }

    @Override
    public void reject(ActionContext ctx) {
        throw new IllegalStateTransitionException("Cannot reject from IN_PROGRESS");
    }

    @Override
    public void reopen(ActionContext ctx) {
        throw new IllegalStateTransitionException("Cannot reopen from IN_PROGRESS");
    }
}
