package com.mentalhealth.mentalhealth_backend;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.mentalhealth.mentalhealth_backend.dto.AuthRequests.RegisterRequest;
import com.mentalhealth.mentalhealth_backend.model.Appointment;
import com.mentalhealth.mentalhealth_backend.model.AppointmentStatus;
import com.mentalhealth.mentalhealth_backend.model.User;
import com.mentalhealth.mentalhealth_backend.repository.UserRepository;
import com.mentalhealth.mentalhealth_backend.service.ApiException;
import com.mentalhealth.mentalhealth_backend.service.AppointmentService;
import com.mentalhealth.mentalhealth_backend.service.AuthService;
import com.mentalhealth.mentalhealth_backend.service.CrisisService;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class AppointmentServiceTests {

    @Autowired AuthService authService;
    @Autowired AppointmentService appointmentService;
    @Autowired CrisisService crisisService;
    @Autowired UserRepository userRepository;

    User patient;
    User otherPatient;
    User therapist;
    LocalDateTime nextWeek;

    @BeforeEach
    void setUp() {
        patient = authService.register(new RegisterRequest("patient1", "password123", "Pat", "One", "patient", null));
        otherPatient = authService.register(new RegisterRequest("patient2", "password123", "Pat", "Two", "patient", null));
        therapist = authService.register(new RegisterRequest("therapist1", "password123", "Terry", "Ther", "therapist", "Anxiety"));
        therapist.setIsVerified(true);
        userRepository.save(therapist);
        nextWeek = LocalDateTime.now().plusDays(7).withHour(15).withMinute(0).withSecond(0).withNano(0);
    }

    @Test
    void aValidBookingIsSavedAsScheduled() {
        Appointment a = appointmentService.book(patient.getUserId(), therapist.getUserId(), nextWeek, 50);
        assertEquals(AppointmentStatus.Scheduled, a.getStatus());
        assertEquals(1, appointmentService.listFor(patient).size());
        assertEquals(1, appointmentService.listFor(therapist).size());
    }

    @Test
    void bookingsInThePastAreRejected() {
        assertThrows(ApiException.class, () ->
                appointmentService.book(patient.getUserId(), therapist.getUserId(), LocalDateTime.now().minusDays(1), 50));
    }

    @Test
    void onlyAllowedSessionLengthsAreAccepted() {
        assertThrows(ApiException.class, () ->
                appointmentService.book(patient.getUserId(), therapist.getUserId(), nextWeek, 45));
    }

    @Test
    void unverifiedTherapistsCannotBeBooked() {
        User newTherapist = authService.register(
                new RegisterRequest("therapist2", "password123", "New", "Ther", "therapist", ""));
        assertThrows(ApiException.class, () ->
                appointmentService.book(patient.getUserId(), newTherapist.getUserId(), nextWeek, 50));
    }

    @Test
    void doubleBookingATherapistIsRejected() {
        appointmentService.book(patient.getUserId(), therapist.getUserId(), nextWeek, 50);
        ApiException e = assertThrows(ApiException.class, () ->
                appointmentService.book(otherPatient.getUserId(), therapist.getUserId(), nextWeek.plusMinutes(30), 30));
        assertEquals(HttpStatus.CONFLICT, e.getStatus());
    }

    @Test
    void anotherPatientCannotCancelMyAppointment() {
        Appointment a = appointmentService.book(patient.getUserId(), therapist.getUserId(), nextWeek, 50);
        ApiException e = assertThrows(ApiException.class, () ->
                appointmentService.cancel(otherPatient.getUserId(), a.getAppointmentId()));
        assertEquals(HttpStatus.FORBIDDEN, e.getStatus());

        assertEquals(AppointmentStatus.Cancelled,
                appointmentService.cancel(patient.getUserId(), a.getAppointmentId()).getStatus());
    }

    @Test
    void crisisAlertsReachOnlyThePatientsOwnTherapist() {
        appointmentService.book(patient.getUserId(), therapist.getUserId(), nextWeek, 50);

        assertTrue(crisisService.raiseAlert(patient.getUserId()).notified());
        assertEquals(1, crisisService.alertsFor(therapist.getUserId()).size());

        // A patient with no therapist yet: nobody to notify
        assertFalse(crisisService.raiseAlert(otherPatient.getUserId()).notified());
        assertEquals(1, crisisService.alertsFor(therapist.getUserId()).size());

        crisisService.resolve(therapist.getUserId(), patient.getUserId());
        assertEquals(0, crisisService.alertsFor(therapist.getUserId()).size());
    }
}
