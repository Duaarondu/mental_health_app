package com.mentalhealth.mentalhealth_backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.mentalhealth.mentalhealth_backend.model.User;

public interface UserRepository extends JpaRepository<User, Integer> {

    Optional<User> findByUsername(String username);

    boolean existsByUsername(String username);

    @Query("select u from User u where u.role = :role and u.isVerified = true order by u.lastName")
    List<User> findVerifiedByRole(@Param("role") String role);
}
