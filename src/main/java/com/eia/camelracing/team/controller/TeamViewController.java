package com.eia.camelracing.team.controller;

import com.eia.camelracing.competitor.repository.ICompetitorRepository;
import com.eia.camelracing.team.dto.TeamRequest;
import com.eia.camelracing.team.service.TeamService;
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
@RequestMapping("/teams")
@RequiredArgsConstructor
public class TeamViewController {

    private final TeamService service;
    private final ICompetitorRepository competitorRepository; // para el selector de "agregar miembro"

    @GetMapping
    public String list(Model model) {
        model.addAttribute("teams", service.findAll());
        return "teams/list";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable UUID id, Model model, HttpServletResponse response) {
        try {
            model.addAttribute("team", service.findById(id));
        } catch (NoSuchElementException ex) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return "error/404";
        }
        // Lista de competidores SIN equipo, para poblar el selector de "agregar miembro".
        model.addAttribute("availableCompetitors", competitorRepository.findAll().stream()
                .filter(c -> c.getTeam() == null).toList());
        return "teams/detail";
    }

    @GetMapping("/new")
    @PreAuthorize("hasRole('ADMIN')")
    public String newForm() {
        return "teams/form";
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public String create(@RequestParam String name, @RequestParam(required = false) String description,
                         @RequestParam(required = false) String coachName,
                         RedirectAttributes redirectAttributes) {
        try {
            var created = service.create(new TeamRequest(name, description, coachName));
            redirectAttributes.addFlashAttribute("successMessage", "Equipo creado correctamente");
            return "redirect:/teams/" + created.id();
        } catch (IllegalStateException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/teams/new";
        }
    }

    @PostMapping("/{teamId}/members/{competitorId}")
    @PreAuthorize("hasRole('ADMIN')")
    public String addMember(@PathVariable UUID teamId, @PathVariable UUID competitorId,
                            RedirectAttributes redirectAttributes) {
        try {
            service.addMember(teamId, competitorId);
            redirectAttributes.addFlashAttribute("successMessage", "Miembro agregado");
        } catch (IllegalStateException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/teams/" + teamId;
    }

    @PostMapping("/{teamId}/members/{competitorId}/remove")
    @PreAuthorize("hasRole('ADMIN')")
    public String removeMember(@PathVariable UUID teamId, @PathVariable UUID competitorId,
                               RedirectAttributes redirectAttributes) {
        service.removeMember(teamId, competitorId);
        redirectAttributes.addFlashAttribute("successMessage", "Miembro removido");
        return "redirect:/teams/" + teamId;
    }

    @PostMapping("/{id}/delete")
    @PreAuthorize("hasRole('ADMIN')")
    public String delete(@PathVariable UUID id, RedirectAttributes redirectAttributes) {
        try {
            service.delete(id);
            redirectAttributes.addFlashAttribute("successMessage", "Equipo eliminado");
        } catch (IllegalStateException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/teams/" + id;
        }
        return "redirect:/teams";
    }
}