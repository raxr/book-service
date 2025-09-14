package com.th.ascend.book;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = BookController.class)
@ActiveProfiles("test")
public class BookControllerTest {

    private static final String BASE_PATH = "/api/v1/books";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BookService bookService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void givenBookListByAuthor_ReturnsListFromService() throws Exception {
        String author = "Robert C. Martin";
        List<ResponseBook> serviceResult = List.of(
                new ResponseBook(1L, "Clean Code", author, LocalDateTime.of(2020, 1, 1, 10, 0, 0)),
                new ResponseBook(2L, "Clean Coder", author, LocalDateTime.of(2021, 1, 1, 10, 0, 0))
        );
        when(bookService.getBookListByAuthor(eq(new RequestBookByAuthor(author)))).thenReturn(serviceResult);

        String body = objectMapper.writeValueAsString(new RequestBookByAuthor(author));

        mockMvc.perform(get(BASE_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", Matchers.hasSize(2)))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].title").value("Clean Code"))
                .andExpect(jsonPath("$[0].author").value(author))
                .andExpect(jsonPath("$[0].publishedDate").value("2020-01-01T10:00:00"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].title").value("Clean Coder"))
                .andExpect(jsonPath("$[1].author").value(author))
                .andExpect(jsonPath("$[1].publishedDate").value("2021-01-01T10:00:00"));

        ArgumentCaptor<RequestBookByAuthor> captor = ArgumentCaptor.forClass(RequestBookByAuthor.class);
        verify(bookService).getBookListByAuthor(captor.capture());
        assertEquals(author, captor.getValue().author());
    }

    @Test
    void givenCreateBook_ReturnsCreatedBookFromService() throws Exception {
        RequestBook request = new RequestBook(
                "Clean Architecture",
                "Robert C. Martin",
                "Pearson",
                "2018-09-20 10:00:00"
        );
        ResponseBook created = new ResponseBook(
                100L,
                "Clean Architecture",
                "Robert C. Martin",
                LocalDateTime.of(2018, 9, 20, 10, 0, 0)
        );

        when(bookService.createBook(any(RequestBook.class))).thenReturn(created);

        mockMvc.perform(post(BASE_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(100))
                .andExpect(jsonPath("$.title").value("Clean Architecture"))
                .andExpect(jsonPath("$.author").value("Robert C. Martin"))
                .andExpect(jsonPath("$.publishedDate").value("2018-09-20T10:00:00"));

        ArgumentCaptor<RequestBook> captor = ArgumentCaptor.forClass(RequestBook.class);
        verify(bookService).createBook(captor.capture());
        assertEquals(request.title(), captor.getValue().title());
        assertEquals(request.author(), captor.getValue().author());
        assertEquals(request.publisher(), captor.getValue().publisher());
        assertEquals(request.publishedDate(), captor.getValue().publishedDate());
    }

    @Test
    void givenBookListByAuthor_ReturnsEmptyListWhenNoneFound() throws Exception {
        String author = "Unknown Author";
        when(bookService.getBookListByAuthor(any(RequestBookByAuthor.class))).thenReturn(Collections.emptyList());

        mockMvc.perform(get(BASE_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new RequestBookByAuthor(author))))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    @Test
    void givenCreateBook_Returns400_WhenTitleOrAuthorBlank() throws Exception {
        RequestBook invalid = new RequestBook(
                "",
                "Some Author",
                "Pub",
                "2020-01-01 10:00:00"
        );

        mockMvc.perform(post(BASE_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(bookService);
    }

    @Test
    void givenCreateBook_Returns400_WhenPublishedDateInvalid() throws Exception {
        RequestBook invalid = new RequestBook(
                "Some Title",
                "Some Author",
                "Pub",
                "0999-01-01 00:00:00"
        );

        mockMvc.perform(post(BASE_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(bookService);
    }

    @Test
    void givenBookListByAuthor_Returns400_WhenRequestBodyMissingOrMalformed() throws Exception {
        mockMvc.perform(get(BASE_PATH)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(bookService);
    }
}