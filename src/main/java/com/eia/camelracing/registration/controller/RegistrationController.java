package com.eia.camelracing.registration.controller;

import com.eia.camelracing.registration.dto.RegistrationRequest;
import com.eia.camelracing.registration.dto.RegistrationResponse;
import com.eia.camelracing.registration.dto.RejectRequest;
import com.eia.camelracing.registration.service.RegistrationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class RegistrationController {

    private final RegistrationService service;

    @PostMapping("/api/races/{raceId}/registrations")
    public ResponseEntity<RegistrationResponse> register(
            @PathVariable UUID raceId, @RequestBody RegistrationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.register(raceId, request));
    }

    @GetMapping("/api/races/{raceId}/registrations")
    public ResponseEntity<List<RegistrationResponse>> findByRace(@PathVariable UUID raceId) {
        return ResponseEntity.ok(service.findByRace(raceId));
    }

    @GetMapping("/api/registrations/{id}")
    public ResponseEntity<RegistrationResponse> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @PatchMapping("/api/registrations/{id}/approve")
    public ResponseEntity<RegistrationResponse> approve(@PathVariable UUID id) {
        return ResponseEntity.ok(service.approve(id));
    }

    @PatchMapping("/api/registrations/{id}/reject")
    public ResponseEntity<RegistrationResponse> reject(
            @PathVariable UUID id, @RequestBody RejectRequest request) {
        return ResponseEntity.ok(service.reject(id, request.reason()));
    }

    @DeleteMapping("/api/registrations/{id}")
    public ResponseEntity<Void> cancel(@PathVariable UUID id) {
        service.cancel(id);
        return ResponseEntity.noContent().build();
    }
}