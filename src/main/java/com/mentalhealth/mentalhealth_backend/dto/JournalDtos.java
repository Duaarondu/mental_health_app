package com.mentalhealth.mentalhealth_backend.dto;

import java.time.LocalDateTime;

/** Request and response bodies for /api/journal. */
public final class JournalDtos {

    private JournalDtos() {
    }

    /** moodScore 1-5. content may be empty: that's a quick mood check-in. */
    public record NewEntryRequest(Integer moodScore, String content) {
    }

    /** Only ever sent to the patient who wrote it. */
    public record JournalEntryDto(Integer id, LocalDateTime entryDate, Integer moodScore, String content) {
    }
}
