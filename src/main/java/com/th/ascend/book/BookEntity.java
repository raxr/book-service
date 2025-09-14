package com.th.ascend.book;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Data;
import org.hibernate.annotations.ColumnDefault;

import java.time.LocalDateTime;

@Entity
@Table(name = "book", indexes = {
        @Index(name = "idx_author", columnList = "author"),
        @Index(name = "idx_author_published_date", columnList = "author, published_date")
})
@Data
public class BookEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    long id;
    String title;

    @Column(nullable = false)
    String author;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "published_date", nullable = false)
    private LocalDateTime publishedDate;
}
