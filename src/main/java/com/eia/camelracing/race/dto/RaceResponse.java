package com.eia.camelracing.race.dto;

import com.eia.camelracing.race.entity.RaceStatus;
import com.eia.camelracing.race.entity.RaceType;

import java.time.LocalDateTime;
import java.util.UUID;

public record RaceResponse(
        UUID id,
        String name,
        String description,
        LocalDateTime scheduledAt,
        String startLocation,
        String finishLocation,
        Double distanceMeters,
        Integer maxParticipants,
        RaceType type,
        RaceStatus status,
        String organizerName,
        LocalDateTime registrationDeadline,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}