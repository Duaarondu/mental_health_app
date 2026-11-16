package com.mentalhealth.mentalhealth_backend.model;

/**
 * Values stored in USER.role.
 * Anonymous users are guests who booked without giving their real name;
 * they get the same permissions as a Client.
 */
public final class Role {

    public static final String CLIENT = "Client";
    public static final String THERAPIST = "Therapist";
    public static final String ANONYMOUS = "Anonymous";
    public static final String ADMIN = "Admin";

    private Role() {
    }
}
