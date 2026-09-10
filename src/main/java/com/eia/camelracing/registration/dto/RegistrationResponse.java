package com.eia.camelracing.registration.dto;

import com.eia.camelracing.registration.entity.RegistrationStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record RegistrationResponse(
        UUID id,
        UUID raceId,
        UUID competitorId,
        String competitorNickname, // null si es una inscripción de equipo
        UUID teamId,
        String teamName,           // null si es una inscripción individual
        LocalDateTime registrationDate,
        RegistrationStatus status,
        Integer startingPosition,
        String validationNotes,
        String performedBy
) {}