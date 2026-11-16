package com.mentalhealth.mentalhealth_backend.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.logout.HttpStatusReturningLogoutSuccessHandler;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;

/**
 * Who can call what.
 *
 * Login uses a session cookie (stored in the database by Spring Session, so it survives
 * server restarts). The cookie is HttpOnly and SameSite=Strict (see application.properties).
 * Because the cookie is never sent by other websites, CSRF tokens are not needed here.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .formLogin(AbstractHttpConfigurer::disable)
            .httpBasic(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(auth -> auth
                // Public: anyone, logged in or not
                .requestMatchers("/api/auth/**", "/api/home", "/api/resources", "/api/therapists").permitAll()
                // Therapist-only pages
                .requestMatchers("/api/therapist/**").hasRole("THERAPIST")
                // Only patients (clients and anonymous guests) can book or raise a crisis alert
                .requestMatchers(HttpMethod.POST, "/api/appointments").hasRole("CLIENT")
                .requestMatchers("/api/crisis/**").hasRole("CLIENT")
                // Everything else under /api needs a login
                .requestMatchers("/api/**").authenticated()
                // Static files (if any) are public
                .anyRequest().permitAll())
            // Not logged in -> 401 (the React app shows the login page) instead of a redirect
            .exceptionHandling(e -> e.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
            .logout(logout -> logout
                .logoutUrl("/api/auth/logout")
                .logoutSuccessHandler(new HttpStatusReturningLogoutSuccessHandler(HttpStatus.OK)));
        return http.build();
    }

    /** BCrypt: slow, salted hashing. Plain passwords are never stored. */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AppUserDetailsService userDetailsService,
            PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return new ProviderManager(provider);
    }

    /** Saves the logged-in user into the session after our JSON login endpoint runs. */
    @Bean
    public SecurityContextRepository securityContextRepository() {
        return new HttpSessionSecurityContextRepository();
    }
}
