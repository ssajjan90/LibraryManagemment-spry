package com.example.library.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class WishlistCreateRequest {

    @NotNull
    private Long userId;

    @NotNull
    private Long bookId;
}
