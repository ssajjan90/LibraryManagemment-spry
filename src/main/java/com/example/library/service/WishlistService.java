package com.example.library.service;

import com.example.library.dto.WishlistCreateRequest;
import com.example.library.dto.WishlistResponse;

import java.util.List;

public interface WishlistService {
    WishlistResponse create(WishlistCreateRequest request);

    List<WishlistResponse> getByUserId(Long userId);
}
