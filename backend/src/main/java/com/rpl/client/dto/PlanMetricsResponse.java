package com.rpl.client.dto;

public record PlanMetricsResponse(
    double completionRatio,
    int totalLeaves,
    int completedLeaves,
    double totalResourceCost,
    int riskScore
) {}
