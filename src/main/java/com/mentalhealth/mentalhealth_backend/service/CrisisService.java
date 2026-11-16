package com.mentalhealth.mentalhealth_backend.service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mentalhealth.mentalhealth_backend.dto.CrisisDtos.CrisisAlertDto;
import com.mentalhealth.mentalhealth_backend.dto.CrisisDtos.CrisisAlertResult;
import com.mentalhealth.mentalhealth_backend.model.Appointment;
import com.mentalhealth.mentalhealth_backend.model.AppointmentStatus;
import com.mentalhealth.mentalhealth_backend.model.ClientProfile;
import com.mentalhealth.mentalhealth_backend.model.User;
import com.mentalhealth.mentalhealth_backend.repository.AppointmentRepository;
import com.mentalhealth.mentalhealth_backend.repository.ClientProfileRepository;

/**
 * The crisis button. Opening the crisis screen does nothing on the server; only when the
 * patient taps "Let my therapist know" do we set CLIENT_PROFILE.isCrisisActive, which shows
 * up on the dashboard of every therapist they have booked with.
 */
@Service
@Transactional
public class CrisisService {

    private final ClientProfileRepository clientProfileRepository;
    private final AppointmentRepository appointmentRepository;

    public CrisisService(ClientProfileRepository clientProfileRepository,
            AppointmentRepository appointmentRepository) {
        this.clientProfileRepository = clientProfileRepository;
        this.appointmentRepository = appointmentRepository;
    }

    public CrisisAlertResult raiseAlert(Integer clientId) {
        ClientProfile profile = clientProfileRepository.findById(clientId)
                .orElseGet(() -> new ClientProfile(clientId));
        profile.setIsCrisisActive(true);
        clientProfileRepository.save(profile);

        List<String> therapists = appointmentRepository.findForClient(clientId).stream()
                .filter(CrisisService::notCancelled)
                .map(a -> a.getTherapist().getDisplayName())
                .distinct()
                .toList();
        return new CrisisAlertResult(!therapists.isEmpty(), therapists);
    }

    @Transactional(readOnly = true)
    public List<CrisisAlertDto> alertsFor(Integer therapistId) {
        Map<Integer, User> clients = new LinkedHashMap<>();
        for (Appointment a : appointmentRepository.findForTherapist(therapistId)) {
            if (!notCancelled(a)) {
                continue;
            }
            clients.putIfAbsent(a.getClient().getUserId(), a.getClient());
        }
        return clients.values().stream()
                .filter(c -> clientProfileRepository.findById(c.getUserId())
                        .map(p -> Boolean.TRUE.equals(p.getIsCrisisActive()))
                        .orElse(false))
                .map(c -> new CrisisAlertDto(c.getUserId(), c.getDisplayName()))
                .toList();
    }

    /** The therapist has followed up: clear the alert. Only for their own clients. */
    public void resolve(Integer therapistId, Integer clientId) {
        if (!appointmentRepository.existsActiveBetween(clientId, therapistId, AppointmentStatus.Cancelled)) {
            throw ApiException.forbidden("This person isn't one of your clients.");
        }
        clientProfileRepository.findById(clientId).ifPresent(p -> p.setIsCrisisActive(false));
    }

    /** A cancelled booking doesn't make someone your therapist. */
    private static boolean notCancelled(Appointment a) {
        return a.getStatus() != AppointmentStatus.Cancelled;
    }
}
