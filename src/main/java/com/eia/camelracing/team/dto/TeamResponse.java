package com.eia.camelracing.team.dto;

import com.eia.camelracing.team.entity.TeamStatus;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record TeamResponse(
        UUID id,
        String name,
        String description,
        String coachName,
        TeamStatus status,
        LocalDateTime createdAt,
        Integer wins,
        Integer losses,
        List<TeamMemberSummary> members
) {}