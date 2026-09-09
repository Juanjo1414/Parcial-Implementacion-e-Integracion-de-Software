package com.eia.camelracing.team.dto;

import jakarta.validation.constraints.NotBlank;

public record TeamRequest(
        @NotBlank(message = "El nombre del equipo no puede estar vacío")
        String name,
        String description,
        String coachName
) {}