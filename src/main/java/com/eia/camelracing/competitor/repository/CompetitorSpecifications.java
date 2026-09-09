package com.eia.camelracing.competitor.repository;

import com.eia.camelracing.competitor.entity.Competitor;
import com.eia.camelracing.competitor.entity.CompetitorStatus;
import com.eia.camelracing.competitor.entity.CompetitorType;
import org.springframework.data.jpa.domain.Specification;

// Construye el filtro WHERE dinámicamente: si un parámetro es null,
// simplemente no se agrega esa condición.
public class CompetitorSpecifications {

    private CompetitorSpecifications() {}

    public static Specification<Competitor> withFilters(
            String name, CompetitorType type, CompetitorStatus status) {

        return (root, query, cb) -> {
            var predicates = cb.conjunction(); // empieza como "true AND ..."

            if (name != null && !name.isBlank()) {
                predicates = cb.and(predicates,
                        cb.like(cb.lower(root.get("name")), "%" + name.toLowerCase() + "%"));
            }
            if (type != null) {
                predicates = cb.and(predicates, cb.equal(root.get("type"), type));
            }
            if (status != null) {
                predicates = cb.and(predicates, cb.equal(root.get("status"), status));
            }
            return predicates;
        };
    }
}