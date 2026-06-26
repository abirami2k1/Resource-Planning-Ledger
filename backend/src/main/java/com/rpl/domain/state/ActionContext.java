package com.rpl.domain.state;

import com.rpl.domain.ProposedAction;

/**
 * Mutable context passed to every ActionState method.
 * Holds the entity being transitioned and a callback handle to ActionManager
 * so states can trigger side effects without a Spring bean dependency.
 */
public class ActionContext {
    private final ProposedAction action;
    private final ActionCallbacks callbacks;

    public ActionContext(ProposedAction action, ActionCallbacks callbacks) {
        this.action = action;
        this.callbacks = callbacks;
    }

    public ProposedAction getAction() {
        return action;
    }

    public ActionCallbacks getCallbacks() {
        return callbacks;
    }
}
