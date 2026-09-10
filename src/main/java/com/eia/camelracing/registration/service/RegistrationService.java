package com.eia.camelracing.registration.service;

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

    @Transactional
    public RegistrationResponse register(UUID raceId, RegistrationRequest request) {
        Race race = raceRepository.findById(raceId)
                .orElseThrow(() -> new NoSuchElementException("Carrera " + raceId + " no encontrada"));

        // Regla: "Registration is allowed only while the race is open and
        // before the deadline."
        if (race.getStatus() != RaceStatus.OPEN_FOR_REGISTRATION) {
            throw new IllegalStateException(
                    "La carrera no está abierta para inscripciones (estado actual: " + race.getStatus() + ")");
        }
        if (LocalDateTime.now().isAfter(race.getRegistrationDeadline())) {
            throw new IllegalStateException("La fecha límite de inscripción ya pasó");
        }

        // XOR: exactamente uno de los dos debe venir.
        boolean hasCompetitor = request.competitorId() != null;
        boolean hasTeam = request.teamId() != null;
        if (hasCompetitor == hasTeam) { // true==true o false==false -> ambos o ninguno
            throw new IllegalStateException("Debes indicar exactamente un competitor o un team, no ambos ni ninguno");
        }

        RaceRegistration registration = hasCompetitor
                ? registerCompetitor(race, request)
                : registerTeam(race, request);

        registration.setStartingPosition(resolveStartingPosition(race.getId(), request.startingPosition()));

        return RegistrationMapper.toResponse(registrationRepository.save(registration));
    }

    private RaceRegistration registerCompetitor(Race race, RegistrationRequest request) {
        // Regla: "Registration type must match race type."
        if (race.getType() == RaceType.TEAM) {
            throw new IllegalStateException("Esta carrera es solo de equipos, no admite competidores individuales");
        }

        Competitor competitor = competitorRepository.findById(request.competitorId())
                .orElseThrow(() -> new NoSuchElementException("Competidor " + request.competitorId() + " no encontrado"));

        // Regla: "All individual competitors ... must be eligible."
        if (competitor.getStatus() != CompetitorStatus.ACTIVE) {
            throw new IllegalStateException("El competidor '" + competitor.getNickname() + "' no está ACTIVO");
        }

        // Regla: "A competitor or team cannot be registered twice in the same race."
        if (registrationRepository.existsByRaceIdAndCompetitorId(race.getId(), competitor.getId())) {
            throw new IllegalStateException("El competidor ya está inscrito en esta carrera");
        }

        // Regla: "A participant cannot compete simultaneously as an individual
        // and as a team member in the same race."
        if (competitor.getTeam() != null
                && registrationRepository.existsByRaceIdAndTeamId(race.getId(), competitor.getTeam().getId())) {
            throw new IllegalStateException(
                    "El competidor ya participa en esta carrera como miembro de su equipo");
        }

        return RaceRegistration.builder()
                .race(race)
                .competitor(competitor)
                .performedBy("sistema") // se reemplaza en Etapa 6
                .build();
    }

    private RaceRegistration registerTeam(Race race, RegistrationRequest request) {
        // Regla: "Registration type must match race type."
        if (race.getType() == RaceType.INDIVIDUAL) {
            throw new IllegalStateException("Esta carrera es solo individual, no admite equipos");
        }

        Team team = teamRepository.findById(request.teamId())
                .orElseThrow(() -> new NoSuchElementException("Equipo " + request.teamId() + " no encontrado"));

        // Regla (heredada del módulo Team): "A suspended team cannot enter a race."
        if (team.getStatus() != TeamStatus.ACTIVE) {
            throw new IllegalStateException("El equipo '" + team.getName() + "' no está activo");
        }
        // Regla (heredada del módulo Team): "A team must contain at least one
        // competitor before entering a race."
        if (team.getMembers().isEmpty()) {
            throw new IllegalStateException("El equipo no tiene miembros, no puede inscribirse");
        }

        if (registrationRepository.existsByRaceIdAndTeamId(race.getId(), team.getId())) {
            throw new IllegalStateException("El equipo ya está inscrito en esta carrera");
        }

        // Regla: "A participant cannot compete simultaneously as an individual
        // and as a team member in the same race." (chequeo inverso al de arriba:
        // ninguno de los miembros del equipo debe estar ya inscrito individualmente)
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

    // Regla: "Starting positions cannot be duplicated."
    // Si el cliente no especifica una posición, se asigna la siguiente
    // disponible automáticamente (1, 2, 3...).
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
        return RegistrationMapper.toResponse(registrationRepository.save(registration));
    }

    @Transactional
    public RegistrationResponse reject(UUID id, String reason) {
        // Regla: "Rejected registrations must include a clear reason."
        if (reason == null || reason.isBlank()) {
            throw new IllegalStateException("Debes indicar un motivo de rechazo");
        }
        RaceRegistration registration = getOrThrow(id);
        if (registration.getStatus() != RegistrationStatus.PENDING) {
            throw new IllegalStateException("Solo se pueden rechazar inscripciones en estado PENDING");
        }
        registration.setStatus(RegistrationStatus.REJECTED);
        registration.setValidationNotes(reason);
        return RegistrationMapper.toResponse(registrationRepository.save(registration));
    }

    @Transactional
    public void cancel(UUID id) {
        RaceRegistration registration = getOrThrow(id);
        registrationRepository.delete(registration);
    }

    private RaceRegistration getOrThrow(UUID id) {
        return registrationRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Inscripción " + id + " no encontrada"));
    }
}