package com.eia.camelracing.competitor.controller;

import com.eia.camelracing.competitor.dto.CompetitorRequest;
import com.eia.camelracing.competitor.dto.CompetitorResponse;
import com.eia.camelracing.competitor.entity.CompetitorStatus;
import com.eia.camelracing.competitor.entity.CompetitorType;
import com.eia.camelracing.competitor.service.CompetitorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/competitors")
@RequiredArgsConstructor
public class CompetitorController {

    private final CompetitorService service;

    // Escritura: solo ADMIN puede gestionar competidores (tabla de roles, Módulo 1).
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CompetitorResponse> create(@Valid @RequestBody CompetitorRequest request) {
        CompetitorResponse created = service.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    // Lectura: sin @PreAuthorize -> cualquier usuario autenticado (ADMIN,
    // ORGANIZER o VIEWER) puede consultar, tal como dice la guía.
    @GetMapping
    public ResponseEntity<Page<CompetitorResponse>> findAll(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) CompetitorType type,
            @RequestParam(required = false) CompetitorStatus status,
            Pageable pageable) {
        return ResponseEntity.ok(service.findAll(name, type, status, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CompetitorResponse> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CompetitorResponse> update(
            @PathVariable UUID id, @Valid @RequestBody CompetitorRequest request) {
        return ResponseEntity.ok(service.update(id, request));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CompetitorResponse> changeStatus(
            @PathVariable UUID id, @RequestParam CompetitorStatus status) {
        return ResponseEntity.ok(service.changeStatus(id, status));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}