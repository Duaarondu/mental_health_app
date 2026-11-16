package com.mentalhealth.mentalhealth_backend.security;

import java.time.Duration;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.LongSupplier;

import org.springframework.stereotype.Component;

/**
 * Counts events per key over a sliding time window, in memory.
 * Used to slow down password guessing and mass account creation.
 *
 * Each Cloud Run instance keeps its own counts, so cap the number of instances
 * (gcloud run services update mindcare-api --max-instances=2) to keep the limits meaningful.
 * Memory is bounded: old keys are swept once a minute, and when the table is full new keys are
 * refused (the caller gets "too many requests") instead of growing without limit.
 */
@Component
public class RateLimiter {

    static final int MAX_KEYS = 100_000;
    private static final Duration KEEP = Duration.ofHours(2);
    private static final long SWEEP_EVERY_MS = 60_000;

    private final Map<String, Deque<Long>> events = new ConcurrentHashMap<>();
    private final AtomicLong lastSweep = new AtomicLong();
    private final LongSupplier clock;

    public RateLimiter() {
        this(System::currentTimeMillis);
    }

    /** For tests: a fake clock in milliseconds. */
    public RateLimiter(LongSupplier clock) {
        this.clock = clock;
    }

    /** How many events were recorded for this key within the window. */
    public int count(String key, Duration window) {
        Deque<Long> d = events.get(key);
        if (d == null) {
            return 0;
        }
        synchronized (d) {
            prune(d, window);
            return d.size();
        }
    }

    /** Records an event. Returns false (nothing recorded) only if the table is full. */
    public boolean record(String key) {
        Deque<Long> d = slot(key);
        if (d == null) {
            return false;
        }
        synchronized (d) {
            d.addLast(clock.getAsLong());
        }
        return true;
    }

    /**
     * Records an event unless the key already has {@code max} in the window, as one atomic step
     * (so parallel requests can't all slip through). Returns false if blocked.
     */
    public boolean tryAcquire(String key, int max, Duration window) {
        Deque<Long> d = slot(key);
        if (d == null) {
            return false;
        }
        synchronized (d) {
            prune(d, window);
            if (d.size() >= max) {
                return false;
            }
            d.addLast(clock.getAsLong());
            return true;
        }
    }

    public void reset(String key) {
        events.remove(key);
    }

    private Deque<Long> slot(String key) {
        sweepIfDue();
        Deque<Long> d = events.get(key);
        if (d == null) {
            if (events.size() >= MAX_KEYS) {
                return null;
            }
            d = events.computeIfAbsent(key, k -> new ArrayDeque<>());
        }
        return d;
    }

    private void prune(Deque<Long> d, Duration window) {
        long cutoff = clock.getAsLong() - window.toMillis();
        while (!d.isEmpty() && d.peekFirst() < cutoff) {
            d.pollFirst();
        }
    }

    private void sweepIfDue() {
        long now = clock.getAsLong();
        long last = lastSweep.get();
        if (now - last < SWEEP_EVERY_MS || !lastSweep.compareAndSet(last, now)) {
            return;
        }
        events.entrySet().removeIf(e -> {
            Deque<Long> d = e.getValue();
            synchronized (d) {
                prune(d, KEEP);
                return d.isEmpty();
            }
        });
    }
}
