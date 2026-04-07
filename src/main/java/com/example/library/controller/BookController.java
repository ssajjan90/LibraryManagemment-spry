package com.example.library.controller;

import com.example.library.dto.BookCreateRequest;
import com.example.library.dto.BookResponse;
import com.example.library.dto.BookUpdateRequest;
import com.example.library.service.BookService;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
@Validated
public class BookController {

    private final BookService bookService;

    @PostMapping
    public ResponseEntity<BookResponse> create(@Valid @RequestBody BookCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(bookService.create(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BookResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(bookService.getById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<BookResponse> update(@PathVariable Long id, @Valid @RequestBody BookUpdateRequest request) {
        return ResponseEntity.ok(bookService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        bookService.softDelete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<Page<BookResponse>> list(@RequestParam(required = false) String author,
                                                   @RequestParam(required = false) Integer publishedYear,
                                                   @RequestParam(defaultValue = "0") @Min(0) int page,
                                                   @RequestParam(defaultValue = "10") @Min(1) int size,
                                                   @RequestParam(defaultValue = "id") String sortBy,
                                                   @RequestParam(defaultValue = "asc") String sortDir) {
        return ResponseEntity.ok(bookService.list(author, publishedYear, page, size, sortBy, sortDir));
    }

    @GetMapping("/search")
    public ResponseEntity<Page<BookResponse>> search(@RequestParam @NotBlank String query,
                                                     @RequestParam(defaultValue = "0") @Min(0) int page,
                                                     @RequestParam(defaultValue = "10") @Min(1) int size,
                                                     @RequestParam(defaultValue = "id") String sortBy,
                                                     @RequestParam(defaultValue = "asc") String sortDir) {
        return ResponseEntity.ok(bookService.search(query, page, size, sortBy, sortDir));
    }
}
