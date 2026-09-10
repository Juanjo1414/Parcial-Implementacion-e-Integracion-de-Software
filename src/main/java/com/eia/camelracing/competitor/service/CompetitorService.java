package com.eia.camelracing.competitor.service;

import com.eia.camelracing.common.audit.AuditPublisher;
import com.eia.camelracing.competitor.dto.CompetitorRequest;
import com.eia.camelracing.competitor.dto.CompetitorResponse;
import com.eia.camelracing.competitor.entity.Competitor;
import com.eia.camelracing.competitor.entity.CompetitorStatus;
import com.eia.camelracing.competitor.entity.CompetitorType;
import com.eia.camelracing.competitor.mapper.CompetitorMapper;
import com.eia.camelracing.competitor.repository.CompetitorSpecifications;
import com.eia.camelracing.competitor.repository.ICompetitorRepository;
import com.eia.camelracing.result.repository.IRaceResultRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CompetitorService {

    private final ICompetitorRepository repository;
    // Agregado en la Etapa 5: para validar que no tenga resultados oficiales antes de borrar.
    private final IRaceResultRepository resultRepository;
    // Agregado en la Etapa 7: para publicar eventos de auditoría.
    private final AuditPublisher auditPublisher;

    @Transactional
    public CompetitorResponse create(CompetitorRequest request) {
        if (repository.existsByNicknameIgnoreCase(request.nickname())) {
            throw new IllegalStateException(
                    "Ya existe un competidor con el nickname '" + request.nickname() + "'");
        }
        Competitor saved = repository.save(CompetitorMapper.toEntity(request));

        auditPublisher.publish("CREATE", "Competitor", saved.getId().toString(),
                "Competidor creado: " + saved.getNickname());

        return CompetitorMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public Page<CompetitorResponse> findAll(
            String name, CompetitorType type, CompetitorStatus status, Pageable pageable) {
        return repository
                .findAll(CompetitorSpecifications.withFilters(name, type, status), pageable)
                .map(CompetitorMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public CompetitorResponse findById(UUID id) {
        return repository.findById(id)
                .map(CompetitorMapper::toResponse)
                .orElseThrow(() -> new NoSuchElementException("Competidor " + id + " no encontrado"));
    }

    @Transactional
    public CompetitorResponse update(UUID id, CompetitorRequest request) {
        Competitor existing = repository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Competidor " + id + " no encontrado"));

        if (!existing.getNickname().equalsIgnoreCase(request.nickname())
                && repository.existsByNicknameIgnoreCase(request.nickname())) {
            throw new IllegalStateException(
                    "Ya existe un competidor con el nickname '" + request.nickname() + "'");
        }

        existing.setName(request.name());
        existing.setNickname(request.nickname());
        existing.setType(request.type());
        existing.setBirthDate(request.birthDate());
        existing.setWeight(request.weight());
        existing.setHeight(request.height());
        existing.setOriginCountry(request.originCountry());

        Competitor saved = repository.save(existing);

        auditPublisher.publish("UPDATE", "Competitor", id.toString(),
                "Competidor actualizado: " + saved.getNickname());

        return CompetitorMapper.toResponse(saved);
    }

    @Transactional
    public CompetitorResponse changeStatus(UUID id, CompetitorStatus newStatus) {
        Competitor existing = repository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Competidor " + id + " no encontrado"));

        // Se guarda el estado ANTERIOR antes de sobreescribirlo, porque una vez
        // que se llama a setStatus(newStatus), el valor viejo se pierde
        // (Java no guarda copias automáticas de los valores anteriores).
        CompetitorStatus previousStatus = existing.getStatus();
        existing.setStatus(newStatus);
        Competitor saved = repository.save(existing);

        auditPublisher.publish("STATUS_CHANGE", "Competitor", id.toString(),
                "Cambio de estado de competidor " + saved.getNickname(),
                previousStatus.name(), newStatus.name());

        return CompetitorMapper.toResponse(saved);
    }

    @Transactional
    public void delete(UUID id) {
        Competitor existing = repository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Competidor " + id + " no encontrado"));

        // Regla (Módulo 2): "A competitor with official results cannot be
        // physically deleted; it must be retired or deactivated."
        if (resultRepository.existsByRegistration_Competitor_Id(id)) {
            throw new IllegalStateException(
                    "Este competidor tiene resultados oficiales registrados; no se puede eliminar. "
                            + "Usa PATCH /api/competitors/" + id + "/status?status=RETIRED en su lugar");
        }

        repository.delete(existing);

        auditPublisher.publish("DELETE", "Competitor", id.toString(),
                "Competidor eliminado: " + existing.getNickname());
    }
}