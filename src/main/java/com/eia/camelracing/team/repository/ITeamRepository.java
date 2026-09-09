package com.eia.camelracing.team.repository;

import com.eia.camelracing.team.entity.Team;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ITeamRepository extends JpaRepository<Team, UUID> {
    boolean existsByNameIgnoreCase(String name);
}