package com.eia.camelracing.competitor.controller;

import com.eia.camelracing.competitor.dto.CompetitorRequest;
import com.eia.camelracing.competitor.entity.CompetitorStatus;
import com.eia.camelracing.competitor.entity.CompetitorType;
import com.eia.camelracing.competitor.service.CompetitorService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.NoSuchElementException;
import java.util.UUID;

@Controller
@RequestMapping("/competitors")
@RequiredArgsConstructor
public class CompetitorViewController {

    private final CompetitorService service;

    @GetMapping
    public String list(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) CompetitorType type,
            @RequestParam(required = false) CompetitorStatus status,
            @RequestParam(defaultValue = "0") int page,
            Model model) {

        Pageable pageable = PageRequest.of(page, 10);
        var result = service.findAll(name, type, status, pageable);

        model.addAttribute("competitorsPage", result);
        model.addAttribute("types", CompetitorType.values());
        model.addAttribute("statuses", CompetitorStatus.values());
        model.addAttribute("filterName", name);
        model.addAttribute("filterType", type);
        model.addAttribute("filterStatus", status);
        return "competitors/list";
    }

    @GetMapping("/new")
    @PreAuthorize("hasRole('ADMIN')")
    public String newForm(Model model) {
        model.addAttribute("formAction", "/competitors");
        model.addAttribute("types", CompetitorType.values());
        model.addAttribute("isEdit", false);
        return "competitors/form";
    }

    // CORREGIDO: ahora captura NoSuchElementException si el id no existe
    // (por ejemplo, alguien escribe una URL con un id inventado o ya
    // borrado) y muestra la página 404 en vez de romper con un error 500.
    @GetMapping("/{id}/edit")
    @PreAuthorize("hasRole('ADMIN')")
    public String editForm(@PathVariable UUID id, Model model, HttpServletResponse response) {
        try {
            model.addAttribute("competitor", service.findById(id));
        } catch (NoSuchElementException ex) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return "error/404";
        }
        model.addAttribute("formAction", "/competitors/" + id);
        model.addAttribute("types", CompetitorType.values());
        model.addAttribute("isEdit", true);
        return "competitors/form";
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public String create(
            @RequestParam String name, @RequestParam String nickname,
            @RequestParam CompetitorType type,
            @RequestParam(required = false) LocalDate birthDate,
            @RequestParam Double weight, @RequestParam Double height,
            @RequestParam(required = false) String originCountry,
            RedirectAttributes redirectAttributes) {
        try {
            service.create(new CompetitorRequest(name, nickname, type, birthDate, weight, height, originCountry));
            redirectAttributes.addFlashAttribute("successMessage", "Competidor creado correctamente");
        } catch (IllegalStateException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/competitors";
    }

    @PostMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String update(
            @PathVariable UUID id,
            @RequestParam String name, @RequestParam String nickname,
            @RequestParam CompetitorType type,
            @RequestParam(required = false) LocalDate birthDate,
            @RequestParam Double weight, @RequestParam Double height,
            @RequestParam(required = false) String originCountry,
            RedirectAttributes redirectAttributes) {
        try {
            service.update(id, new CompetitorRequest(name, nickname, type, birthDate, weight, height, originCountry));
            redirectAttributes.addFlashAttribute("successMessage", "Competidor actualizado correctamente");
        } catch (IllegalStateException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/competitors";
    }

    @PostMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public String changeStatus(@PathVariable UUID id, @RequestParam CompetitorStatus status,
                               RedirectAttributes redirectAttributes) {
        service.changeStatus(id, status);
        redirectAttributes.addFlashAttribute("successMessage", "Estado actualizado");
        return "redirect:/competitors";
    }

    @PostMapping("/{id}/delete")
    @PreAuthorize("hasRole('ADMIN')")
    public String delete(@PathVariable UUID id, RedirectAttributes redirectAttributes) {
        try {
            service.delete(id);
            redirectAttributes.addFlashAttribute("successMessage", "Competidor eliminado");
        } catch (IllegalStateException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/competitors";
    }
}