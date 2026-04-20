package com.example.library.service.impl;

import com.example.library.exception.RateLimitExceededException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Duration;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.ConcurrentHashMap;

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
