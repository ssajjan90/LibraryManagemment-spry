package com.example.library.mapper;

import com.example.library.dto.*;
import com.example.library.entity.Book;
import com.example.library.entity.User;
import com.example.library.entity.Wishlist;
import org.springframework.stereotype.Component;

@Component
public class EntityMapper {

    public BookResponse toBookResponse(Book book) {
        return BookResponse.builder()
                .id(book.getId())
                .title(book.getTitle())
                .author(book.getAuthor())
                .isbn(book.getIsbn())
                .publishedYear(book.getPublishedYear())
                .availabilityStatus(book.getAvailabilityStatus())
                .createdAt(book.getCreatedAt())
                .updatedAt(book.getUpdatedAt())
                .build();
    }

    public UserResponse toUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }

    public WishlistResponse toWishlistResponse(Wishlist wishlist) {
        return WishlistResponse.builder()
                .id(wishlist.getId())
                .userId(wishlist.getUser().getId())
                .userName(wishlist.getUser().getName())
                .bookId(wishlist.getBook().getId())
                .bookTitle(wishlist.getBook().getTitle())
                .createdAt(wishlist.getCreatedAt())
                .build();
    }
}
