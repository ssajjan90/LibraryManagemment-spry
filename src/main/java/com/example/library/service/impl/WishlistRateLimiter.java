package com.example.library.service.impl;

public interface WishlistRateLimiter {
    void validateRequest(Long userId);
}
