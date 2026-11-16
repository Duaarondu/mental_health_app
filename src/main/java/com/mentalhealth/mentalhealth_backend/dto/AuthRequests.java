package com.mentalhealth.mentalhealth_backend.dto;

/** Request bodies for /api/auth/... */
public final class AuthRequests {

    private AuthRequests() {
    }

    /** accountType is "patient" or "therapist", from the toggle on the login page. */
    public record LoginRequest(String username, String password, String accountType) {
    }

    /** accountType is "patient" or "therapist". specializations only for therapists. */
    public record RegisterRequest(String username, String password, String firstName, String lastName,
            String accountType, String specializations) {
    }

    public record AnonymousRequest(String nickname) {
    }

    /** Returned once when an anonymous account is made: the code is never shown again. */
    public record AnonymousResponse(UserDto user, String username, String recoveryCode) {
    }
}
