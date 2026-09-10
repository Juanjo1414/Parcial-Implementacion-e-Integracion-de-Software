package com.eia.camelracing.common.audit;

import com.eia.camelracing.common.audit.event.AuditEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * Punto único que todos los services usan para "avisar" que ocurrió algo
 * auditable. Internamente solo publica un evento de Spring -> quien realmente
 * guarda el registro en la base de datos es AuditEventListener, en otro hilo
 * de responsabilidad completamente separado.
 */
@Component
@RequiredArgsConstructor
public class AuditPublisher {

    private final ApplicationEventPublisher eventPublisher;

    public void publish(String action, String entityType, String entityId, String description) {
        eventPublisher.publishEvent(
                new AuditEvent(currentUsername(), action, entityType, entityId, description));
    }

    public void publish(String action, String entityType, String entityId, String description,
                        String previousValue, String newValue) {
        eventPublisher.publishEvent(
                new AuditEvent(currentUsername(), action, entityType, entityId, description, previousValue, newValue));
    }

    private String currentUsername() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null ? auth.getName() : "sistema";
    }
}