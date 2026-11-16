package com.mentalhealth.mentalhealth_backend.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mentalhealth.mentalhealth_backend.dto.AuthRequests.AnonymousRequest;
import com.mentalhealth.mentalhealth_backend.dto.AuthRequests.AnonymousResponse;
import com.mentalhealth.mentalhealth_backend.dto.AuthRequests.LoginRequest;
import com.mentalhealth.mentalhealth_backend.dto.AuthRequests.RegisterRequest;
import com.mentalhealth.mentalhealth_backend.dto.UserDto;
import com.mentalhealth.mentalhealth_backend.model.User;
import com.mentalhealth.mentalhealth_backend.repository.UserRepository;
import com.mentalhealth.mentalhealth_backend.security.AppUserPrincipal;
import com.mentalhealth.mentalhealth_backend.service.ApiException;
import com.mentalhealth.mentalhealth_backend.service.AuthService;
import com.mentalhealth.mentalhealth_backend.service.AuthService.AnonymousAccount;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * POST /api/auth/login      {username, password, accountType: "patient" | "therapist"}
 * POST /api/auth/register   {username, password, firstName, lastName, accountType, specializations}
 * POST /api/auth/anonymous  {nickname}  -> also returns a one-time recovery code
 * GET  /api/auth/me         the logged-in user, or 401
 * POST /api/auth/logout     (handled by SecurityConfig)
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final AuthenticationManager authenticationManager;
    private final SecurityContextRepository securityContextRepository;
    private final UserRepository userRepository;

    public AuthController(AuthService authService, AuthenticationManager authenticationManager,
            SecurityContextRepository securityContextRepository, UserRepository userRepository) {
        this.authService = authService;
        this.authenticationManager = authenticationManager;
        this.securityContextRepository = securityContextRepository;
        this.userRepository = userRepository;
    }

    @PostMapping("/login")
    public UserDto login(@RequestBody LoginRequest req, HttpServletRequest request, HttpServletResponse response) {
        if (req.username() == null || req.username().isBlank() || req.password() == null || req.password().isEmpty()) {
            throw ApiException.badRequest("Please enter your username and password.");
        }
        Authentication auth = authenticate(req.username().trim(), req.password());
        User user = currentUser(auth);

        // The Patient / Therapist toggle on the login page must match the account
        if ("therapist".equalsIgnoreCase(req.accountType()) && !user.isTherapist()) {
            throw ApiException.forbidden("This is a patient account. Switch to Patient to sign in.");
        }
        if ("patient".equalsIgnoreCase(req.accountType()) && user.isTherapist()) {
            throw ApiException.forbidden("This is a therapist account. Switch to Therapist to sign in.");
        }

        startSession(auth, request, response);
        return UserDto.from(user);
    }

    @PostMapping("/register")
    public UserDto register(@RequestBody RegisterRequest req, HttpServletRequest request,
            HttpServletResponse response) {
        User user = authService.register(req);
        Authentication auth = authenticate(user.getUsername(), req.password());
        startSession(auth, request, response);
        return UserDto.from(user);
    }

    @PostMapping("/anonymous")
    public AnonymousResponse anonymous(@RequestBody(required = false) AnonymousRequest req,
            HttpServletRequest request, HttpServletResponse response) {
        AnonymousAccount account = authService.createAnonymous(req == null ? null : req.nickname());
        Authentication auth = authenticate(account.user().getUsername(), account.recoveryCode());
        startSession(auth, request, response);
        return new AnonymousResponse(UserDto.from(account.user()), account.user().getUsername(),
                account.recoveryCode());
    }

    @GetMapping("/me")
    public ResponseEntity<UserDto> me(@AuthenticationPrincipal AppUserPrincipal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return userRepository.findById(principal.getUserId())
                .map(u -> ResponseEntity.ok(UserDto.from(u)))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
    }

    private Authentication authenticate(String username, String password) {
        return authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken.unauthenticated(username, password));
    }

    private User currentUser(Authentication auth) {
        AppUserPrincipal principal = (AppUserPrincipal) auth.getPrincipal();
        return userRepository.findById(principal.getUserId())
                .orElseThrow(() -> ApiException.notFound("Account not found."));
    }

    /** Stores the login in the session (new session id, to prevent session fixation). */
    private void startSession(Authentication auth, HttpServletRequest request, HttpServletResponse response) {
        if (request.getSession(false) != null) {
            request.changeSessionId();
        }
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);
        SecurityContextHolder.setContext(context);
        securityContextRepository.saveContext(context, request, response);
    }
}
