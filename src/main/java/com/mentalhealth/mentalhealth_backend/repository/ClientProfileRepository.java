package com.mentalhealth.mentalhealth_backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mentalhealth.mentalhealth_backend.model.ClientProfile;

public interface ClientProfileRepository extends JpaRepository<ClientProfile, Integer> {
}
