package com.th.ascend.book;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public final class BookService implements Book {

    private final BookRepository bookRepository;

    private final BookMapper bookMapper;

    @Override
    public List<ResponseBook> getBookListByAuthor(RequestBookByAuthor requestBookByAuthor) {
        int defaultValueSize = 10;
        int defaultValuePage = 0;
        Pageable pageable = PageRequest.of(defaultValuePage, defaultValueSize, Sort.by("publishedDate").descending());
        Page<BookEntity> bookEntityPage = bookRepository.findByAuthor(requestBookByAuthor.author(), pageable);
        return bookMapper.toResponseList(bookEntityPage);
    }

    public ResponseBook createBook(RequestBook requestBook) {
        BookEntity bookEntity = bookMapper.toEntity(requestBook);
        BookEntity savedBookEntity = bookRepository.save(bookEntity);
        return bookMapper.toResponse(savedBookEntity);
    }
}
