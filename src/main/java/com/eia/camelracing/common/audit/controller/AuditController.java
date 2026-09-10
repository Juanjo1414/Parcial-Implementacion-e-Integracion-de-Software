package com.eia.camelracing.common.audit.controller;

import com.eia.camelracing.common.audit.dto.AuditLogResponse;
import com.eia.camelracing.common.audit.entity.AuditLog;
import com.eia.camelracing.common.audit.repository.IAuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AuditController {

    private final IAuditLogRepository repository;

    // Regla explícita de la guía: "Only administrators may view the
    // complete audit log."
    @GetMapping("/api/audit")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<AuditLogResponse>> findAll(Pageable pageable) {
        Page<AuditLogResponse> page = repository.findAllByOrderByTimestampDesc(pageable)
                .map(this::toResponse);
        return ResponseEntity.ok(page);
    }

    private AuditLogResponse toResponse(AuditLog log) {
        return new AuditLogResponse(
                log.getId(), log.getUsername(), log.getAction(), log.getEntityType(),
                log.getEntityId(), log.getTimestamp(), log.getDescription(),
                log.getPreviousValue(), log.getNewValue());
    }
}