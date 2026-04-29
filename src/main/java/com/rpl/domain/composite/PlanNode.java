package com.rpl.domain.composite;

import com.rpl.domain.ActionStatus;

public interface PlanNode {
    String getName();
    ActionStatus getStatus();
    Long getId();
}
