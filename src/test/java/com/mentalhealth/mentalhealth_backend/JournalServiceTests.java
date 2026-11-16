package com.mentalhealth.mentalhealth_backend;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.mentalhealth.mentalhealth_backend.dto.AuthRequests.RegisterRequest;
import com.mentalhealth.mentalhealth_backend.dto.JournalDtos.JournalEntryDto;
import com.mentalhealth.mentalhealth_backend.model.User;
import com.mentalhealth.mentalhealth_backend.repository.JournalEntryRepository;
import com.mentalhealth.mentalhealth_backend.service.ApiException;
import com.mentalhealth.mentalhealth_backend.service.AuthService;
import com.mentalhealth.mentalhealth_backend.service.JournalCrypto;
import com.mentalhealth.mentalhealth_backend.service.JournalService;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class JournalServiceTests {

    @Autowired AuthService authService;
    @Autowired JournalService journalService;
    @Autowired JournalEntryRepository journalRepository;
    @Autowired JournalCrypto crypto;

    User patient;
    User otherPatient;
    User therapist;

    @BeforeEach
    void setUp() {
        patient = authService.register(new RegisterRequest("patient1", "password123", "Pat", "One", "patient", null));
        otherPatient = authService.register(new RegisterRequest("patient2", "password123", "Pat", "Two", "patient", null));
        therapist = authService.register(new RegisterRequest("therapist1", "password123", "Terry", "Ther", "therapist", "Anxiety"));
    }

    @Test
    void journalTextIsEncryptedInTheDatabaseButReadableByTheOwner() {
        JournalEntryDto saved = journalService.add(patient.getUserId(), 4, "Had a calm walk today.");

        String stored = journalRepository.findById(saved.id()).orElseThrow().getEncryptedContent();
        assertTrue(stored.startsWith("v1:"));
        assertFalse(stored.contains("calm walk"));

        List<JournalEntryDto> mine = journalService.listFor(patient.getUserId());
        assertEquals(1, mine.size());
        assertEquals("Had a calm walk today.", mine.get(0).content());
        assertEquals(4, mine.get(0).moodScore());
    }

    @Test
    void aMoodCheckInNeedsNoText() {
        JournalEntryDto checkIn = journalService.add(patient.getUserId(), 2, null);
        assertEquals("", checkIn.content());
        assertEquals(2, checkIn.moodScore());
    }

    @Test
    void moodMustBeBetweenOneAndFive() {
        assertThrows(ApiException.class, () -> journalService.add(patient.getUserId(), 0, "x"));
        assertThrows(ApiException.class, () -> journalService.add(patient.getUserId(), 6, "x"));
        assertThrows(ApiException.class, () -> journalService.add(patient.getUserId(), null, "x"));
    }

    @Test
    void veryLongEntriesAreRejected() {
        String tooLong = "a".repeat(JournalService.MAX_LENGTH + 1);
        assertThrows(ApiException.class, () -> journalService.add(patient.getUserId(), 3, tooLong));
    }

    @Test
    void patientsOnlySeeAndDeleteTheirOwnEntries() {
        JournalEntryDto saved = journalService.add(patient.getUserId(), 3, "Private thought");

        assertTrue(journalService.listFor(otherPatient.getUserId()).isEmpty());
        assertThrows(ApiException.class, () -> journalService.delete(otherPatient.getUserId(), saved.id()));

        journalService.delete(patient.getUserId(), saved.id());
        assertTrue(journalService.listFor(patient.getUserId()).isEmpty());
    }

    @Test
    void therapistsCannotWriteJournalEntries() {
        assertThrows(ApiException.class, () -> journalService.add(therapist.getUserId(), 3, "x"));
    }

    @Test
    void sameTextEncryptsDifferentlyEachTimeAndTamperingIsDetected() {
        String a = crypto.encrypt("hello");
        String b = crypto.encrypt("hello");
        assertNotEquals(a, b);
        assertEquals("hello", crypto.decrypt(a));

        char[] chars = a.toCharArray();
        int i = chars.length - 5;
        chars[i] = chars[i] == 'A' ? 'B' : 'A';
        String tampered = new String(chars);
        assertThrows(IllegalStateException.class, () -> crypto.decrypt(tampered));
    }

    @Test
    void oneDamagedEntryDoesNotHideTheRest() {
        JournalEntryDto good = journalService.add(patient.getUserId(), 4, "Still readable");
        JournalEntryDto bad = journalService.add(patient.getUserId(), 2, "Will be damaged");
        journalRepository.findById(bad.id()).orElseThrow().setEncryptedContent("v1:AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");

        List<JournalEntryDto> mine = journalService.listFor(patient.getUserId());
        assertEquals(2, mine.size());
        assertTrue(mine.stream().anyMatch(e -> e.id().equals(good.id()) && e.content().equals("Still readable")));
        assertTrue(mine.stream().anyMatch(e -> e.id().equals(bad.id()) && e.content().equals(JournalService.UNREADABLE)));
    }
}
