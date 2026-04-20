package com.example.library.service.impl;

import com.example.library.exception.RateLimitExceededException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

/**
 * Distributed token-bucket limiter backed by Redis.
 *
 * <p>How it works:
 * Each user has a Redis hash bucket with current tokens and last refill time.
 * A Lua script runs atomically in Redis for every request: it refills tokens
 * based on elapsed time, checks whether at least one token is available, and
 * then consumes one token when allowed. This guarantees correctness across
 * multiple application instances because the refill + consume logic executes
 * as a single atomic Redis operation.</p>
 */
@Component
@ConditionalOnProperty(name = "wishlist.rate-limiter.strategy", havingValue = "redis-token-bucket")
public class RedisTokenBucketWishlistRateLimiter implements WishlistRateLimiter {

    private static final long BUCKET_CAPACITY = 10L;
    private static final long TOKENS_PER_MINUTE = 10L;
    private static final long REQUESTED_TOKENS = 1L;
    private static final long BUCKET_TTL_SECONDS = Duration.ofMinutes(5).toSeconds();

    private static final String SCRIPT = """
            local bucketKey = KEYS[1]
            local capacity = tonumber(ARGV[1])
            local tokensPerMinute = tonumber(ARGV[2])
            local nowMillis = tonumber(ARGV[3])
            local requestedTokens = tonumber(ARGV[4])
            local ttlSeconds = tonumber(ARGV[5])

            local tokens = tonumber(redis.call('HGET', bucketKey, 'tokens'))
            local lastRefill = tonumber(redis.call('HGET', bucketKey, 'last_refill'))

            if tokens == nil then
              tokens = capacity
            end
            if lastRefill == nil then
              lastRefill = nowMillis
            end

            local elapsedMillis = math.max(0, nowMillis - lastRefill)
            local refill = (elapsedMillis * tokensPerMinute) / 60000
            tokens = math.min(capacity, tokens + refill)

            if tokens < requestedTokens then
              redis.call('HSET', bucketKey, 'tokens', tokens)
              redis.call('HSET', bucketKey, 'last_refill', nowMillis)
              redis.call('EXPIRE', bucketKey, ttlSeconds)
              return 0
            end

            tokens = tokens - requestedTokens
            redis.call('HSET', bucketKey, 'tokens', tokens)
            redis.call('HSET', bucketKey, 'last_refill', nowMillis)
            redis.call('EXPIRE', bucketKey, ttlSeconds)
            return 1
            """;

    private final StringRedisTemplate redisTemplate;
    private final RedisScript<Long> tokenBucketScript;

    public RedisTokenBucketWishlistRateLimiter(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
        DefaultRedisScript<Long> script = new DefaultRedisScript<>();
        script.setScriptText(SCRIPT);
        script.setResultType(Long.class);
        this.tokenBucketScript = script;
    }

    @Override
    public void validateRequest(Long userId) {
        String bucketKey = "wishlist:rate-limit:user:" + userId;

        Long result = redisTemplate.execute(
                tokenBucketScript,
                List.of(bucketKey),
                String.valueOf(BUCKET_CAPACITY),
                String.valueOf(TOKENS_PER_MINUTE),
                String.valueOf(Instant.now().toEpochMilli()),
                String.valueOf(REQUESTED_TOKENS),
                String.valueOf(BUCKET_TTL_SECONDS)
        );

        if (result == null) {
            throw new IllegalStateException("Failed to evaluate redis token bucket rate limiter");
        }

        if (result == 0L) {
            throw new RateLimitExceededException("Rate limit exceeded for user " + userId
                    + ": max " + BUCKET_CAPACITY + " requests per minute");
        }
    }
}
