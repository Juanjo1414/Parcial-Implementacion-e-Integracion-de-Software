// DashboardController.java
package com.eia.camelracing.security.controller;

import com.eia.camelracing.competitor.repository.ICompetitorRepository;
import com.eia.camelracing.race.entity.RaceStatus;
import com.eia.camelracing.race.repository.IRaceRepository;
import com.eia.camelracing.team.repository.ITeamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class DashboardController {

    private final ICompetitorRepository competitorRepository;
    private final ITeamRepository teamRepository;
    private final IRaceRepository raceRepository;

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        // "Model" es el puente entre el controller y la plantilla: todo lo
        // que pongas aquí, Thymeleaf lo puede leer con ${...} en el HTML.
        model.addAttribute("totalCompetitors", competitorRepository.count());
        model.addAttribute("totalTeams", teamRepository.count());
        model.addAttribute("upcomingRaces", raceRepository.findAll().stream()
                .filter(r -> r.getStatus() == RaceStatus.OPEN_FOR_REGISTRATION
                        || r.getStatus() == RaceStatus.CLOSED_FOR_REGISTRATION)
                .toList());
        return "dashboard";
    }
}