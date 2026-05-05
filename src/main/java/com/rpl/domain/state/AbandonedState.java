package com.rpl.domain.state;

import com.rpl.domain.ActionStatus;
import com.rpl.exception.IllegalStateTransitionException;
import org.springframework.stereotype.Component;

@Component
public class AbandonedState implements ActionState {
    @Override
    public String name() {
        return "ABANDONED";
    }

    @Override
    public ActionStatus implement() { throw new IllegalStateTransitionException("Cannot implement from ABANDONED"); }
    @Override
    public ActionStatus suspend() { throw new IllegalStateTransitionException("Cannot suspend from ABANDONED"); }
    @Override
    public ActionStatus resume() { throw new IllegalStateTransitionException("Cannot resume from ABANDONED"); }
    @Override
    public ActionStatus complete() { throw new IllegalStateTransitionException("Cannot complete from ABANDONED"); }
    @Override
    public ActionStatus abandon() { throw new IllegalStateTransitionException("Cannot abandon from ABANDONED"); }
}
