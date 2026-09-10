package com.eia.camelracing.common.audit;

import com.eia.camelracing.common.audit.entity.AuditLog;
import com.eia.camelracing.common.audit.event.AuditEvent;
import com.eia.camelracing.common.audit.repository.IAuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class AuditEventListener {

    private final IAuditLogRepository auditLogRepository;

    /**
     * @TransactionalEventListener con AFTER_COMMIT (en vez de @EventListener
     * normal) es una decisión deliberada: así el registro de auditoría solo
     * se guarda si la operación de negocio realmente se confirmó en la base
     * de datos. Si, por ejemplo, un create() de Competitor falla a mitad de
     * camino y hace rollback, NO queremos un log de auditoría diciendo que
     * "se creó" algo que en realidad no quedó guardado.
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onAuditEvent(AuditEvent event) {
        AuditLog log = AuditLog.builder()
                .username(event.username())
                .action(event.action())
                .entityType(event.entityType())
                .entityId(event.entityId())
                .description(event.description())
                .previousValue(event.previousValue())
                .newValue(event.newValue())
                .build();
        auditLogRepository.save(log);
    }
}