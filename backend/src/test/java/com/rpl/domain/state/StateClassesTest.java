package com.rpl.domain.state;

import com.rpl.domain.ActionStatus;
import com.rpl.domain.ProposedAction;
import com.rpl.exception.IllegalStateTransitionException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for all ActionState implementations.
 * Covers every legal and illegal transition per the FSM spec.
 * No @SpringBootTest — state objects are stateless and need no Spring context.
 */
@ExtendWith(MockitoExtension.class)
class StateClassesTest {

    @Mock ActionCallbacks callbacks;

    ProposedState proposedState = new ProposedState();
    SuspendedState suspendedState = new SuspendedState();
    InProgressState inProgressState = new InProgressState();
    CompletedState completedState = new CompletedState();
    AbandonedState abandonedState = new AbandonedState();
    PendingApprovalState pendingApprovalState = new PendingApprovalState();
    ReopenedState reopenedState = new ReopenedState();

    private ActionContext ctx(ActionStatus status) {
        ProposedAction action = new ProposedAction();
        action.setStatus(status);
        return new ActionContext(action, callbacks);
    }

    // ── PROPOSED ────────────────────────────────────────────────────────────

    @Test
    void proposed_implement_throwsIllegalStateTransition() {
        assertThrows(IllegalStateTransitionException.class,
                () -> proposedState.implement(ctx(ActionStatus.PROPOSED)));
    }

    @Test
    void proposed_submitForApproval_setsPendingApproval() {
        var c = ctx(ActionStatus.PROPOSED);
        proposedState.submitForApproval(c);
        assertEquals(ActionStatus.PENDING_APPROVAL, c.getAction().getStatus());
    }

    @Test
    void proposed_suspend_setsSuspended() {
        var c = ctx(ActionStatus.PROPOSED);
        proposedState.suspend(c, "blocked");
        assertEquals(ActionStatus.SUSPENDED, c.getAction().getStatus());
    }

    @Test
    void proposed_abandon_setsAbandoned() {
        var c = ctx(ActionStatus.PROPOSED);
        proposedState.abandon(c);
        assertEquals(ActionStatus.ABANDONED, c.getAction().getStatus());
    }

    @Test
    void proposed_resume_throwsIllegalStateTransition() {
        assertThrows(IllegalStateTransitionException.class,
                () -> proposedState.resume(ctx(ActionStatus.PROPOSED)));
    }

    @Test
    void proposed_complete_throwsIllegalStateTransition() {
        assertThrows(IllegalStateTransitionException.class,
                () -> proposedState.complete(ctx(ActionStatus.PROPOSED)));
    }

    // ── PENDING_APPROVAL ────────────────────────────────────────────────────

    @Test
    void pendingApproval_approve_setsInProgress() {
        var c = ctx(ActionStatus.PENDING_APPROVAL);
        pendingApprovalState.approve(c);
        assertEquals(ActionStatus.IN_PROGRESS, c.getAction().getStatus());
    }

    @Test
    void pendingApproval_reject_setsProposed() {
        var c = ctx(ActionStatus.PENDING_APPROVAL);
        pendingApprovalState.reject(c);
        assertEquals(ActionStatus.PROPOSED, c.getAction().getStatus());
    }

    @Test
    void pendingApproval_implement_throwsIllegalStateTransition() {
        assertThrows(IllegalStateTransitionException.class,
                () -> pendingApprovalState.implement(ctx(ActionStatus.PENDING_APPROVAL)));
    }

    @Test
    void pendingApproval_abandon_throwsIllegalStateTransition() {
        assertThrows(IllegalStateTransitionException.class,
                () -> pendingApprovalState.abandon(ctx(ActionStatus.PENDING_APPROVAL)));
    }

    // ── IN_PROGRESS ─────────────────────────────────────────────────────────

    @Test
    void inProgress_complete_setsCompleted() {
        var c = ctx(ActionStatus.IN_PROGRESS);
        inProgressState.complete(c);
        assertEquals(ActionStatus.COMPLETED, c.getAction().getStatus());
    }

    @Test
    void inProgress_suspend_setsSuspended() {
        var c = ctx(ActionStatus.IN_PROGRESS);
        inProgressState.suspend(c, "resource gap");
        assertEquals(ActionStatus.SUSPENDED, c.getAction().getStatus());
    }

    @Test
    void inProgress_abandon_setsAbandoned() {
        var c = ctx(ActionStatus.IN_PROGRESS);
        inProgressState.abandon(c);
        assertEquals(ActionStatus.ABANDONED, c.getAction().getStatus());
    }

    @Test
    void inProgress_implement_throwsIllegalStateTransition() {
        assertThrows(IllegalStateTransitionException.class,
                () -> inProgressState.implement(ctx(ActionStatus.IN_PROGRESS)));
    }

    // ── SUSPENDED ───────────────────────────────────────────────────────────

    @Test
    void suspended_resume_setsProposed() {
        var c = ctx(ActionStatus.SUSPENDED);
        suspendedState.resume(c);
        assertEquals(ActionStatus.PROPOSED, c.getAction().getStatus());
    }

    @Test
    void suspended_abandon_setsAbandoned() {
        var c = ctx(ActionStatus.SUSPENDED);
        suspendedState.abandon(c);
        assertEquals(ActionStatus.ABANDONED, c.getAction().getStatus());
    }

    @Test
    void suspended_complete_throwsIllegalStateTransition() {
        assertThrows(IllegalStateTransitionException.class,
                () -> suspendedState.complete(ctx(ActionStatus.SUSPENDED)));
    }

    // ── COMPLETED ───────────────────────────────────────────────────────────

    @Test
    void completed_reopen_setsReopened() {
        var c = ctx(ActionStatus.COMPLETED);
        completedState.reopen(c);
        assertEquals(ActionStatus.REOPENED, c.getAction().getStatus());
    }

    @Test
    void completed_abandon_throwsIllegalStateTransition() {
        assertThrows(IllegalStateTransitionException.class,
                () -> completedState.abandon(ctx(ActionStatus.COMPLETED)));
    }

    // ── REOPENED ────────────────────────────────────────────────────────────

    @Test
    void reopened_complete_setsCompleted() {
        var c = ctx(ActionStatus.REOPENED);
        reopenedState.complete(c);
        assertEquals(ActionStatus.COMPLETED, c.getAction().getStatus());
    }

    @Test
    void reopened_abandon_setsAbandoned() {
        var c = ctx(ActionStatus.REOPENED);
        reopenedState.abandon(c);
        assertEquals(ActionStatus.ABANDONED, c.getAction().getStatus());
    }

    @Test
    void reopened_implement_throwsIllegalStateTransition() {
        assertThrows(IllegalStateTransitionException.class,
                () -> reopenedState.implement(ctx(ActionStatus.REOPENED)));
    }

    // ── ABANDONED ───────────────────────────────────────────────────────────

    @Test
    void abandoned_allTransitions_throw() {
        var c = ctx(ActionStatus.ABANDONED);
        assertThrows(IllegalStateTransitionException.class, () -> abandonedState.implement(c));
        assertThrows(IllegalStateTransitionException.class, () -> abandonedState.complete(c));
        assertThrows(IllegalStateTransitionException.class, () -> abandonedState.resume(c));
        assertThrows(IllegalStateTransitionException.class, () -> abandonedState.reopen(c));
    }

    // ── State names ─────────────────────────────────────────────────────────

    @Test
    void stateNames_matchExpectedStrings() {
        assertEquals("PROPOSED",          proposedState.name());
        assertEquals("SUSPENDED",         suspendedState.name());
        assertEquals("IN_PROGRESS",       inProgressState.name());
        assertEquals("COMPLETED",         completedState.name());
        assertEquals("ABANDONED",         abandonedState.name());
        assertEquals("PENDING_APPROVAL",  pendingApprovalState.name());
        assertEquals("REOPENED",          reopenedState.name());
    }
}
