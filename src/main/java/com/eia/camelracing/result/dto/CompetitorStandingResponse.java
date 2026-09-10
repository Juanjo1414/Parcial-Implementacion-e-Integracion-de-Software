package com.eia.camelracing.result.dto;

import java.util.UUID;

public record CompetitorStandingResponse(
        UUID competitorId,
        String nickname,
        int totalPoints,
        int wins,
        int racesCompleted
) {}