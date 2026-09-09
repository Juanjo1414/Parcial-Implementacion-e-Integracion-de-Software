package com.eia.camelracing.team.controller;

import com.eia.camelracing.team.dto.TeamRequest;
import com.eia.camelracing.team.dto.TeamResponse;
import com.eia.camelracing.team.service.TeamService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/teams")
@RequiredArgsConstructor
public class TeamController {

    private final TeamService service;

    @PostMapping
    public ResponseEntity<TeamResponse> create(@Valid @RequestBody TeamRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request));
    }

    @GetMapping
    public ResponseEntity<List<TeamResponse>> findAll() {
        return ResponseEntity.ok(service.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<TeamResponse> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TeamResponse> update(@PathVariable UUID id, @Valid @RequestBody TeamRequest request) {
        return ResponseEntity.ok(service.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{teamId}/members/{competitorId}")
    public ResponseEntity<TeamResponse> addMember(
            @PathVariable UUID teamId, @PathVariable UUID competitorId) {
        return ResponseEntity.ok(service.addMember(teamId, competitorId));
    }

    @DeleteMapping("/{teamId}/members/{competitorId}")
    public ResponseEntity<TeamResponse> removeMember(
            @PathVariable UUID teamId, @PathVariable UUID competitorId) {
        return ResponseEntity.ok(service.removeMember(teamId, competitorId));
    }
}