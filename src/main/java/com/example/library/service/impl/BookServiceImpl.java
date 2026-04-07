package com.example.library.service.impl;

import com.example.library.dto.BookCreateRequest;
import com.example.library.dto.BookResponse;
import com.example.library.dto.BookUpdateRequest;
import com.example.library.entity.Book;
import com.example.library.enums.AvailabilityStatus;
import com.example.library.event.BookAvailableEvent;
import com.example.library.exception.DuplicateResourceException;
import com.example.library.exception.ResourceNotFoundException;
import com.example.library.mapper.EntityMapper;
import com.example.library.repository.BookRepository;
import com.example.library.service.BookService;
import com.example.library.spec.BookSpecifications;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BookServiceImpl implements BookService {

    private final BookRepository bookRepository;
    private final EntityMapper mapper;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public BookResponse create(BookCreateRequest request) {
        if (bookRepository.existsByIsbn(request.getIsbn())) {
            throw new DuplicateResourceException("Book with the same ISBN already exists");
        }

        Book book = Book.builder()
                .title(request.getTitle())
                .author(request.getAuthor())
                .isbn(request.getIsbn())
                .publishedYear(request.getPublishedYear())
                .availabilityStatus(request.getAvailabilityStatus())
                .deleted(false)
                .build();

        return mapper.toBookResponse(bookRepository.save(book));
    }

    @Override
    @Transactional(readOnly = true)
    public BookResponse getById(Long id) {
        return mapper.toBookResponse(getActiveBookById(id));
    }

    @Override
    @Transactional
    public BookResponse update(Long id, BookUpdateRequest request) {
        Book book = getActiveBookById(id);
        AvailabilityStatus oldStatus = book.getAvailabilityStatus();

        if (bookRepository.existsByIsbnAndIdNot(request.getIsbn(), id)) {
            throw new DuplicateResourceException("Book with the same ISBN already exists");
        }

        book.setTitle(request.getTitle());
        book.setAuthor(request.getAuthor());
        book.setIsbn(request.getIsbn());
        book.setPublishedYear(request.getPublishedYear());
        book.setAvailabilityStatus(request.getAvailabilityStatus());

        Book saved = bookRepository.save(book);

        if (oldStatus == AvailabilityStatus.BORROWED && saved.getAvailabilityStatus() == AvailabilityStatus.AVAILABLE) {
            eventPublisher.publishEvent(new BookAvailableEvent(saved.getId(), saved.getTitle()));
        }

        return mapper.toBookResponse(saved);
    }

    @Override
    @Transactional
    public void softDelete(Long id) {
        Book book = getActiveBookById(id);
        book.setDeleted(true);
        bookRepository.save(book);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BookResponse> list(String author, Integer publishedYear, int page, int size, String sortBy, String sortDir) {
        Pageable pageable = createPageable(page, size, sortBy, sortDir);
        Specification<Book> spec = Specification.where(BookSpecifications.activeBooks());

        if (author != null && !author.isBlank()) {
            spec = spec.and(BookSpecifications.hasAuthor(author));
        }
        if (publishedYear != null) {
            spec = spec.and(BookSpecifications.hasPublishedYear(publishedYear));
        }

        return bookRepository.findAll(spec, pageable).map(mapper::toBookResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BookResponse> search(String query, int page, int size, String sortBy, String sortDir) {
        Pageable pageable = createPageable(page, size, sortBy, sortDir);
        Specification<Book> spec = Specification.where(BookSpecifications.activeBooks())
                .and(BookSpecifications.searchByTitleOrAuthor(query));
        return bookRepository.findAll(spec, pageable).map(mapper::toBookResponse);
    }

    private Book getActiveBookById(Long id) {
        return bookRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found with id: " + id));
    }

    private Pageable createPageable(int page, int size, String sortBy, String sortDir) {
        Sort sort = "desc".equalsIgnoreCase(sortDir) ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        return PageRequest.of(page, size, sort);
    }
}
