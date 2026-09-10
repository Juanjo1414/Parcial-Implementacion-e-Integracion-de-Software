package com.eia.camelracing.common.audit.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "audit_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String username;

    @Column(nullable = false)
    private String action; // ej: "CREATE", "STATUS_CHANGE", "DELETE", "LOGIN"

    @Column(nullable = false)
    private String entityType; // ej: "Competitor", "Race", "User"

    // Se guarda como String (no UUID tipado) a propósito: un mismo log puede
    // referirse a cualquier tipo de entidad, y no vale la pena crear una
    // relación JPA real solo para auditoría -> eso acoplaría el audit log a
    // cada módulo del sistema, justo lo que queremos evitar.
    private String entityId;

    @Column(nullable = false, updatable = false)
    private LocalDateTime timestamp;

    private String description;

    @Column(columnDefinition = "TEXT")
    private String previousValue;

    @Column(columnDefinition = "TEXT")
    private String newValue;

    @PrePersist
    public void prePersist() {
        this.timestamp = LocalDateTime.now();
    }
}