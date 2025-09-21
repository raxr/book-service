package com.th.ascend.book;

import org.springframework.data.domain.Pageable;

import java.util.List;

public sealed interface Book permits BookService {
    List<ResponseBook> getBookListByAuthor(RequestBookByAuthor requestBookByAuthor , Pageable pageable);
}
