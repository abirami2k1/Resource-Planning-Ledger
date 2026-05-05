package com.rpl.domain.state;

import com.rpl.domain.ActionStatus;
import com.rpl.exception.IllegalStateTransitionException;
import org.springframework.stereotype.Component;

@Component
public class SuspendedState implements ActionState {
    @Override
    public String name() {
        return "SUSPENDED";
    }

    @Override
    public ActionStatus implement() { throw new IllegalStateTransitionException("Cannot implement from SUSPENDED"); }
    @Override
    public ActionStatus suspend() { throw new IllegalStateTransitionException("Cannot suspend from SUSPENDED"); }
    @Override
    public ActionStatus resume() { return ActionStatus.PROPOSED; }
    @Override
    public ActionStatus complete() { throw new IllegalStateTransitionException("Cannot complete from SUSPENDED"); }
    @Override
    public ActionStatus abandon() { return ActionStatus.ABANDONED; }
}
