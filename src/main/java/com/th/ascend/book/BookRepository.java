package com.th.ascend.book;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface BookRepository extends JpaRepository<BookEntity, Long> {

    @Query(value = """
            SELECT id, title, author, published_date 
            FROM book USE INDEX (idx_author_published_date)
            WHERE author = :author
            ORDER BY published_date DESC
            LIMIT :#{#pageable.pageSize} 
            OFFSET :#{#pageable.offset}
            """, nativeQuery = true)
    Page<BookEntity> findByAuthor(String author, Pageable pageable);
}