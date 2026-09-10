package com.eia.camelracing.result.mapper;

import com.eia.camelracing.result.dto.ResultResponse;
import com.eia.camelracing.result.entity.RaceResult;
import com.eia.camelracing.result.entity.ResultPoints;

public class ResultMapper {

    private ResultMapper() {}

    public static ResultResponse toResponse(RaceResult r) {
        if (r == null) return null;
        var registration = r.getRegistration();

        String label = registration.getCompetitor() != null
                ? registration.getCompetitor().getNickname()
                : registration.getTeam().getName();

        return new ResultResponse(
                r.getId(),
                registration.getId(),
                registration.getRace().getId(),
                label,
                r.getFinalPosition(),
                r.getCompletionTimeSeconds(),
                r.getPenaltyTimeSeconds(),
                r.getStatus(),
                ResultPoints.pointsFor(r.getStatus(), r.getFinalPosition()),
                r.getNotes(),
                r.getRecordedBy(),
                r.getRecordedAt()
        );
    }
}