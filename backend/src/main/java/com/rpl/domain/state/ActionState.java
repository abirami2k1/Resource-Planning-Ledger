package com.rpl.domain.state;

/**
 * State pattern interface for ProposedAction lifecycle.
 * Each concrete state is a stateless Spring singleton bean.
 * All mutable data lives in ActionContext (wrapping the JPA entity).
 * Unsupported transitions throw IllegalStateTransitionException.
 */
public interface ActionState {
    String name();

    void implement(ActionContext ctx);

    void suspend(ActionContext ctx, String reason);

    void resume(ActionContext ctx);

    void complete(ActionContext ctx);

    void abandon(ActionContext ctx);

    // Week 2 events
    void submitForApproval(ActionContext ctx);

    void approve(ActionContext ctx);

    void reject(ActionContext ctx);

    void reopen(ActionContext ctx);
}
