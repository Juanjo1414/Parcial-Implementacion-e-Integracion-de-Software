package com.eia.camelracing.common.audit.event;

// Un simple "mensaje" inmutable: describe QUÉ pasó, sin saber QUIÉN lo va a
// escuchar ni QUÉ se va a hacer con esa información. Eso es justo lo que
// desacopla la lógica de negocio de la auditoría.
public record AuditEvent(
        String username,
        String action,
        String entityType,
        String entityId,
        String description,
        String previousValue,
        String newValue
) {
    // Constructor corto para el caso común: sin valores previo/nuevo.
    public AuditEvent(String username, String action, String entityType, String entityId, String description) {
        this(username, action, entityType, entityId, description, null, null);
    }
}