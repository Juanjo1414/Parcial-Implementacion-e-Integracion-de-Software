package com.eia.camelracing.race.service;

import com.eia.camelracing.common.audit.AuditPublisher;
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
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RaceService {

    private final IRaceRepository repository;
    // Agregados en la Etapa 5 para completar las reglas de transición de estado.
    private final IRaceRegistrationRepository registrationRepository;
    private final IRaceResultRepository resultRepository;
    // Agregado en la Etapa 7.
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
            throw new IllegalStateException(
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
            throw new IllegalStateException(
                    "No se puede pasar la carrera de " + existing.getStatus() + " a " + newStatus);
        }

        // Regla (Módulo 4): "At least two valid participants are required to start."
        if (newStatus == RaceStatus.IN_PROGRESS) {
            long approvedCount = registrationRepository.findByRaceId(id).stream()
                    .filter(r -> r.getStatus() == RegistrationStatus.APPROVED)
                    .count();
            if (approvedCount < 2) {
                throw new IllegalStateException(
                        "Se necesitan al menos 2 inscripciones APROBADAS para iniciar la carrera (hay "
                                + approvedCount + ")");
            }
        }

        // Regla (Módulo 4): "A race cannot be completed without official results."
        if (newStatus == RaceStatus.COMPLETED) {
            boolean hasResults = !resultRepository.findByRegistration_Race_Id(id).isEmpty();
            if (!hasResults) {
                throw new IllegalStateException("No se puede completar una carrera sin resultados registrados");
            }
        }

        // Se guarda el estado anterior ANTES de sobreescribirlo, para poder
        // reportarlo correctamente en el log de auditoría.
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
            throw new IllegalStateException(
                    "La fecha límite de inscripción debe ser anterior a la fecha de la carrera");
        }
    }

    private Race getOrThrow(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Carrera " + id + " no encontrada"));
    }
}