package com.example.library.spec;

import com.example.library.entity.Book;
import org.springframework.data.jpa.domain.Specification;

public final class BookSpecifications {

    private BookSpecifications() {
    }

    public static Specification<Book> activeBooks() {
        return (root, query, cb) -> cb.isFalse(root.get("deleted"));
    }

    public static Specification<Book> hasAuthor(String author) {
        String pattern = "%" + author.toLowerCase() + "%";
        return (root, query, cb) -> cb.like(cb.lower(root.get("author")), pattern);
    }

    public static Specification<Book> hasPublishedYear(Integer year) {
        return (root, query, cb) -> cb.equal(root.get("publishedYear"), year);
    }

    public static Specification<Book> searchByTitleOrAuthor(String queryText) {
        String pattern = "%" + queryText.toLowerCase() + "%";
        return (root, query, cb) -> cb.or(
                cb.like(cb.lower(root.get("title")), pattern),
                cb.like(cb.lower(root.get("author")), pattern)
        );
    }
}
