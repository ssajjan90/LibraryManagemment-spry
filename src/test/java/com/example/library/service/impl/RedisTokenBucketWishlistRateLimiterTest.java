package com.example.library.service.impl;

import com.example.library.exception.RateLimitExceededException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RedisTokenBucketWishlistRateLimiterTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Test
    void shouldAllowRequestWhenRedisScriptReturnsOne() {
        RedisTokenBucketWishlistRateLimiter rateLimiter = new RedisTokenBucketWishlistRateLimiter(redisTemplate);

        when(redisTemplate.execute(
                ArgumentMatchers.any(),
                ArgumentMatchers.anyList(),
                ArgumentMatchers.<String>any()
        )).thenReturn(1L);

        assertDoesNotThrow(() -> rateLimiter.validateRequest(1L));
    }

    @Test
    void shouldRejectRequestWhenRedisScriptReturnsZero() {
        RedisTokenBucketWishlistRateLimiter rateLimiter = new RedisTokenBucketWishlistRateLimiter(redisTemplate);

        when(redisTemplate.execute(
                ArgumentMatchers.any(),
                ArgumentMatchers.anyList(),
                ArgumentMatchers.<String>any()
        )).thenReturn(0L);

        assertThrows(RateLimitExceededException.class, () -> rateLimiter.validateRequest(1L));
    }

    @Test
    void shouldThrowWhenRedisScriptReturnsNull() {
        RedisTokenBucketWishlistRateLimiter rateLimiter = new RedisTokenBucketWishlistRateLimiter(redisTemplate);

        when(redisTemplate.execute(
                ArgumentMatchers.any(),
                ArgumentMatchers.anyList(),
                ArgumentMatchers.<String>any()
        )).thenReturn(null);

        assertThrows(IllegalStateException.class, () -> rateLimiter.validateRequest(1L));
    }
}
