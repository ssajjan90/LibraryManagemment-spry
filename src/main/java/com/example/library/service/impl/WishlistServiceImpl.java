package com.example.library.service.impl;

import com.example.library.dto.WishlistCreateRequest;
import com.example.library.dto.WishlistResponse;
import com.example.library.entity.Book;
import com.example.library.entity.User;
import com.example.library.entity.Wishlist;
import com.example.library.exception.DuplicateResourceException;
import com.example.library.exception.ResourceNotFoundException;
import com.example.library.mapper.EntityMapper;
import com.example.library.repository.BookRepository;
import com.example.library.repository.UserRepository;
import com.example.library.repository.WishlistRepository;
import com.example.library.service.WishlistService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class WishlistServiceImpl implements WishlistService {

    private final WishlistRepository wishlistRepository;
    private final UserRepository userRepository;
    private final BookRepository bookRepository;
    private final EntityMapper mapper;

    @Override
    @Transactional
    public WishlistResponse create(WishlistCreateRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + request.getUserId()));

        Book book = bookRepository.findByIdAndDeletedFalse(request.getBookId())
                .orElseThrow(() -> new ResourceNotFoundException("Book not found with id: " + request.getBookId()));

        if (wishlistRepository.existsByUserIdAndBookId(user.getId(), book.getId())) {
            throw new DuplicateResourceException("Book already exists in user's wishlist");
        }

        Wishlist wishlist = Wishlist.builder()
                .user(user)
                .book(book)
                .build();

        return mapper.toWishlistResponse(wishlistRepository.save(wishlist));
    }

    @Override
    @Transactional(readOnly = true)
    public List<WishlistResponse> getByUserId(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found with id: " + userId);
        }

        return wishlistRepository.findAllByUserId(userId)
                .stream()
                .filter(w -> !w.getBook().isDeleted())
                .map(mapper::toWishlistResponse)
                .toList();
    }
}
