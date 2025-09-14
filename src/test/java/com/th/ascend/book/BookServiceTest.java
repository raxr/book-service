package com.th.ascend.book;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BookServiceTest {

    @Mock
    private BookRepository bookRepository;

    @Mock
    private BookMapper bookMapper;

    private BookService bookService;

    @BeforeEach
    void setUp() {
        bookService = new BookService(bookRepository, bookMapper);
    }

    @Test
    void shouldReturnBooksSortedByPublishedDateDescAndLimitedToDefaultSize_whenAuthorHasBooks() {
        String author = "Author A";
        int defaultPage = 0;
        int defaultSize = 10;
        Sort sort = Sort.by("publishedDate").descending();

        LocalDateTime base = LocalDateTime.now();
        List<BookEntity> tenDescending = new ArrayList<>();
        for (int i = 0; i < defaultSize; i++) {
            BookEntity be = new BookEntity();
            be.setId(i + 1);
            be.setTitle("Title " + (i + 1));
            be.setAuthor(author);
            be.setPublishedDate(base.minusMinutes(i));
            tenDescending.add(be);
        }

        Page<BookEntity> page = new PageImpl<>(tenDescending, PageRequest.of(defaultPage, defaultSize, sort), 12);
        when(bookRepository.findByAuthor(anyString(), any(Pageable.class))).thenReturn(page);

        when(bookMapper.toResponseList(any(Page.class))).thenAnswer(invocation -> {
            Page<BookEntity> input = invocation.getArgument(0);
            List<ResponseBook> responses = new ArrayList<>();
            for (BookEntity be : input.getContent()) {
                responses.add(new ResponseBook(be.getId(), be.getTitle(), be.getAuthor(), be.getPublishedDate()));
            }
            return responses;
        });

        List<ResponseBook> result = bookService.getBookListByAuthor(new RequestBookByAuthor(author));

        assertNotNull(result);
        assertEquals(defaultSize, result.size(), "Result should be limited to default page size (10)");
        for (int i = 0; i < result.size() - 1; i++) {
            LocalDateTime current = result.get(i).publishedDate();
            LocalDateTime next = result.get(i + 1).publishedDate();
            assertTrue(current.isAfter(next), "List should be sorted by publishedDate descending");
        }
    }

    @Test
    void shouldUseDefaultPageableAndAuthorFilter_whenGettingBooksByAuthor() {
        String author = "Bob";
        when(bookRepository.findByAuthor(anyString(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(Collections.emptyList(), PageRequest.of(0, 10, Sort.by("publishedDate").descending()), 0));
        when(bookMapper.toResponseList(any(Page.class))).thenReturn(Collections.emptyList());

        bookService.getBookListByAuthor(new RequestBookByAuthor(author));

        ArgumentCaptor<String> authorCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);

        verify(bookRepository, times(1)).findByAuthor(authorCaptor.capture(), pageableCaptor.capture());

        assertEquals(author, authorCaptor.getValue());

        Pageable pageable = pageableCaptor.getValue();
        assertEquals(0, pageable.getPageNumber());
        assertEquals(10, pageable.getPageSize());
        Sort.Order order = pageable.getSort().getOrderFor("publishedDate");
        assertNotNull(order, "Sort should contain 'publishedDate'");
        assertEquals(Sort.Direction.DESC, order.getDirection(), "Sort direction should be DESC");
    }

    @Test
    void shouldMapRequestSaveEntityAndReturnMappedResponse_whenCreatingBook() {
        RequestBook request = new RequestBook("Clean Architecture", "Robert C. Martin", "Pearson", "2020-01-01 10:00:00");

        BookEntity toSave = new BookEntity();
        toSave.setTitle("Clean Architecture");
        toSave.setAuthor("Robert C. Martin");
        toSave.setPublishedDate(LocalDateTime.of(2020, 1, 1, 10, 0, 0));

        BookEntity saved = new BookEntity();
        saved.setId(1L);
        saved.setTitle("Clean Architecture");
        saved.setAuthor("Robert C. Martin");
        saved.setPublishedDate(LocalDateTime.of(2020, 1, 1, 10, 0, 0));

        ResponseBook expectedResponse = new ResponseBook(saved.getId(), saved.getTitle(), saved.getAuthor(), saved.getPublishedDate());

        when(bookMapper.toEntity(request)).thenReturn(toSave);
        when(bookRepository.save(toSave)).thenReturn(saved);
        when(bookMapper.toResponse(saved)).thenReturn(expectedResponse);

        ResponseBook actual = bookService.createBook(request);

        assertNotNull(actual);
        assertEquals(expectedResponse, actual);

        verify(bookMapper, times(1)).toEntity(request);
        verify(bookRepository, times(1)).save(toSave);
        verify(bookMapper, times(1)).toResponse(saved);
        verifyNoMoreInteractions(bookMapper, bookRepository);
    }

    @Test
    void shouldReturnEmptyList_whenNoBooksFoundForAuthor() {
        String author = "Unknown";
        when(bookRepository.findByAuthor(anyString(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(Collections.emptyList(), PageRequest.of(0, 10, Sort.by("publishedDate").descending()), 0));
        when(bookMapper.toResponseList(any(Page.class))).thenReturn(Collections.emptyList());

        List<ResponseBook> result = bookService.getBookListByAuthor(new RequestBookByAuthor(author));

        assertNotNull(result);
        assertTrue(result.isEmpty(), "Result list should be empty when repository returns no books");
    }

    @Test
    void shouldThrowNullPointerException_whenRequestBookByAuthorIsNull() {
        assertThrows(NullPointerException.class, () -> bookService.getBookListByAuthor(null));
        verifyNoInteractions(bookRepository, bookMapper);
    }

    @Test
    void shouldPropagateDataAccessException_whenRepositorySaveFailsOnCreateBook() {
        RequestBook request = new RequestBook("Title", "Author", "Publisher", "2024-05-01 12:00:00");

        BookEntity toSave = new BookEntity();
        toSave.setTitle("Title");
        toSave.setAuthor("Author");
        toSave.setPublishedDate(LocalDateTime.of(2024, 5, 1, 12, 0, 0));

        when(bookMapper.toEntity(request)).thenReturn(toSave);
        DataAccessException failure = new DataAccessResourceFailureException("DB down");
        when(bookRepository.save(toSave)).thenThrow(failure);

        DataAccessException thrown = assertThrows(DataAccessException.class, () -> bookService.createBook(request));
        assertSame(failure, thrown);

        verify(bookMapper, times(1)).toEntity(request);
        verify(bookRepository, times(1)).save(toSave);
        verify(bookMapper, never()).toResponse(any());
    }
}
