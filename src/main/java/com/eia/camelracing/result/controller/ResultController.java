package com.eia.camelracing.result.controller;

import com.eia.camelracing.result.dto.ResultRequest;
import com.eia.camelracing.result.dto.ResultResponse;
import com.eia.camelracing.result.service.ResultService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class ResultController {

    private final ResultService service;

    @PostMapping("/api/races/{raceId}/results")
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANIZER')")
    public ResponseEntity<ResultResponse> record(
            @PathVariable UUID raceId, @Valid @RequestBody ResultRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.record(raceId, request));
    }

    @GetMapping("/api/races/{raceId}/results")
    public ResponseEntity<List<ResultResponse>> findByRace(@PathVariable UUID raceId) {
        return ResponseEntity.ok(service.findByRace(raceId));
    }

    @PutMapping("/api/results/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANIZER')")
    public ResponseEntity<ResultResponse> update(
            @PathVariable UUID id, @Valid @RequestBody ResultRequest request) {
        return ResponseEntity.ok(service.update(id, request));
    }

    @GetMapping("/api/results/{id}")
    public ResponseEntity<ResultResponse> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(service.findById(id));
    }
}