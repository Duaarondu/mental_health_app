
package com.mentalhealth.mentalhealth_backend;

import java.util.ArrayList;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class Appointment_Controller {

    ArrayList<Appointment> appointments = new ArrayList<>();

    @GetMapping("/api/appointments")
    public ArrayList<Appointment> getAppointments() {
        return appointments;
    }
    @PostMapping("/api/appointments")
    public String bookAppointment(@RequestBody Appointment appointment) {
        appointments.add(appointment);
        return "Appointment booked successfully!";
    }
}
