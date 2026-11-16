package com.mentalhealth.mentalhealth_backend.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.mentalhealth.mentalhealth_backend.dto.AppointmentDtos.AppointmentDto;
import com.mentalhealth.mentalhealth_backend.dto.AppointmentDtos.BookRequest;
import com.mentalhealth.mentalhealth_backend.model.User;
import com.mentalhealth.mentalhealth_backend.repository.UserRepository;
import com.mentalhealth.mentalhealth_backend.security.AppUserPrincipal;
import com.mentalhealth.mentalhealth_backend.service.ApiException;
import com.mentalhealth.mentalhealth_backend.service.AppointmentService;

/**
 * GET  /api/appointments              my appointments (patient: mine; therapist: my clients')
 * POST /api/appointments              book {therapistId, scheduledTime, duration} (patients only)
 * POST /api/appointments/{id}/cancel  cancel one of my appointments
 */
@RestController
@RequestMapping("/api/appointments")
public class AppointmentController {

    private final AppointmentService appointmentService;
    private final UserRepository userRepository;

    public AppointmentController(AppointmentService appointmentService, UserRepository userRepository) {
        this.appointmentService = appointmentService;
        this.userRepository = userRepository;
    }

    @GetMapping
    public List<AppointmentDto> list(@AuthenticationPrincipal AppUserPrincipal principal) {
        User me = userRepository.findById(principal.getUserId())
                .orElseThrow(() -> ApiException.notFound("Account not found."));
        return appointmentService.listFor(me).stream().map(AppointmentDto::from).toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AppointmentDto book(@AuthenticationPrincipal AppUserPrincipal principal, @RequestBody BookRequest req) {
        return AppointmentDto.from(appointmentService.book(principal.getUserId(), req.therapistId(),
                req.scheduledTime(), req.duration()));
    }

    @PostMapping("/{id}/cancel")
    public AppointmentDto cancel(@AuthenticationPrincipal AppUserPrincipal principal, @PathVariable Integer id) {
        return AppointmentDto.from(appointmentService.cancel(principal.getUserId(), id));
    }
}
