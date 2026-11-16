package com.mentalhealth.mentalhealth_backend;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.mentalhealth.mentalhealth_backend.controller.AuthController;
import com.mentalhealth.mentalhealth_backend.dto.AuthRequests.LoginRequest;
import com.mentalhealth.mentalhealth_backend.dto.AuthRequests.RegisterRequest;
import com.mentalhealth.mentalhealth_backend.service.ApiException;
import com.mentalhealth.mentalhealth_backend.service.AuthService;

/** Password guessing and mass sign-up are slowed down. */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class LoginLimitTests {

    @Autowired AuthController authController;
    @Autowired AuthService authService;

    @AfterEach
    void clearLogin() {
        SecurityContextHolder.clearContext();
    }

    private void login(String username, String password) {
        authController.login(new LoginRequest(username, password, "patient"),
                new MockHttpServletRequest(), new MockHttpServletResponse());
    }

    @Test
    void fiveWrongPasswordsLockTheAccountForAWhile() {
        authService.register(new RegisterRequest("lockme", "password123", "Lock", "Me", "patient", null));
        for (int i = 0; i < 5; i++) {
            assertThrows(AuthenticationException.class, () -> login("lockme", "wrong-password"));
        }
        // Even the right password is refused while the account is locked
        ApiException e = assertThrows(ApiException.class, () -> login("lockme", "password123"));
        assertEquals(HttpStatus.TOO_MANY_REQUESTS, e.getStatus());
    }

    @Test
    void aCorrectPasswordClearsEarlierMistakes() {
        authService.register(new RegisterRequest("forgetful", "password123", "For", "Getful", "patient", null));
        for (int i = 0; i < 4; i++) {
            assertThrows(AuthenticationException.class, () -> login("forgetful", "wrong-password"));
        }
        login("forgetful", "password123");
        for (int i = 0; i < 4; i++) {
            assertThrows(AuthenticationException.class, () -> login("forgetful", "wrong-password"));
        }
        login("forgetful", "password123");
    }

    @Test
    void impossibleUsernamesAreJustAWrongLogin() {
        assertThrows(AuthenticationException.class, () -> login("x".repeat(5000), "password123"));
        assertThrows(AuthenticationException.class, () -> login("dr.r\u00edvera", "password123"));
    }

    @Test
    void oneAddressCanOnlyCreateTenAccountsAnHour() {
        for (int i = 0; i < 10; i++) {
            authController.anonymous(null, fromAddress("203.0.113.7"), new MockHttpServletResponse());
        }
        ApiException e = assertThrows(ApiException.class, () ->
                authController.anonymous(null, fromAddress("203.0.113.7"), new MockHttpServletResponse()));
        assertEquals(HttpStatus.TOO_MANY_REQUESTS, e.getStatus());

        // Someone else is not affected
        authController.anonymous(null, fromAddress("198.51.100.20"), new MockHttpServletResponse());
    }

    private static MockHttpServletRequest fromAddress(String ip) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-Forwarded-For", ip + ", 10.0.0.1");
        return request;
    }
}
