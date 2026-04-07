package com.example.library.dto;

import com.example.library.enums.AvailabilityStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class BookResponse {
    private Long id;
    private String title;
    private String author;
    private String isbn;
    private Integer publishedYear;
    private AvailabilityStatus availabilityStatus;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
