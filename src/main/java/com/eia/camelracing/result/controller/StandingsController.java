package com.eia.camelracing.result.controller;

import com.eia.camelracing.result.dto.CompetitorStandingResponse;
import com.eia.camelracing.result.dto.TeamStandingResponse;
import com.eia.camelracing.result.service.StandingsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class StandingsController {

    private final StandingsService service;

    // Ningún método lleva @PreAuthorize: es de solo lectura, y la guía dice
    // que hasta el rol VIEWER puede consultar "results and standings" -> por
    // eso alcanza con estar autenticado (regla general del SecurityConfig),
    // sin restricción de rol adicional.

    @GetMapping("/api/standings/competitors")
    public ResponseEntity<List<CompetitorStandingResponse>> competitors() {
        return ResponseEntity.ok(service.competitorStandings());
    }

    @GetMapping("/api/standings/teams")
    public ResponseEntity<List<TeamStandingResponse>> teams() {
        return ResponseEntity.ok(service.teamStandings());
    }

    @GetMapping("/api/standings")
    public ResponseEntity<Map<String, Object>> general() {
        return ResponseEntity.ok(Map.of(
                "competitors", service.competitorStandings(),
                "teams", service.teamStandings()
        ));
    }
}