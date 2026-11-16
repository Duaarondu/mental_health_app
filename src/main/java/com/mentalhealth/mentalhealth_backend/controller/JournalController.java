package com.mentalhealth.mentalhealth_backend.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.mentalhealth.mentalhealth_backend.dto.JournalDtos.JournalEntryDto;
import com.mentalhealth.mentalhealth_backend.dto.JournalDtos.NewEntryRequest;
import com.mentalhealth.mentalhealth_backend.security.AppUserPrincipal;
import com.mentalhealth.mentalhealth_backend.service.JournalService;

/**
 * GET    /api/journal              patient: my entries, newest first (decrypted for me only)
 * POST   /api/journal              patient: {moodScore 1-5, content} (empty content = mood check-in)
 * DELETE /api/journal/{id}         patient: delete one of my entries
 */
@RestController
public class JournalController {

    private final JournalService journalService;

    public JournalController(JournalService journalService) {
        this.journalService = journalService;
    }

    @GetMapping("/api/journal")
    public List<JournalEntryDto> list(@AuthenticationPrincipal AppUserPrincipal principal) {
        return journalService.listFor(principal.getUserId());
    }

    @PostMapping("/api/journal")
    @ResponseStatus(HttpStatus.CREATED)
    public JournalEntryDto add(@AuthenticationPrincipal AppUserPrincipal principal, @RequestBody NewEntryRequest req) {
        return journalService.add(principal.getUserId(), req.moodScore(), req.content());
    }

    @DeleteMapping("/api/journal/{id}")
    public Map<String, Boolean> delete(@AuthenticationPrincipal AppUserPrincipal principal, @PathVariable Integer id) {
        journalService.delete(principal.getUserId(), id);
        return Map.of("deleted", true);
    }
}
