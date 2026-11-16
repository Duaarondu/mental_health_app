package com.mentalhealth.mentalhealth_backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mentalhealth.mentalhealth_backend.model.TherapistProfile;

public interface TherapistProfileRepository extends JpaRepository<TherapistProfile, Integer> {
}
