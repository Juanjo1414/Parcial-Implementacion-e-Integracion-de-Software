package com.eia.camelracing.result.dto;

import com.eia.camelracing.result.entity.ResultStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record ResultResponse(
        UUID id,
        UUID registrationId,
        UUID raceId,
        String participantLabel, // nickname del competidor, o nombre del equipo
        Integer finalPosition,
        Double completionTimeSeconds,
        Double penaltyTimeSeconds,
        ResultStatus status,
        int points, // calculado, no se guarda en BD: se recalcula siempre desde ResultPoints
        String notes,
        String recordedBy,
        LocalDateTime recordedAt
) {}