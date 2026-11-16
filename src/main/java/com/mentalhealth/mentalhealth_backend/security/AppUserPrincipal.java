package com.mentalhealth.mentalhealth_backend.security;

import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.mentalhealth.mentalhealth_backend.model.Role;
import com.mentalhealth.mentalhealth_backend.model.User;

/**
 * The logged-in user as Spring Security sees it.
 * Roles become authorities: Client -> ROLE_CLIENT, Therapist -> ROLE_THERAPIST,
 * Anonymous -> ROLE_CLIENT + ROLE_GUEST (guests can do everything a client can).
 */
public class AppUserPrincipal implements UserDetails {

    private final Integer userId;
    private final String username;
    private final String passwordHash;
    private final String role;

    public AppUserPrincipal(User user) {
        this.userId = user.getUserId();
        this.username = user.getUsername();
        this.passwordHash = user.getPasswordHash();
        this.role = user.getRole();
    }

    public Integer getUserId() {
        return userId;
    }

    public String getRole() {
        return role;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return switch (role) {
            case Role.THERAPIST -> List.of(new SimpleGrantedAuthority("ROLE_THERAPIST"));
            case Role.ANONYMOUS -> List.of(new SimpleGrantedAuthority("ROLE_CLIENT"),
                    new SimpleGrantedAuthority("ROLE_GUEST"));
            case Role.ADMIN -> List.of(new SimpleGrantedAuthority("ROLE_ADMIN"));
            default -> List.of(new SimpleGrantedAuthority("ROLE_CLIENT"));
        };
    }

    @Override
    public String getPassword() {
        return passwordHash;
    }

    @Override
    public String getUsername() {
        return username;
    }
}
