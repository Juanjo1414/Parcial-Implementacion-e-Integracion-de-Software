package com.eia.camelracing.result.dto;

import com.eia.camelracing.result.entity.ResultStatus;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record ResultRequest(
        @NotNull(message = "Debes indicar la inscripción (registration) a la que pertenece este resultado")
        UUID registrationId,

        Integer finalPosition, // obligatorio solo si status = FINISHED (se valida en el service)

        Double completionTimeSeconds, // obligatorio y positivo solo si status = FINISHED

        Double penaltyTimeSeconds,

        @NotNull(message = "El estado del resultado es obligatorio")
        ResultStatus status,

        String notes
) {}