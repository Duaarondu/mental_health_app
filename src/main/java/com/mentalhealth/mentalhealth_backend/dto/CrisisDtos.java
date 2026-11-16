package com.mentalhealth.mentalhealth_backend.dto;

import java.util.List;

public final class CrisisDtos {

    private CrisisDtos() {
    }

    /** notified = false when the patient has no therapist yet. */
    public record CrisisAlertResult(boolean notified, List<String> therapistNames) {
    }

    /** One row on the therapist's dashboard. */
    public record CrisisAlertDto(Integer clientId, String clientName) {
    }
}
