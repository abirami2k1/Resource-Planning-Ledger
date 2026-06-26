package com.rpl.manager;

import com.rpl.client.dto.SuspensionResponse;
import com.rpl.domain.ActionStatus;
import com.rpl.domain.ImplementedAction;
import com.rpl.domain.ProposedAction;
import com.rpl.domain.Suspension;
import com.rpl.domain.state.ActionCallbacks;
import com.rpl.domain.state.ActionContext;
import com.rpl.domain.state.ActionState;
import com.rpl.domain.state.AbandonedState;
import com.rpl.domain.state.CompletedState;
import com.rpl.domain.state.InProgressState;
import com.rpl.domain.state.PendingApprovalState;
import com.rpl.domain.state.ProposedState;
import com.rpl.domain.state.ReopenedState;
import com.rpl.domain.state.SuspendedState;
import com.rpl.engine.LedgerEntryEngine;
import com.rpl.exception.NotFoundException;
import com.rpl.resourceaccess.ImplementedActionRepository;
import com.rpl.resourceaccess.ProposedActionRepository;
import com.rpl.resourceaccess.SuspensionRepository;
import java.time.Clock;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class ActionManager implements ActionCallbacks {

    private final ProposedActionRepository proposedActionRepository;
    private final ImplementedActionRepository implementedActionRepository;
    private final SuspensionRepository suspensionRepository;
    private final LedgerEntryEngine ledgerEntryEngine;
    private final AuditLogManager auditLogManager;
    private final Clock clock;
    private final Map<ActionStatus, ActionState> states;

    public ActionManager(
            ProposedActionRepository proposedActionRepository,
            ImplementedActionRepository implementedActionRepository,
            SuspensionRepository suspensionRepository,
            LedgerEntryEngine ledgerEntryEngine,
            AuditLogManager auditLogManager,
            Clock clock,
            ProposedState proposedState,
            SuspendedState suspendedState,
            InProgressState inProgressState,
            CompletedState completedState,
            AbandonedState abandonedState,
            PendingApprovalState pendingApprovalState,
            ReopenedState reopenedState) {
        this.proposedActionRepository = proposedActionRepository;
        this.implementedActionRepository = implementedActionRepository;
        this.suspensionRepository = suspensionRepository;
        this.ledgerEntryEngine = ledgerEntryEngine;
        this.auditLogManager = auditLogManager;
        this.clock = clock;
        this.states = Map.of(
                ActionStatus.PROPOSED,          proposedState,
                ActionStatus.SUSPENDED,         suspendedState,
                ActionStatus.IN_PROGRESS,       inProgressState,
                ActionStatus.COMPLETED,         completedState,
                ActionStatus.ABANDONED,         abandonedState,
                ActionStatus.PENDING_APPROVAL,  pendingApprovalState,
                ActionStatus.REOPENED,          reopenedState
        );
    }

    // ── Public API ──────────────────────────────────────────────────────────

    public ProposedAction transition(Long id, String event) {
        return doTransition(id, event, "");
    }

    public ProposedAction suspendWithReason(Long id, String reason) {
        return doTransition(id, "suspend", reason != null ? reason : "");
    }

    public List<SuspensionResponse> getSuspensions(Long actionId) {
        proposedActionRepository.findById(actionId)
                .orElseThrow(() -> new NotFoundException("Action not found"));
        return suspensionRepository.findByProposedAction_Id(actionId).stream()
                .map(s -> new SuspensionResponse(s.getId(), s.getReason(),
                        s.getStartDate(), s.getEndDate(), s.getDurationMinutes()))
                .toList();
    }

    // ── Dispatch ────────────────────────────────────────────────────────────

    private ProposedAction doTransition(Long id, String event, String suspendReason) {
        ProposedAction action = proposedActionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Action not found: " + id));
        ActionState state = states.get(action.getStatus());
        ActionContext ctx = new ActionContext(action, this);

        switch (event.toLowerCase()) {
            case "implement"          -> state.implement(ctx);
            case "suspend"            -> state.suspend(ctx, suspendReason);
            case "resume"             -> state.resume(ctx);
            case "complete"           -> state.complete(ctx);
            case "abandon"            -> state.abandon(ctx);
            case "submitforapproval"  -> state.submitForApproval(ctx);
            case "approve"            -> state.approve(ctx);
            case "reject"             -> state.reject(ctx);
            case "reopen"             -> state.reopen(ctx);
            default -> throw new IllegalArgumentException("Unknown event: " + event);
        }

        return proposedActionRepository.save(action);
    }

    // ── ActionCallbacks implementation ──────────────────────────────────────

    @Override
    public void onImplement(ProposedAction action) {
        // Note: Week 2 removes the direct implement path; this is kept for backwards compatibility
        // but PendingApprovalState.approve() calls onApprove() which creates ImplementedAction.
        createImplementedAction(action);
        auditLogManager.record("ACTION_IMPLEMENTED", null, null, action.getId());
    }

    @Override
    public void onSuspend(ProposedAction action, String reason) {
        Suspension s = new Suspension();
        s.setProposedAction(action);
        s.setReason(reason != null ? reason : "");
        s.setStartDate(clock.instant());
        suspensionRepository.save(s);
        auditLogManager.record("ACTION_SUSPENDED", null, null, action.getId());
    }

    @Override
    public void onResume(ProposedAction action) {
        suspensionRepository.findOpenByActionId(action.getId()).ifPresent(s -> {
            s.setEndDate(clock.instant());
            suspensionRepository.save(s);
        });
        auditLogManager.record("ACTION_RESUMED", null, null, action.getId());
    }

    @Override
    public void onComplete(ProposedAction action) {
        implementedActionRepository.findByProposedAction_Id(action.getId()).ifPresent(impl -> {
            impl.setStatus(ActionStatus.COMPLETED);
            implementedActionRepository.save(impl);
            ledgerEntryEngine.generate(impl);
        });
        auditLogManager.record("ACTION_COMPLETED", null, null, action.getId());
    }

    @Override
    public void onAbandon(ProposedAction action) {
        suspensionRepository.findOpenByActionId(action.getId()).ifPresent(s -> {
            s.setEndDate(clock.instant());
            suspensionRepository.save(s);
        });
        implementedActionRepository.findByProposedAction_Id(action.getId()).ifPresent(impl -> {
            impl.setStatus(ActionStatus.ABANDONED);
            implementedActionRepository.save(impl);
        });
        auditLogManager.record("ACTION_ABANDONED", null, null, action.getId());
    }

    @Override
    public void onSubmitForApproval(ProposedAction action) {
        auditLogManager.record("ACTION_SUBMITTED_FOR_APPROVAL", null, null, action.getId());
    }

    @Override
    public void onApprove(ProposedAction action) {
        implementedActionRepository.findByProposedAction_Id(action.getId()).ifPresentOrElse(
                impl -> {
                    impl.setStatus(ActionStatus.IN_PROGRESS);
                    implementedActionRepository.save(impl);
                },
                () -> createImplementedAction(action));
        auditLogManager.record("ACTION_APPROVED", null, null, action.getId());
    }

    @Override
    public void onReject(ProposedAction action) {
        auditLogManager.record("ACTION_REJECTED", null, null, action.getId());
    }

    @Override
    public void onReopen(ProposedAction action) {
        implementedActionRepository.findByProposedAction_Id(action.getId()).ifPresent(impl -> {
            impl.setStatus(ActionStatus.REOPENED);
            implementedActionRepository.save(impl);
            ledgerEntryEngine.generateReversal(impl);
        });
        auditLogManager.record("ACTION_REOPENED", null, null, action.getId());
    }

    // ── Helpers ─────────────────────────────────────────────────────────────

    private void createImplementedAction(ProposedAction action) {
        ImplementedAction impl = new ImplementedAction();
        impl.setProposedAction(action);
        impl.setActualStart(clock.instant());
        impl.setActualParty(action.getParty());
        impl.setActualLocation(action.getLocation());
        impl.setStatus(ActionStatus.IN_PROGRESS);
        implementedActionRepository.save(impl);
    }
}
