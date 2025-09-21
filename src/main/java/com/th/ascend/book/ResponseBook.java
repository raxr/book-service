package com.th.ascend.book;

import java.time.LocalDate;


public record ResponseBook(
        long id,
        String title,
        String author,
        LocalDate publishedDate) {
}