package com.rpl.manager;

import com.rpl.domain.ActionStatus;
import com.rpl.domain.ImplementedAction;
import com.rpl.domain.ProposedAction;
import com.rpl.domain.state.AbandonedState;
import com.rpl.domain.state.ActionState;
import com.rpl.domain.state.CompletedState;
import com.rpl.domain.state.InProgressState;
import com.rpl.domain.state.ProposedState;
import com.rpl.domain.state.SuspendedState;
import com.rpl.engine.LedgerEntryEngine;
import com.rpl.exception.NotFoundException;
import com.rpl.resourceaccess.ImplementedActionRepository;
import com.rpl.resourceaccess.ProposedActionRepository;
import java.time.Instant;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class ActionManager {
    private final ProposedActionRepository proposedActionRepository;
    private final ImplementedActionRepository implementedActionRepository;
    private final LedgerEntryEngine ledgerEntryEngine;
    private final Map<ActionStatus, ActionState> states;

    public ActionManager(
            ProposedActionRepository proposedActionRepository,
            ImplementedActionRepository implementedActionRepository,
            LedgerEntryEngine ledgerEntryEngine,
            ProposedState proposedState,
            SuspendedState suspendedState,
            InProgressState inProgressState,
            CompletedState completedState,
            AbandonedState abandonedState) {
        this.proposedActionRepository = proposedActionRepository;
        this.implementedActionRepository = implementedActionRepository;
        this.ledgerEntryEngine = ledgerEntryEngine;
        this.states = Map.of(
                ActionStatus.PROPOSED, proposedState,
                ActionStatus.SUSPENDED, suspendedState,
                ActionStatus.IN_PROGRESS, inProgressState,
                ActionStatus.COMPLETED, completedState,
                ActionStatus.ABANDONED, abandonedState
        );
    }

    public ProposedAction transition(Long id, String event) {
        ProposedAction action = proposedActionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Action not found"));
        ActionState state = states.get(action.getStatus());
        ActionStatus next;
        next = switch (event.toLowerCase()) {
            case "implement" -> state.implement();
            case "suspend" -> state.suspend();
            case "resume" -> state.resume();
            case "complete" -> state.complete();
            case "abandon" -> state.abandon();
            default -> throw new IllegalArgumentException("Unknown event: " + event);
        };
        action.setStatus(next);
        ProposedAction saved = proposedActionRepository.save(action);
        if ("implement".equalsIgnoreCase(event)) {
            ImplementedAction implementedAction = new ImplementedAction();
            implementedAction.setProposedAction(saved);
            implementedAction.setActualStart(Instant.now());
            implementedAction.setActualParty(saved.getParty());
            implementedAction.setActualLocation(saved.getLocation());
            implementedActionRepository.save(implementedAction);
        }
        if ("complete".equalsIgnoreCase(event)) {
            implementedActionRepository.findAll().stream()
                    .filter(a -> a.getProposedAction().getId().equals(saved.getId()))
                    .findFirst()
                    .ifPresent(ledgerEntryEngine::generate);
        }
        return saved;
    }
}
