package com.rpl.client.dto;

import com.rpl.domain.AllocationKind;

public record AllocationRequest(
        Long resourceTypeId,
        double quantity,
        AllocationKind kind,
        String assetId,
        Double timePeriodHours
) {}
