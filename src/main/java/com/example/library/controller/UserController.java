package com.example.library.controller;

import com.example.library.dto.UserCreateRequest;
import com.example.library.dto.UserResponse;
import com.example.library.dto.WishlistResponse;
import com.example.library.service.UserService;
import com.example.library.service.WishlistService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final WishlistService wishlistService;

    @PostMapping
    public ResponseEntity<UserResponse> create(@Valid @RequestBody UserCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.create(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getById(id));
    }

    @GetMapping("/{userId}/wishlists")
    public ResponseEntity<List<WishlistResponse>> getWishlistsByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(wishlistService.getByUserId(userId));
    }
}
