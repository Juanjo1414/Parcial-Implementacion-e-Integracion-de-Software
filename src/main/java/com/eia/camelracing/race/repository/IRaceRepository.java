package com.eia.camelracing.race.repository;

import com.eia.camelracing.race.entity.Race;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface IRaceRepository extends JpaRepository<Race, UUID> {
}