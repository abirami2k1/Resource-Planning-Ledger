package com.rpl.domain.state;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Verifies state name() contracts.
 * Full transition coverage is in StateClassesTest.
 */
class ActionStateNameTest {

    @Test
    void proposedState_name_isProposed() {
        assertEquals("PROPOSED", new ProposedState().name());
    }

    @Test
    void suspendedState_name_isSuspended() {
        assertEquals("SUSPENDED", new SuspendedState().name());
    }

    @Test
    void inProgressState_name_isInProgress() {
        assertEquals("IN_PROGRESS", new InProgressState().name());
    }

    @Test
    void completedState_name_isCompleted() {
        assertEquals("COMPLETED", new CompletedState().name());
    }

    @Test
    void abandonedState_name_isAbandoned() {
        assertEquals("ABANDONED", new AbandonedState().name());
    }

    @Test
    void pendingApprovalState_name_isPendingApproval() {
        assertEquals("PENDING_APPROVAL", new PendingApprovalState().name());
    }

    @Test
    void reopenedState_name_isReopened() {
        assertEquals("REOPENED", new ReopenedState().name());
    }
}
