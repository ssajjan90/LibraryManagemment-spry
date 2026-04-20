package com.example.library.service.impl;

import com.example.library.exception.RateLimitExceededException;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FixedWindowWishlistRateLimiterTest {

    @Test
    void shouldAllowTenRequestsWithinOneMinuteAndRejectEleventh() {
        MutableClock clock = new MutableClock(Instant.parse("2026-01-01T00:00:00Z"));
        FixedWindowWishlistRateLimiter rateLimiter = new FixedWindowWishlistRateLimiter(clock);

        for (int i = 0; i < 10; i++) {
            assertDoesNotThrow(() -> rateLimiter.validateRequest(1L));
        }

        assertThrows(RateLimitExceededException.class, () -> rateLimiter.validateRequest(1L));
    }

    @Test
    void shouldResetWindowAfterOneMinute() {
        MutableClock clock = new MutableClock(Instant.parse("2026-01-01T00:00:00Z"));
        FixedWindowWishlistRateLimiter rateLimiter = new FixedWindowWishlistRateLimiter(clock);

        for (int i = 0; i < 10; i++) {
            rateLimiter.validateRequest(1L);
        }

        clock.setInstant(Instant.parse("2026-01-01T00:01:00Z"));

        assertDoesNotThrow(() -> rateLimiter.validateRequest(1L));
    }

    private static class MutableClock extends Clock {
        private Instant instant;

        private MutableClock(Instant instant) {
            this.instant = instant;
        }

        public void setInstant(Instant instant) {
            this.instant = instant;
        }

        @Override
        public ZoneId getZone() {
            return ZoneId.of("UTC");
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return instant;
        }
    }
}
