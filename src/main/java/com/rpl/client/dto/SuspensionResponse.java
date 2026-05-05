package com.rpl.client.dto;

import java.time.Instant;

public record SuspensionResponse(
        Long id,
        String reason,
        Instant startDate,
        Instant endDate,
        Long durationMinutes) {
}
