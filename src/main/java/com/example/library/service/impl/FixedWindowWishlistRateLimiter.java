package com.example.library.service.impl;

import com.example.library.exception.RateLimitExceededException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Fixed-window limiter backed by in-memory state.
 *
 * <p>How it works:
 * For each user we keep a window start timestamp and request count.
 * If the request arrives after 60 seconds from the stored window start,
 * we open a new window and reset count to 1. Otherwise, we increment the
 * count and reject once the count would exceed 10 requests in the current
 * minute window.</p>
 */
@Component
@ConditionalOnProperty(name = "wishlist.rate-limiter.strategy", havingValue = "fixed", matchIfMissing = true)
public class FixedWindowWishlistRateLimiter implements WishlistRateLimiter {

    private static final int MAX_REQUESTS_PER_WINDOW = 10;
    private static final long WINDOW_MILLIS = Duration.ofMinutes(1).toMillis();

    private final ConcurrentHashMap<Long, RequestWindow> requestWindows = new ConcurrentHashMap<>();
    private final Clock clock;

    public FixedWindowWishlistRateLimiter() {
        this(Clock.systemUTC());
    }

    FixedWindowWishlistRateLimiter(Clock clock) {
        this.clock = clock;
    }

    @Override
    public void validateRequest(Long userId) {
        long nowMillis = clock.millis();
        requestWindows.compute(userId, (id, currentWindow) -> {
            if (currentWindow == null || nowMillis - currentWindow.windowStartMillis() >= WINDOW_MILLIS) {
                return new RequestWindow(nowMillis, 1);
            }

            if (currentWindow.requestCount() >= MAX_REQUESTS_PER_WINDOW) {
                throw new RateLimitExceededException("Rate limit exceeded for user " + userId
                        + ": max " + MAX_REQUESTS_PER_WINDOW + " requests per minute");
            }

            return new RequestWindow(currentWindow.windowStartMillis(), currentWindow.requestCount() + 1);
        });
    }

    private record RequestWindow(long windowStartMillis, int requestCount) {
    }
}
