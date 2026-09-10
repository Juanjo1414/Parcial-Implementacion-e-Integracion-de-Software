package com.eia.camelracing.registration.mapper;

import com.eia.camelracing.registration.dto.RegistrationResponse;
import com.eia.camelracing.registration.entity.RaceRegistration;

public class RegistrationMapper {

    private RegistrationMapper() {}

    public static RegistrationResponse toResponse(RaceRegistration r) {
        if (r == null) return null;
        return new RegistrationResponse(
                r.getId(),
                r.getRace().getId(),
                r.getCompetitor() != null ? r.getCompetitor().getId() : null,
                r.getCompetitor() != null ? r.getCompetitor().getNickname() : null,
                r.getTeam() != null ? r.getTeam().getId() : null,
                r.getTeam() != null ? r.getTeam().getName() : null,
                r.getRegistrationDate(),
                r.getStatus(),
                r.getStartingPosition(),
                r.getValidationNotes(),
                r.getPerformedBy()
        );
    }
}