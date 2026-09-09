package com.eia.camelracing.competitor.dto;

import com.eia.camelracing.competitor.entity.CompetitorType;
import jakarta.validation.constraints.*;
import java.time.LocalDate;

public record CompetitorRequest(
        @NotBlank(message = "El nombre no puede estar vacío")
        String name,

        @NotBlank(message = "El nickname no puede estar vacío")
        @Size(max = 50, message = "El nickname no puede superar 50 caracteres")
        String nickname,

        @NotNull(message = "El tipo de competidor es obligatorio")
        CompetitorType type,

        @Past(message = "La fecha de nacimiento debe ser en el pasado")
        LocalDate birthDate,

        @NotNull @Positive(message = "El peso debe ser un número positivo")
        Double weight,

        @NotNull @Positive(message = "La altura debe ser un número positivo")
        Double height,

        String originCountry
) {}