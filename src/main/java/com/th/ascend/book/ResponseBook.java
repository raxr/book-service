package com.th.ascend.book;

import java.time.LocalDateTime;


public record ResponseBook(
        long id,
        String title,
        String author,
        LocalDateTime publishedDate) {
}