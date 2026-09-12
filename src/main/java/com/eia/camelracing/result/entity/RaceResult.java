package com.eia.camelracing.result.entity;

import com.eia.camelracing.registration.entity.RaceRegistration;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "race_results")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RaceResult {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // OneToOne: una inscripción tiene A LO SUMO un resultado.
    // unique = true en la columna es lo que obliga esa regla a nivel de BD.
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "registration_id", nullable = false, unique = true)
    private RaceRegistration registration;

    // null si el status no es FINISHED (un DNS o DSQ no tiene posición de llegada)
    private Integer finalPosition;

    private Double completionTimeSeconds;

    private Double penaltyTimeSeconds;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ResultStatus status;

    private String notes;

    // Nombre del usuario autenticado que registró el resultado, para trazabilidad.
    private String recordedBy;

    @Column(nullable = false, updatable = false)
    private LocalDateTime recordedAt;

    @PrePersist
    public void prePersist() {
        this.recordedAt = LocalDateTime.now();
    }
}