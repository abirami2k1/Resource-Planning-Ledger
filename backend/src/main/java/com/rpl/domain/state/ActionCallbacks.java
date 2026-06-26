package com.rpl.domain.state;

import com.rpl.domain.ProposedAction;

/**
 * Callback interface implemented by ActionManager.
 * States call these methods to trigger side effects (ledger entries,
 * suspension records, audit log) without holding a direct Spring bean reference.
 */
public interface ActionCallbacks {
    void onImplement(ProposedAction action);
    void onSuspend(ProposedAction action, String reason);
    void onResume(ProposedAction action);
    void onComplete(ProposedAction action);
    void onAbandon(ProposedAction action);
    void onSubmitForApproval(ProposedAction action);
    void onApprove(ProposedAction action);
    void onReject(ProposedAction action);
    void onReopen(ProposedAction action);
}
