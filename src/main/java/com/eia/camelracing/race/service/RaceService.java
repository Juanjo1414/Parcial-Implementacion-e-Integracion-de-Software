package com.eia.camelracing.race.service;

import com.eia.camelracing.race.dto.RaceRequest;
import com.eia.camelracing.race.dto.RaceResponse;
import com.eia.camelracing.race.entity.Race;
import com.eia.camelracing.race.entity.RaceStatus;
import com.eia.camelracing.race.entity.RaceStatusTransitions;
import com.eia.camelracing.race.mapper.RaceMapper;
import com.eia.camelracing.race.repository.IRaceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RaceService {

    private final IRaceRepository repository;

    @Transactional
    public RaceResponse create(RaceRequest request) {
        validateDeadlineBeforeStart(request.registrationDeadline(), request.scheduledAt());
        Race saved = repository.save(RaceMapper.toEntity(request));
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

        // Regla: "A completed race cannot be edited."
        // (y por extensión, tampoco tiene sentido editar una cancelada)
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

        return RaceMapper.toResponse(repository.save(existing));
    }

    @Transactional
    public RaceResponse changeStatus(UUID id, RaceStatus newStatus) {
        Race existing = getOrThrow(id);

        // Aquí es donde usamos la máquina de estados de la Etapa 3.
        if (!RaceStatusTransitions.isValid(existing.getStatus(), newStatus)) {
            throw new IllegalStateException(
                    "No se puede pasar la carrera de " + existing.getStatus() + " a " + newStatus);
        }

        // Nota: la regla "A race cannot be completed without official results"
        // y "at least two valid participants are required to start" se
        // terminan de aplicar en la Etapa 4/5, cuando existan Registration y
        // Result y podamos consultarlos desde aquí. Por ahora solo validamos
        // la transición de estado.

        existing.setStatus(newStatus);
        return RaceMapper.toResponse(repository.save(existing));
    }

    @Transactional
    public void delete(UUID id) {
        Race existing = getOrThrow(id);
        repository.delete(existing);
    }

    // Regla: "The registration deadline must be earlier than the race start time."
    // No se puede expresar con una anotación estándar porque compara dos
    // campos del mismo request entre sí, así que va aquí en el service.
    private void validateDeadlineBeforeStart(java.time.LocalDateTime deadline,
                                             java.time.LocalDateTime scheduledAt) {
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