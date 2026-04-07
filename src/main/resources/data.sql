INSERT INTO books (title, author, isbn, published_year, availability_status, deleted, created_at, updated_at)
VALUES ('Clean Code', 'Robert C. Martin', '9780132350884', 2008, 'AVAILABLE', false, now(), now()),
       ('Domain-Driven Design', 'Eric Evans', '9780321125217', 2003, 'BORROWED', false, now(), now())
ON CONFLICT (isbn) DO NOTHING;

INSERT INTO library_users (name, email, created_at, updated_at)
VALUES ('Suresh', 'suresh@example.com', now(), now()),
       ('Anita', 'anita@example.com', now(), now())
ON CONFLICT (email) DO NOTHING;
