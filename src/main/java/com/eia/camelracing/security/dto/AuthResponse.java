package com.eia.camelracing.security.dto;

public record AuthResponse(
        String token,
        String username,
        String role
) {}