package com.mentalhealth.mentalhealth_backend.service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mentalhealth.mentalhealth_backend.model.Appointment;
import com.mentalhealth.mentalhealth_backend.model.AppointmentStatus;
import com.mentalhealth.mentalhealth_backend.model.User;
import com.mentalhealth.mentalhealth_backend.repository.AppointmentRepository;
import com.mentalhealth.mentalhealth_backend.repository.UserRepository;

/** Booking rules live here, so they can be tested without the web layer. */
@Service
@Transactional
public class AppointmentService {

    public static final Set<Integer> ALLOWED_DURATIONS = Set.of(30, 50);

    private final AppointmentRepository appointmentRepository;
    private final UserRepository userRepository;
    private final ZoneId zone;

    public AppointmentService(AppointmentRepository appointmentRepository, UserRepository userRepository,
            @Value("${app.time-zone:America/New_York}") String timeZone) {
        this.appointmentRepository = appointmentRepository;
        this.userRepository = userRepository;
        this.zone = ZoneId.of(timeZone);
    }

    @Transactional(readOnly = true)
    public List<Appointment> listFor(User user) {
        return user.isTherapist()
                ? appointmentRepository.findForTherapist(user.getUserId())
                : appointmentRepository.findForClient(user.getUserId());
    }

    public Appointment book(Integer clientId, Integer therapistId, LocalDateTime start, Integer duration) {
        User client = userRepository.findById(clientId)
                .filter(User::isPatient)
                .orElseThrow(() -> ApiException.forbidden("Only patients can book appointments."));
        if (therapistId == null) {
            throw ApiException.badRequest("Please choose a therapist.");
        }
        User therapist = userRepository.findById(therapistId)
                .filter(User::isTherapist)
                .filter(t -> Boolean.TRUE.equals(t.getIsVerified()))
                .orElseThrow(() -> ApiException.badRequest("That therapist isn't available for booking."));
        if (start == null) {
            throw ApiException.badRequest("Please choose a date and time.");
        }
        LocalDateTime now = LocalDateTime.now(zone);
        if (!start.isAfter(now)) {
            throw ApiException.badRequest("Please choose a time in the future.");
        }
        if (start.isAfter(now.plusMonths(6))) {
            throw ApiException.badRequest("You can book up to 6 months ahead.");
        }
        if (duration == null || !ALLOWED_DURATIONS.contains(duration)) {
            throw ApiException.badRequest("Sessions are 30 or 50 minutes long.");
        }

        LocalDateTime end = start.plusMinutes(duration);
        for (Appointment a : appointmentRepository.findForTherapistWithStatus(therapistId, AppointmentStatus.Scheduled)) {
            if (overlaps(a, start, end)) {
                throw ApiException.conflict(therapist.getDisplayName()
                        + " already has a session at that time. Please pick another time.");
            }
        }
        for (Appointment a : appointmentRepository.findForClientWithStatus(clientId, AppointmentStatus.Scheduled)) {
            if (overlaps(a, start, end)) {
                throw ApiException.conflict("You already have a session at that time.");
            }
        }

        Appointment appointment = new Appointment();
        appointment.setClient(client);
        appointment.setTherapist(therapist);
        appointment.setScheduledTime(start);
        appointment.setDuration(duration);
        appointment.setStatus(AppointmentStatus.Scheduled);
        return appointmentRepository.save(appointment);
    }

    /** The patient or the therapist of an appointment can cancel it. */
    public Appointment cancel(Integer userId, Integer appointmentId) {
        Appointment a = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> ApiException.notFound("Appointment not found."));
        boolean mine = a.getClient().getUserId().equals(userId) || a.getTherapist().getUserId().equals(userId);
        if (!mine) {
            throw ApiException.forbidden("You can only cancel your own appointments.");
        }
        if (a.getStatus() != AppointmentStatus.Scheduled) {
            throw ApiException.badRequest("Only scheduled appointments can be cancelled.");
        }
        a.setStatus(AppointmentStatus.Cancelled);
        return a;
    }

    private static boolean overlaps(Appointment a, LocalDateTime start, LocalDateTime end) {
        return a.getScheduledTime().isBefore(end) && start.isBefore(a.getEndTime());
    }
}
