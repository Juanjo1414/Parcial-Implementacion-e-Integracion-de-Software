package com.eia.camelracing.registration.dto;

public record RejectRequest(
        String reason // obligatorio, se valida en el service (no @NotBlank aquí para poder dar un mensaje de negocio más claro)
) {}