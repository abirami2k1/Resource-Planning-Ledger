package com.rpl.manager;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.rpl.domain.ActionStatus;
import com.rpl.domain.ProposedAction;
import com.rpl.domain.Suspension;
import com.rpl.domain.state.*;
import com.rpl.engine.LedgerEntryEngine;
import com.rpl.resourceaccess.*;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ActionManagerSuspensionTest {

    private static final Instant T0 = Instant.parse("2026-05-02T08:00:00Z");

    @Mock ProposedActionRepository proposedActionRepository;
    @Mock ImplementedActionRepository implementedActionRepository;
    @Mock SuspensionRepository suspensionRepository;
    @Mock LedgerEntryEngine ledgerEntryEngine;
    @Mock AuditLogManager auditLogManager;

    private Clock clock;
    private ActionManager actionManager;

    @BeforeEach
    void setUp() {
        clock = Clock.fixed(T0, ZoneOffset.UTC);
        actionManager = new ActionManager(
                proposedActionRepository,
                implementedActionRepository,
                suspensionRepository,
                ledgerEntryEngine,
                auditLogManager,
                clock,
                new ProposedState(),
                new SuspendedState(),
                new InProgressState(),
                new CompletedState(),
                new AbandonedState(),
                new PendingApprovalState(),
                new ReopenedState());
    }

    @Test
    void suspendAction_createsSuspensionWithStartDateAndNullEndDate() {
        // Arrange
        ProposedAction pa = action(1L, ActionStatus.IN_PROGRESS);
        when(proposedActionRepository.findById(1L)).thenReturn(Optional.of(pa));
        when(proposedActionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        // Act
        actionManager.suspendWithReason(1L, "weather");

        // Assert
        ArgumentCaptor<Suspension> cap = ArgumentCaptor.forClass(Suspension.class);
        verify(suspensionRepository).save(cap.capture());
        assertEquals("weather", cap.getValue().getReason());
        assertEquals(T0, cap.getValue().getStartDate());
        assertNull(cap.getValue().getEndDate());
    }

    @Test
    void resumeAction_closesOpenSuspensionWithEndDate() {
        // Arrange
        ProposedAction pa = action(2L, ActionStatus.SUSPENDED);
        Suspension open = new Suspension();
        open.setStartDate(T0.minusSeconds(3600));
        when(proposedActionRepository.findById(2L)).thenReturn(Optional.of(pa));
        when(proposedActionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(suspensionRepository.findOpenByActionId(2L)).thenReturn(Optional.of(open));

        // Act
        actionManager.transition(2L, "resume");

        // Assert
        assertEquals(T0, open.getEndDate());
        verify(suspensionRepository).save(open);
    }

    @Test
    void abandonFromSuspended_closesOpenSuspension() {
        // Arrange
        ProposedAction pa = action(3L, ActionStatus.SUSPENDED);
        Suspension open = new Suspension();
        when(proposedActionRepository.findById(3L)).thenReturn(Optional.of(pa));
        when(proposedActionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(suspensionRepository.findOpenByActionId(3L)).thenReturn(Optional.of(open));

        // Act
        actionManager.transition(3L, "abandon");

        // Assert
        assertEquals(T0, open.getEndDate());
    }

    @Test
    void submitForApproval_setsStatusToPendingApproval() {
        // Arrange
        ProposedAction pa = action(5L, ActionStatus.PROPOSED);
        when(proposedActionRepository.findById(5L)).thenReturn(Optional.of(pa));
        when(proposedActionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        // Act
        ProposedAction result = actionManager.transition(5L, "submitforapproval");

        // Assert
        assertEquals(ActionStatus.PENDING_APPROVAL, result.getStatus());
    }

    @Test
    void approve_setsStatusToInProgress() {
        // Arrange
        ProposedAction pa = action(6L, ActionStatus.PENDING_APPROVAL);
        when(proposedActionRepository.findById(6L)).thenReturn(Optional.of(pa));
        when(proposedActionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(implementedActionRepository.findByProposedAction_Id(6L)).thenReturn(Optional.empty());

        // Act
        ProposedAction result = actionManager.transition(6L, "approve");

        // Assert
        assertEquals(ActionStatus.IN_PROGRESS, result.getStatus());
    }

    @Test
    void reject_setsStatusBackToProposed() {
        // Arrange
        ProposedAction pa = action(7L, ActionStatus.PENDING_APPROVAL);
        when(proposedActionRepository.findById(7L)).thenReturn(Optional.of(pa));
        when(proposedActionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        // Act
        ProposedAction result = actionManager.transition(7L, "reject");

        // Assert
        assertEquals(ActionStatus.PROPOSED, result.getStatus());
    }

    private static ProposedAction action(long id, ActionStatus status) {
        ProposedAction pa = new ProposedAction();
        pa.setId(id);
        pa.setStatus(status);
        return pa;
    }
}
