package com.example.library.service;

import com.example.library.dto.BookCreateRequest;
import com.example.library.dto.BookResponse;
import com.example.library.dto.BookUpdateRequest;
import org.springframework.data.domain.Page;

public interface BookService {
    BookResponse create(BookCreateRequest request);

    BookResponse getById(Long id);

    BookResponse update(Long id, BookUpdateRequest request);

    void softDelete(Long id);

    Page<BookResponse> list(String author, Integer publishedYear, int page, int size, String sortBy, String sortDir);

    Page<BookResponse> search(String query, int page, int size, String sortBy, String sortDir);
}
