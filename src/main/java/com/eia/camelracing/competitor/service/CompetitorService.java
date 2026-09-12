package com.eia.camelracing.competitor.service;

import com.eia.camelracing.common.audit.AuditPublisher;
import com.eia.camelracing.common.exception.BusinessRuleException;
import com.eia.camelracing.common.exception.ResourceNotFoundException;
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

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CompetitorService {

    private final ICompetitorRepository repository;
    // Se consulta antes de un borrado físico: un competidor con historial oficial debe retirarse, no eliminarse.
    private final IRaceResultRepository resultRepository;
    private final AuditPublisher auditPublisher;

    @Transactional
    public CompetitorResponse create(CompetitorRequest request) {
        if (repository.existsByNicknameIgnoreCase(request.nickname())) {
            throw new BusinessRuleException(
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
                .orElseThrow(() -> new ResourceNotFoundException("Competidor " + id + " no encontrado"));
    }

    @Transactional
    public CompetitorResponse update(UUID id, CompetitorRequest request) {
        Competitor existing = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Competidor " + id + " no encontrado"));

        if (!existing.getNickname().equalsIgnoreCase(request.nickname())
                && repository.existsByNicknameIgnoreCase(request.nickname())) {
            throw new BusinessRuleException(
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
                .orElseThrow(() -> new ResourceNotFoundException("Competidor " + id + " no encontrado"));

        // Se captura el estado previo antes de sobrescribirlo para dejarlo
        // registrado en el log de auditoría junto con el nuevo valor.
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
                .orElseThrow(() -> new ResourceNotFoundException("Competidor " + id + " no encontrado"));

        // Un competidor con historial de carreras no se borra físicamente:
        // se pierde la trazabilidad de resultados pasados. Se redirige al
        // llamador hacia el endpoint de cambio de estado (retiro).
        if (resultRepository.existsByRegistration_Competitor_Id(id)) {
            throw new BusinessRuleException(
                    "Este competidor tiene resultados oficiales registrados; no se puede eliminar. "
                            + "Usa PATCH /api/competitors/" + id + "/status?status=RETIRED en su lugar");
        }

        repository.delete(existing);

        auditPublisher.publish("DELETE", "Competitor", id.toString(),
                "Competidor eliminado: " + existing.getNickname());
    }
}