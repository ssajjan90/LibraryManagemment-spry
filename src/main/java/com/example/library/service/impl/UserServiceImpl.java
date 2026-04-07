package com.example.library.service.impl;

import com.example.library.dto.UserCreateRequest;
import com.example.library.dto.UserResponse;
import com.example.library.entity.User;
import com.example.library.exception.DuplicateResourceException;
import com.example.library.exception.ResourceNotFoundException;
import com.example.library.mapper.EntityMapper;
import com.example.library.repository.UserRepository;
import com.example.library.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final EntityMapper mapper;

    @Override
    @Transactional
    public UserResponse create(UserCreateRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("User with the same email already exists");
        }

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .build();
        return mapper.toUserResponse(userRepository.save(user));
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        return mapper.toUserResponse(user);
    }
}
