package com.rpl.domain.state;

import com.rpl.domain.ActionStatus;
import com.rpl.exception.IllegalStateTransitionException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class StateClassesTest {
    @Test
    void proposedImplementTransitionsToInProgress() {
        ProposedState proposedState = new ProposedState();
        Assertions.assertEquals(ActionStatus.IN_PROGRESS, proposedState.implement());
    }

    @Test
    void suspendedResumeTransitionsToProposed() {
        SuspendedState suspendedState = new SuspendedState();
        Assertions.assertEquals(ActionStatus.PROPOSED, suspendedState.resume());
    }

    @Test
    void inProgressCompleteTransitionsToCompleted() {
        InProgressState inProgressState = new InProgressState();
        Assertions.assertEquals(ActionStatus.COMPLETED, inProgressState.complete());
    }

    @Test
    void proposedCompleteThrows() {
        ProposedState proposedState = new ProposedState();
        Assertions.assertThrows(IllegalStateTransitionException.class, proposedState::complete);
    }
}
