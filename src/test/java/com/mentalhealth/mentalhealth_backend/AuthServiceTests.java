package com.mentalhealth.mentalhealth_backend;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.mentalhealth.mentalhealth_backend.dto.AuthRequests.RegisterRequest;
import com.mentalhealth.mentalhealth_backend.model.Role;
import com.mentalhealth.mentalhealth_backend.model.User;
import com.mentalhealth.mentalhealth_backend.repository.ClientProfileRepository;
import com.mentalhealth.mentalhealth_backend.repository.TherapistProfileRepository;
import com.mentalhealth.mentalhealth_backend.service.ApiException;
import com.mentalhealth.mentalhealth_backend.service.AuthService;
import com.mentalhealth.mentalhealth_backend.service.AuthService.AnonymousAccount;

@SpringBootTest
@ActiveProfiles("test")
@Transactional // each test's data is rolled back afterwards
class AuthServiceTests {

    @Autowired AuthService authService;
    @Autowired PasswordEncoder passwordEncoder;
    @Autowired ClientProfileRepository clientProfiles;
    @Autowired TherapistProfileRepository therapistProfiles;

    @Test
    void registeringAPatientHashesThePasswordAndCreatesAProfile() {
        User u = authService.register(new RegisterRequest("sam_patient", "correct-horse", "Sam", "Lee", "patient", null));

        assertEquals(Role.CLIENT, u.getRole());
        assertNotEquals("correct-horse", u.getPasswordHash(), "password must not be stored as plain text");
        assertTrue(passwordEncoder.matches("correct-horse", u.getPasswordHash()));
        assertTrue(clientProfiles.existsById(u.getUserId()));
    }

    @Test
    void newTherapistsStartUnverified() {
        User t = authService.register(new RegisterRequest("dr.test", "password123", "Taylor", "Test", "therapist", "Anxiety"));

        assertEquals(Role.THERAPIST, t.getRole());
        assertFalse(t.getIsVerified());
        assertTrue(therapistProfiles.existsById(t.getUserId()));
    }

    @Test
    void duplicateUsernamesAreRejected() {
        authService.register(new RegisterRequest("same_name", "password123", "A", "B", "patient", null));
        ApiException e = assertThrows(ApiException.class, () ->
                authService.register(new RegisterRequest("same_name", "password123", "C", "D", "patient", null)));
        assertEquals(HttpStatus.CONFLICT, e.getStatus());
    }

    @Test
    void shortPasswordsAndBadUsernamesAreRejected() {
        assertThrows(ApiException.class, () ->
                authService.register(new RegisterRequest("okname", "short", "A", "B", "patient", null)));
        assertThrows(ApiException.class, () ->
                authService.register(new RegisterRequest("no spaces!", "password123", "A", "B", "patient", null)));
        assertThrows(ApiException.class, () ->
                authService.register(new RegisterRequest("anon-fake", "password123", "A", "B", "patient", null)));
    }

    @Test
    void anonymousAccountsHideTheRealNameAndCanLogBackIn() {
        AnonymousAccount acc = authService.createAnonymous("Quiet Fox");

        assertEquals(Role.ANONYMOUS, acc.user().getRole());
        assertTrue(acc.user().getUsername().startsWith("anon-"));
        assertEquals("Quiet Fox (anonymous)", acc.user().getDisplayName());
        assertTrue(passwordEncoder.matches(acc.recoveryCode(), acc.user().getPasswordHash()),
                "the recovery code works as the password");
    }

    @Test
    void anonymousAccountsGetANicknameWhenNoneIsGiven() {
        AnonymousAccount acc = authService.createAnonymous("  ");
        assertFalse(acc.user().getFirstName().isBlank());
    }

    @Test
    void passwordsLongerThan72BytesAreRejected() {
        // 40 characters, but 80 bytes: BCrypt would silently ignore the end of it
        String accented = "\u00e9".repeat(40);
        assertThrows(ApiException.class, () ->
                authService.register(new RegisterRequest("accent.user", accented, "A", "User", "patient", null)));
    }
}
