package com.eia.camelracing.result.service;

import com.eia.camelracing.common.audit.AuditPublisher;
import com.eia.camelracing.competitor.entity.Competitor;
import com.eia.camelracing.competitor.repository.ICompetitorRepository;
import com.eia.camelracing.race.entity.RaceStatus;
import com.eia.camelracing.registration.entity.RaceRegistration;
import com.eia.camelracing.registration.entity.RegistrationStatus;
import com.eia.camelracing.registration.repository.IRaceRegistrationRepository;
import com.eia.camelracing.result.dto.ResultRequest;
import com.eia.camelracing.result.dto.ResultResponse;
import com.eia.camelracing.result.entity.RaceResult;
import com.eia.camelracing.result.entity.ResultStatus;
import com.eia.camelracing.result.mapper.ResultMapper;
import com.eia.camelracing.result.repository.IRaceResultRepository;
import com.eia.camelracing.team.entity.Team;
import com.eia.camelracing.team.repository.ITeamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ResultService {

    private final IRaceResultRepository resultRepository;
    private final IRaceRegistrationRepository registrationRepository;
    private final ICompetitorRepository competitorRepository;
    private final ITeamRepository teamRepository;
    // Agregado en la Etapa 7.
    private final AuditPublisher auditPublisher;

    @Transactional
    public ResultResponse record(UUID raceId, ResultRequest request) {
        RaceRegistration registration = registrationRepository.findById(request.registrationId())
                .orElseThrow(() -> new NoSuchElementException(
                        "Inscripción " + request.registrationId() + " no encontrada"));

        if (!registration.getRace().getId().equals(raceId)) {
            throw new IllegalStateException("Esa inscripción no pertenece a la carrera indicada");
        }

        if (registration.getRace().getStatus() != RaceStatus.IN_PROGRESS) {
            throw new IllegalStateException(
                    "Solo se pueden registrar resultados en carreras EN_PROGRESO (estado actual: "
                            + registration.getRace().getStatus() + ")");
        }

        if (registration.getStatus() != RegistrationStatus.APPROVED) {
            throw new IllegalStateException("Solo se pueden registrar resultados de inscripciones APROBADAS");
        }

        if (resultRepository.existsByRegistration_Id(registration.getId())) {
            throw new IllegalStateException("Esta inscripción ya tiene un resultado registrado (usa PUT para editarlo)");
        }

        validateResultData(raceId, request, null);

        RaceResult result = RaceResult.builder()
                .registration(registration)
                .finalPosition(request.finalPosition())
                .completionTimeSeconds(request.completionTimeSeconds())
                .penaltyTimeSeconds(request.penaltyTimeSeconds())
                .status(request.status())
                .notes(request.notes())
                .recordedBy(currentUsername())
                .build();

        RaceResult saved = resultRepository.save(result);
        updateStatistics(registration, request.status(), request.finalPosition());

        auditPublisher.publish("CREATE", "RaceResult", saved.getId().toString(),
                "Resultado registrado para inscripción " + registration.getId());

        return ResultMapper.toResponse(saved);
    }

    @Transactional
    public ResultResponse update(UUID resultId, ResultRequest request) {
        RaceResult existing = resultRepository.findById(resultId)
                .orElseThrow(() -> new NoSuchElementException("Resultado " + resultId + " no encontrado"));

        UUID raceId = existing.getRegistration().getRace().getId();
        validateResultData(raceId, request, resultId);

        revertStatistics(existing.getRegistration(), existing.getStatus(), existing.getFinalPosition());

        existing.setFinalPosition(request.finalPosition());
        existing.setCompletionTimeSeconds(request.completionTimeSeconds());
        existing.setPenaltyTimeSeconds(request.penaltyTimeSeconds());
        existing.setStatus(request.status());
        existing.setNotes(request.notes());
        existing.setRecordedBy(currentUsername());

        RaceResult saved = resultRepository.save(existing);
        updateStatistics(existing.getRegistration(), request.status(), request.finalPosition());

        auditPublisher.publish("UPDATE", "RaceResult", resultId.toString(), "Resultado modificado");

        return ResultMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<ResultResponse> findByRace(UUID raceId) {
        return resultRepository.findByRegistration_Race_Id(raceId).stream()
                .map(ResultMapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public ResultResponse findById(UUID id) {
        return ResultMapper.toResponse(resultRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Resultado " + id + " no encontrado")));
    }

    private void validateResultData(UUID raceId, ResultRequest request, UUID excludeResultId) {
        if (request.status() == ResultStatus.FINISHED) {
            if (request.completionTimeSeconds() == null || request.completionTimeSeconds() <= 0) {
                throw new IllegalStateException("El tiempo de finalización debe ser positivo para un resultado FINISHED");
            }
            if (request.finalPosition() == null || request.finalPosition() <= 0) {
                throw new IllegalStateException("Un resultado FINISHED debe tener una posición final válida");
            }
            boolean positionTaken = resultRepository.existsByRegistration_Race_IdAndFinalPosition(
                    raceId, request.finalPosition());
            if (positionTaken && excludeResultId == null) {
                throw new IllegalStateException(
                        "La posición " + request.finalPosition() + " ya fue registrada en esta carrera");
            }
        } else {
            if (request.finalPosition() != null) {
                throw new IllegalStateException(
                        "Un resultado con estado " + request.status() + " no debe tener posición final");
            }
        }
    }

    private void updateStatistics(RaceRegistration registration, ResultStatus status, Integer position) {
        applyStatDelta(registration, status, position, +1);
    }

    private void revertStatistics(RaceRegistration registration, ResultStatus status, Integer position) {
        applyStatDelta(registration, status, position, -1);
    }

    private void applyStatDelta(RaceRegistration registration, ResultStatus status, Integer position, int delta) {
        boolean isWin = status == ResultStatus.FINISHED && position != null && position == 1;

        if (registration.getCompetitor() != null) {
            Competitor competitor = registration.getCompetitor();
            competitor.setRacesCompleted(competitor.getRacesCompleted() + delta);
            if (isWin) {
                competitor.setWins(competitor.getWins() + delta);
            } else {
                competitor.setLosses(competitor.getLosses() + delta);
            }
            competitorRepository.save(competitor);
        } else if (registration.getTeam() != null) {
            Team team = registration.getTeam();
            if (isWin) {
                team.setWins(team.getWins() + delta);
            } else {
                team.setLosses(team.getLosses() + delta);
            }
            teamRepository.save(team);
        }
    }

    private String currentUsername() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null ? auth.getName() : "sistema";
    }
}