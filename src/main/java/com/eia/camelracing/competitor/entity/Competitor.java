package com.eia.camelracing.competitor.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
import com.eia.camelracing.team.entity.Team;

@Entity
@Table(name = "competitors")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Competitor {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    // unique = true crea una restricción UNIQUE en la tabla: la BD misma
    // rechaza un nickname duplicado, no solo nuestra validación en Java.
    @Column(nullable = false, unique = true)
    private String nickname;

    @Enumerated(EnumType.STRING) // guarda "CAMEL" en vez de un número (0,1,2..)
    @Column(nullable = false)
    private CompetitorType type;

    private LocalDate birthDate;

    @Column(nullable = false)
    private Double weight;

    @Column(nullable = false)
    private Double height;

    private String originCountry;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private CompetitorStatus status = CompetitorStatus.ACTIVE;

    @Column(nullable = false, updatable = false)
    private LocalDateTime registeredAt;

    @ManyToOne(fetch = FetchType.LAZY) // LAZY: no trae el Team completo a menos que lo pidas explícitamente
    @JoinColumn(name = "team_id") // nombre de la columna FK en la tabla competitors
    private Team team; // puede ser null: "team opcional" según la guía

    // Estadísticas simples pedidas por la guía.
    @Builder.Default
    private Integer wins = 0;
    @Builder.Default
    private Integer losses = 0;
    @Builder.Default
    private Integer racesCompleted = 0;

    // Se ejecuta automáticamente justo antes del primer INSERT.
    @PrePersist
    public void prePersist() {
        this.registeredAt = LocalDateTime.now();
    }
}