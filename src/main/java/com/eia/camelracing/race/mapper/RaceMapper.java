package com.eia.camelracing.race.mapper;

import com.eia.camelracing.race.dto.RaceRequest;
import com.eia.camelracing.race.dto.RaceResponse;
import com.eia.camelracing.race.entity.Race;

public class RaceMapper {

    private RaceMapper() {}

    public static Race toEntity(RaceRequest request) {
        if (request == null) return null;
        return Race.builder()
                .name(request.name())
                .description(request.description())
                .scheduledAt(request.scheduledAt())
                .startLocation(request.startLocation())
                .finishLocation(request.finishLocation())
                .distanceMeters(request.distanceMeters())
                .maxParticipants(request.maxParticipants())
                .type(request.type())
                .organizerName(request.organizerName())
                .registrationDeadline(request.registrationDeadline())
                .build();
    }

    public static RaceResponse toResponse(Race race) {
        if (race == null) return null;
        return new RaceResponse(
                race.getId(), race.getName(), race.getDescription(), race.getScheduledAt(),
                race.getStartLocation(), race.getFinishLocation(), race.getDistanceMeters(),
                race.getMaxParticipants(), race.getType(), race.getStatus(),
                race.getOrganizerName(), race.getRegistrationDeadline(),
                race.getCreatedAt(), race.getUpdatedAt()
        );
    }
}