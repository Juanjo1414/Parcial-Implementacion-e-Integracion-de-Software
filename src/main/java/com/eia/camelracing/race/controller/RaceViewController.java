package com.eia.camelracing.race.controller;

import com.eia.camelracing.race.dto.RaceRequest;
import com.eia.camelracing.race.entity.RaceStatus;
import com.eia.camelracing.race.entity.RaceType;
import com.eia.camelracing.race.service.RaceService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.NoSuchElementException;
import java.util.UUID;

@Controller
@RequestMapping("/races")
@RequiredArgsConstructor
public class RaceViewController {

    private final RaceService service;

    @GetMapping
    public String list(Model model) {
        model.addAttribute("races", service.findAll());
        return "races/list";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable UUID id, Model model, HttpServletResponse response) {
        try {
            model.addAttribute("race", service.findById(id));
        } catch (NoSuchElementException ex) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return "error/404";
        }
        return "races/detail";
    }

    @GetMapping("/new")
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANIZER')")
    public String newForm(Model model) {
        model.addAttribute("types", RaceType.values());
        return "races/form";
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANIZER')")
    public String create(
            @RequestParam String name, @RequestParam(required = false) String description,
            @RequestParam @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME) LocalDateTime scheduledAt,
            @RequestParam(required = false) String startLocation, @RequestParam(required = false) String finishLocation,
            @RequestParam Double distanceMeters, @RequestParam Integer maxParticipants,
            @RequestParam RaceType type, @RequestParam(required = false) String organizerName,
            @RequestParam @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME) LocalDateTime registrationDeadline,
            RedirectAttributes redirectAttributes) {
        try {
            var created = service.create(new RaceRequest(name, description, scheduledAt, startLocation,
                    finishLocation, distanceMeters, maxParticipants, type, organizerName, registrationDeadline));
            redirectAttributes.addFlashAttribute("successMessage", "Carrera creada correctamente");
            return "redirect:/races/" + created.id();
        } catch (IllegalStateException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/races/new";
        }
    }

    @PostMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANIZER')")
    public String changeStatus(@PathVariable UUID id, @RequestParam RaceStatus status,
                               RedirectAttributes redirectAttributes) {
        try {
            service.changeStatus(id, status);
            redirectAttributes.addFlashAttribute("successMessage", "Estado de la carrera actualizado");
        } catch (IllegalStateException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/races/" + id;
    }
}