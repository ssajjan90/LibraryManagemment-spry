package com.example.library.service.impl;

import com.example.library.dto.WishlistCreateRequest;
import com.example.library.entity.Book;
import com.example.library.entity.User;
import com.example.library.enums.AvailabilityStatus;
import com.example.library.exception.DuplicateResourceException;
import com.example.library.exception.RateLimitExceededException;
import com.example.library.mapper.EntityMapper;
import com.example.library.repository.BookRepository;
import com.example.library.repository.UserRepository;
import com.example.library.repository.WishlistRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WishlistServiceImplTest {

    @Mock
    private WishlistRepository wishlistRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private BookRepository bookRepository;
    @Mock
    private EntityMapper mapper;
    @Mock
    private WishlistRateLimiter wishlistRateLimiter;

    @InjectMocks
    private WishlistServiceImpl wishlistService;

    @Test
    void createShouldRejectDuplicateWishlist() {
        User user = User.builder().id(1L).name("Suresh").email("suresh@example.com").build();
        Book book = Book.builder().id(2L).title("Clean Code").author("Robert").isbn("123").publishedYear(2008)
                .availabilityStatus(AvailabilityStatus.AVAILABLE).deleted(false).build();

        WishlistCreateRequest request = new WishlistCreateRequest();
        request.setUserId(1L);
        request.setBookId(2L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(bookRepository.findByIdAndDeletedFalse(2L)).thenReturn(Optional.of(book));
        when(wishlistRepository.existsByUserIdAndBookId(1L, 2L)).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> wishlistService.create(request));
    }

    @Test
    void createShouldRejectWhenRateLimitExceeded() {
        WishlistCreateRequest request = new WishlistCreateRequest();
        request.setUserId(1L);
        request.setBookId(2L);

        doThrow(new RateLimitExceededException("Rate limit exceeded"))
                .when(wishlistRateLimiter).validateRequest(1L);

        assertThrows(RateLimitExceededException.class, () -> wishlistService.create(request));
    }
}
