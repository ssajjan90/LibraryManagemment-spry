package com.example.library.repository;

import com.example.library.entity.Wishlist;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WishlistRepository extends JpaRepository<Wishlist, Long> {
    boolean existsByUserIdAndBookId(Long userId, Long bookId);

    List<Wishlist> findAllByUserId(Long userId);

    List<Wishlist> findAllByBookId(Long bookId);
}
