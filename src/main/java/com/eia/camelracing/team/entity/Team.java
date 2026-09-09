package com.eia.camelracing.team.entity;

import com.eia.camelracing.competitor.entity.Competitor;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "teams")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Team {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String name;

    private String description;

    private String coachName; // "coach o responsable"

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private TeamStatus status = TeamStatus.ACTIVE;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder.Default
    private Integer wins = 0;
    @Builder.Default
    private Integer losses = 0;

    // mappedBy = "team": le dice a JPA "esta lista NO es dueña de la relación,
    // el dueño es el campo 'team' que está en Competitor.java". Este lado
    // solo sirve para navegar desde Team hacia sus miembros en Java,
    // no genera ninguna columna en la tabla teams.
    @OneToMany(mappedBy = "team", fetch = FetchType.LAZY)
    @Builder.Default
    private List<Competitor> members = new ArrayList<>();

    /**
     * Sincroniza AMBOS lados de la relación bidireccional al mismo tiempo:
     * agrega el competidor a esta lista Y le asigna este equipo al competidor.
     * Así evitamos depender de volver a consultar la base de datos para que
     * la respuesta refleje el cambio correctamente.
     */
    public void addMember(Competitor competitor) {
        this.members.add(competitor);
        competitor.setTeam(this);
    }

    public void removeMember(Competitor competitor) {
        this.members.remove(competitor);
        competitor.setTeam(null);
    }

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}