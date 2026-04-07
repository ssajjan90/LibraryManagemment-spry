package com.example.library.service.impl;

import com.example.library.dto.BookCreateRequest;
import com.example.library.dto.BookUpdateRequest;
import com.example.library.entity.Book;
import com.example.library.enums.AvailabilityStatus;
import com.example.library.event.BookAvailableEvent;
import com.example.library.exception.DuplicateResourceException;
import com.example.library.exception.ResourceNotFoundException;
import com.example.library.mapper.EntityMapper;
import com.example.library.repository.BookRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookServiceImplTest {

    @Mock
    private BookRepository bookRepository;
    @Mock
    private EntityMapper mapper;
    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private BookServiceImpl bookService;

    private BookCreateRequest createRequest;

    @BeforeEach
    void setUp() {
        createRequest = new BookCreateRequest();
        createRequest.setTitle("Clean Code");
        createRequest.setAuthor("Robert C. Martin");
        createRequest.setIsbn("9780132350884");
        createRequest.setPublishedYear(2008);
        createRequest.setAvailabilityStatus(AvailabilityStatus.AVAILABLE);
    }

    @Test
    void createShouldThrowWhenDuplicateIsbn() {
        when(bookRepository.existsByIsbn("9780132350884")).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> bookService.create(createRequest));

        verify(bookRepository, never()).save(any());
    }

    @Test
    void updateShouldTriggerEventWhenBorrowedBecomesAvailable() {
        Book existing = Book.builder()
                .id(1L)
                .title("Clean Code")
                .author("Robert C. Martin")
                .isbn("9780132350884")
                .publishedYear(2008)
                .availabilityStatus(AvailabilityStatus.BORROWED)
                .deleted(false)
                .build();

        BookUpdateRequest request = new BookUpdateRequest();
        request.setTitle("Clean Code");
        request.setAuthor("Robert C. Martin");
        request.setIsbn("9780132350884");
        request.setPublishedYear(2008);
        request.setAvailabilityStatus(AvailabilityStatus.AVAILABLE);

        when(bookRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(existing));
        when(bookRepository.existsByIsbnAndIdNot("9780132350884", 1L)).thenReturn(false);
        when(bookRepository.save(any(Book.class))).thenAnswer(invocation -> invocation.getArgument(0));

        bookService.update(1L, request);

        ArgumentCaptor<BookAvailableEvent> captor = ArgumentCaptor.forClass(BookAvailableEvent.class);
        verify(eventPublisher).publishEvent(captor.capture());
    }

    @Test
    void getByIdShouldHideSoftDeletedBook() {
        when(bookRepository.findByIdAndDeletedFalse(9L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> bookService.getById(9L));
    }
}
