package com.rpl.domain.state;

import com.rpl.domain.ActionStatus;

public interface ActionState {
    String name();

    ActionStatus implement();
    ActionStatus suspend();
    ActionStatus resume();
    ActionStatus complete();
    ActionStatus abandon();
}
