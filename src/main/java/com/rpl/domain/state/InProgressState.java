package com.rpl.domain.state;

import com.rpl.domain.ActionStatus;
import com.rpl.exception.IllegalStateTransitionException;
import org.springframework.stereotype.Component;

@Component
public class InProgressState implements ActionState {
    @Override
    public ActionStatus implement() { throw new IllegalStateTransitionException("Cannot implement from IN_PROGRESS"); }
    @Override
    public ActionStatus suspend() { return ActionStatus.SUSPENDED; }
    @Override
    public ActionStatus resume() { throw new IllegalStateTransitionException("Cannot resume from IN_PROGRESS"); }
    @Override
    public ActionStatus complete() { return ActionStatus.COMPLETED; }
    @Override
    public ActionStatus abandon() { return ActionStatus.ABANDONED; }
}
