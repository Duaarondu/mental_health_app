package com.mentalhealth.mentalhealth_backend.dto;

import com.mentalhealth.mentalhealth_backend.model.User;

/** What the frontend gets to know about the logged-in user. Never includes the password hash. */
public record UserDto(
        Integer id,
        String username,
        String firstName,
        String lastName,
        String displayName,
        String role,
        boolean verified,
        boolean anonymous) {

    public static UserDto from(User u) {
        return new UserDto(u.getUserId(), u.getUsername(), u.getFirstName(), u.getLastName(),
                u.getDisplayName(), u.getRole(), Boolean.TRUE.equals(u.getIsVerified()), u.isAnonymous());
    }
}
