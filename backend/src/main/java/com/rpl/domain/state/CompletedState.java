package com.rpl.domain.state;

import com.rpl.domain.ActionStatus;
import com.rpl.exception.IllegalStateTransitionException;
import org.springframework.stereotype.Component;

@Component
public class CompletedState implements ActionState {

    @Override
    public String name() { return "COMPLETED"; }

    @Override
    public void implement(ActionContext ctx) {
        throw new IllegalStateTransitionException("Cannot implement from COMPLETED");
    }

    @Override
    public void submitForApproval(ActionContext ctx) {
        throw new IllegalStateTransitionException("Cannot submit for approval from COMPLETED");
    }

    @Override
    public void suspend(ActionContext ctx, String reason) {
        throw new IllegalStateTransitionException("Cannot suspend from COMPLETED");
    }

    @Override
    public void resume(ActionContext ctx) {
        throw new IllegalStateTransitionException("Cannot resume from COMPLETED");
    }

    @Override
    public void complete(ActionContext ctx) {
        throw new IllegalStateTransitionException("Cannot complete from COMPLETED");
    }

    @Override
    public void abandon(ActionContext ctx) {
        throw new IllegalStateTransitionException("Cannot abandon from COMPLETED");
    }

    @Override
    public void approve(ActionContext ctx) {
        throw new IllegalStateTransitionException("Cannot approve from COMPLETED");
    }

    @Override
    public void reject(ActionContext ctx) {
        throw new IllegalStateTransitionException("Cannot reject from COMPLETED");
    }

    @Override
    public void reopen(ActionContext ctx) {
        ctx.getAction().setStatus(ActionStatus.REOPENED);
        ctx.getCallbacks().onReopen(ctx.getAction());
    }
}
