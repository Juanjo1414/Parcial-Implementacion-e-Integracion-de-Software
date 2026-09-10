package com.eia.camelracing.registration.entity;

import com.eia.camelracing.competitor.entity.Competitor;
import com.eia.camelracing.race.entity.Race;
import com.eia.camelracing.team.entity.Team;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "race_registrations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RaceRegistration {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "race_id", nullable = false)
    private Race race;

    // Ambos son opcionales A NIVEL DE BASE DE DATOS (nullable = true),
    // pero exactamente uno de los dos debe venir lleno: esa regla la
    // valida el service, no la base de datos ni Bean Validation.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "competitor_id")
    private Competitor competitor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id")
    private Team team;

    @Column(nullable = false, updatable = false)
    private LocalDateTime registrationDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private RegistrationStatus status = RegistrationStatus.PENDING;

    private Integer startingPosition;

    private String validationNotes;

    // Placeholder hasta la Etapa 6 (seguridad): por ahora guardamos un
    // nombre libre; cuando exista autenticación real, este campo se
    // llenará automáticamente con el usuario autenticado.
    private String performedBy;

    @PrePersist
    public void prePersist() {
        this.registrationDate = LocalDateTime.now();
    }
}