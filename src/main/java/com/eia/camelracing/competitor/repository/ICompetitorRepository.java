package com.eia.camelracing.competitor.repository;

import com.eia.camelracing.competitor.entity.Competitor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;
import java.util.UUID;

// JpaSpecificationExecutor nos da el metodo findAll(Specification, Pageable),
// que usamos para combinar filtros dinámicos + paginación + orden.
public interface ICompetitorRepository extends
        JpaRepository<Competitor, UUID>,
        JpaSpecificationExecutor<Competitor> {

    boolean existsByNicknameIgnoreCase(String nickname);
}