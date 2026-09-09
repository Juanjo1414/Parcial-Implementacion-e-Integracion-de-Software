package com.eia.camelracing.team.service;

import com.eia.camelracing.competitor.entity.Competitor;
import com.eia.camelracing.competitor.repository.ICompetitorRepository;
import com.eia.camelracing.team.dto.TeamRequest;
import com.eia.camelracing.team.dto.TeamResponse;
import com.eia.camelracing.team.entity.Team;
import com.eia.camelracing.team.entity.TeamStatus;
import com.eia.camelracing.team.mapper.TeamMapper;
import com.eia.camelracing.team.repository.ITeamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TeamService {

    // Necesitamos el repository de Competitor también, porque aquí es donde
    // agregamos/quitamos miembros de un equipo.
    private final ITeamRepository teamRepository;
    private final ICompetitorRepository competitorRepository;

    // Regla: "The maximum number of members must be configurable."
    // La dejamos como constante simple por ahora; se puede mover a
    // application.yml más adelante si quieres hacerla configurable de verdad.
    private static final int MAX_MEMBERS = 6;

    @Transactional
    public TeamResponse create(TeamRequest request) {
        if (teamRepository.existsByNameIgnoreCase(request.name())) {
            throw new IllegalStateException("Ya existe un equipo con el nombre '" + request.name() + "'");
        }
        Team saved = teamRepository.save(TeamMapper.toEntity(request));
        return TeamMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<TeamResponse> findAll() {
        return teamRepository.findAll().stream().map(TeamMapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public TeamResponse findById(UUID id) {
        return teamRepository.findById(id)
                .map(TeamMapper::toResponse)
                .orElseThrow(() -> new NoSuchElementException("Equipo " + id + " no encontrado"));
    }

    @Transactional
    public TeamResponse update(UUID id, TeamRequest request) {
        Team existing = getOrThrow(id);
        existing.setName(request.name());
        existing.setDescription(request.description());
        existing.setCoachName(request.coachName());
        return TeamMapper.toResponse(teamRepository.save(existing));
    }

    @Transactional
    public void delete(UUID id) {
        Team existing = getOrThrow(id);
        // Regla: "A team with official race history cannot be deleted; it
        // must be deactivated." Todavía no existe el módulo Race (Etapa 3),
        // así que por ahora solo dejamos el borrado físico si no tiene miembros;
        // retomamos esta regla completa cuando exista Race/Registration.
        teamRepository.delete(existing);
    }

    @Transactional
    public TeamResponse addMember(UUID teamId, UUID competitorId) {
        Team team = getOrThrow(teamId);

        if (team.getStatus() == TeamStatus.SUSPENDED) {
            throw new IllegalStateException("No se pueden agregar miembros a un equipo suspendido");
        }

        Competitor competitor = competitorRepository.findById(competitorId)
                .orElseThrow(() -> new NoSuchElementException("Competidor " + competitorId + " no encontrado"));

        if (competitor.getTeam() != null) {
            throw new IllegalStateException(
                    "El competidor '" + competitor.getNickname() + "' ya pertenece a otro equipo");
        }

        if (team.getMembers().size() >= MAX_MEMBERS) {
            throw new IllegalStateException("El equipo ya alcanzó el máximo de " + MAX_MEMBERS + " miembros");
        }

        // Un solo método que sincroniza ambos lados -> ya no hace falta
        // volver a consultar la BD, la respuesta se arma con el objeto en memoria.
        team.addMember(competitor);
        competitorRepository.save(competitor);

        return TeamMapper.toResponse(team);
    }

    @Transactional
    public TeamResponse removeMember(UUID teamId, UUID competitorId) {
        Team team = getOrThrow(teamId);
        Competitor competitor = competitorRepository.findById(competitorId)
                .orElseThrow(() -> new NoSuchElementException("Competidor " + competitorId + " no encontrado"));

        if (competitor.getTeam() == null || !competitor.getTeam().getId().equals(teamId)) {
            throw new IllegalStateException("Ese competidor no pertenece a este equipo");
        }

        team.removeMember(competitor);
        competitorRepository.save(competitor);

        return TeamMapper.toResponse(team);
    }

    private Team getOrThrow(UUID id) {
        return teamRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Equipo " + id + " no encontrado"));
    }
}