package com.example.library.service;

import com.example.library.dto.UserCreateRequest;
import com.example.library.dto.UserResponse;

public interface UserService {
    UserResponse create(UserCreateRequest request);

    UserResponse getById(Long id);
}
