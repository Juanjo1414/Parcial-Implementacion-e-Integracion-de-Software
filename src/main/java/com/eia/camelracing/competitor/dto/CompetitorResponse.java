package com.eia.camelracing.competitor.dto;

import com.eia.camelracing.competitor.entity.CompetitorStatus;
import com.eia.camelracing.competitor.entity.CompetitorType;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

// Este NO lleva anotaciones de validación: solo viaja HACIA el cliente,
// nadie valida lo que nosotros mismos generamos.
public record CompetitorResponse(
        UUID id,
        String name,
        String nickname,
        CompetitorType type,
        LocalDate birthDate,
        Double weight,
        Double height,
        String originCountry,
        CompetitorStatus status,
        LocalDateTime registeredAt,
        Integer wins,
        Integer losses,
        Integer racesCompleted
) {}