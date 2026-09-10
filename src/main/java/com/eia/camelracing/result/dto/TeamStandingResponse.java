package com.eia.camelracing.result.dto;

import java.util.UUID;

public record TeamStandingResponse(
        UUID teamId,
        String teamName,
        int totalPoints,
        int wins,
        int racesCompleted
) {}