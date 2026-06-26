package com.rpl.domain.state;

import com.rpl.domain.ActionStatus;
import com.rpl.exception.IllegalStateTransitionException;
import org.springframework.stereotype.Component;

@Component
public class ProposedState implements ActionState {

    @Override
    public String name() { return "PROPOSED"; }

    @Override
    public void implement(ActionContext ctx) {
        throw new IllegalStateTransitionException(
                "Cannot implement directly from PROPOSED. Use submitForApproval() first.");
    }

    @Override
    public void submitForApproval(ActionContext ctx) {
        ctx.getAction().setStatus(ActionStatus.PENDING_APPROVAL);
        ctx.getCallbacks().onSubmitForApproval(ctx.getAction());
    }

    @Override
    public void suspend(ActionContext ctx, String reason) {
        ctx.getAction().setStatus(ActionStatus.SUSPENDED);
        ctx.getCallbacks().onSuspend(ctx.getAction(), reason);
    }

    @Override
    public void resume(ActionContext ctx) {
        throw new IllegalStateTransitionException("Cannot resume from PROPOSED");
    }

    @Override
    public void complete(ActionContext ctx) {
        throw new IllegalStateTransitionException("Cannot complete from PROPOSED");
    }

    @Override
    public void abandon(ActionContext ctx) {
        ctx.getAction().setStatus(ActionStatus.ABANDONED);
        ctx.getCallbacks().onAbandon(ctx.getAction());
    }

    @Override
    public void approve(ActionContext ctx) {
        throw new IllegalStateTransitionException("Cannot approve from PROPOSED");
    }

    @Override
    public void reject(ActionContext ctx) {
        throw new IllegalStateTransitionException("Cannot reject from PROPOSED");
    }

    @Override
    public void reopen(ActionContext ctx) {
        throw new IllegalStateTransitionException("Cannot reopen from PROPOSED");
    }
}
