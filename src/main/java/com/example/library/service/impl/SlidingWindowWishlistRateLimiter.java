package com.example.library.service.impl;

import com.example.library.exception.RateLimitExceededException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Duration;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Sliding-window limiter backed by in-memory state.
 *
 * <p>How it works:
 * For each user we store timestamps of recent requests. On every new request,
 * timestamps older than 60 seconds are evicted. If fewer than 10 timestamps
 * remain, the current request timestamp is appended and the request is allowed.
 * If 10 timestamps are still inside the last 60 seconds, the request is rejected.</p>
 */
@Component
@ConditionalOnProperty(name = "wishlist.rate-limiter.strategy", havingValue = "sliding")
public class SlidingWindowWishlistRateLimiter implements WishlistRateLimiter {

    private static final int MAX_REQUESTS_PER_WINDOW = 10;
    private static final long WINDOW_MILLIS = Duration.ofMinutes(1).toMillis();

    private final ConcurrentHashMap<Long, Deque<Long>> requestTimestampsByUser = new ConcurrentHashMap<>();
    private final Clock clock;

    public SlidingWindowWishlistRateLimiter() {
        this(Clock.systemUTC());
    }

    SlidingWindowWishlistRateLimiter(Clock clock) {
        this.clock = clock;
    }

    @Override
    public void validateRequest(Long userId) {
        long nowMillis = clock.millis();
        long oldestAllowedTimestamp = nowMillis - WINDOW_MILLIS;

        requestTimestampsByUser.compute(userId, (id, timestamps) -> {
            Deque<Long> userTimestamps = timestamps == null ? new ArrayDeque<>() : timestamps;

            while (!userTimestamps.isEmpty() && userTimestamps.peekFirst() <= oldestAllowedTimestamp) {
                userTimestamps.pollFirst();
            }

            if (userTimestamps.size() >= MAX_REQUESTS_PER_WINDOW) {
                throw new RateLimitExceededException("Rate limit exceeded for user " + userId
                        + ": max " + MAX_REQUESTS_PER_WINDOW + " requests per minute");
            }

            userTimestamps.offerLast(nowMillis);
            return userTimestamps;
        });
    }
}
