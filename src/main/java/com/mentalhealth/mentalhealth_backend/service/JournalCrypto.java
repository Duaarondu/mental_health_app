package com.mentalhealth.mentalhealth_backend.service;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Encrypts journal text before it goes into the database (AES-256-GCM).
 *
 * The key comes from the JOURNAL_KEY environment variable: 32 random bytes, base64-encoded.
 * Make one with:  openssl rand -base64 32
 * Keep it in Google Secret Manager, never in GitHub. If the key is lost, old entries
 * can't be read any more.
 *
 * Stored format: "v1:" + base64(12-byte random IV + ciphertext + 16-byte auth tag).
 * GCM also detects tampering: a changed ciphertext fails to decrypt instead of
 * returning garbage.
 */
@Component
public class JournalCrypto {

    private static final Logger log = LoggerFactory.getLogger(JournalCrypto.class);
    private static final String PREFIX = "v1:";
    private static final int IV_BYTES = 12;
    private static final int TAG_BITS = 128;

    private final SecretKey key;
    private final SecureRandom random = new SecureRandom();

    public JournalCrypto(@Value("${app.journal-key:}") String base64Key) {
        byte[] raw;
        if (base64Key == null || base64Key.isBlank()) {
            log.warn("JOURNAL_KEY is not set: journal entries are encrypted with a built-in development key. "
                    + "Set JOURNAL_KEY on the hosted server.");
            raw = sha256("mindcare-development-key-do-not-use-in-production");
        } else {
            try {
                raw = Base64.getDecoder().decode(base64Key.trim());
            } catch (IllegalArgumentException e) {
                throw new IllegalStateException("JOURNAL_KEY must be base64 (make one with: openssl rand -base64 32)");
            }
            if (raw.length != 32) {
                throw new IllegalStateException("JOURNAL_KEY must be 32 bytes (make one with: openssl rand -base64 32)");
            }
        }
        this.key = new SecretKeySpec(raw, "AES");
    }

    public String encrypt(String plainText) {
        try {
            byte[] iv = new byte[IV_BYTES];
            random.nextBytes(iv);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(TAG_BITS, iv));
            byte[] encrypted = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
            byte[] out = new byte[iv.length + encrypted.length];
            System.arraycopy(iv, 0, out, 0, iv.length);
            System.arraycopy(encrypted, 0, out, iv.length, encrypted.length);
            return PREFIX + Base64.getEncoder().encodeToString(out);
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Could not encrypt journal entry", e);
        }
    }

    public String decrypt(String stored) {
        if (stored == null || !stored.startsWith(PREFIX)) {
            throw new IllegalStateException("Journal entry is not in a known encrypted format");
        }
        try {
            byte[] in = Base64.getDecoder().decode(stored.substring(PREFIX.length()));
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(TAG_BITS, in, 0, IV_BYTES));
            byte[] plain = cipher.doFinal(in, IV_BYTES, in.length - IV_BYTES);
            return new String(plain, StandardCharsets.UTF_8);
        } catch (GeneralSecurityException | IllegalArgumentException e) {
            throw new IllegalStateException("Could not decrypt journal entry (wrong JOURNAL_KEY?)", e);
        }
    }

    private static byte[] sha256(String s) {
        try {
            return MessageDigest.getInstance("SHA-256").digest(s.getBytes(StandardCharsets.UTF_8));
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException(e);
        }
    }
}
