package com.mentalhealth.mentalhealth_backend.service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mentalhealth.mentalhealth_backend.dto.JournalDtos.JournalEntryDto;
import com.mentalhealth.mentalhealth_backend.model.JournalEntry;
import com.mentalhealth.mentalhealth_backend.model.User;
import com.mentalhealth.mentalhealth_backend.repository.JournalEntryRepository;
import com.mentalhealth.mentalhealth_backend.repository.UserRepository;

/**
 * Mood check-ins and the private journal.
 *
 * - Patients see and delete only their own entries. Therapists can't read them.
 * - Journal text is encrypted before it is saved (JournalCrypto).
 */
@Service
@Transactional
public class JournalService {

    public static final int MAX_LENGTH = 5000;
    public static final String UNREADABLE = "(This entry can't be opened.)";

    private static final Logger log = LoggerFactory.getLogger(JournalService.class);

    private final JournalEntryRepository journalRepository;
    private final UserRepository userRepository;
    private final JournalCrypto crypto;
    private final ZoneId zone;

    public JournalService(JournalEntryRepository journalRepository, UserRepository userRepository,
            JournalCrypto crypto,
            @Value("${app.time-zone:America/New_York}") String timeZone) {
        this.journalRepository = journalRepository;
        this.userRepository = userRepository;
        this.crypto = crypto;
        this.zone = ZoneId.of(timeZone);
    }

    public JournalEntryDto add(Integer clientId, Integer moodScore, String content) {
        User client = userRepository.findById(clientId)
                .filter(User::isPatient)
                .orElseThrow(() -> ApiException.forbidden("Only patients can keep a journal."));
        if (moodScore == null || moodScore < 1 || moodScore > 5) {
            throw ApiException.badRequest("Please pick how you're feeling (1 to 5).");
        }
        String text = content == null ? "" : content.strip();
        if (text.length() > MAX_LENGTH) {
            throw ApiException.badRequest("Entries can be up to " + MAX_LENGTH + " characters.");
        }

        JournalEntry entry = new JournalEntry();
        entry.setClient(client);
        entry.setEntryDate(LocalDateTime.now(zone).withNano(0));
        entry.setMoodScore(moodScore.floatValue());
        entry.setEncryptedContent(crypto.encrypt(text));
        return toDto(journalRepository.save(entry));
    }

    @Transactional(readOnly = true)
    public List<JournalEntryDto> listFor(Integer clientId) {
        return journalRepository.findForClient(clientId).stream().map(this::toDto).toList();
    }

    public void delete(Integer clientId, Integer entryId) {
        JournalEntry entry = journalRepository.findById(entryId)
                .filter(e -> e.getClient().getUserId().equals(clientId))
                .orElseThrow(() -> ApiException.notFound("Entry not found."));
        journalRepository.delete(entry);
    }

    private JournalEntryDto toDto(JournalEntry e) {
        String text;
        try {
            text = crypto.decrypt(e.getEncryptedContent());
        } catch (IllegalStateException ex) {
            log.warn("Journal entry {} could not be decrypted", e.getEntryId());
            text = UNREADABLE;
        }
        return new JournalEntryDto(e.getEntryId(), e.getEntryDate(), Math.round(e.getMoodScore()), text);
    }
}
