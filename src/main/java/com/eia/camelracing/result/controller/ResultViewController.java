package com.eia.camelracing.result.controller;

import com.eia.camelracing.race.repository.IRaceRepository;
import com.eia.camelracing.registration.entity.RegistrationStatus;
import com.eia.camelracing.registration.repository.IRaceRegistrationRepository;
import com.eia.camelracing.result.dto.ResultRequest;
import com.eia.camelracing.result.entity.ResultStatus;
import com.eia.camelracing.result.repository.IRaceResultRepository;
import com.eia.camelracing.result.service.ResultService;
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
@RequestMapping("/races/{raceId}/results")
@RequiredArgsConstructor
public class ResultViewController {

    private final ResultService service;
    private final IRaceRepository raceRepository;
    private final IRaceRegistrationRepository registrationRepository;
    private final IRaceResultRepository resultRepository;

    @GetMapping
    public String list(@PathVariable UUID raceId, Model model, HttpServletResponse response) {
        try {
            model.addAttribute("race", raceRepository.findById(raceId).orElseThrow());
        } catch (NoSuchElementException ex) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return "error/404";
        }
        model.addAttribute("results", service.findByRace(raceId));
        // Inscripciones aprobadas que TODAVÍA no tienen resultado registrado.
        model.addAttribute("pendingRegistrations", registrationRepository.findByRaceId(raceId).stream()
                .filter(r -> r.getStatus() == RegistrationStatus.APPROVED)
                .filter(r -> !resultRepository.existsByRegistration_Id(r.getId()))
                .toList());
        model.addAttribute("statuses", ResultStatus.values());
        return "results/list";
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANIZER')")
    public String record(@PathVariable UUID raceId,
                         @RequestParam UUID registrationId,
                         @RequestParam(required = false) Integer finalPosition,
                         @RequestParam(required = false) Double completionTimeSeconds,
                         @RequestParam(required = false, defaultValue = "0") Double penaltyTimeSeconds,
                         @RequestParam ResultStatus status,
                         @RequestParam(required = false) String notes,
                         RedirectAttributes redirectAttributes) {
        try {
            service.record(raceId, new ResultRequest(
                    registrationId, finalPosition, completionTimeSeconds, penaltyTimeSeconds, status, notes));
            redirectAttributes.addFlashAttribute("successMessage", "Resultado registrado");
        } catch (IllegalStateException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/races/" + raceId + "/results";
    }
}