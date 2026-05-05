package com.rpl.domain.state;

import com.rpl.domain.ActionStatus;
import com.rpl.exception.IllegalStateTransitionException;
import org.springframework.stereotype.Component;

@Component
public class ProposedState implements ActionState {
    @Override
    public String name() {
        return "PROPOSED";
    }

    @Override
    public ActionStatus implement() { return ActionStatus.IN_PROGRESS; }
    @Override
    public ActionStatus suspend() { return ActionStatus.SUSPENDED; }
    @Override
    public ActionStatus resume() { throw new IllegalStateTransitionException("Cannot resume from PROPOSED"); }
    @Override
    public ActionStatus complete() { throw new IllegalStateTransitionException("Cannot complete from PROPOSED"); }
    @Override
    public ActionStatus abandon() { return ActionStatus.ABANDONED; }
}
