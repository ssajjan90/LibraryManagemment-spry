package com.example.library.service.impl;

import com.example.library.event.BookAvailableEvent;
import com.example.library.repository.WishlistRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationListener {

    private final WishlistRepository wishlistRepository;

    @Async
    @EventListener
    public void onBookAvailable(BookAvailableEvent event) {
        wishlistRepository.findAllByBookId(event.bookId()).forEach(wishlist ->
                log.info("Notification prepared for user_id: {}, Book [{}] is now available.",
                        wishlist.getUser().getId(),
                        event.bookTitle())
        );
    }
}
