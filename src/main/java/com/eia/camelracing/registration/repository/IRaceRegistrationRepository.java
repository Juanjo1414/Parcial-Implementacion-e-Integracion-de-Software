package com.eia.camelracing.registration.repository;

import com.eia.camelracing.registration.entity.RaceRegistration;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface IRaceRegistrationRepository extends JpaRepository<RaceRegistration, UUID> {

    List<RaceRegistration> findByRaceId(UUID raceId);

    boolean existsByRaceIdAndCompetitorId(UUID raceId, UUID competitorId);

    boolean existsByRaceIdAndTeamId(UUID raceId, UUID teamId);

    boolean existsByRaceIdAndStartingPosition(UUID raceId, Integer startingPosition);
}