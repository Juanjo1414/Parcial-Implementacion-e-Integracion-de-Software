package com.eia.camelracing.common.audit.controller;

import com.eia.camelracing.common.audit.repository.IAuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class AuditViewController {

    private final IAuditLogRepository repository;

    @GetMapping("/audit")
    @PreAuthorize("hasRole('ADMIN')")
    public String list(@RequestParam(defaultValue = "0") int page, Model model) {
        var result = repository.findAll(PageRequest.of(page, 20, Sort.by(Sort.Direction.DESC, "timestamp")));
        model.addAttribute("logsPage", result);
        return "audit/list";
    }
}