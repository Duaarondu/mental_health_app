package com.mentalhealth.mentalhealth_backend.dto;

import java.time.LocalDateTime;

import com.mentalhealth.mentalhealth_backend.model.Appointment;

/** Request and response bodies for /api/appointments. */
public final class AppointmentDtos {

    private AppointmentDtos() {
    }

    /** scheduledTime like "2026-11-20T15:00", duration in minutes (30 or 50). */
    public record BookRequest(Integer therapistId, LocalDateTime scheduledTime, Integer duration) {
    }

    public record AppointmentDto(
            Integer id,
            LocalDateTime scheduledTime,
            Integer duration,
            String status,
            Integer therapistId,
            String therapistName,
            Integer clientId,
            String clientName) {

        public static AppointmentDto from(Appointment a) {
            return new AppointmentDto(a.getAppointmentId(), a.getScheduledTime(), a.getDuration(),
                    a.getStatus().name(), a.getTherapist().getUserId(), a.getTherapist().getDisplayName(),
                    a.getClient().getUserId(), a.getClient().getDisplayName());
        }
    }

    public record TherapistDto(Integer id, String name, String specializations) {
    }
}
