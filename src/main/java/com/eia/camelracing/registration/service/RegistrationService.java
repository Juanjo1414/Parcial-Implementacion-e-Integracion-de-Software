package com.eia.camelracing.registration.service;

import com.eia.camelracing.common.audit.AuditPublisher;
import com.eia.camelracing.competitor.entity.Competitor;
import com.eia.camelracing.competitor.entity.CompetitorStatus;
import com.eia.camelracing.competitor.repository.ICompetitorRepository;
import com.eia.camelracing.race.entity.Race;
import com.eia.camelracing.race.entity.RaceStatus;
import com.eia.camelracing.race.entity.RaceType;
import com.eia.camelracing.race.repository.IRaceRepository;
import com.eia.camelracing.registration.dto.RegistrationRequest;
import com.eia.camelracing.registration.dto.RegistrationResponse;
import com.eia.camelracing.registration.entity.RaceRegistration;
import com.eia.camelracing.registration.entity.RegistrationStatus;
import com.eia.camelracing.registration.mapper.RegistrationMapper;
import com.eia.camelracing.registration.repository.IRaceRegistrationRepository;
import com.eia.camelracing.team.entity.Team;
import com.eia.camelracing.team.entity.TeamStatus;
import com.eia.camelracing.team.repository.ITeamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RegistrationService {

    private final IRaceRegistrationRepository registrationRepository;
    private final IRaceRepository raceRepository;
    private final ICompetitorRepository competitorRepository;
    private final ITeamRepository teamRepository;
    // Agregado en la Etapa 7.
    private final AuditPublisher auditPublisher;

    @Transactional
    public RegistrationResponse register(UUID raceId, RegistrationRequest request) {
        Race race = raceRepository.findById(raceId)
                .orElseThrow(() -> new NoSuchElementException("Carrera " + raceId + " no encontrada"));

        if (race.getStatus() != RaceStatus.OPEN_FOR_REGISTRATION) {
            throw new IllegalStateException(
                    "La carrera no está abierta para inscripciones (estado actual: " + race.getStatus() + ")");
        }
        if (LocalDateTime.now().isAfter(race.getRegistrationDeadline())) {
            throw new IllegalStateException("La fecha límite de inscripción ya pasó");
        }

        boolean hasCompetitor = request.competitorId() != null;
        boolean hasTeam = request.teamId() != null;
        if (hasCompetitor == hasTeam) {
            throw new IllegalStateException("Debes indicar exactamente un competitor o un team, no ambos ni ninguno");
        }

        RaceRegistration registration = hasCompetitor
                ? registerCompetitor(race, request)
                : registerTeam(race, request);

        registration.setStartingPosition(resolveStartingPosition(race.getId(), request.startingPosition()));
        registration.setPerformedBy(currentUsername());

        RaceRegistration saved = registrationRepository.save(registration);

        auditPublisher.publish("CREATE", "RaceRegistration", saved.getId().toString(),
                "Inscripción registrada en la carrera " + race.getName());

        return RegistrationMapper.toResponse(saved);
    }

    private RaceRegistration registerCompetitor(Race race, RegistrationRequest request) {
        if (race.getType() == RaceType.TEAM) {
            throw new IllegalStateException("Esta carrera es solo de equipos, no admite competidores individuales");
        }

        Competitor competitor = competitorRepository.findById(request.competitorId())
                .orElseThrow(() -> new NoSuchElementException("Competidor " + request.competitorId() + " no encontrado"));

        if (competitor.getStatus() != CompetitorStatus.ACTIVE) {
            throw new IllegalStateException("El competidor '" + competitor.getNickname() + "' no está ACTIVO");
        }

        if (registrationRepository.existsByRaceIdAndCompetitorId(race.getId(), competitor.getId())) {
            throw new IllegalStateException("El competidor ya está inscrito en esta carrera");
        }

        if (competitor.getTeam() != null
                && registrationRepository.existsByRaceIdAndTeamId(race.getId(), competitor.getTeam().getId())) {
            throw new IllegalStateException(
                    "El competidor ya participa en esta carrera como miembro de su equipo");
        }

        return RaceRegistration.builder()
                .race(race)
                .competitor(competitor)
                .build();
    }

    private RaceRegistration registerTeam(Race race, RegistrationRequest request) {
        if (race.getType() == RaceType.INDIVIDUAL) {
            throw new IllegalStateException("Esta carrera es solo individual, no admite equipos");
        }

        Team team = teamRepository.findById(request.teamId())
                .orElseThrow(() -> new NoSuchElementException("Equipo " + request.teamId() + " no encontrado"));

        if (team.getStatus() != TeamStatus.ACTIVE) {
            throw new IllegalStateException("El equipo '" + team.getName() + "' no está activo");
        }
        if (team.getMembers().isEmpty()) {
            throw new IllegalStateException("El equipo no tiene miembros, no puede inscribirse");
        }

        if (registrationRepository.existsByRaceIdAndTeamId(race.getId(), team.getId())) {
            throw new IllegalStateException("El equipo ya está inscrito en esta carrera");
        }

        boolean anyMemberAlreadyIndividual = team.getMembers().stream()
                .anyMatch(member -> registrationRepository.existsByRaceIdAndCompetitorId(race.getId(), member.getId()));
        if (anyMemberAlreadyIndividual) {
            throw new IllegalStateException(
                    "Uno de los miembros del equipo ya está inscrito individualmente en esta carrera");
        }

        return RaceRegistration.builder()
                .race(race)
                .team(team)
                .build();
    }

    private Integer resolveStartingPosition(UUID raceId, Integer requested) {
        if (requested == null) {
            long count = registrationRepository.findByRaceId(raceId).size();
            return (int) count + 1;
        }
        if (registrationRepository.existsByRaceIdAndStartingPosition(raceId, requested)) {
            throw new IllegalStateException("La posición de salida " + requested + " ya está ocupada en esta carrera");
        }
        return requested;
    }

    @Transactional(readOnly = true)
    public List<RegistrationResponse> findByRace(UUID raceId) {
        return registrationRepository.findByRaceId(raceId).stream()
                .map(RegistrationMapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public RegistrationResponse findById(UUID id) {
        return RegistrationMapper.toResponse(getOrThrow(id));
    }

    @Transactional
    public RegistrationResponse approve(UUID id) {
        RaceRegistration registration = getOrThrow(id);
        if (registration.getStatus() != RegistrationStatus.PENDING) {
            throw new IllegalStateException("Solo se pueden aprobar inscripciones en estado PENDING");
        }
        registration.setStatus(RegistrationStatus.APPROVED);
        RaceRegistration saved = registrationRepository.save(registration);

        auditPublisher.publish("APPROVE", "RaceRegistration", id.toString(), "Inscripción aprobada");

        return RegistrationMapper.toResponse(saved);
    }

    @Transactional
    public RegistrationResponse reject(UUID id, String reason) {
        if (reason == null || reason.isBlank()) {
            throw new IllegalStateException("Debes indicar un motivo de rechazo");
        }
        RaceRegistration registration = getOrThrow(id);
        if (registration.getStatus() != RegistrationStatus.PENDING) {
            throw new IllegalStateException("Solo se pueden rechazar inscripciones en estado PENDING");
        }
        registration.setStatus(RegistrationStatus.REJECTED);
        registration.setValidationNotes(reason);
        RaceRegistration saved = registrationRepository.save(registration);

        auditPublisher.publish("REJECT", "RaceRegistration", id.toString(), "Inscripción rechazada: " + reason);

        return RegistrationMapper.toResponse(saved);
    }

    @Transactional
    public void cancel(UUID id) {
        RaceRegistration registration = getOrThrow(id);
        registrationRepository.delete(registration);

        auditPublisher.publish("CANCEL", "RaceRegistration", id.toString(), "Inscripción cancelada");
    }

    private RaceRegistration getOrThrow(UUID id) {
        return registrationRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Inscripción " + id + " no encontrada"));
    }

    private String currentUsername() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null ? auth.getName() : "sistema";
    }
}