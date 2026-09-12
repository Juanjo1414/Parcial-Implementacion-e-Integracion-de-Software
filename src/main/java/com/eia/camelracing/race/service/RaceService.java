package com.eia.camelracing.race.service;

import com.eia.camelracing.common.audit.AuditPublisher;
import com.eia.camelracing.common.exception.InvalidStateTransitionException;
import com.eia.camelracing.common.exception.ResourceNotFoundException;
import com.eia.camelracing.race.dto.RaceRequest;
import com.eia.camelracing.race.dto.RaceResponse;
import com.eia.camelracing.race.entity.Race;
import com.eia.camelracing.race.entity.RaceStatus;
import com.eia.camelracing.race.entity.RaceStatusTransitions;
import com.eia.camelracing.race.mapper.RaceMapper;
import com.eia.camelracing.race.repository.IRaceRepository;
import com.eia.camelracing.registration.entity.RegistrationStatus;
import com.eia.camelracing.registration.repository.IRaceRegistrationRepository;
import com.eia.camelracing.result.repository.IRaceResultRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RaceService {

    private final IRaceRepository repository;
    // Se consultan para validar las reglas de la máquina de estados: cuántas
    // inscripciones aprobadas hay antes de iniciar y si ya existen resultados
    // antes de marcar la carrera como completada.
    private final IRaceRegistrationRepository registrationRepository;
    private final IRaceResultRepository resultRepository;
    private final AuditPublisher auditPublisher;

    @Transactional
    public RaceResponse create(RaceRequest request) {
        validateDeadlineBeforeStart(request.registrationDeadline(), request.scheduledAt());
        Race saved = repository.save(RaceMapper.toEntity(request));

        auditPublisher.publish("CREATE", "Race", saved.getId().toString(),
                "Carrera creada: " + saved.getName());

        return RaceMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<RaceResponse> findAll() {
        return repository.findAll().stream().map(RaceMapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public RaceResponse findById(UUID id) {
        return RaceMapper.toResponse(getOrThrow(id));
    }

    @Transactional
    public RaceResponse update(UUID id, RaceRequest request) {
        Race existing = getOrThrow(id);

        if (existing.getStatus() == RaceStatus.COMPLETED || existing.getStatus() == RaceStatus.CANCELLED) {
            throw new InvalidStateTransitionException(
                    "No se puede editar una carrera en estado " + existing.getStatus());
        }

        validateDeadlineBeforeStart(request.registrationDeadline(), request.scheduledAt());

        existing.setName(request.name());
        existing.setDescription(request.description());
        existing.setScheduledAt(request.scheduledAt());
        existing.setStartLocation(request.startLocation());
        existing.setFinishLocation(request.finishLocation());
        existing.setDistanceMeters(request.distanceMeters());
        existing.setMaxParticipants(request.maxParticipants());
        existing.setType(request.type());
        existing.setOrganizerName(request.organizerName());
        existing.setRegistrationDeadline(request.registrationDeadline());

        Race saved = repository.save(existing);

        auditPublisher.publish("UPDATE", "Race", id.toString(), "Carrera actualizada: " + saved.getName());

        return RaceMapper.toResponse(saved);
    }

    @Transactional
    public RaceResponse changeStatus(UUID id, RaceStatus newStatus) {
        Race existing = getOrThrow(id);

        if (!RaceStatusTransitions.isValid(existing.getStatus(), newStatus)) {
            throw new InvalidStateTransitionException(
                    "No se puede pasar la carrera de " + existing.getStatus() + " a " + newStatus);
        }

        // Una grilla de una sola persona no es una carrera: se exige un
        // mínimo de dos inscripciones aprobadas antes de poder arrancar.
        if (newStatus == RaceStatus.IN_PROGRESS) {
            long approvedCount = registrationRepository.findByRaceId(id).stream()
                    .filter(r -> r.getStatus() == RegistrationStatus.APPROVED)
                    .count();
            if (approvedCount < 2) {
                throw new InvalidStateTransitionException(
                        "Se necesitan al menos 2 inscripciones APROBADAS para iniciar la carrera (hay "
                                + approvedCount + ")");
            }
        }

        // Una carrera no queda oficialmente cerrada mientras no tenga al
        // menos un resultado cargado.
        if (newStatus == RaceStatus.COMPLETED) {
            boolean hasResults = !resultRepository.findByRegistration_Race_Id(id).isEmpty();
            if (!hasResults) {
                throw new InvalidStateTransitionException("No se puede completar una carrera sin resultados registrados");
            }
        }

        // Se captura el estado previo antes de sobrescribirlo para dejarlo
        // registrado en el log de auditoría junto con el nuevo valor.
        RaceStatus previousStatus = existing.getStatus();
        existing.setStatus(newStatus);
        Race saved = repository.save(existing);

        auditPublisher.publish("STATUS_CHANGE", "Race", id.toString(),
                "Carrera '" + saved.getName() + "' cambió de estado",
                previousStatus.name(), newStatus.name());

        return RaceMapper.toResponse(saved);
    }

    @Transactional
    public void delete(UUID id) {
        Race existing = getOrThrow(id);
        repository.delete(existing);

        auditPublisher.publish("DELETE", "Race", id.toString(), "Carrera eliminada: " + existing.getName());
    }

    private void validateDeadlineBeforeStart(LocalDateTime deadline, LocalDateTime scheduledAt) {
        if (!deadline.isBefore(scheduledAt)) {
            throw new InvalidStateTransitionException(
                    "La fecha límite de inscripción debe ser anterior a la fecha de la carrera");
        }
    }

    private Race getOrThrow(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Carrera " + id + " no encontrada"));
    }
}