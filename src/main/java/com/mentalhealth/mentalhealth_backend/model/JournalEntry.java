package com.mentalhealth.mentalhealth_backend.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * Maps JOURNAL_ENTRY. One row is either a quick mood check-in (empty text) or a
 * journal entry with text. The text is stored only in encrypted form
 * (see JournalCrypto); moodScore is kept readable so it can be charted.
 */
@Entity
@Table(name = "JOURNAL_ENTRY")
public class JournalEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "entryId")
    private Integer entryId;

    @ManyToOne(optional = false)
    @JoinColumn(name = "clientId", nullable = false)
    private User client;

    @Column(name = "entryDate")
    private LocalDateTime entryDate;

    /** 1 (very low) to 5 (great). */
    @Column(name = "moodScore", nullable = false, columnDefinition = "FLOAT")
    private Float moodScore;

    /** AES-256-GCM ciphertext, never plain text. */
    @Column(name = "encryptedContent", nullable = false, columnDefinition = "TEXT")
    private String encryptedContent;

    public Integer getEntryId() { return entryId; }
    public void setEntryId(Integer entryId) { this.entryId = entryId; }
    public User getClient() { return client; }
    public void setClient(User client) { this.client = client; }
    public LocalDateTime getEntryDate() { return entryDate; }
    public void setEntryDate(LocalDateTime entryDate) { this.entryDate = entryDate; }
    public Float getMoodScore() { return moodScore; }
    public void setMoodScore(Float moodScore) { this.moodScore = moodScore; }
    public String getEncryptedContent() { return encryptedContent; }
    public void setEncryptedContent(String encryptedContent) { this.encryptedContent = encryptedContent; }
}
