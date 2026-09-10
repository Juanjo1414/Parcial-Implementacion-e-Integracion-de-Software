package com.eia.camelracing.race.dto;

import com.eia.camelracing.race.entity.RaceType;
import jakarta.validation.constraints.*;

import java.time.LocalDateTime;

public record RaceRequest(
        @NotBlank(message = "El nombre de la carrera no puede estar vacío")
        String name,

        String description,

        @NotNull(message = "La fecha programada es obligatoria")
        @Future(message = "La carrera no puede programarse en el pasado")
        LocalDateTime scheduledAt,

        String startLocation,
        String finishLocation,

        @NotNull @Positive(message = "La distancia debe ser mayor que cero")
        Double distanceMeters,

        @NotNull @Min(value = 2, message = "Se necesitan al menos 2 participantes")
        Integer maxParticipants,

        @NotNull(message = "El tipo de carrera es obligatorio")
        RaceType type,

        String organizerName,

        @NotNull(message = "La fecha límite de inscripción es obligatoria")
        LocalDateTime registrationDeadline
) {}