package com.eia.camelracing.team.mapper;

import com.eia.camelracing.team.dto.TeamMemberSummary;
import com.eia.camelracing.team.dto.TeamRequest;
import com.eia.camelracing.team.dto.TeamResponse;
import com.eia.camelracing.team.entity.Team;

public class TeamMapper {

    private TeamMapper() {}

    public static Team toEntity(TeamRequest request) {
        if (request == null) return null;
        return Team.builder()
                .name(request.name())
                .description(request.description())
                .coachName(request.coachName())
                .build();
    }

    public static TeamResponse toResponse(Team team) {
        if (team == null) return null;
        return new TeamResponse(
                team.getId(),
                team.getName(),
                team.getDescription(),
                team.getCoachName(),
                team.getStatus(),
                team.getCreatedAt(),
                team.getWins(),
                team.getLosses(),
                team.getMembers().stream()
                        .map(c -> new TeamMemberSummary(c.getId(), c.getName(), c.getNickname()))
                        .toList()
        );
    }
}