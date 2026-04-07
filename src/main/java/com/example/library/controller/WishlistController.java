package com.example.library.controller;

import com.example.library.dto.WishlistCreateRequest;
import com.example.library.dto.WishlistResponse;
import com.example.library.service.WishlistService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/wishlists")
@RequiredArgsConstructor
public class WishlistController {

    private final WishlistService wishlistService;

    @PostMapping
    public ResponseEntity<WishlistResponse> create(@Valid @RequestBody WishlistCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(wishlistService.create(request));
    }
}
