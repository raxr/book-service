package com.th.ascend.book;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class BookControllerIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private BookRepository bookRepository;

    private String baseUrl(String path) {
        return "http://localhost:" + port + "/api/v1/books" + path;
    }

    @BeforeEach
    void clean() {
        bookRepository.deleteAll();
    }

    @Test
    void givenPostCreateBook_persistsAndReturnsCreatedBook() {
        RequestBook request = new RequestBook(
                "Clean Code",
                "Robert C. Martin",
                "Pearson",
                "2020-01-01 10:00:00"
        );

        ResponseEntity<ResponseBook> response = restTemplate.postForEntity(baseUrl(""), request, ResponseBook.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ResponseBook body = response.getBody();
        assertNotNull(body);
        assertTrue(body.id() > 0);
        assertEquals("Clean Code", body.title());
        assertEquals("Robert C. Martin", body.author());
        assertEquals(LocalDateTime.of(2020, 1, 1, 10, 0, 0), body.publishedDate());

        List<BookEntity> all = bookRepository.findAll();
        assertEquals(1, all.size());
        BookEntity saved = all.getFirst();
        assertEquals("Clean Code", saved.getTitle());
        assertEquals("Robert C. Martin", saved.getAuthor());
        assertEquals(LocalDateTime.of(2020, 1, 1, 10, 0, 0), saved.getPublishedDate());
    }

    @Test
    void givenGetBooksByAuthor_returnsSortedByPublishedDateDesc() {
        String author = "Author A";
        // Seed test data
        BookEntity b1 = new BookEntity();
        b1.setTitle("T1");
        b1.setAuthor(author);
        b1.setPublishedDate(LocalDateTime.of(2024, 5, 1, 12, 0, 0));

        BookEntity b2 = new BookEntity();
        b2.setTitle("T2");
        b2.setAuthor(author);
        b2.setPublishedDate(LocalDateTime.of(2024, 5, 2, 12, 0, 0));

        BookEntity other = new BookEntity();
        other.setTitle("Other");
        other.setAuthor("Another");
        other.setPublishedDate(LocalDateTime.of(2024, 5, 3, 12, 0, 0));

        bookRepository.saveAll(Arrays.asList(b1, b2, other));

        RequestBookByAuthor request = new RequestBookByAuthor(author);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<RequestBookByAuthor> httpEntity = new HttpEntity<>(request, headers);

        ResponseEntity<ResponseBook[]> response = restTemplate.exchange(
                baseUrl(""), HttpMethod.GET, httpEntity, ResponseBook[].class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        ResponseBook[] body = response.getBody();
        assertNotNull(body);
        assertEquals(2, body.length);

        assertEquals("T2", body[0].title());
        assertEquals(LocalDateTime.of(2024, 5, 2, 12, 0, 0), body[0].publishedDate());
        assertEquals("T1", body[1].title());
        assertEquals(LocalDateTime.of(2024, 5, 1, 12, 0, 0), body[1].publishedDate());
    }

    @Test
    void givenPostCreateBook_withInvalidYear_returnsBadRequest() {
        RequestBook invalid = new RequestBook(
                "Some Title",
                "An Author",
                "Pub",
                "0999-01-01 00:00:00"
        );

        ResponseEntity<String> response = restTemplate.postForEntity(baseUrl(""), invalid, String.class);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }
}
