package com.th.ascend.book;

import java.util.List;

public sealed interface Book permits BookService {
    List<ResponseBook> getBookListByAuthor(RequestBookByAuthor requestBookByAuthor);
}
