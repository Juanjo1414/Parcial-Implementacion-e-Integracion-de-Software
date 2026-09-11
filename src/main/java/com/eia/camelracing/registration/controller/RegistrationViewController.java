package com.eia.camelracing.registration.controller;

import com.eia.camelracing.competitor.repository.ICompetitorRepository;
import com.eia.camelracing.race.repository.IRaceRepository;
import com.eia.camelracing.registration.dto.RegistrationRequest;
import com.eia.camelracing.registration.service.RegistrationService;
import com.eia.camelracing.team.repository.ITeamRepository;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.NoSuchElementException;
import java.util.UUID;

@Controller
@RequestMapping("/races/{raceId}/registrations")
@RequiredArgsConstructor
public class RegistrationViewController {

    private final RegistrationService service;
    private final IRaceRepository raceRepository;
    private final ICompetitorRepository competitorRepository;
    private final ITeamRepository teamRepository;

    @GetMapping
    public String list(@PathVariable UUID raceId, Model model, HttpServletResponse response) {
        try {
            model.addAttribute("race", raceRepository.findById(raceId).orElseThrow());
        } catch (NoSuchElementException ex) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return "error/404";
        }
        model.addAttribute("registrations", service.findByRace(raceId));
        // Para el formulario de nueva inscripción: solo competidores ACTIVOS y equipos ACTIVOS.
        model.addAttribute("competitors", competitorRepository.findAll());
        model.addAttribute("teams", teamRepository.findAll());
        return "registrations/list";
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANIZER')")
    public String register(@PathVariable UUID raceId,
                           @RequestParam(required = false) UUID competitorId,
                           @RequestParam(required = false) UUID teamId,
                           RedirectAttributes redirectAttributes) {
        try {
            service.register(raceId, new RegistrationRequest(competitorId, teamId, null));
            redirectAttributes.addFlashAttribute("successMessage", "Inscripción registrada");
        } catch (IllegalStateException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/races/" + raceId + "/registrations";
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANIZER')")
    public String approve(@PathVariable UUID raceId, @PathVariable UUID id, RedirectAttributes redirectAttributes) {
        try {
            service.approve(id);
            redirectAttributes.addFlashAttribute("successMessage", "Inscripción aprobada");
        } catch (IllegalStateException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/races/" + raceId + "/registrations";
    }

    @PostMapping("/{id}/reject")
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANIZER')")
    public String reject(@PathVariable UUID raceId, @PathVariable UUID id,
                         @RequestParam String reason, RedirectAttributes redirectAttributes) {
        try {
            service.reject(id, reason);
            redirectAttributes.addFlashAttribute("successMessage", "Inscripción rechazada");
        } catch (IllegalStateException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/races/" + raceId + "/registrations";
    }
}