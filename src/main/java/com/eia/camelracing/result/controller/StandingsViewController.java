package com.eia.camelracing.result.controller;

import com.eia.camelracing.result.service.StandingsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class StandingsViewController {

    private final StandingsService service;

    @GetMapping("/standings")
    public String standings(Model model) {
        model.addAttribute("competitorStandings", service.competitorStandings());
        model.addAttribute("teamStandings", service.teamStandings());
        return "standings/index";
    }
}