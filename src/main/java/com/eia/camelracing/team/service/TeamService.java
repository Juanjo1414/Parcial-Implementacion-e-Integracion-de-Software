package com.eia.camelracing.team.service;

import com.eia.camelracing.common.exception.BusinessRuleException;
import com.eia.camelracing.common.exception.InvalidStateTransitionException;
import com.eia.camelracing.common.exception.ResourceNotFoundException;
import com.eia.camelracing.competitor.entity.Competitor;
import com.eia.camelracing.competitor.repository.ICompetitorRepository;
import com.eia.camelracing.result.repository.IRaceResultRepository;
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
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TeamService {

    // Se necesita también el repositorio de Competitor porque aquí es donde
    // se agregan y quitan miembros de un equipo.
    private final ITeamRepository teamRepository;
    private final ICompetitorRepository competitorRepository;
    private final IRaceResultRepository resultRepository;

    // Cupo máximo por equipo. Se deja como constante porque ningún módulo
    // requiere hoy configurarlo por equipo o en tiempo de ejecución.
    private static final int MAX_MEMBERS = 6;

    @Transactional
    public TeamResponse create(TeamRequest request) {
        if (teamRepository.existsByNameIgnoreCase(request.name())) {
            throw new BusinessRuleException("Ya existe un equipo con el nombre '" + request.name() + "'");
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
                .orElseThrow(() -> new ResourceNotFoundException("Equipo " + id + " no encontrado"));
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

        if (resultRepository.existsByRegistration_Team_Id(id)) {
            throw new BusinessRuleException(
                    "Este equipo tiene resultados oficiales registrados; no se puede eliminar. "
                            + "Debe desactivarse en su lugar");
        }

        teamRepository.delete(existing);
    }

    @Transactional
    public TeamResponse addMember(UUID teamId, UUID competitorId) {
        Team team = getOrThrow(teamId);

        if (team.getStatus() == TeamStatus.SUSPENDED) {
            throw new InvalidStateTransitionException("No se pueden agregar miembros a un equipo suspendido");
        }

        Competitor competitor = competitorRepository.findById(competitorId)
                .orElseThrow(() -> new ResourceNotFoundException("Competidor " + competitorId + " no encontrado"));

        if (competitor.getTeam() != null) {
            throw new BusinessRuleException(
                    "El competidor '" + competitor.getNickname() + "' ya pertenece a otro equipo");
        }

        if (team.getMembers().size() >= MAX_MEMBERS) {
            throw new BusinessRuleException("El equipo ya alcanzó el máximo de " + MAX_MEMBERS + " miembros");
        }

        // addMember sincroniza ambos lados de la relación, así que la
        // respuesta se puede construir con el objeto en memoria sin
        // necesidad de volver a consultar la base de datos.
        team.addMember(competitor);
        competitorRepository.save(competitor);

        return TeamMapper.toResponse(team);
    }

    @Transactional
    public TeamResponse removeMember(UUID teamId, UUID competitorId) {
        Team team = getOrThrow(teamId);
        Competitor competitor = competitorRepository.findById(competitorId)
                .orElseThrow(() -> new ResourceNotFoundException("Competidor " + competitorId + " no encontrado"));

        if (competitor.getTeam() == null || !competitor.getTeam().getId().equals(teamId)) {
            throw new BusinessRuleException("Ese competidor no pertenece a este equipo");
        }

        team.removeMember(competitor);
        competitorRepository.save(competitor);

        return TeamMapper.toResponse(team);
    }

    private Team getOrThrow(UUID id) {
        return teamRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Equipo " + id + " no encontrado"));
    }
}