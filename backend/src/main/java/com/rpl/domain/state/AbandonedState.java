package com.rpl.domain.state;

import com.rpl.exception.IllegalStateTransitionException;
import org.springframework.stereotype.Component;

@Component
public class AbandonedState implements ActionState {

    @Override
    public String name() { return "ABANDONED"; }

    @Override
    public void implement(ActionContext ctx) {
        throw new IllegalStateTransitionException("Cannot implement from ABANDONED");
    }

    @Override
    public void submitForApproval(ActionContext ctx) {
        throw new IllegalStateTransitionException("Cannot submit for approval from ABANDONED");
    }

    @Override
    public void suspend(ActionContext ctx, String reason) {
        throw new IllegalStateTransitionException("Cannot suspend from ABANDONED");
    }

    @Override
    public void resume(ActionContext ctx) {
        throw new IllegalStateTransitionException("Cannot resume from ABANDONED");
    }

    @Override
    public void complete(ActionContext ctx) {
        throw new IllegalStateTransitionException("Cannot complete from ABANDONED");
    }

    @Override
    public void abandon(ActionContext ctx) {
        throw new IllegalStateTransitionException("Cannot abandon from ABANDONED");
    }

    @Override
    public void approve(ActionContext ctx) {
        throw new IllegalStateTransitionException("Cannot approve from ABANDONED");
    }

    @Override
    public void reject(ActionContext ctx) {
        throw new IllegalStateTransitionException("Cannot reject from ABANDONED");
    }

    @Override
    public void reopen(ActionContext ctx) {
        throw new IllegalStateTransitionException("Cannot reopen from ABANDONED");
    }
}
