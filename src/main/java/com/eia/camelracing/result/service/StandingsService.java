package com.eia.camelracing.result.service;

import com.eia.camelracing.competitor.entity.Competitor;
import com.eia.camelracing.competitor.repository.ICompetitorRepository;
import com.eia.camelracing.result.dto.CompetitorStandingResponse;
import com.eia.camelracing.result.dto.TeamStandingResponse;
import com.eia.camelracing.result.entity.RaceResult;
import com.eia.camelracing.result.entity.ResultPoints;
import com.eia.camelracing.result.repository.IRaceResultRepository;
import com.eia.camelracing.team.entity.Team;
import com.eia.camelracing.team.repository.ITeamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StandingsService {

    private final IRaceResultRepository resultRepository;
    private final ICompetitorRepository competitorRepository;
    private final ITeamRepository teamRepository;

    @Transactional(readOnly = true)
    public List<CompetitorStandingResponse> competitorStandings() {
        List<RaceResult> allResults = resultRepository.findAll();

        // Agrupamos todos los resultados por competitor.id (ignorando los de equipo).
        var pointsByCompetitor = allResults.stream()
                .filter(r -> r.getRegistration().getCompetitor() != null)
                .collect(Collectors.groupingBy(
                        r -> r.getRegistration().getCompetitor().getId(),
                        Collectors.summingInt(r -> ResultPoints.pointsFor(r.getStatus(), r.getFinalPosition()))
                ));

        return competitorRepository.findAll().stream()
                .filter(c -> pointsByCompetitor.containsKey(c.getId())) // solo los que han corrido
                .map(c -> new CompetitorStandingResponse(
                        c.getId(),
                        c.getNickname(),
                        pointsByCompetitor.getOrDefault(c.getId(), 0),
                        c.getWins(),
                        c.getRacesCompleted()))
                .sorted(Comparator.comparingInt(CompetitorStandingResponse::totalPoints).reversed())
                .toList();
    }

    @Transactional(readOnly = true)
    public List<TeamStandingResponse> teamStandings() {
        List<RaceResult> allResults = resultRepository.findAll();

        var pointsByTeam = allResults.stream()
                .filter(r -> r.getRegistration().getTeam() != null)
                .collect(Collectors.groupingBy(
                        r -> r.getRegistration().getTeam().getId(),
                        Collectors.summingInt(r -> ResultPoints.pointsFor(r.getStatus(), r.getFinalPosition()))
                ));

        return teamRepository.findAll().stream()
                .filter(t -> pointsByTeam.containsKey(t.getId()))
                .map(t -> new TeamStandingResponse(
                        t.getId(),
                        t.getName(),
                        pointsByTeam.getOrDefault(t.getId(), 0),
                        t.getWins(),
                        t.getWins() + t.getLosses()))
                .sorted(Comparator.comparingInt(TeamStandingResponse::totalPoints).reversed())
                .toList();
    }
}