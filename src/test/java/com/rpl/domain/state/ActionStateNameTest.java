package com.rpl.domain.state;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ActionStateNameTest {

    @Test
    void proposedState_name_returnsProposed() {
        Assertions.assertEquals("PROPOSED", new ProposedState().name());
    }

    @Test
    void suspendedState_name_returnsSuspended() {
        Assertions.assertEquals("SUSPENDED", new SuspendedState().name());
    }

    @Test
    void inProgressState_name_returnsInProgress() {
        Assertions.assertEquals("IN_PROGRESS", new InProgressState().name());
    }

    @Test
    void completedState_name_returnsCompleted() {
        Assertions.assertEquals("COMPLETED", new CompletedState().name());
    }

    @Test
    void abandonedState_name_returnsAbandoned() {
        Assertions.assertEquals("ABANDONED", new AbandonedState().name());
    }
}
