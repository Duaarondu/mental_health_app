package com.mentalhealth.mentalhealth_backend.model;

/** Must match APPOINTMENT.status ENUM('Scheduled', 'Cancelled', 'Completed') exactly. */
public enum AppointmentStatus {
    Scheduled,
    Cancelled,
    Completed
}
