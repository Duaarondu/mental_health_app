package com.mentalhealth.mentalhealth_backend.controller;

import java.time.Duration;
import java.util.Locale;
import java.util.regex.Pattern;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
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
import com.mentalhealth.mentalhealth_backend.security.RateLimiter;
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
 *
 * Abuse limits (per server instance):
 * - 5 wrong passwords for one username -> that username is locked for 15 minutes
 * - 10 new accounts (sign-up or anonymous) per network address per hour, 1000 per hour in total
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final AuthenticationManager authenticationManager;
    private final SecurityContextRepository securityContextRepository;
    private final UserRepository userRepository;
    private final RateLimiter rateLimiter;

    static final int MAX_FAILED_LOGINS = 5;
    static final Duration LOGIN_LOCK = Duration.ofMinutes(15);
    static final int MAX_SIGNUPS_PER_ADDRESS = 10;
    static final int MAX_SIGNUPS_TOTAL = 1000;
    private static final Pattern IP_ADDRESS = Pattern.compile("^[0-9A-Fa-f:.]{2,45}$");
    static final Duration SIGNUP_WINDOW = Duration.ofHours(1);

    public AuthController(AuthService authService, AuthenticationManager authenticationManager,
            SecurityContextRepository securityContextRepository, UserRepository userRepository,
            RateLimiter rateLimiter) {
        this.authService = authService;
        this.authenticationManager = authenticationManager;
        this.securityContextRepository = securityContextRepository;
        this.userRepository = userRepository;
        this.rateLimiter = rateLimiter;
    }

    @PostMapping("/login")
    public UserDto login(@RequestBody LoginRequest req, HttpServletRequest request, HttpServletResponse response) {
        if (req.username() == null || req.username().isBlank() || req.password() == null || req.password().isEmpty()) {
            throw ApiException.badRequest("Please enter your username and password.");
        }
        String username = req.username().trim();
        // A name that could never exist is just a wrong login. Checking it first also stops
        // huge or accented look-alike names from getting their own lockout counter.
        if (!AuthService.isValidUsername(username)) {
            throw new BadCredentialsException("Wrong username or password.");
        }
        // Every attempt counts (recorded before the password check, so parallel guesses can't
        // slip past); a successful login clears the count.
        String throttleKey = "login:" + username.toLowerCase(Locale.ROOT);
        if (!rateLimiter.tryAcquire(throttleKey, MAX_FAILED_LOGINS, LOGIN_LOCK)) {
            throw new ApiException(HttpStatus.TOO_MANY_REQUESTS,
                    "Too many wrong passwords for this account. Please wait 15 minutes and try again.");
        }
        Authentication auth = authenticate(username, req.password());
        rateLimiter.reset(throttleKey);
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
        String address = checkSignupLimit(request);
        User user = authService.register(req);
        countSignup(address);
        Authentication auth = authenticate(user.getUsername(), req.password());
        startSession(auth, request, response);
        return UserDto.from(user);
    }

    @PostMapping("/anonymous")
    public AnonymousResponse anonymous(@RequestBody(required = false) AnonymousRequest req,
            HttpServletRequest request, HttpServletResponse response) {
        String address = checkSignupLimit(request);
        AnonymousAccount account = authService.createAnonymous(req == null ? null : req.nickname());
        countSignup(address);
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

    /**
     * Stops scripts from creating thousands of accounts. Only accounts that were actually created
     * are counted (countSignup), so typos and taken usernames don't use up anyone's allowance.
     * Returns the visitor's address.
     */
    private String checkSignupLimit(HttpServletRequest request) {
        String address = clientAddress(request);
        if (rateLimiter.count("signup:" + address, SIGNUP_WINDOW) >= MAX_SIGNUPS_PER_ADDRESS
                || rateLimiter.count("signup:all", SIGNUP_WINDOW) >= MAX_SIGNUPS_TOTAL) {
            throw new ApiException(HttpStatus.TOO_MANY_REQUESTS,
                    "Too many new accounts were created from here. Please try again in an hour.");
        }
        return address;
    }

    private void countSignup(String address) {
        rateLimiter.record("signup:" + address);
        rateLimiter.record("signup:all");
    }

    /**
     * The visitor's address: the first entry of X-Forwarded-For, which Vercel sets to the real
     * visitor. Someone calling the Cloud Run URL directly can fake it, so anything that isn't a
     * plain IP address is ignored, and the overall hourly cap still applies.
     */
    static String clientAddress(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            String first = forwarded.split(",", 2)[0].trim();
            if (IP_ADDRESS.matcher(first).matches()) {
                return first;
            }
        }
        return request.getRemoteAddr();
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
