package com.eia.camelracing.competitor.service;

import com.eia.camelracing.competitor.dto.CompetitorRequest;
import com.eia.camelracing.competitor.dto.CompetitorResponse;
import com.eia.camelracing.competitor.entity.Competitor;
import com.eia.camelracing.competitor.entity.CompetitorStatus;
import com.eia.camelracing.competitor.entity.CompetitorType;
import com.eia.camelracing.competitor.mapper.CompetitorMapper;
import com.eia.camelracing.competitor.repository.CompetitorSpecifications;
import com.eia.camelracing.competitor.repository.ICompetitorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;
import java.util.UUID;

@Service
@RequiredArgsConstructor // Lombok genera el constructor con "final" fields -> inyección de dependencias
public class CompetitorService {

    private final ICompetitorRepository repository;

    @Transactional
    public CompetitorResponse create(CompetitorRequest request) {
        // Regla de negocio: "Nickname must be unique" (no alcanza con @Valid).
        if (repository.existsByNicknameIgnoreCase(request.nickname())) {
            throw new IllegalStateException(
                    "Ya existe un competidor con el nickname '" + request.nickname() + "'");
        }
        Competitor saved = repository.save(CompetitorMapper.toEntity(request));
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

        // Si cambió el nickname, hay que volver a chequear unicidad.
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

        return CompetitorMapper.toResponse(repository.save(existing));
    }

    @Transactional
    public CompetitorResponse changeStatus(UUID id, CompetitorStatus newStatus) {
        Competitor existing = repository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Competidor " + id + " no encontrado"));
        existing.setStatus(newStatus);
        return CompetitorMapper.toResponse(repository.save(existing));
    }

    @Transactional
    public void delete(UUID id) {
        // Regla de negocio: "A competitor with official results cannot be
        // physically deleted; it must be retired or deactivated."
        // Todavía no existe el módulo Result (lo hacemos en la Etapa 5), así que
        // por ahora dejamos el borrado físico y volvemos a esta regla más
        // adelante cuando exista RaceResult para consultar.
        Competitor existing = repository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Competidor " + id + " no encontrado"));
        repository.delete(existing);
    }
}