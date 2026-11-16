package com.mentalhealth.mentalhealth_backend;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.jupiter.api.Test;

import com.mentalhealth.mentalhealth_backend.security.RateLimiter;

class RateLimiterTests {

    @Test
    void blocksAfterTheLimitAndRecoversWhenTheWindowPasses() {
        AtomicLong now = new AtomicLong(1_000_000);
        RateLimiter limiter = new RateLimiter(now::get);
        Duration hour = Duration.ofHours(1);

        for (int i = 0; i < 3; i++) {
            assertTrue(limiter.tryAcquire("k", 3, hour));
        }
        assertFalse(limiter.tryAcquire("k", 3, hour));
        assertEquals(3, limiter.count("k", hour));

        now.addAndGet(hour.toMillis() + 1);
        assertEquals(0, limiter.count("k", hour));
        assertTrue(limiter.tryAcquire("k", 3, hour));
    }

    @Test
    void keysAreCountedSeparatelyAndCanBeReset() {
        RateLimiter limiter = new RateLimiter();
        limiter.record("a");
        limiter.record("a");
        limiter.record("b");
        assertEquals(2, limiter.count("a", Duration.ofMinutes(1)));
        assertEquals(1, limiter.count("b", Duration.ofMinutes(1)));

        limiter.reset("a");
        assertEquals(0, limiter.count("a", Duration.ofMinutes(1)));
    }
}
