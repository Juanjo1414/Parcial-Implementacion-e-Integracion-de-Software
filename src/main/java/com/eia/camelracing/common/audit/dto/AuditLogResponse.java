package com.eia.camelracing.common.audit.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record AuditLogResponse(
        UUID id,
        String username,
        String action,
        String entityType,
        String entityId,
        LocalDateTime timestamp,
        String description,
        String previousValue,
        String newValue
) {}