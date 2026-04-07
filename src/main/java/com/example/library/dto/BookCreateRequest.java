package com.example.library.dto;

import com.example.library.enums.AvailabilityStatus;
import com.example.library.validation.ValidPublishedYear;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class BookCreateRequest {

    @NotBlank
    private String title;

    @NotBlank
    private String author;

    @NotBlank
    private String isbn;

    @NotNull
    @ValidPublishedYear
    private Integer publishedYear;

    @NotNull
    private AvailabilityStatus availabilityStatus;
}
