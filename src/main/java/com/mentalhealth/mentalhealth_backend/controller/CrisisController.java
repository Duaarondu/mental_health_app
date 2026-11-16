package com.mentalhealth.mentalhealth_backend.controller;

import java.util.List;
import java.util.Map;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mentalhealth.mentalhealth_backend.dto.CrisisDtos.CrisisAlertDto;
import com.mentalhealth.mentalhealth_backend.dto.CrisisDtos.CrisisAlertResult;
import com.mentalhealth.mentalhealth_backend.security.AppUserPrincipal;
import com.mentalhealth.mentalhealth_backend.service.CrisisService;

/**
 * POST /api/crisis/alert                            patient: "let my therapist know"
 * GET  /api/therapist/alerts                        therapist: my clients who asked for help
 * POST /api/therapist/alerts/{clientId}/resolve     therapist: I've followed up
 */
@RestController
public class CrisisController {

    private final CrisisService crisisService;

    public CrisisController(CrisisService crisisService) {
        this.crisisService = crisisService;
    }

    @PostMapping("/api/crisis/alert")
    public CrisisAlertResult alert(@AuthenticationPrincipal AppUserPrincipal principal) {
        return crisisService.raiseAlert(principal.getUserId());
    }

    @GetMapping("/api/therapist/alerts")
    public List<CrisisAlertDto> alerts(@AuthenticationPrincipal AppUserPrincipal principal) {
        return crisisService.alertsFor(principal.getUserId());
    }

    @PostMapping("/api/therapist/alerts/{clientId}/resolve")
    public Map<String, Boolean> resolve(@AuthenticationPrincipal AppUserPrincipal principal,
            @PathVariable Integer clientId) {
        crisisService.resolve(principal.getUserId(), clientId);
        return Map.of("resolved", true);
    }
}
