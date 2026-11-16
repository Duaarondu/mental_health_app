package com.mentalhealth.mentalhealth_backend.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mentalhealth.mentalhealth_backend.dto.AppointmentDtos.TherapistDto;
import com.mentalhealth.mentalhealth_backend.model.Role;
import com.mentalhealth.mentalhealth_backend.model.TherapistProfile;
import com.mentalhealth.mentalhealth_backend.repository.TherapistProfileRepository;
import com.mentalhealth.mentalhealth_backend.repository.UserRepository;

/** GET /api/therapists: verified therapists that patients can book. */
@RestController
public class TherapistController {

    private final UserRepository userRepository;
    private final TherapistProfileRepository therapistProfileRepository;

    public TherapistController(UserRepository userRepository, TherapistProfileRepository therapistProfileRepository) {
        this.userRepository = userRepository;
        this.therapistProfileRepository = therapistProfileRepository;
    }

    @GetMapping("/api/therapists")
    public List<TherapistDto> therapists() {
        return userRepository.findVerifiedByRole(Role.THERAPIST).stream()
                .map(t -> new TherapistDto(t.getUserId(), t.getDisplayName(),
                        therapistProfileRepository.findById(t.getUserId())
                                .map(TherapistProfile::getSpecializations)
                                .orElse("")))
                .toList();
    }
}
