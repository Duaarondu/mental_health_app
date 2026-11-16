package com.mentalhealth.mentalhealth_backend.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/** Maps APPOINTMENT. Replaces the old in-memory Appointment (type/date/time). */
@Entity
@Table(name = "APPOINTMENT")
public class Appointment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "appointmentId")
    private Integer appointmentId;

    @ManyToOne(optional = false)
    @JoinColumn(name = "clientId", nullable = false)
    private User client;

    @ManyToOne(optional = false)
    @JoinColumn(name = "therapistId", nullable = false)
    private User therapist;

    @Column(name = "scheduledTime", nullable = false)
    private LocalDateTime scheduledTime;

    /** Length in minutes. */
    @Column(name = "duration", nullable = false)
    private Integer duration;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private AppointmentStatus status = AppointmentStatus.Scheduled;

    public LocalDateTime getEndTime() {
        return scheduledTime.plusMinutes(duration);
    }

    public Integer getAppointmentId() { return appointmentId; }
    public void setAppointmentId(Integer appointmentId) { this.appointmentId = appointmentId; }
    public User getClient() { return client; }
    public void setClient(User client) { this.client = client; }
    public User getTherapist() { return therapist; }
    public void setTherapist(User therapist) { this.therapist = therapist; }
    public LocalDateTime getScheduledTime() { return scheduledTime; }
    public void setScheduledTime(LocalDateTime scheduledTime) { this.scheduledTime = scheduledTime; }
    public Integer getDuration() { return duration; }
    public void setDuration(Integer duration) { this.duration = duration; }
    public AppointmentStatus getStatus() { return status; }
    public void setStatus(AppointmentStatus status) { this.status = status; }
}
