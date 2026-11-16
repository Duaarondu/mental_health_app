package com.mentalhealth.mentalhealth_backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.mentalhealth.mentalhealth_backend.model.Appointment;
import com.mentalhealth.mentalhealth_backend.model.AppointmentStatus;

public interface AppointmentRepository extends JpaRepository<Appointment, Integer> {

    @Query("select a from Appointment a where a.client.userId = :clientId order by a.scheduledTime")
    List<Appointment> findForClient(@Param("clientId") Integer clientId);

    @Query("select a from Appointment a where a.therapist.userId = :therapistId order by a.scheduledTime")
    List<Appointment> findForTherapist(@Param("therapistId") Integer therapistId);

    @Query("select a from Appointment a where a.therapist.userId = :therapistId and a.status = :status")
    List<Appointment> findForTherapistWithStatus(@Param("therapistId") Integer therapistId,
            @Param("status") AppointmentStatus status);

    @Query("select a from Appointment a where a.client.userId = :clientId and a.status = :status")
    List<Appointment> findForClientWithStatus(@Param("clientId") Integer clientId,
            @Param("status") AppointmentStatus status);

    /** True if the client has a scheduled or completed (not cancelled) session with this therapist. */
    @Query("select count(a) > 0 from Appointment a where a.client.userId = :clientId and a.therapist.userId = :therapistId"
            + " and a.status <> :cancelled")
    boolean existsActiveBetween(@Param("clientId") Integer clientId, @Param("therapistId") Integer therapistId,
            @Param("cancelled") AppointmentStatus cancelled);
}
