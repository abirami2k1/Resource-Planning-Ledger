package com.rpl.domain.state;

import com.rpl.domain.ActionStatus;
import com.rpl.exception.IllegalStateTransitionException;
import org.springframework.stereotype.Component;

/**
 * PENDING_APPROVAL state (Week 2).
 * Entered from PROPOSED via submitForApproval().
 * Only approve() and reject() are legal.
 */
@Component
public class PendingApprovalState implements ActionState {

    @Override
    public String name() { return "PENDING_APPROVAL"; }

    @Override
    public void implement(ActionContext ctx) {
        throw new IllegalStateTransitionException("Cannot implement from PENDING_APPROVAL");
    }

    @Override
    public void submitForApproval(ActionContext ctx) {
        throw new IllegalStateTransitionException("Already pending approval");
    }

    @Override
    public void suspend(ActionContext ctx, String reason) {
        throw new IllegalStateTransitionException("Cannot suspend from PENDING_APPROVAL");
    }

    @Override
    public void resume(ActionContext ctx) {
        throw new IllegalStateTransitionException("Cannot resume from PENDING_APPROVAL");
    }

    @Override
    public void complete(ActionContext ctx) {
        throw new IllegalStateTransitionException("Cannot complete from PENDING_APPROVAL");
    }

    @Override
    public void abandon(ActionContext ctx) {
        throw new IllegalStateTransitionException("Cannot abandon from PENDING_APPROVAL");
    }

    @Override
    public void approve(ActionContext ctx) {
        ctx.getAction().setStatus(ActionStatus.IN_PROGRESS);
        ctx.getCallbacks().onApprove(ctx.getAction());
    }

    @Override
    public void reject(ActionContext ctx) {
        ctx.getAction().setStatus(ActionStatus.PROPOSED);
        ctx.getCallbacks().onReject(ctx.getAction());
    }

    @Override
    public void reopen(ActionContext ctx) {
        throw new IllegalStateTransitionException("Cannot reopen from PENDING_APPROVAL");
    }
}
