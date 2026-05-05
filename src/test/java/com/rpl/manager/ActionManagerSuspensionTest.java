package com.rpl.manager;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.rpl.domain.ActionStatus;
import com.rpl.domain.ProposedAction;
import com.rpl.domain.Suspension;
import com.rpl.domain.state.AbandonedState;
import com.rpl.domain.state.CompletedState;
import com.rpl.domain.state.InProgressState;
import com.rpl.domain.state.ProposedState;
import com.rpl.domain.state.SuspendedState;
import com.rpl.engine.LedgerEntryEngine;
import com.rpl.resourceaccess.ImplementedActionRepository;
import com.rpl.resourceaccess.ProposedActionRepository;
import com.rpl.resourceaccess.SuspensionRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ActionManagerSuspensionTest {

    private static final Instant T0 = Instant.parse("2026-05-02T08:00:00Z");
    @Mock
    private ProposedActionRepository proposedActionRepository;

    @Mock
    private ImplementedActionRepository implementedActionRepository;

    @Mock
    private SuspensionRepository suspensionRepository;

    @Mock
    private LedgerEntryEngine ledgerEntryEngine;

    @Mock
    private AuditLogManager auditLogManager;

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
                new AbandonedState());
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
        ArgumentCaptor<Suspension> captor = ArgumentCaptor.forClass(Suspension.class);
        verify(suspensionRepository).save(captor.capture());
        Suspension s = captor.getValue();
        assertEquals("weather", s.getReason());
        assertEquals(T0, s.getStartDate());
        assertNull(s.getEndDate());
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
        verify(suspensionRepository).save(open);
    }

    @Test
    void twoSuspendResumeCycles_createTwoSeparateSuspensionRecords() {
        // Arrange
        ProposedAction pa = action(4L, ActionStatus.IN_PROGRESS);
        when(proposedActionRepository.findById(4L)).thenReturn(Optional.of(pa));
        when(proposedActionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        // Act — suspend → resume → implement → suspend again
        actionManager.suspendWithReason(4L, "cycle-one");

        ArgumentCaptor<Suspension> firstCap = ArgumentCaptor.forClass(Suspension.class);
        verify(suspensionRepository).save(firstCap.capture());
        Suspension firstSuspension = firstCap.getValue();

        when(suspensionRepository.findOpenByActionId(4L)).thenReturn(Optional.of(firstSuspension));

        actionManager.transition(4L, "resume");
        actionManager.transition(4L, "implement");
        actionManager.suspendWithReason(4L, "cycle-two");

        // Assert — three saves: create first, close first, create second
        ArgumentCaptor<Suspension> allCap = ArgumentCaptor.forClass(Suspension.class);
        verify(suspensionRepository, times(3)).save(allCap.capture());
        List<Suspension> saved = allCap.getAllValues();
        assertEquals("cycle-one", saved.get(0).getReason());
        assertEquals(T0, saved.get(1).getEndDate());
        assertEquals("cycle-two", saved.get(2).getReason());
    }

    private static ProposedAction action(long id, ActionStatus status) {
        ProposedAction pa = new ProposedAction();
        pa.setId(id);
        pa.setStatus(status);
        return pa;
    }
}
