package com.rpl.domain.composite;

import com.rpl.domain.ActionStatus;
import com.rpl.domain.ResourceType;
import java.math.BigDecimal;

public interface PlanNode {
    String getName();

    ActionStatus getStatus();

    Long getId();

    BigDecimal getTotalAllocatedQuantity(ResourceType rt);

    void accept(PlanNodeVisitor v);
}
