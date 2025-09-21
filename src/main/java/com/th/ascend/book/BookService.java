package com.th.ascend.book;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public final class BookService implements Book {

    private final BookRepository bookRepository;

    private final BookMapper bookMapper;

    @Override
    public List<ResponseBook> getBookListByAuthor(RequestBookByAuthor requestBookByAuthor, Pageable pageable) {
        pageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize());
        Page<BookEntity> bookEntityPage = bookRepository.findByAuthor(requestBookByAuthor.author(), pageable);
        return bookMapper.toResponseList(bookEntityPage);
    }

    public ResponseBook createBook(RequestBook requestBook) {
        BookEntity bookEntity = bookMapper.toEntity(requestBook);
        log.info("Saving book: {} by {}, date: {}",
                bookEntity.getTitle(),
                bookEntity.getAuthor(),
                bookEntity.getPublishedDate());
        BookEntity savedBookEntity = bookRepository.save(bookEntity);
        return bookMapper.toResponse(savedBookEntity);
    }
}
